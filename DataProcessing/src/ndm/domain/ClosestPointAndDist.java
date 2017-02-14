package ndm.domain;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;


public class ClosestPointAndDist {
	CallableStatement cs = null;
	Connection conn = null;
	Struct struct_point = null;
	Struct struct_line = null;
	public ClosestPointAndDist(Connection conn,Struct struct_point,Struct struct_line)
	{
		this.conn = conn;
		this.struct_point = struct_point;
		this.struct_line = struct_line;
	}
	
	public ClosestPoint CPTAndDist()
	{		
		try
		{
			cs = conn.prepareCall ( "{ call test_closestpoint(?,?,?,?,?) }" );
            cs.setObject ( 1 , struct_point );         
            cs.setObject ( 2 , struct_line );           
            cs.registerOutParameter ( 3 , OracleTypes.NUMBER);
            cs.registerOutParameter ( 4 , OracleTypes.STRUCT , "MDSYS.SDO_GEOMETRY");         
            cs.registerOutParameter ( 5 , OracleTypes.STRUCT , "MDSYS.SDO_GEOMETRY");        
            cs.execute ();
            Struct cpt = (Struct)cs.getObject ( 5 );
            if (cpt!=null)
            {
            	JGeometry geo_pt = JGeometry.loadJS ( cpt );
            	double dist = cs.getDouble ( 3 );
            	cs.clearBatch();
                cs.close();
                return new ClosestPoint(geo_pt, dist,cpt);
			}
            else 
            {
            	JGeometry geo_pt = JGeometry.loadJS ( struct_point );
            	cs.clearBatch();
            	cs.close();
                return new ClosestPoint(geo_pt, 0,struct_point);
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
}
