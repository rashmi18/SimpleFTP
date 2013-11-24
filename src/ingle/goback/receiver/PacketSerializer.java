package ingle.goback.receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PacketSerializer {

public static byte[] serialize(Segment segment) throws IOException
{
	
	
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ObjectOutputStream oos = new ObjectOutputStream(baos);
	oos.writeObject(segment);
	oos.close();
	// get the byte array of the object
	byte []serializedData = baos.toByteArray();
	System.out.println("Sending segment of   buffersize" + serializedData.length);
	baos.close();
	
	return serializedData;
}
public static Segment deserialize(byte[]data) throws IOException, ClassNotFoundException
{
	ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(data));
    Segment obj = (Segment) iStream.readObject();
    iStream.close();
        return obj;
}



}

