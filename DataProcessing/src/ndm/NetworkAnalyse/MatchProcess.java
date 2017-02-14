package ndm.NetworkAnalyse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import ndm.DataConnection;
import oracle.spatial.network.Network;
import oracle.spatial.network.NetworkManager;


public class MatchProcess {
	private static Logger logger = Logger.getLogger(MatchProcess.class);
	 public static void main ( String[] args )
	    {    
	    	//File f = new File("D:\\mapmatching\\data\\mapmatch", "error.txt");
			try
			{
				//FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\mapmatching\\data\\mapmatch\\error.txt",true)));
//				for (int i = 36; i <= 50; i++)
//	            {
				
	    		   mapmatching(3452, 3500, bw); 
			   // }
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	    	
	    	
	    	    		
	    }
	
	private static void mapmatching(int min, int max, BufferedWriter bw)
    {
    	 Connection conn = DataConnection.GetConnection();;
         if(conn != null)
         {
             System.out.println ( "连接成功！" );
             
         }
         else
         {
             System.out.println("连接失败！");
         } 
         String sql = "select fileid,count(*) count from wh_tras group by fileid order by fileid";
         try 
         {
        	Network wh_roads = NetworkManager.readNetwork(conn, "wh_roads"); //加载网络
			PreparedStatement ps_tra = conn.prepareStatement(sql);
			ResultSet rs = ps_tra.executeQuery();
			while (rs.next()) 
			{
				int fileid = rs.getInt("fileid");
//				if (fileid/100 >= num)
//				{
//					break;
//				}
				if (fileid < min)
				{
					continue;
				}
				if ( fileid >= max ) 
				{
					break;
				}
				
				System.out.println("正在匹配：--------------------"+ fileid +"---------------------");
				logger.debug("正在匹配：--------------------"+ fileid +"---------------------");
				logger.error("正在匹配：--------------------"+ fileid +"---------------------");
				int count = rs.getInt("count");
				Match match = new Match(conn,fileid, count, 50.0, 20.0, 120.0, 5.0);
				if (!match.MapMatch(wh_roads))
				{
					String str = "这条轨迹匹配出现问题：" + fileid;
					logger.error(str);
					bw.write(str);
					bw.flush();
				}
		        System.out.println("匹配结束：------------------"+ fileid +"-----------------------");
		        logger.debug("匹配结束：------------------"+ fileid +"-----------------------");
		        logger.error("匹配结束：------------------"+ fileid +"-----------------------");
			}
			rs.close();
			ps_tra.close();
			conn.close();
		 } 
         catch (Exception e)
         {
			e.printStackTrace();
		 } 
    }
}
