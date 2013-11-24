package ingle.goback.receiver;

import java.io.IOException;
import java.net.DatagramSocket;

public class SenderMain {

	public static void main(String[] args) throws IOException {

		int MSS = 1024, windowSize = 10;
		DatagramSocket socket = new DatagramSocket();
		DataSender sender = new DataSender(windowSize, MSS, args[0], args[1],
				socket);
		DataReceiver receiver = new DataReceiver(socket);

		WindowManager windowManager = new WindowManager(sender, receiver, MSS
				* windowSize);
		windowManager.begin();

	}
}
