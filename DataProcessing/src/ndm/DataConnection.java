package ndm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 主要用于数据库的链接
 * @author xmL
 *
 */
public class DataConnection {
	public static final String DRIVER = "oracle.jdbc.driver.OracleDriver" ;
    public static final String URL = "jdbc:oracle:thin:@localhost:1521/pdbndm" ;
    private static final String DATANAME = "ndm_wh" ;
    private static final String DATAPASS = "123456" ;

    public DataConnection ()
    {
        super () ;
        try
        {
            Class.forName ( DRIVER ) ;
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace () ;
        }
    }

    public static Connection GetConnection ()
    {
        Connection conn = null ;
        try
        {
            conn = DriverManager.getConnection ( URL , DATANAME , DATAPASS ) ;
        }
        catch ( SQLException e )
        {
            e.printStackTrace () ;
        }
        return conn ;
    }
}
