package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Timer;

public class SenderMain {

	public static void main(String[] args) throws IOException {

		int MSS = 1000, windowSize = 3;
		DatagramSocket socket = new DatagramSocket();
		WindowManager windowManager = new WindowManager();
		Timer timer = new Timer();
		FrameSender sender = new FrameSender(windowSize, MSS, args[0], args[1],
				socket, windowManager, timer);

		Thread tSender = new Thread(sender);
		// Thread tReceiver = new Thread(receiver);

		tSender.start();
		// tReceiver.start();

	}
}
