package ndm.ConstructNetWork;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;






import java.util.ArrayList;
import java.util.List;

import ndm.DataConnection;
import ndm.domain.LinkText;

public class Execute {
	public static void main(String[] args){
		Connection conn=null;
		Statement stmt=null;
		
		//读取node，splitway，link文本的内容
		String nodePath="D:\\mapmatching\\data\\Wuhan\\osm_txt\\n2.txt";
		String linkPath="D:\\mapmatching\\data\\Wuhan\\osm_txt\\l2.txt";
		String splitwayPath="D:\\mapmatching\\data\\Wuhan\\osm_txt\\spw2.txt";
		
		List<String[]> nodes=readNodeFile(nodePath);
		List<String[]> links=readTxtFile(linkPath);
		List<String[]> splitways=readTxtFile(splitwayPath);
		
		try{
			conn=DataConnection.GetConnection();
			stmt=conn.createStatement();
			//CreateNodeTable cnt=new CreateNodeTable(stmt);
			CreateLinkTable clt = new CreateLinkTable(stmt);
			/*
			if(!checkExistTable(conn, "wh_nodes"))
			   {
				   if (cnt.CreateNodesTable())
				   {
					  if (cnt.fillNodesTable(conn, nodes))
					  {
						 if (cnt.createNodeTableSpatialIndex()) 
						 {
							System.out.println("创建Node表，填充表，创建索引成功！");
						 }
						 else 
						 {
							 System.out.println("创建Node表索引失败！");
						 }
					  }
					  else
					  {
						  System.out.println("填充Node表失败！");
					  }
				   }
				   else
				   {
					   System.out.println("创建Node表失败！");
				   }
			   }
			   */
			   if(!checkExistTable(conn, "wh_links"))
			   {
				   //if (clt.CreateLinksTable())
				   //{
					  if (clt.filllinksTable(conn, splitways, links))
					  {
						 if (clt.createLinkTableSpatialIndex()) 
						 {
							System.out.println("创建Link表，填充表，创建索引成功！");
						 }
						 else 
						 {
							 System.out.println("创建Link表索引失败！");
						 }
					  }
					  else
					  {
						  System.out.println("填充Link表失败！");
					  }
				  // }
				   //else
				   //{
					//   System.out.println("创建Link表失败！");
				   //}
			   }
			   
		}
		catch(SQLException e){
			e.printStackTrace();
			try{
				conn.rollback();
			}
			catch(SQLException e2){
				e2.printStackTrace();
			}
		}
		finally{
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 功能：Java读取node的txt文件的内容
	 * 步骤：1：先获得文件句柄
	 * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
	 * 3：读取到输入流后，需要读取生成字节流
	 * 4：一行一行的输出。readline()。
	 * 备注：需要考虑的是异常情况
	 * @param filePath
	 */
	public static List<String[]> readNodeFile(String filePath){
		List<String[]> list=new ArrayList<String[]>();
		try {
				String encoding="GBK";
		        File file=new File(filePath);
		        if(file.isFile() && file.exists()){ //判断文件是否存在
		        	InputStreamReader read = new InputStreamReader(
					new FileInputStream(file),encoding);//考虑到编码格式
		        	BufferedReader bufferedReader = new BufferedReader(read);
		        	String lineTxt = null;
		        	while((lineTxt = bufferedReader.readLine()) != null){
		        		//System.out.println(lineTxt);
		        		String[] strs=lineTxt.split(",");
		        		list.add(strs);
		        	}
		        	read.close();
		        }
		        else{
		        	System.out.println("找不到指定的文件");
		        }
		        
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
			
		}
		return list;
	}
	
	/**
	 * 功能：Java读取link或splitway的txt文件的内容
	 * 步骤：1：先获得文件句柄
	 * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
	 * 3：读取到输入流后，需要读取生成字节流
	 * 4：一行一行的输出。readline()。
	 * 备注：需要考虑的是异常情况
	 * @param filePath
	 */
	public static List<String[]> readTxtFile(String filePath){
		List<String[]> list=new ArrayList<String[]>();
		try {
				String encoding="utf-8";
		        File file=new File(filePath);
		        if(file.isFile() && file.exists()){ //判断文件是否存在
		        	InputStreamReader read = new InputStreamReader(
					new FileInputStream(file),encoding);//考虑到编码格式
		        	BufferedReader bufferedReader = new BufferedReader(read);
		        	String lineTxt = null;
		        	while((lineTxt = bufferedReader.readLine()) != null){
		        		//System.out.println(lineTxt);
		        		String[] strs=lineTxt.split(",");
		        		list.add(strs);
		        	}
		        	read.close();
		        }
		        else{
		        	System.out.println("找不到指定的文件");
		        }
		        
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
			
		}
		return list;
	}
	
	private static boolean checkExistTable(Connection conn,String tableName)
	 {
		 String sql="select * from ?";
		 PreparedStatement ps = null;
		 ResultSet rs=null;
		 try 
		 {
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableName);
			rs = ps.executeQuery();
			return true;
		 }
		 catch (SQLException e)
		 {
			return false;
		 }
		 finally
		 {
			try 
			{
				ps.close();
			} 
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		 }
	 }	 
}
