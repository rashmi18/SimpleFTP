package ingle.goback.receiver;

import java.awt.Frame;
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
	AtomicBoolean isTimerRunning;
	private Semaphore windowAccess;

	class RetransmitTimer extends TimerTask {

		
		public void resendFrames() throws IOException
		{
			InetAddress address = InetAddress.getByName(host);
			byte[] segmentData;
			int windowEnd = windowManager.getWindow().getWindowEnd();
			for(int i =0;i<=windowEnd;i++)
			{
				//if(!isTimerRunning.get())
				//{
					timer = new Timer();
					timer.schedule(new RetransmitTimer(),1000);
				//}
				Segment segment = windowManager.getWindow().getBuffer().get(i);
				segmentData = convertToBytes(segment);
				DatagramPacket packet = new DatagramPacket(segmentData,
						segmentData.length, address, 4445);

				 System.out.println("Resending segment sequence number" +
				 segment.sequenceNumber);
				socket.send(packet);

			}
				
		}
		@Override
		public void run() {

			if (windowManager.getWindow().size() > 0) {
				isRetransmitting.set(true);
				timer = new Timer();
				timer.schedule(new RetransmitTimer(), 1000);

			}

				try {
			//		windowAccess.acquire();
					resendFrames();
					windowAccess.release();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
					windowAccess.acquire();
					while (!windowManager.isEmpty()
							&& windowManager.getWindow().get(0).sequenceNumber < ack.sequenceNumber) {
						// if retransmitting, don't remove the element for which
						// late ack received

						
						count++;
						System.out
								.println("Removing segment with sequence number "
										+ windowManager.getWindow().get(0).sequenceNumber);
						windowManager.getWindow().remove();
					}
					if(isTimerRunning.get())
					timer.cancel();
					isTimerRunning.set(false);
				}

				// socket.setSoTimeout(0);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Scoket timed out yay");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
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
		this.isTimerRunning = new AtomicBoolean(false);
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
				Segment segment = formSegment(extractedData);
				storeSegment(windowManager.getWindow(), segment);
				sequenceNumber++;
			}	
				
			while(!windowManager.getWindow().isEmpty())
			{
				windowManager.getWindow().setWindow();
				//byte[] segmentData = convertToBytes(segment);
				//sendFrames(segmentData);
				//windowManager.numberOfoutstandingFrames.getAndIncrement();
					//		+ segment.sequenceNumber);
					flag = 1;
					//Timer timer = new Timer();
					//this.timer = timer;
					//timer.schedule(new RetransmitTimer(), 1000);
					//isTimerRunning.set(true);
					sendFrames();
					while(!windowManager.getWindow().isWindowMoved())
						;
			} 
		}
		catch (InterruptedIOException timeOut) {

			System.out.println("In socket timeout");
		}

		return dataSentSize;
	}

	public void sendFrames() throws IOException {
		InetAddress address = InetAddress.getByName(host);
		byte[] segmentData;
		int windowEnd = windowManager.getWindow().getWindowEnd();
		for(int i =0;i<=windowEnd;i++)
		{
			//if(!isTimerRunning.get())
			//{
				timer = new Timer();
				timer.schedule(new RetransmitTimer(),1000);
			//}
			Segment segment = windowManager.getWindow().getBuffer().get(i);
			segmentData = convertToBytes(segment);
			DatagramPacket packet = new DatagramPacket(segmentData,
					segmentData.length, address, 4445);

			 System.out.println("Sending segment sequence number" +
			 segment.sequenceNumber);
			socket.send(packet);

		}
			
		
	}

	public void storeSegment(Window window, Segment segment) {
		window.add(segment);
		int index = window.indexOf(segment);
		System.out.println("Segment of sequence number "
				+ segment.sequenceNumber + "added at position " + index);

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
