package ingle.goback.receiver;

public class WindowManager {

	
	DataSender sender;
	DataReceiver receiver;
	Integer windowSize;
	Integer outstandingFrame;
	Integer nextFrame;
	byte[] mainBuffer;
	public WindowManager(DataSender sender, DataReceiver receiver, Integer windowSize) {
		this.sender = sender;
		this.receiver = receiver;
		this.windowSize = windowSize;
		
		mainBuffer = new byte[windowSize];

		
	
	}

	public void begin()
	{
		Thread tSender = new Thread(sender);
		Thread tReceiver = new Thread(receiver);

		tSender.start();
		tReceiver.start();
	}
}
