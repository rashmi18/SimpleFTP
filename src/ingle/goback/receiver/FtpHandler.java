package ingle.goback.receiver;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FtpHandler {

	byte[] inputData;
	String fileName;
	FileOutputStream out;
	double probability;
	DatagramPacket packet;
	DatagramSocket socket;
	int sequenceNumberExpected = 0;

	public FtpHandler(String fileName, String probability, DatagramSocket socket)
			throws IOException {
		this.fileName = fileName;
		this.probability = Double.parseDouble(probability);
		this.socket = socket;
		out = new FileOutputStream(fileName);

	}

	public void acceptData(int length, byte[] inputData, DatagramPacket packet)
			throws IOException, ClassNotFoundException {
		this.packet = packet;
		Segment obj = PacketSerializer.deserialize(inputData);
		if (packetToBeDiscarded(obj)) {
			System.out.println("Sequence Number received " + obj.sequenceNumber
					+ " and packet lost");

		} else {

			writeToFile(length, obj);
			sendACK(obj.sequenceNumber + 1);
			sequenceNumberExpected = obj.sequenceNumber + 1;
		}
	}

	public void sendACK(int sequenceNumber) throws IOException {
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
		Acknowledgement acknowledgement = new Acknowledgement();
		acknowledgement.sequenceNumber = sequenceNumber;

		System.out.println("Ack sent");
		byte[] buf = PacketSerializer.acknowledgementSerialize(acknowledgement);
		DatagramPacket ackPacket = new DatagramPacket(buf, buf.length, address,
				port);
		socket.send(ackPacket);
	}

	public void writeToFile(int length, Segment obj) throws IOException,
			ClassNotFoundException {

		// System.out.println("Sequence Number is "+extractSequenceNumber(inputData));

		System.out.println("Sequence Number received " + obj.sequenceNumber
				+ " and packet accepted");
		String str = new String("Packet no." + obj.sequenceNumber);
		out.write(str.getBytes());
		out.write(obj.dataTobeSent);
	}

	public boolean packetToBeDiscarded(Segment obj) {
		int max = 1, min = 0;
		double randomValue = Math.random() * ((max - min) + 1);
		if (randomValue > 1.0)
			randomValue -= 1.0;
		System.out.println(randomValue);
		 if(obj.sequenceNumber == 0)
		 return true;
		//if (obj.sequenceNumber != sequenceNumberExpected)
			//return true;
		if (randomValue > probability) // accept packet
		{
			return true;

		}
		return false;

	}

	public int extractSequenceNumber(byte[] input) throws IOException {
		int sequenceNumber = 0;
		byte[] sequence = new byte[3];
		System.arraycopy(input, 0, sequence, 0, 2);

		ByteArrayInputStream bis = new ByteArrayInputStream(sequence);
		ObjectInputStream ois = new ObjectInputStream(bis);
		sequenceNumber = ois.readInt();
		ois.close();
		return sequenceNumber;
	}

	public byte[] extractData(byte[] inputData) {
		byte[] input = new byte[inputData.length - 2];

		System.arraycopy(inputData, 2, input, 0, inputData.length - 2);

		return input;
	}

}
