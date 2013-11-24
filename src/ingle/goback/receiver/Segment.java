package ingle.goback.receiver;

import java.io.Serializable;

public class Segment implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer sequenceNumber;
	byte [] dataTobeSent;
	Segment(Integer sequenceNumber, byte []dataTobeSent)
	{
		this.sequenceNumber = sequenceNumber;
		this.dataTobeSent = dataTobeSent;
	}

}
