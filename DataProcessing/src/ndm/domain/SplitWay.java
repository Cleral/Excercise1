package ndm.domain;

import java.util.ArrayList;

public class SplitWay {
	private int wayid;
	private int nodeNum;
	private ArrayList<String> nodeIds;
	private String type;
	
	public int getWayid() {
		return wayid;
	}
	public void setWayid(int wayid) {
		this.wayid = wayid;
	}
	public int getNodeNum() {
		return nodeNum;
	}
	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}
	public ArrayList<String> getNodeIds() {
		return nodeIds;
	}
	public void setNodeIds(ArrayList<String> nodeIds) {
		this.nodeIds = nodeIds;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
