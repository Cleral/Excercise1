package ndm.domain;

import java.sql.Struct;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.network.Path;
import oracle.spatial.network.SubPath;

public class Tra_Node {
	public int root_index;
	public int id;
	public int link_id;
    public int fileid;
    public String time;
    public double cost;
    public Struct st_old;
    public JGeometry geo_old;
    public Struct st_new;
    public JGeometry geo_new;
    public Struct st_link;
    public JGeometry geo_link;
    public int link_startNodeID;
    public int link_endNodeID;
    public String oneway;
    public double link_length;
    public double percentage;
    public Path path;
    public SubPath subPath;
    public int beforeid;
	/**
	 * @param root_index 父节点的索引
	 * @param id  当前轨迹gps的轨迹
	 * @param link_id link编号
	 * @param fileid 当前轨迹数据文件编号
	 * @param time 当前轨迹点采集时间
	 * @param cost 消耗成本
	 * @param geo_old 原始点
	 * @param geo_new 最近点
	 * @param geo_link link
	 * @param st_old 原始点
	 * @param st_new 最近点
	 * @param st_link link
	 * @param link_startNodeID link开始端点
	 * @param link_endNodeID link结束端点
	 * @param oneway link单向还是双向的属性
	 * @param link_length link的长度
	 * @param percentage 起点到最近点所占link的比重
	 * @param path 前后两结点之间的最短路径
	 * @param subpath 前后两点之间的最短路径
	 * @param beforeid 前一结点
	 */
	public Tra_Node(int root_index,int id, int link_id,int fileid,String time,double cost,
			Struct st_old,JGeometry geo_old,Struct st_new,JGeometry geo_new,Struct st_link,
			JGeometry geo_link,int link_startNodeID,int link_endNodeID,String oneway,double link_length, 
			double percentage,Path path,SubPath subPath,int beforeid) 
	{
		this.root_index = root_index;
		this.id = id;
		this.link_id = link_id;
		this.fileid = fileid;
		this.time = time;
		this.cost = cost;
		this.geo_new = geo_new;
		this.geo_old = geo_old;
		this.geo_link = geo_link;
		this.link_startNodeID = link_startNodeID;
		this.link_endNodeID = link_endNodeID;
		this.oneway = oneway;
		this.link_length = link_length;
		this.st_old = st_old;
		this.st_new = st_new;
		this.st_link = st_link;
		this.percentage = percentage;
		this.path = path;
		this.subPath = subPath;
		this.beforeid = beforeid;
	}

}

