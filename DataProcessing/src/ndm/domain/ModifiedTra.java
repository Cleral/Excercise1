package ndm.domain;

import java.sql.Struct;

public class ModifiedTra {
	private int node_id;
	private int file_id;
	private String time;
	private Struct struct;
	private int road_id;
	private String oneway;
	private boolean preNodeOnSameLink;
	
	public boolean isPreNodeOnSameLink() {
		return preNodeOnSameLink;
	}
	public void setPreNodeOnSameLink(boolean preNodeOnSameLink) {
		this.preNodeOnSameLink = preNodeOnSameLink;
	}
	public int getNode_id() {
		return node_id;
	}
	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}
	public int getFile_id() {
		return file_id;
	}
	public void setFile_id(int file_id) {
		this.file_id = file_id;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Struct getStruct() {
		return struct;
	}
	public void setStruct(Struct struct) {
		this.struct = struct;
	}
	public int getRoad_id() {
		return road_id;
	}
	public void setRoad_id(int road_id) {
		this.road_id = road_id;
	}
	public String getOneway() {
		return oneway;
	}
	public void setOneway(String oneway) {
		this.oneway = oneway;
	}
}
