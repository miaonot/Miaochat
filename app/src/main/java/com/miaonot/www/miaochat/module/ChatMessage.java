package com.miaonot.www.miaochat.module;

import java.util.Calendar;
import java.util.UUID;

//������Ϣ��
public class ChatMessage {

	private String id = null;
	private String from = null;
	private String to = null;
	private String time = null;
	private String content = null;
	
	//�����ͻ���ChatMessage
	public ChatMessage(String from, String to, String content)
	{
		this.id = UUID.randomUUID().toString();
		this.from = from;
		this.to = to;
		this.content = content;
		Calendar c = Calendar.getInstance();
		this.time = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.DATE) + " "
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

	}
	
	//�������Է�������ChatMessage
	public ChatMessage(String id, String from, String to,  String time,String content)
	{
		this.id = id;
		this.from = from;
		this.to = to;
		this.content = content;
		this.time = time;
	
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	
	//���ر��ĸ�ʽ
	public String getMessage()
	{
		String msg = null;
		msg = id +'\n'+ from +'\n'+ to +'\n'+ time +'\n'+ content;
		return msg;
	}
}
