package ndm.domain;

import java.sql.Struct;

public class LinkData {
	private int linkid;
	private int startNodeId;
	private int endNodeId;
	private Struct struct;
	private String linkName;
	private String type;
	private double linkLength;
	private char bidirected;
	private char active='Y';
	private int linklevel=1;
	
	public int getLinkid() {
		return linkid;
	}
	public void setLinkid(int linkid) {
		this.linkid = linkid;
	}
	public int getStartNodeId() {
		return startNodeId;
	}
	public void setStartNodeId(int startNodeId) {
		this.startNodeId = startNodeId;
	}
	public int getEndNodeId() {
		return endNodeId;
	}
	public void setEndNodeId(int endNodeId) {
		this.endNodeId = endNodeId;
	}
	public Struct getStruct() {
		return struct;
	}
	public void setStruct(Struct struct) {
		this.struct = struct;
	}
	public String getLinkName() {
		return linkName;
	}
	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getLinkLength() {
		return linkLength;
	}
	public void setLinkLength(double linkLength) {
		this.linkLength = linkLength;
	}
	public char getBidirected() {
		return bidirected;
	}
	public void setBidirected(char bidirected) {
		this.bidirected = bidirected;
	}
	public char getActive() {
		return active;
	}
	public void setActive(char active) {
		this.active = active;
	}
	public int getLinklevel() {
		return linklevel;
	}
	public void setLinklevel(int linklevel) {
		this.linklevel = linklevel;
	}
	
}
