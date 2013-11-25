package ingle.goback.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.List;

public class DataSender implements Runnable {

	Integer windowSize;
	Integer MSS;
	String host;
	DatagramSocket socket;
	Integer sequenceNumber;
	String sourceFileName;
	WindowManager windowManager;

	public DataSender(Integer windowSize, Integer MSS, String fileName,
			String host, DatagramSocket socket, WindowManager windowManager) {

		this.MSS = MSS;
		this.windowSize = windowSize;
		this.host = host;
		this.sequenceNumber = 0;
		this.sourceFileName = fileName;
		this.socket = socket;
		this.windowManager = windowManager;
	}

	public byte[] extractData(byte[] buf, int srcpos, int extractLength) {
		byte dest[] = new byte[extractLength];
		System.out.println("Source pos is "+srcpos + "extract length is "+extractLength);
		System.arraycopy(buf, srcpos, dest, 0, extractLength);
		return dest;
	}

	public int rdtSend(byte[] input) throws IOException {
		int dataSentSize = 0;
		int srcPos = 0, length = input.length, originalLength = input.length;
		int extractLength = 0;

		try {
			while (srcPos < originalLength) {
				System.out.println("Source pos is "+srcPos + "original length is "+originalLength);
				if (length < MSS) {
					extractLength = length;
				} else {
					length = length - MSS;
					extractLength = MSS;
				}

				
				byte[] extractedData = extractData(input, srcPos, extractLength);
				srcPos = srcPos + extractLength;
				System.out.println("ExtractedData is of length"
						+ extractedData.length);

				while (windowManager.numberOfoutstandingFrames.get() >= windowSize) {
					System.out.println("numberof outstanding frames"
							+ windowManager.numberOfoutstandingFrames.get());
				}
				System.out.println("after while loop");
				Segment segment = formSegment(extractedData);
				storeSegment(windowManager.getWindow(), segment);

				byte[] segmentData = convertToBytes(segment);
				sendPacket(segmentData);
				windowManager.numberOfoutstandingFrames.getAndIncrement();
				
				//System.out.println("Time out is "+socket.getSoTimeout());	
				//if (socket.getSoTimeout() == 0) {
					socket.setSoTimeout(1000);
			//	}

			}
		} catch (SocketTimeoutException timeOut) {

			System.out.println("In socket timeout");
		}

		return dataSentSize;
	}

	public void sendPacket(byte[] segmentData) throws IOException {
		InetAddress address = InetAddress.getByName(host);

		DatagramPacket packet = new DatagramPacket(segmentData,
				segmentData.length, address, 4445);

		System.out.println("Sending segment of size" + packet.getLength());
		socket.send(packet);
		sequenceNumber++;
	}

	public void storeSegment(List<Segment> window, Segment segment) {
		window.add(segment);

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
