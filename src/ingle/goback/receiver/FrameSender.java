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
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameSender {

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
	int numberOfSegments;
	int sLast;
	int sFirst;
	int sNext;
	int receivedACK;
	int portNumber;
	int receivedFrame=0;

	public FrameSender(String host, Integer portNumber,String fileName,Integer windowSize, Integer MSS, 
			DatagramSocket socket, WindowManager windowManager,
			Timer timer) {

		this.portNumber=portNumber;
		this.MSS = MSS;
		this.windowSize = windowSize;
		this.host = host;
		this.sequenceNumber = 0;
		this.sourceFileName = fileName;
		this.socket = socket;
		this.windowManager = windowManager;
		this.timer = timer;
		this.sLast = 0;
		this.sFirst = 0;
		this.receivedFrame=0;
		this.receivedACK = 0;
	}

	public byte[] extractData(byte[] buf, int srcpos, int extractLength) {
		byte dest[] = new byte[extractLength];
		// System.out.println("Source pos is " + srcpos + "extract length is "
		// + extractLength);
		System.arraycopy(buf, srcpos, dest, 0, extractLength);
		return dest;
	}

	public void rdtSend(byte[] input) throws IOException,
			InterruptedIOException, ClassNotFoundException {
		int dataSentSize = 0;
		int srcPos = 0, length = input.length, originalLength = input.length;
		int extractLength = 0;
		int flag = 0;

		while (srcPos < originalLength) {
			// ystem.out.println("Source pos is " + srcPos
			// + "original length is " + originalLength);
			if (length < MSS) {
				extractLength = length;
			} else {
				length = length - MSS;
				extractLength = MSS;
			}

			byte [] extractedData = extractData(input, srcPos, extractLength);
			srcPos = srcPos + extractLength;
			Segment segment = formSegment(extractedData);
			storeSegment(windowManager.getWindow(), segment);
			sequenceNumber++;
		}
		numberOfSegments = sequenceNumber;
		this.sNext = (windowSize > numberOfSegments) ? numberOfSegments
				: windowSize - 1;

		//begin sending 
		long startTime = System.currentTimeMillis();
		while (receivedFrame != numberOfSegments) {
			try {
				sendFrames();
				int expectedSeq = receiveACK();
				moveWindow(expectedSeq);
			} catch (SocketTimeoutException timeOut) {

				System.out.println("Timeout, sequence number = "
						+ windowManager.getWindow().getBuffer()
						.get(sFirst).sequenceNumber);
				
				for (int i = sFirst; i <= sNext; i++) {
					
					
					Segment segment = windowManager.getWindow().getBuffer()
							.get(i);
					//System.out.println("Retransmitting "+segment.sequenceNumber);
					byte[] segmentData = convertToBytes(segment);
					InetAddress address = InetAddress.getByName(host);
					DatagramPacket packet = new DatagramPacket(segmentData,
							segmentData.length, address, portNumber);

					

					socket.send(packet);
					socket.setSoTimeout(2000);
				}

			}

		}
		long stopTime = System.currentTimeMillis();
		long delay=stopTime-startTime;
		
		double elapsedSeconds = delay/1000.0;
		//System.out.println("Done sending data");
		System.out.println("Delay is "+elapsedSeconds+" seconds");
	}

	public void moveWindow(int expectedSequenceNumber) {

		//System.out.println("R ACK"expectedSequenceNumber+"S EXP"+windowManager.getWindow().get(sFirst).sequenceNumber);
		if (expectedSequenceNumber == (windowManager.getWindow().get(sFirst).sequenceNumber+1)) {
			receivedFrame++;
			sLast = sNext + 1;
			
			if (sNext < numberOfSegments - 1) {
				sNext++;
			}
			if (sFirst <= sNext) {
				sFirst++;
			}

		}

	}

	public int receiveACK() throws IOException, ClassNotFoundException {
		//System.out.println("Waiting for ack");
		byte[] buf = new byte[200];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		byte[] object = new byte[packet.getLength()];
		System.arraycopy(buf, 0, object, 0, packet.getLength());
		Acknowledgement ack = PacketSerializer
				.acknowledgementDeserialize(object);
		//System.out.println("ACK received" + ack.sequenceNumber);
		receivedACK++;
		return ack.sequenceNumber;

	}

	public void sendFrames() throws IOException {
		byte[] segmentData;
		InetAddress address = InetAddress.getByName(host);

		for (int i = sLast; i <= sNext; i++) {
			Segment segment = windowManager.getWindow().getBuffer().get(i);
			segmentData = convertToBytes(segment);
			DatagramPacket packet = new DatagramPacket(segmentData,
					segmentData.length, address, portNumber);

		//	System.out.println("Sending segment sequence number"
			//		+ segment.sequenceNumber);

			socket.send(packet);
			socket.setSoTimeout(2000);

		}

	}

	public void storeSegment(Window window, Segment segment) {
		window.add(segment);
		int index = window.indexOf(segment);
		//System.out.println("Segment of sequence number "
			//	+ segment.sequenceNumber + "added at position " + index);

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

}
