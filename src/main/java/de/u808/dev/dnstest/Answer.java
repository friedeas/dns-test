package de.u808.dev.dnstest;

import com.google.gson.annotations.SerializedName;

public class Answer {

	private String name;
	private int type;
	@SerializedName("TTL") private int ttl;
	private String data;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
}
