package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReceiverMain {

	public static void main(String[] args) throws ClassNotFoundException {
		try {

			
			int portNumber = Integer.parseInt(args[0]);
			
			String fileName = args[1];
			String probability = args[2];
			
			
			DatagramSocket socket = new DatagramSocket(portNumber);

			byte[] buf = new byte[4000];

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			FtpHandler handler = new FtpHandler(portNumber, fileName,probability,socket);
			while (true) {
				socket.receive(packet);
				int receivedBytes = packet.getLength();
				byte[] object = new byte[receivedBytes];
				System.arraycopy(buf, 0, object, 0, receivedBytes);

				handler.acceptData(object.length, object,packet);

				

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
