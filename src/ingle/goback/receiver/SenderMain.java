package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramSocket;

public class SenderMain {

	public static void main(String[] args) throws IOException {

		int MSS = 1024, windowSize = 10;
		DatagramSocket socket = new DatagramSocket();
		WindowManager windowManager = new WindowManager(MSS * windowSize);
		DataSender sender = new DataSender(windowSize, MSS, args[0], args[1],
				socket, windowManager);
		DataReceiver receiver = new DataReceiver(socket, windowManager);

		Thread tSender = new Thread(sender);
		Thread tReceiver = new Thread(receiver);

		tSender.start();
		tReceiver.start();

	}
}
