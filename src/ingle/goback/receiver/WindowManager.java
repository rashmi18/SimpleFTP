package ingle.goback.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WindowManager {

	AtomicInteger windowSizeInBytes;
	AtomicInteger outstandingFramePointer;
	AtomicInteger nextFramePointer;
	AtomicInteger numberOfoutstandingFrames;
	private List<Segment> window;
	byte[] mainBuffer;

	public WindowManager() {

		//this.windowSizeInBytes.set(windowSize);
		this.outstandingFramePointer = new AtomicInteger(0);
		this.nextFramePointer=new AtomicInteger(0);
		this.outstandingFramePointer= new AtomicInteger(0);
		this.numberOfoutstandingFrames = new AtomicInteger(0);

		//mainBuffer = new byte[windowSizeInBytes.get()];
		window=Collections.synchronizedList(new ArrayList<Segment>());
	}

	public void begin() {

	}

	public List<Segment> getWindow() {
		return window;
	}

	

	
	
}
