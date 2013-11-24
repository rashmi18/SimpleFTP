package ingle.goback.receiver;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class FtpHandler {

	byte[] inputData;
	String fileName;
	FileOutputStream out;
	double probability;

	public FtpHandler(String fileName, String probability) throws IOException {
		this.inputData = inputData;
		this.fileName = fileName;
		this.probability = Double.parseDouble(probability);
		out = new FileOutputStream(fileName);

	}

	public void writeToFile(int length, byte[] inputData) throws IOException, ClassNotFoundException {

	//	System.out.println("Sequence Number is "+extractSequenceNumber(inputData));
		Segment obj = PacketSerializer.deserialize(inputData);
		
		if(packetToBeDiscarded())
		{
			System.out.println("Sequence Number received "+obj.sequenceNumber+" and packet lost");
			
		}
		else
		{
			System.out.println("Sequence Number received "+obj.sequenceNumber+" and packet accepted");
			out.write(obj.dataTobeSent);
		}

	}

	public boolean packetToBeDiscarded()
	{
		int max = 1,min=0;
		double randomValue = Math.random()*((max-min)+1);
		if(randomValue>1.0)
			randomValue-=1.0;
		System.out.println(randomValue);
		if(randomValue>probability) //accept packet
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
	public byte[] extractData(byte[]inputData)
	{
		byte[] input = new byte[inputData.length-2];
		
		System.arraycopy(inputData, 2, input, 0, inputData.length-2);
		
		return input;
	}

}
