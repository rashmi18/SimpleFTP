package ingle.goback.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
		System.arraycopy(buf, srcpos, dest, 0, extractLength);
		return dest;
	}

	public int rdtSend(byte[] input) throws IOException {
		int dataSentSize = 0;
		int srcPos = 0, length = input.length, originalLength = input.length;
		int extractLength = 0;

		if (input.length > MSS) {

			while (srcPos < originalLength) {
				if (length < MSS) {
					extractLength = length;
				} else {
					length = length - MSS;
					extractLength = MSS;
				}

				srcPos = srcPos + extractLength;
				byte[] extractedData = extractData(input, srcPos, extractLength);
				System.out.println("ExtractedData is of length"
						+ extractedData.length);
				byte[] segment = formSegment(extractedData);

				// FileOutputStream out = new FileOutputStream("temp.txt");
				// if (sequenceNumber == 1)
				// out.write(segment);
				InetAddress address = InetAddress.getByName(host);

				DatagramPacket packet = new DatagramPacket(segment,
						segment.length, address, 4445);

				System.out.println("Sending segment of size"
						+ packet.getLength());
				socket.send(packet);
				sequenceNumber++;
			}

		}

		return dataSentSize;
	}

	public byte[] formSegment(byte[] inputData) throws IOException {

		Segment segmentData = new Segment(sequenceNumber, inputData);
		byte[] segmentTobeSent = PacketSerializer.serialize(segmentData);

		return segmentTobeSent;
	}

	@Override
	public void run() {

		FileInputStream input;
		try {

			int numberOfBytesRead = 0;
			int chunkSize = 1024;
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
