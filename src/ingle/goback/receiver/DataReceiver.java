package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DataReceiver implements Runnable {

	DatagramSocket socket;

	@Override
	public void run() {

		byte[] buf = new byte[40];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		try {
			while (true) {
				System.out.println("Waiting for ack");
				socket.receive(packet);
				byte[] object = new byte[packet.getLength()];
				System.arraycopy(buf, 0, object, 0, packet.getLength());
				System.out.println("Received ACK");
				for (int i = 0; i < packet.getLength(); i++) {
					System.out.println((char) object[i]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DataReceiver(DatagramSocket socket) {

		this.socket = socket;
	}

}
