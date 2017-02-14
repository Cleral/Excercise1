package ndm.domain;

import java.util.List;

public class LinkText {
	private int linkId;
	private String linkName;
	private String oneway;
	private List<Double> ords;
	public List<Double> getOrds() {
		return ords;
	}
	public void setOrds(List<Double> ords) {
		this.ords = ords;
	}
	public int getLinkId() {
		return linkId;
	}
	public void setLinkId(int linkId) {
		this.linkId = linkId;
	}
	public String getLinkName() {
		return linkName;
	}
	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}
	public String getOneway() {
		return oneway;
	}
	public void setOneway(String oneway) {
		this.oneway = oneway;
	}
	
}
