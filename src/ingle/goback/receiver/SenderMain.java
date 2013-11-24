package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramSocket;

public class SenderMain {

	public static void main(String[] args) throws IOException {

		DatagramSocket socket = new DatagramSocket();
		DataSender sender = new DataSender(4, 100, args[0], args[1], socket);
		DataReceiver receiver = new DataReceiver(socket);

		Thread tSender = new Thread(sender);
		Thread tReceiver = new Thread(receiver);

		tSender.start();
		tReceiver.start();

	}
}
