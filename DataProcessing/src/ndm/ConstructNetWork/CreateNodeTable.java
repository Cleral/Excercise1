package ndm.ConstructNetWork;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

public class CreateNodeTable {
	 private Statement stmt = null;
     public CreateNodeTable (Statement stmt)
     {
    	 super () ;
         this.stmt = stmt;
	 }
     
     //创建结点表
     public boolean CreateNodesTable()
     {
         String sql = "CREATE TABLE nodes2(node_id NUMBER,fid NUMBER,node_geometry SDO_GEOMETRY,active char(1) default 'Y'," +
             "CONSTRAINT wh_nodes2_pk PRIMARY KEY (node_id))";
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
     
     /**
     * @param conn 数据库连接Connection
     * @param nodes 解析后得到的结点数据
     * @return 如果填充成功，返回true，反之返回false
     */
    public boolean fillNodesTable(Connection conn, List<String[]> nodes)
     {
         try
         {
             String sql = "insert into nodes2(node_id,fid,node_geometry) values(?,?,?)"; 
             PreparedStatement ps = conn.prepareStatement(sql);
             int count = 0;
             for (int i = 0; i < nodes.size(); i++)
             {
				String[] node = nodes.get(i);
				 double  node_X = Double.valueOf ( node[1] );
                 double  node_Y = Double.valueOf ( node[2] );
                 double[] coord = {node_X,node_Y};                   
                 JGeometry geo = JGeometry.createPoint ( coord , 2 , 8307 );
                 Struct obj = JGeometry.storeJS ( conn , geo );
                 ps.setInt(1, count);
                 count++;
                 ps.setLong( 2 , Long.valueOf (node[0]));
                 ps.setObject ( 3 , obj );
                 ps.addBatch();
                 if (count%10000 == 0)
                 {
                	 ps.executeBatch();
				 }
			 }
             ps.executeBatch();
             ps.close();
             return true;                    
         }
         catch ( Exception e )
         {
             e.printStackTrace () ;
             return false;
         }
     }
     

    /**
     * @return 对Node表创建空间索引
     */
    public boolean createNodeTableSpatialIndex()
    {
        String meta = "INSERT INTO user_sdo_geom_metadata(table_name, column_name, srid, diminfo) VALUES "
        		+ "('nodes2','node_geometry',8307,SDO_DIM_ARRAY( SDO_DIM_ELEMENT('LONGITUDE',113,115,0.5),"
        		+ "SDO_DIM_ELEMENT('LATITUDE',30,31,0.5)))";
        try
        {
            int result = stmt.executeUpdate ( meta ) ;
            if(result != -1)
            {
            	String _index = "CREATE INDEX node2_index ON nodes2(node_geometry) INDEXTYPE IS MDSYS.SPATIAL_INDEX";
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
        	e.printStackTrace();
            return false;
        }
    }
}
