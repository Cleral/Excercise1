package ndm.ConstructNetWork;

import java.sql.Connection;

import oracle.spatial.network.lod.LODNetworkException;
import oracle.spatial.network.lod.LODNetworkManager;
import oracle.spatial.network.lod.LogicalPath;
import oracle.spatial.network.lod.LogicalSubPath;
import oracle.spatial.network.lod.NetworkAnalyst;
import oracle.spatial.network.lod.NetworkIO;
import oracle.spatial.network.lod.PointOnNet;
import ndm.*;

public class ShortestPathTest {
	static Connection conn;
	public static void main(String[] args)
	{
		try {
			conn=DataConnection.GetConnection();
			//TrasPostProcessing test=new TrasPostProcessing(conn,"WH_ROADS2");
			LogicalSubPath subPath = getsubPathWithoutConstr(16247, 0.5, 22281, 0.5);
			LogicalPath path = subPath.getReferencePath();
			long[] links = path.getLinkIds();
			StringBuffer sb = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			for (int i = 0; i < links.length; i++) {
				sb.append(String.valueOf(links[i]));
				sb2.append("'"+links[i]+"'"+","+(i+1));
				if (i != links.length-1) {
					sb.append(",");
					sb2.append(",");
				}
			}
			System.out.println(sb);
			System.out.println(sb2);
			conn.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * @param slinkid 起点所匹配到的link id
	 * @param sper 起点在link上所占的比例
	 * @param elinkid 终点所匹配到的link id
	 * @param eper 终点在link上所占的比例
	 * @return 起点到终点的最短路径 
	 */
	private static LogicalSubPath getsubPathWithoutConstr(long slinkid,double sper,long elinkid,double eper){
		try {
			NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(conn, "WH_ROADS", "WH_ROADS", null);
			NetworkAnalyst networkAnalyst = LODNetworkManager.getNetworkAnalyst(networkIO);
			LogicalSubPath path = networkAnalyst.shortestPathDijkstra(new PointOnNet(slinkid, sper), 
					new PointOnNet(elinkid, eper), null);
			return path;
		} catch (LODNetworkException e) {
			e.printStackTrace();
			return null;
		}
	}
}
