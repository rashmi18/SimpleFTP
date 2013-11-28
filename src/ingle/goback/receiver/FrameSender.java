package ingle.goback.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameSender implements Runnable {

	Integer windowSize;
	Integer MSS;
	String host;
	DatagramSocket socket;
	Integer sequenceNumber;
	String sourceFileName;
	WindowManager windowManager;
	Timer timer;
	AtomicBoolean isRetransmitting;
	private Semaphore windowAccess;

	class RetransmitTimer extends TimerTask {

		@Override
		public void run() {
			// try {
			System.out.println("Before acquire");
			// windowAccess.acquire();
			System.out.println("After acquire");
			// }
			/*
			 * catch (InterruptedException e1) { // TODO Auto-generated catch
			 * block e1.printStackTrace(); }
			 */
			System.out.println("Before retransmitting started outstanding="
					+ windowManager.numberOfoutstandingFrames.get());

			if (windowManager.getWindow().size() > 0) {
				isRetransmitting.set(true);
				timer = new Timer();
				timer.schedule(new RetransmitTimer(), 1000);

			}

			for (int i = 0; i < windowManager.numberOfoutstandingFrames.get(); i++) {
				byte[] segmentData;
				try {

					System.out.println("\nResending packet no."
							+ windowManager.getWindow().get(i).sequenceNumber);
					System.out.println("\nNumber of outstanding frames."
							+ windowManager.numberOfoutstandingFrames.get());

					segmentData = convertToBytes(windowManager.getWindow().get(
							i));
					sendPacket(segmentData);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// windowAccess.release();
			isRetransmitting.set(false);
		}
	}

	class AcknowledgementReceiver implements Runnable {

		@Override
		public void run() {
			byte[] buf = new byte[200];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			try {
				while (true) {
					System.out.println("Waiting for ack");
					socket.receive(packet);
					windowManager.firstFrameSent.set(false);
					byte[] object = new byte[packet.getLength()];
					System.arraycopy(buf, 0, object, 0, packet.getLength());
					Acknowledgement ack = PacketSerializer
							.acknowledgementDeserialize(object);
					System.out.println("Data expected" + ack.sequenceNumber);

					int count = 0;
					while (windowManager.getWindow().size() > 0
							&& windowManager.getWindow().get(0).sequenceNumber < ack.sequenceNumber) {
						// if retransmitting, don't remove the element for which
						// late ack received

						if (count == 0)
							timer.cancel();
						count++;
						System.out
								.println("Removing segment with sequence number "
										+ windowManager.getWindow().get(0).sequenceNumber);
						windowManager.getWindow().remove();
						windowManager.numberOfoutstandingFrames
								.getAndDecrement();
					}

				}

				// socket.setSoTimeout(0);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Scoket timed out yay");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public FrameSender(Integer windowSize, Integer MSS, String fileName,
			String host, DatagramSocket socket, WindowManager windowManager,
			Timer timer) {

		this.MSS = MSS;
		this.windowSize = windowSize;
		this.host = host;
		this.sequenceNumber = 0;
		this.sourceFileName = fileName;
		this.socket = socket;
		this.windowManager = windowManager;
		this.timer = timer;
		this.isRetransmitting = new AtomicBoolean(false);
		this.windowAccess = new Semaphore(1);
	}

	public byte[] extractData(byte[] buf, int srcpos, int extractLength) {
		byte dest[] = new byte[extractLength];
		// System.out.println("Source pos is " + srcpos + "extract length is "
		// + extractLength);
		System.arraycopy(buf, srcpos, dest, 0, extractLength);
		return dest;
	}

	public int rdtSend(byte[] input) throws IOException, InterruptedIOException {
		int dataSentSize = 0;
		int srcPos = 0, length = input.length, originalLength = input.length;
		int extractLength = 0;
		int flag = 0;
		try {

			AcknowledgementReceiver ackReceiver = new AcknowledgementReceiver();
			Thread tReceiver = new Thread(ackReceiver);
			tReceiver.start();
			while (srcPos < originalLength) {
				// ystem.out.println("Source pos is " + srcPos
				// + "original length is " + originalLength);
				if (length < MSS) {
					extractLength = length;
				} else {
					length = length - MSS;
					extractLength = MSS;
				}

				byte[] extractedData = extractData(input, srcPos, extractLength);
				srcPos = srcPos + extractLength;
				// System.out.println("ExtractedData is of length"
				// + extractedData.length);
				int segmentCount = windowSize;
				while(!windowManager.getWindow().isWindowFlushed())
					;

				Segment segment = formSegment(extractedData);
				int first=storeSegment(windowManager.getWindow(), segment);
				System.out.println("First value"+ first);
				System.out.println("sending sequence number"
						+ segment.sequenceNumber);
				sequenceNumber++;
				byte[] segmentData = convertToBytes(segment);
				sendPacket(segmentData);
				windowManager.numberOfoutstandingFrames.getAndIncrement();
				if (first == 1) {
					System.out.println("TImer scheduled for "
							+ segment.sequenceNumber);
					flag = 1;
					Timer timer = new Timer();
					this.timer = timer;
					timer.schedule(new RetransmitTimer(), 1000);

					}
			}
		} catch (InterruptedIOException timeOut) {

			System.out.println("In socket timeout");
		}

		return dataSentSize;
	}

	public void sendPacket(byte[] segmentData) throws IOException {
		InetAddress address = InetAddress.getByName(host);

		DatagramPacket packet = new DatagramPacket(segmentData,
				segmentData.length, address, 4445);

		// System.out.println("Sending segment sequence number" +
		// packet.getLength());
		socket.send(packet);

	}

	public int storeSegment(Window window, Segment segment) {
		int firstSegment = window.add(segment);
		int index = window.indexOf(segment);
		System.out.println("Segment of sequence number "
				+ segment.sequenceNumber + "added at position " + index);

		return firstSegment;
		// window.get
	}

	public byte[] convertToBytes(Segment data) throws IOException {
		byte[] segmentTobeSent = PacketSerializer.serialize(data);

		return segmentTobeSent;
	}

	public Segment formSegment(byte[] inputData) throws IOException {

		Segment segmentData = new Segment(sequenceNumber, inputData);
		return segmentData;
	}

	@Override
	public void run() {

		FileInputStream input;
		try {

			File file = new File(sourceFileName);
			input = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			input.read(buf, 0, (int) file.length());

			rdtSend(buf);
			/*
			 * if (numberOfBytesRead < chunkSize) { byte[] smallBuffer = new
			 * byte[numberOfBytesRead]; System.arraycopy(buf, 0, smallBuffer, 0,
			 * numberOfBytesRead); rdtSend(smallBuffer); } else { rdtSend(buf);
			 * } buf = new byte[1024];
			 */

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void dataBufferingdDone() {

	}

}
