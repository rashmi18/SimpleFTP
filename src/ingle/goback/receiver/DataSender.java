package ingle.goback.receiver;

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

	public DataSender(Integer windowSize, Integer MSS, String fileName,
			String host, DatagramSocket socket) {

		this.MSS = MSS;
		this.windowSize = windowSize;
		this.host = host;
		this.sequenceNumber = 0;
		this.sourceFileName = fileName;
		this.socket = socket;
	}

	public int rdtSend(byte[] input) throws IOException {
		int dataSentSize = 0;
		if (input.length > MSS) {

			byte[] segment = formSegment(input);

			// FileOutputStream out = new FileOutputStream("temp.txt");
			// if (sequenceNumber == 1)
			// out.write(segment);
			InetAddress address = InetAddress.getByName(host);

			DatagramPacket packet = new DatagramPacket(segment, segment.length,
					address, 4445);

			System.out.println("Sending segment of size" + packet.getLength());
			socket.send(packet);
			sequenceNumber++;
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
			input = new FileInputStream(sourceFileName);
			byte[] buf = new byte[1024];
			while ((numberOfBytesRead = input.read(buf)) != -1) {

				if (numberOfBytesRead < chunkSize) {
					byte[] smallBuffer = new byte[numberOfBytesRead];
					System.arraycopy(buf, 0, smallBuffer, 0, numberOfBytesRead);
					rdtSend(smallBuffer);
				} else {
					rdtSend(buf);
				}
				buf = new byte[1024];

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
