package ingle.goback.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Window {

	List<Segment> buffer;
	AtomicInteger windowStart;
	AtomicInteger windowEnd;
	AtomicInteger windowSize;

	
	public int getWindowEnd()
	{
		return windowEnd.get();
	}
	public List<Segment> getBuffer() {
		return buffer;
	}

	Window(Integer windowSize) {
		buffer = Collections.synchronizedList(new ArrayList<Segment>());
		windowStart = new AtomicInteger(-1);
		windowEnd = new AtomicInteger(-1);
		this.windowSize = new AtomicInteger(windowSize);

	}

	public void setWindow() {
		int val;
		windowStart.set(0);
		val = (Integer) (buffer.size() < windowSize.get() ? buffer.size()
				: windowSize.get() - 1);
		windowEnd.set(val);
	}
	
	public boolean isEmpty()
	{
		return (buffer.size()==0);
	}

	public void add(Segment segment) {

		buffer.add(segment);
	}

	public boolean isWindowMoved() {
		return (windowEnd.get() == -1);

	}

	public Segment get(int i) {
		return buffer.get(i);
	}

	public int size() {
		return buffer.size();
	}

	public int indexOf(Segment segment) {
		return buffer.indexOf(segment);
	}

	public void remove() {
		System.out.println("Window ending is "+windowEnd.get());
		if (windowEnd.get() != -1) {
			buffer.remove(0);
			windowEnd.decrementAndGet();
			if (windowStart.get() == windowEnd.get()) {
				windowEnd.set(-1);
				windowStart.set(-1);
			}
			windowStart.getAndIncrement();

		}
	}

}