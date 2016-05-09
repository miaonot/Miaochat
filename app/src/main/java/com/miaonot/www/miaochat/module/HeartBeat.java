package com.miaonot.www.miaochat.module;

import java.util.UUID;

public class HeartBeat {

	private UUID id = null;
	public HeartBeat()
	{
		this.id = UUID.randomUUID();
	}
	
	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}
}
