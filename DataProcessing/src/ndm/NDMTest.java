package ndm;

import java.sql.Connection;
import java.util.Arrays;

import oracle.spatial.network.lod.LODNetworkException;
import oracle.spatial.network.lod.LODNetworkManager;
import oracle.spatial.network.lod.LogicalPath;
import oracle.spatial.network.lod.LogicalSubPath;
import oracle.spatial.network.lod.NetworkAnalyst;
import oracle.spatial.network.lod.NetworkIO;
import oracle.spatial.network.lod.PointOnNet;

/**
 * 主要用于试验NDM的可用性
 * @author xmL
 *
 */
public class NDMTest {
	static Connection conn=DataConnection.GetConnection();
	static String netName="WH_ROADS";
	
	public static void main(String[] args){
		LogicalSubPath subPath=getsubPathWithoutConstr(22281, 0.1, 27857, 0.8);
		LogicalPath path=subPath.getReferencePath();
		long[] links = path.getLinkIds();
		System.out.println(Arrays.toString(links));
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
			NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(conn, netName, netName, null);
			NetworkAnalyst networkAnalyst = LODNetworkManager.getNetworkAnalyst(networkIO);
			LogicalSubPath path = networkAnalyst.shortestPathDijkstra(new PointOnNet(slinkid, sper), 
					new PointOnNet(elinkid, eper), null);
			return path;
		} catch (LODNetworkException e) {
			//e.printStackTrace();
			return null;
		}
	}
}
