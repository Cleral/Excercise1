package ndm.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

@Repository
public class LinkTrasDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public ResultSet getLinks(){
		String sql="select * from wh_modifiedtra where fileid<100 order by fileid,node_id";
		ResultSet rs=jdbcTemplate.query(sql,new ResultSetExtractor(){

			@Override
			public Object extractData(ResultSet rs_temp) throws SQLException,
					DataAccessException {
				// TODO Auto-generated method stub
				return rs_temp;
			}
			
		});
		return rs;
	}
}
