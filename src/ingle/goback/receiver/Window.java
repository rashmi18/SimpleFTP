package ingle.goback.receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Window {

	List<Segment> buffer;
	AtomicInteger windowStart;
	AtomicInteger windowEnd;

	public List<Segment> getBuffer() {
		return buffer;
	}

	Window() {
		buffer = Collections.synchronizedList(new ArrayList<Segment>());
		windowStart = new AtomicInteger(-1);
		windowEnd = new AtomicInteger(-1);
	}

	public int add(Segment segment) {

		buffer.add(segment);
		windowEnd.getAndIncrement();
		if (windowEnd.get() == 0) {
			windowStart.getAndIncrement();
			return 1;
		}

		return 0;
	}

	public Segment get(int i)
	{
		return buffer.get(i);
	}
	public int size()
	{
		return buffer.size();
	}
	
	public int indexOf(Segment segment)
	{
		return buffer.indexOf(segment);
	}
	public void remove() {
		if (windowEnd.get() != -1) {
			if (windowStart.get() == windowEnd.get()) {
				buffer.remove(0);
				windowEnd.set(-1);
				windowStart.set(-1);
			}
			buffer.remove(0);
		}
	}
	public boolean isWindowFlushed()
	{
		return (windowEnd.get()== -1);
	}
}