package ingle.goback.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Timer;

public class SenderMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		DatagramSocket socket = new DatagramSocket();
		
		Timer timer = new Timer();
		
		String hostName = args[0];
		int serverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		int windowSize = Integer.parseInt(args[3]);
		int MSS = Integer.parseInt(args[4]);
		
		
		
		
		WindowManager windowManager = new WindowManager(windowSize);
		FrameSender sender = new FrameSender(hostName, serverPort, fileName,
				windowSize,MSS, socket,windowManager, timer);


		File file = new File(fileName);
		FileInputStream input = new FileInputStream(file);
		byte[] buf = new byte[(int) file.length()];
		input.read(buf, 0, (int) file.length());

		sender.rdtSend(buf);
		//Thread tSender = new Thread(sender);
		// Thread tReceiver = new Thread(receiver);

		//tSender.start();
		// tReceiver.start();

	}
}
