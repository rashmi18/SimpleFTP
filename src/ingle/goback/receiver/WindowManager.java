package ingle.goback.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WindowManager {

	AtomicInteger windowSizeInBytes;
	AtomicInteger outstandingFramePointer;
	AtomicInteger nextFramePointer;
	AtomicInteger numberOfoutstandingFrames;
	AtomicBoolean firstFrameSent;
	Window window;
	byte[] mainBuffer;

	public WindowManager() {

		// this.windowSizeInBytes.set(windowSize);
		this.outstandingFramePointer = new AtomicInteger(0);
		this.nextFramePointer = new AtomicInteger(0);
		this.outstandingFramePointer = new AtomicInteger(0);
		this.numberOfoutstandingFrames = new AtomicInteger(0);
		this.firstFrameSent = new AtomicBoolean(false);
		// mainBuffer = new byte[windowSizeInBytes.get()];
		this.window = new Window();
	}

	public void begin() {

	}
	
	public Window getWindow()
	{
		return window;
	}

	public boolean isEmpty()
	{
		if(window.getBuffer().size()!=0)
		return false;
		
		return true;
		
	}
	

}
