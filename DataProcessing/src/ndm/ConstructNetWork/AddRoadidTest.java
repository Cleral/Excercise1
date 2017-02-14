package ndm.ConstructNetWork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ndm.DataConnection;

public class AddRoadidTest {
	
	public static void main(String[] args){
		Connection conn=null;
		
		try {
			conn=DataConnection.GetConnection();
			conn.setAutoCommit(false);
			String splitwayPath="D:\\mapmatching\\data\\Wuhan\\osm_txt\\spw3.txt";
			List<String[]> splitways=readTxtFile(splitwayPath);
			addRoadid(conn, splitways);
			conn.commit();
			conn.close();
			System.out.println("修改完成！");
		} catch (SQLException e) {
			// TODO: handle exception
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				System.out.println("回滚失败！");
				e1.printStackTrace();
			}
			System.out.println("修改失败！");
			e.printStackTrace();
		}
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
	
	public static void addRoadid(Connection conn,List<String[]> list){
		for(int i=0;i<list.size();i++){
			String[] temp=list.get(i);
			int linkid=Integer.valueOf(temp[0]);
			int roadid=Integer.valueOf(temp[1]);
			String addRoadId="update links set road_id="+roadid+" where link_id="+linkid;
			executeUpdate(conn, addRoadId);
		}
	}
	
	public static int executeUpdate(Connection conn,String sql){
		int result=-1;
		PreparedStatement pstm=null;
		try {
			pstm=conn.prepareStatement(sql);
			result=pstm.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				pstm.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
