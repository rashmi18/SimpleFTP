package ingle.goback.receiver;

public class WindowManager {

	Integer windowSize;
	Integer outstandingFrame;
	Integer nextFrame;
	byte[] mainBuffer;

	public WindowManager(Integer windowSize) {

		this.windowSize = windowSize;
		this.outstandingFrame = 0;
		this.nextFrame = 0;

		mainBuffer = new byte[windowSize];

	}

	public void begin() {

	}
}
