package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReceiverMain {

	public static void main(String[] args) throws ClassNotFoundException {
		try {

			DatagramSocket socket = new DatagramSocket(4445);

			byte[] buf = new byte[1999];

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			FtpHandler handler = new FtpHandler(args[0], args[1]);
			while (true) {
				socket.receive(packet);
				int receivedBytes = packet.getLength();
				System.out.println("Received bytes" + receivedBytes);
				byte[] object = new byte[receivedBytes];
				System.arraycopy(buf, 0, object, 0, receivedBytes);

				handler.writeToFile(object.length, object);

				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket("ACK".getBytes(),
						"ACK".getBytes().length, address, port);
				socket.send(packet);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
