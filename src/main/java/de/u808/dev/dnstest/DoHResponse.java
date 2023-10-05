package de.u808.dev.dnstest;

import com.google.gson.annotations.SerializedName;

public class DoHResponse {

	@SerializedName("Status") private int status;
	
	// Response is truncated
	@SerializedName("TC") private boolean tc;
	
	// Response was validated with DNSSEC
	@SerializedName("AD") private boolean ad;
	
	// Client disable DNSSEC
	@SerializedName("CD") private boolean cd;
	
	@SerializedName("Question") private Question[] questions;
	
	@SerializedName("Answer") private Answer[] answers;
	
	@SerializedName("edns_client_subnet") private String ednsClientSubnet;
	
	@SerializedName("Comment") private String comment;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isTc() {
		return tc;
	}

	public void setTc(boolean tc) {
		this.tc = tc;
	}

	public boolean isAd() {
		return ad;
	}

	public void setAd(boolean ad) {
		this.ad = ad;
	}

	public boolean isCd() {
		return cd;
	}

	public void setCd(boolean cd) {
		this.cd = cd;
	}

	public Question[] getQuestions() {
		return questions;
	}

	public void setQuestions(Question[] questions) {
		this.questions = questions;
	}

	public Answer[] getAnswers() {
		return answers;
	}

	public void setAnswers(Answer[] answers) {
		this.answers = answers;
	}

	public String getEdnsClientSubnet() {
		return ednsClientSubnet;
	}

	public void setEdnsClientSubnet(String ednsClientSubnet) {
		this.ednsClientSubnet = ednsClientSubnet;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public boolean successful() {
		return status == 0;
	}
}
