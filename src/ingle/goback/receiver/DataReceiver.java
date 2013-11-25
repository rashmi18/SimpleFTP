package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DataReceiver implements Runnable {

	DatagramSocket socket;
	WindowManager windowManager;

	@Override
	public void run() {

		byte[] buf = new byte[200];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		try {
			while (true) {
				System.out.println("Waiting for ack");
				socket.receive(packet);
				byte[] object = new byte[packet.getLength()];
				System.out
						.println("Received data of size" + packet.getLength());
				System.arraycopy(buf, 0, object, 0, packet.getLength());
				Acknowledgement ack = PacketSerializer
						.acknowledgementDeserialize(object);
				System.out.println("Data expected" + ack.sequenceNumber);

				 while(windowManager.getWindow().get(0).sequenceNumber<=ack.sequenceNumber)
				 windowManager.getWindow().remove(0);

				 windowManager.numberOfoutstandingFrames.getAndDecrement();
				 //socket.setSoTimeout(0);
				 

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DataReceiver(DatagramSocket socket, WindowManager windowManager) {

		this.socket = socket;
		this.windowManager = windowManager;
	}

}
