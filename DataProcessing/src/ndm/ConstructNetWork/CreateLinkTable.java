package ndm.ConstructNetWork;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import ndm.domain.LinkData;
import ndm.domain.LinkText;
import ndm.domain.SplitWay;
import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;


public class CreateLinkTable {
	private Statement stmt = null;
	public CreateLinkTable(Statement stmt)
	{
		 super () ;
         this.stmt = stmt;
	}
	
	 //创建边表
    public boolean CreateLinksTable()
    {
        String sql = "CREATE TABLE links2(link_id NUMBER,start_node_id NUMBER NOT NULL,end_node_id NUMBER NOT NULL," +
                "street_geom SDO_GEOMETRY,street_name varchar2(128),type varchar2(128),street_length NUMBER,bidirected CHAR(1)," +
                "active char(1) default 'Y',link_level number default 1, CONSTRAINT wh_streets2_pk PRIMARY KEY (link_id))";
            try
            {
                int result = stmt.executeUpdate ( sql ) ;
                if(result != -1)
                {
                    return true;
                }
                else
                {
                    return false;
                }         
            }
            catch ( SQLException e )
            {
            	e.printStackTrace () ;
                return false;
            }
    }
    
   public boolean filllinksTable(Connection conn, List<String[]> splitWays, List<String[]> linkTexts)
    {
        try
        {
        	String sql = "insert into links2(link_id,start_node_id,end_node_id,street_geom,street_name,type,street_length," +
                    "bidirected) values(?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            Long count = (long) 0;
            PreparedStatement ps1 = null;
            
            for (int i = 0; i < splitWays.size(); i++) {
				LinkData linkData=new LinkData();
				
				String[] splitWayTxt=splitWays.get(i);
				String[] linkTxt=linkTexts.get(i);
				List<String> nodeIds=new ArrayList<String>();
				for(int j=2;j<Integer.valueOf(splitWayTxt[1])+2;j++){
					nodeIds.add(splitWayTxt[j]);
				}
				String type=null;
				for(int j=Integer.valueOf(splitWayTxt[1])+2;j<splitWayTxt.length;j++){
					if(splitWayTxt[j].trim().equals("highway")){
						type=splitWayTxt[j+1];
						break;
					}
				}
				
				
				//SplitWay splitWay=splitWays.get(i);
				//LinkText linkText=linkTexts.get(i);
				List<Struct> nodes=new ArrayList<Struct>();
				linkData.setType(type);
				linkData.setLinkName(linkTxt[4]);
				linkData.setLinkid(i);
				
				//linkData.setType(splitWay.getType());
				String bidirected=null;
				if(linkTxt[5] != null && linkTxt[5].equals("yes")){
					linkData.setBidirected('N');
					bidirected="N";
				}
				else {
					linkData.setBidirected('Y');
					bidirected="Y";
				}
				List<Double> nodeOrds = new ArrayList<Double>();
				double[] ords=new double[nodeIds.size()*2];
				for(int j = 6; j < linkTxt.length; j += 2){
					try {
						ords[j-6]=Double.valueOf(linkTxt[j+1]);
						ords[j-5]=Double.valueOf(linkTxt[j]);
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(linkTxt[0]+","+linkTxt.toString());
					}
					
				}
				
				double length=0;
				
				JGeometry geo_link = JGeometry.createLinearLineString  ( ords , 2 , 8307 );
                Struct obj_link = JGeometry.storeJS ( conn , geo_link );
                
				//double[] ords=linkText.getOrds().toArray();
				for (int j = 0; j < nodeIds.size(); j++) {
					String nodeQuery="select node_geometry,node_id from nodes2 where fid=? ";
					ps1=conn.prepareStatement(nodeQuery);
					//ps1.setInt(1, Integer.valueOf(nodeIds.get(j)));
					ps1.setString(1, nodeIds.get(j));
					ResultSet rs=ps1.executeQuery();
					while(rs.next()){
						int nodeId=rs.getInt("node_id");
						Struct nodeStruct=(Struct)rs.getObject("node_geometry");
						nodes.add(nodeStruct);
						if(j==0){
							linkData.setStartNodeId(nodeId);
						}
						if(j==nodeIds.size()-1){
							linkData.setEndNodeId(nodeId);
						}
						
					}
					ps1.close();
					rs.close();
				}
				
				if (nodes.size()!=0)
				{
					for (int k = 0; k < nodes.size()-1; k++)
					{
						length = length + getDistance(nodes.get(k),nodes.get(k+1), conn);
					}
				}
				
				ps.setLong( 1 , linkData.getLinkid());
                ps.setLong ( 2 , linkData.getStartNodeId());
                ps.setLong ( 3 , linkData.getEndNodeId() );
                ps.setObject ( 4 , obj_link );
                ps.setString ( 5 , linkData.getLinkName() );
                ps.setString ( 6 , linkData.getType() );
                ps.setDouble ( 7 , length );
                ps.setString( 8 , bidirected );
                ps.addBatch();
                if (linkData.getLinkid()%10000 == 0) 
                {
               	 ps.executeBatch();
				 }
                
			}
            ps.executeBatch();
            ps.close();
            
            
            
            /*
            for (int i = 0; i < links.size(); i++)
            {
				LinkData linkData = links.get(i);
				String oneway = "Y";
				if (linkData.oneway != null && linkData.oneway.equals("yes"))
				{
					oneway = "N";
				}
				String name = linkData.name;
				String type = linkData.type;
				List<Long> nodeids = new ArrayList<Long>();
				nodeids.add(linkData.geo.get(0));
				for (int j = 1; j < linkData.geo.size(); j++)
				{
					long nodeid = linkData.geo.get(j);
					nodeids.add(nodeid);
					double[] coord = new double[nodeids.size()*2];
					int size = 0;
					List<Struct> geos = new ArrayList<Struct>();
					boolean flag = false;
					if (repeateid.contains(nodeid))
					{
						flag = true;
					}
					List<Integer> fid_nodes = new ArrayList<Integer>(); 
					if (flag || j == linkData.geo.size()-1)
					{
						for (int k = 0; k < nodeids.size(); k++) 
						{
							 String sql_geo = "select node_geometry,node_id from nodes where fid = " + nodeids.get(k);
						     ps1 = conn.prepareStatement(sql_geo);
						     ResultSet rs = ps1.executeQuery();
						     JGeometry geom_node = null;
						     Struct struct_node = null;
						     while(rs.next())
						     {
						    	 struct_node = (Struct)rs.getObject ("node_geometry");
						         geom_node = JGeometry.loadJS ( struct_node );
						         fid_nodes.add(rs.getInt("node_id"));
						     }
						    ps1.close();
						     if (geom_node != null)
						     {
						    	geos.add(struct_node);
								double[] xy = geom_node.getPoint();
								coord[size] = xy[0];
								size ++;
								coord[size] = xy[1];
								size ++;
							 }
						     rs.close();
						}
						JGeometry geo_link = JGeometry.createLinearLineString  ( coord , 2 , 8307 );
		                Struct obj_link = JGeometry.storeJS ( conn , geo_link );
						
						double cost = 0;
						if (geos.size()!=0)
						{
							for (int k = 0; k < geos.size()-1; k++)
							{
								cost = cost + getDistance(geos.get(k),geos.get(k+1), conn);
							}
						}
						
						 ps.setLong( 1 , count);
		                 ps.setLong ( 2 , fid_nodes.get(0) );
		                 ps.setLong ( 3 , fid_nodes.get(fid_nodes.size()-1) );
		                 ps.setObject ( 4 , obj_link );
		                 ps.setString ( 5 , name );
		                 ps.setString ( 6 , type );
		                 ps.setDouble ( 7 , cost );
		                 ps.setString( 8 , oneway );
		                 ps.addBatch();
		                 if (count%10000 == 0) 
		                 {
		                	 ps.executeBatch();
						 }
		                 count++;
		                 
		                 if (j != linkData.geo.size()-1)
		                 {
		                	 nodeids.clear();
			                 nodeids.add(nodeid);
						 }
					}
				}
			 }
            ps.executeBatch();
            //ps1.close();
            ps.close();
            */
            return true;                    
        }
        catch ( Exception e )
        {
            try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	e.printStackTrace () ;
            return false;
        }
    }
   
   private double getDistance(Struct p1,Struct p2,Connection conn)
   {
	   double dis = 0;
		try
		{
			CallableStatement cs = conn.prepareCall( "{ call getdistance(?,?,?) }" );
			cs.setObject(1, p1);
			cs.setObject(2, p2);
			cs.registerOutParameter ( 3, OracleTypes.NUMBER);
			cs.execute ();
			dis = cs.getDouble(3);
			cs.clearBatch();
			cs.close();
			return dis;
		} 
		catch (SQLException e) 
		{
			System.out.println("求距离出现异常："+e.getMessage());
			return dis;
		}
   }
   
   /**
    * @return 对Link表创建空间索引
    */
   public boolean createLinkTableSpatialIndex()
   {
       String meta = "INSERT INTO user_sdo_geom_metadata(table_name, column_name, srid, diminfo) VALUES "
       		+ "('links2','street_geom',8307,SDO_DIM_ARRAY( SDO_DIM_ELEMENT('LONGITUDE',113,115,0.5),"
       		+ "SDO_DIM_ELEMENT('LATITUDE',30,31,0.5)))";
       try
       {
           int result = stmt.executeUpdate ( meta ) ;
           if(result != -1)
           {
        	   String _index = "CREATE INDEX link2_index ON links2(street_geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX";
              	result = stmt.executeUpdate ( _index ) ;
              	if (result != -1)
              	{
              		return true;
   				}
              	else
              	{
              		return false;
              	}
           }
           else
           {
               return false;
           }         
       }
       catch ( SQLException e )
       {
    	   e.printStackTrace () ;
           return false;
       }
   }

}
