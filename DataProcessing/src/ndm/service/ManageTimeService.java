package ndm.service;

import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ndm.dao.LinkTrasDao;

@Service
public class ManageTimeService {
	@Autowired
	private LinkTrasDao linkTrasDao;
	
	
	
	public boolean getResultSet(){
		ResultSet rs=linkTrasDao.getLinks();
		if(rs==null){
			return false;
		}
		return true;
	}
}
