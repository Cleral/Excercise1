package ndm.RelatePOI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.network.Network;
import oracle.spatial.network.NetworkDataException;
import ndm.*;

public class CreateFeatureTable {
	private Statement stmt = null;
	private static Connection conn;
	private static String netName = null;
	static Network netWork;

	public CreateFeatureTable(Statement stm, Connection _conn, String _netName) {
		super();
		this.stmt = stm;
		this.conn = _conn;
		this.netName = _netName;

		/*
		 * try { //NetworkMetadata meta=NetworkManager.readNetworkMetadata(conn,
		 * netName); //netWork=meta.getNetwork();
		 * //netWork=NetworkManager.readNetwork(conn, netName,true);
		 * netWork=NetworkFactory.createSDONetwork("wh_roads", 1, true, 8307, 3,
		 * "nodes", "node_geometry", null, "links", "street_geom",
		 * "street_length", null, null, null); Link
		 * nearestLink=netWork.getLink(2); System.out.println("111"); }
		 * 
		 * catch (NetworkDataException e) { // TODO: handle exception
		 * e.printStackTrace(); }
		 */

		/*
		 * try {
		 * 
		 * NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(conn,
		 * netName, netName, null); NetworkMetadata
		 * meta=networkIO.getNetworkMetadata(); netWork=meta.getNetwork();
		 * 
		 * 
		 * 
		 * } catch(LODNetworkException e) { e.printStackTrace(); }
		 */
	}

	public static void main(String args[]) {
		/*
		 * try { Connection _conn=DataConnection.GetConnection(); Statement
		 * stm=_conn.createStatement(); String _netName="wh_roads";
		 * 
		 * NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(_conn,
		 * _netName, _netName, null); NetworkMetadata
		 * meta=networkIO.getNetworkMetadata(); //UserDataMetadata
		 * umeta=meta.getUserDataMetadata(); FeatureMetadata
		 * fmeta=meta.getFeatureMetadata(); String[]
		 * namesStrings=fmeta.getFeatureLayerNames(); FeatureLayerMetadata
		 * fLayerMeta=fmeta.getFeatureLayerMetadata("INTERESTS");
		 * 
		 * 
		 * String fName=fLayerMeta.getFeatureTableName(); FeatureLayerImpl
		 * fLayerImpl=new FeatureLayerImpl(); FeatureDataImpl fDataImpl=new
		 * FeatureDataImpl(); fDataImpl.setFeatureLayer(1, fLayerImpl);
		 * FeatureLayer fLayer=fDataImpl.getFeatureLayer(1); Feature
		 * f=fLayerImpl.getFeature(0); System.out.println(fName);
		 * 
		 * } catch (LODNetworkException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (SQLException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		/*
		 * try { Connection _conn=DataConnection.GetConnection(); Statement
		 * stm=_conn.createStatement(); String _netName="wh_roads";
		 * 
		 * NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(_conn,
		 * _netName, _netName, null); NetworkMetadata
		 * meta=networkIO.getNetworkMetadata(); //UserDataMetadata
		 * umeta=meta.getUserDataMetadata(); FeatureMetadata
		 * fmeta=meta.getFeatureMetadata(); String[]
		 * namesStrings=fmeta.getFeatureLayerNames(); FeatureLayerMetadata
		 * fLayerMeta=fmeta.getFeatureLayerMetadata("INTERESTS"); String
		 * fName=fLayerMeta.getFeatureTableName(); FeatureLayerImpl
		 * fLayerImpl=new FeatureLayerImpl(); FeatureDataImpl fDataImpl=new
		 * FeatureDataImpl(); fDataImpl.setFeatureLayer(1, fLayerImpl);
		 * FeatureLayer fLayer=fDataImpl.getFeatureLayer(1); Feature
		 * f=fLayerImpl.getFeature(0); System.out.println(fName);
		 * 
		 * } catch (LODNetworkException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (SQLException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		try {

			Connection _conn = DataConnection.GetConnection();
			Statement stm = _conn.createStatement();
			String _netName = "wh_roads";

			// 创建景点poi表格
			CreateFeatureTable createInterests = new CreateFeatureTable(stm,
					_conn, _netName);

			if (createInterests.CreateFeatureTable(_conn)) {

				List<POI> restaurants = new ArrayList<POI>();
				restaurants = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\餐饮.txt");
				createInterests.fillTable("restaurants",
						"restaurants_relation", restaurants);
				System.out.println("插入完成！");

				List<POI> shops = new ArrayList<POI>();
				shops = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\购物.txt");
				createInterests.fillTable("shops", "shops_relation", shops);
				System.out.println("插入完成！");

				List<POI> traffic = new ArrayList<POI>();
				traffic = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\交通.txt");
				createInterests.fillTable("traffic", "traffic_relation",
						traffic);
				System.out.println("插入完成！");

				List<POI> interests = new ArrayList<POI>();
				interests = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\旅游.txt");
				createInterests.fillTable("interests", "interests_relation",
						interests);
				System.out.println("插入完成！");

				List<POI> schools = new ArrayList<POI>();
				schools = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\学校.txt");
				createInterests.fillTable("schools", "schools_relation",
						schools);
				System.out.println("插入完成！");

				List<POI> hospitals = new ArrayList<POI>();
				hospitals = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\医疗.txt");
				createInterests.fillTable("hospitals", "hospitals_relation",
						hospitals);
				System.out.println("插入完成！");

				List<POI> banks = new ArrayList<POI>();
				banks = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\银行.txt");
				createInterests.fillTable("banks", "banks_relation", banks);
				System.out.println("插入完成！");

				List<POI> offices = new ArrayList<POI>();
				offices = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\政府.txt");
				createInterests.fillTable("offices", "offices_relation",
						offices);
				System.out.println("插入完成！");

				List<POI> hotels = new ArrayList<POI>();
				hotels = createInterests
						.getInfoList("D:\\mapmatching\\data\\Wuhan\\住宿.txt");
				createInterests.fillTable("hotels", "hotels_relation", hotels);
				System.out.println("插入完成！");

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sdo_net.add_feature_layer("wh_roads","interests",2,"interests","interests_relation",null);

	}

	public boolean CreateFeatureTable(Connection conn) {
		String sql_interests = "create table interests(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT interests_pk primary key(feature_id)) ";
		String sql_interests_relation = "create table interests_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT interests_relation_pk primary key(feature_id,sequence)) ";
		String sql_shops = "create table shops(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT shops_pk primary key(feature_id)) ";
		String sql_shops_relation = "create table shops_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT shops_relation_pk primary key(feature_id,sequence)) ";
		String sql_traffic = "create table traffic(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT traffic_pk primary key(feature_id)) ";
		String sql_traffic_relation = "create table traffic_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT traffic_relation_pk primary key(feature_id,sequence)) ";
		String sql_schools = "create table schools(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT schools_pk primary key(feature_id)) ";
		String sql_schools_relation = "create table schools_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT schools_relation_pk primary key(feature_id,sequence)) ";
		String sql_hospitals = "create table hospitals(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT hospitals_pk primary key(feature_id)) ";
		String sql_hospitals_relation = "create table hospitals_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT hospitals_relation_pk primary key(feature_id,sequence)) ";

		String sql_banks = "create table banks(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT banks_pk primary key(feature_id)) ";
		String sql_banks_relation = "create table banks_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT banks_relation_pk primary key(feature_id,sequence)) ";

		String sql_offices = "create table offices(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT offices_pk primary key(feature_id)) ";
		String sql_offices_relation = "create table offices_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT offices_relation_pk primary key(feature_id,sequence)) ";

		String sql_hotels = "create table hotels(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT hotels_pk primary key(feature_id)) ";
		String sql_hotels_relation = "create table hotels_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT hotels_relation_pk primary key(feature_id,sequence)) ";
		String sql_restaurants = "create table restaurants(feature_id number,name varchar2(48 char),address varchar2(48 char),"
				+ "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT restaurants_pk primary key(feature_id)) ";
		String sql_restaurants_relation = "create table restaurants_relation(feature_id number,feat_elem_type number,net_elem_id number,"
				+ "start_percentage number,end_percentage number,sequence number,CONSTRAINT restaurants_relation_pk primary key(feature_id,sequence)) ";

		int result_restaurants = executeUpdate(conn, sql_restaurants);
		int result_restaurants_relation = executeUpdate(conn,
				sql_restaurants_relation);
		int result_shops = executeUpdate(conn, sql_shops);
		int result_shops_relation = executeUpdate(conn, sql_shops_relation);
		int result_traffic = executeUpdate(conn, sql_traffic);
		int result_traffic_relation = executeUpdate(conn, sql_traffic_relation);
		int result_interests = executeUpdate(conn, sql_interests);
		int result_interests_relation = executeUpdate(conn,
				sql_interests_relation);
		int result_schools = executeUpdate(conn, sql_schools);
		int result_schools_relation = executeUpdate(conn, sql_schools_relation);

		int result_hospitals = executeUpdate(conn, sql_hospitals);
		int result_hospitals_relation = executeUpdate(conn,
				sql_hospitals_relation);
		int result_banks = executeUpdate(conn, sql_banks);
		int result_banks_relation = executeUpdate(conn, sql_banks_relation);
		int result_offices = executeUpdate(conn, sql_offices);
		int result_offices_relation = executeUpdate(conn, sql_offices_relation);
		int result_hotels = executeUpdate(conn, sql_hotels);
		int result_hotels_relation = executeUpdate(conn, sql_hotels_relation);

		if (result_hotels != -1) {
			return true;
		}
		return false;
		/*
		 * String sql_interests=
		 * "create table interests(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT interests_pk primary key(id)) "
		 * ; String sql_interests_relation=
		 * "create table interests_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT interests_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_interests); int
		 * result2=stmt.executeUpdate(sql_interests_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建interests表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * 
		 * 
		 * String sql_shops=
		 * "create table shops(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT shops_pk primary key(id)) "
		 * ; String sql_shops_relation=
		 * "create table shops_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT shops_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_shops); int
		 * result2=stmt.executeUpdate(sql_shops_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建shops表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * 
		 * String sql_traffic=
		 * "create table traffic(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT traffic_pk primary key(id)) "
		 * ; String sql_traffic_relation=
		 * "create table traffic_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT traffic_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_traffic); int
		 * result2=stmt.executeUpdate(sql_traffic_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建traffic表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * 
		 * String sql_schools=
		 * "create table schools(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT schools_pk primary key(id)) "
		 * ; String sql_schools_relation=
		 * "create table schools_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT schools_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_schools); int
		 * result2=stmt.executeUpdate(sql_schools_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建schools表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * 
		 * String sql_hospitals=
		 * "create table hospitals(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT hospitals_pk primary key(id)) "
		 * ; String sql_hospitals_relation=
		 * "create table hospitals_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT hospitals_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_hospitals); int
		 * result2=stmt.executeUpdate(sql_hospitals_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建hospitals表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * 
		 * String sql_banks=
		 * "create table banks(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT banks_pk primary key(id)) "
		 * ; String sql_banks_relation=
		 * "create table banks_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT banks_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_banks); int
		 * result2=stmt.executeUpdate(sql_banks_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建banks表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * String sql_offices=
		 * "create table offices(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT offices_pk primary key(id)) "
		 * ; String sql_offices_relation=
		 * "create table offices_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT offices_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_offices); int
		 * result2=stmt.executeUpdate(sql_offices_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建offices表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * String sql_hotels=
		 * "create table hotels(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT hotels_pk primary key(id)) "
		 * ; String sql_hotels_relation=
		 * "create table hotels_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT hotels_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_hotels); int
		 * result2=stmt.executeUpdate(sql_hotels_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建hotels表格失败！"); return false; }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 * 
		 * String sql_restaurants=
		 * "create table restaurants(feature_id number,name varchar2(48 char),address varchar(48 char),"
		 * +
		 * "geo_org sdo_geometry,geo_prj sdo_geometry,phone varchar2(15),CONSTRAINT restaurants_pk primary key(id)) "
		 * ; String sql_restaurants_relation=
		 * "create table restaurants_relation(feature_id number,feat_elem_type number,net_elem_id number,"
		 * +
		 * "start_percentage number,end_percentage number,sequence number,CONSTRAINT restaurants_relation_pk primary key(id,sequence)) "
		 * ; try { int result=stmt.executeUpdate(sql_restaurants); int
		 * result2=stmt.executeUpdate(sql_restaurants_relation);
		 * if(result!=-1&&result2!=-1) { stmt.close(); return true; } else {
		 * stmt.close(); System.out.println("创建restaurants表格失败！"); return false;
		 * }
		 * 
		 * } catch(SQLException e) { e.printStackTrace(); return false; }
		 */

	}

	private List<POI> getInfoList(String filepath) {
		List<POI> poiInfo = new ArrayList<POI>();
		try {
			List<POIString> pois = readTxtFile(filepath);

			int id = 0;
			for (int i = 0; i < pois.size(); i++) {

				POIString poi = pois.get(i);
				String name = poi.name;
				String address = poi.address;
				String phone = poi.phone;
				int feature_elem_type = 2;
				int sequence = 0;
				double x = Double.valueOf(poi.longitude);
				double y = Double.valueOf(poi.latitude);
				double[] coord = { x, y };
				JGeometry geo = JGeometry.createPoint(coord, 2, 8307);
				Struct orgPt = JGeometry.storeJS(conn, geo);

				double[] or = new double[3];

				// 获取最近的link
				int nearestLinkId = getNearestLink(x, y);

				if(nearestLinkId == -1){
					POI a_poi = new POI(id, name, address, orgPt, orgPt,
							phone, feature_elem_type, nearestLinkId, 0.0,
							sequence);
					id++;
					poiInfo.add(a_poi);
					continue;
				}
				// 获取点在最近的link上的投影点
				Struct prj_struct = getPrjPoint(x, y, nearestLinkId);
				JGeometry j_pt = JGeometry.loadJS(prj_struct);
				or = j_pt.getPoint();

				// Link nearestLink=netWork.getLink(nearestLinkId);
				// double[] percentages=nearestLink.computeDistanceRatio(or[0],
				// or[1]);

				double percentage = getPercentage(netName, nearestLinkId,
						prj_struct);
				if (percentage-0 < 0.0001 || 1-percentage < 0.0001) {
					feature_elem_type = 1;
				}

				POI a_poi = new POI(id, name, address, orgPt, prj_struct,
						phone, feature_elem_type, nearestLinkId, percentage,
						sequence);
				id++;
				poiInfo.add(a_poi);
			}
			return poiInfo;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return poiInfo;
		} catch (NetworkDataException e) {
			// TODO: handle exception
			e.printStackTrace();
			return poiInfo;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return poiInfo;
		}
	}

	private void fillTable(String poiTable, String poiRelation, List<POI> pois) {
		String poi_insert = "insert into "
				+ poiTable
				+ "(feature_id,name,address,geo_org,geo_prj,phone) values(?,?,?,?,?,?)";
		String relation_insert = "insert into "
				+ poiRelation
				+ "(feature_id,feat_elem_type,net_elem_id,start_percentage,end_percentage,sequence) values(?,?,?,?,?,?)";
		try {
			PreparedStatement ps = conn.prepareStatement(poi_insert);

			for (int i = 0; i < pois.size(); i++) {
				ps.setInt(1, pois.get(i).id);
				ps.setString(2, pois.get(i).name);
				ps.setString(3, pois.get(i).address);
				ps.setObject(4, pois.get(i).geo_org);
				/*
				if(pois.get(i).geo_prj==null){
					ps.setObject(5, null);
				}
				else {
					ps.setObject(5, pois.get(i).geo_prj);
				}
				*/
				ps.setObject(5, pois.get(i).geo_prj);
				ps.setString(6, pois.get(i).phone);
				ps.addBatch();
				if (i + 1 % 500 == 0) {
					ps.executeBatch();
				}
				ps.executeBatch();
			}

			PreparedStatement ps2 = conn.prepareStatement(relation_insert);

			for (int i = 0; i < pois.size(); i++) {
				ps2.setInt(1, pois.get(i).id);
				ps2.setInt(2, pois.get(i).feat_elem_type);
				ps2.setInt(3, pois.get(i).feat_elem_id);
				ps2.setObject(4, pois.get(i).start_percentage);
				ps2.setObject(5, pois.get(i).start_percentage);
				ps2.setObject(6, pois.get(i).sequence);
				ps2.addBatch();
				if (i + 1 % 500 == 0) {
					ps2.executeBatch();
				}
				ps2.executeBatch();

			}

			System.out.println("插入完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 读取filePath路径下的文本
	public List<POIString> readTxtFile(String filePath) {
		List<POIString> txt = new ArrayList<POIString>();
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					String[] txtArr = lineTxt.split(",");
					POIString poiString;
					if (txtArr.length == 5) {
						poiString = new POIString(txtArr[0], txtArr[1],
								Double.valueOf(txtArr[2]),
								Double.valueOf(txtArr[3]), txtArr[4]);
					} else {
						poiString = new POIString(txtArr[0], txtArr[1],
								Double.valueOf(txtArr[2]),
								Double.valueOf(txtArr[3]), null);
					}
					txt.add(poiString);
					// System.out.println(lineTxt);
				}
				read.close();

			} else {
				System.out.println("找不到指定的文件");
			}
			return txt;
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
			return txt;
		}

	}

	/**
	 * @param x
	 *            输入坐标点的X坐标
	 * @param y
	 *            输入坐标点的Y坐标
	 * @return 输出最近Link的ID，若有异常，输出-1
	 */
	private int getNearestLink(double x, double y) {
		String sql = "SELECT l1.link_id FROM links l1 WHERE SDO_NN(l1.street_geom, MDSYS.SDO_GEOMETRY(2001,8307,MDSYS.SDO_POINT_TYPE(?,?,NULL),NULL,NULL),'sdo_num_res=1 distance=50 unit=meter')='TRUE'";
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setDouble(1, x);
			psmt.setDouble(2, y);
			rs = psmt.executeQuery();
			int linkid = -1;
			/*
			 * String linkName="temporary"; oracle.spatial.network.Node
			 * startNode; oracle.spatial.network.Node endNode; double cost=0.0;
			 * JGeometry geometry;
			 */
			while (rs.next()) {
				linkid = rs.getInt("link_id");

			}
			
			return linkid;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("获取最近的Link出现问题！");
			return -1;
		}
		finally{
			try {
				rs.close();
				psmt.close();
			} catch (SQLException e2) {
				// TODO: handle exception
				System.out.println("关闭查询错误！");
			}
		}
	}

	/**
	 * @param networkname
	 *            网络数据集名称
	 * @param link_id
	 *            link编号
	 * @param pt
	 *            link上的一点
	 * @return 从起点到pt所占的百分比
	 */
	private double getPercentage(String networkname, int link_id, Struct pt) {
		double percentage = 0;
		try {
			CallableStatement cs = conn
					.prepareCall("{ call getpercentage(?,?,?,?) }");
			cs.setString(1, networkname);
			cs.setObject(2, pt);
			cs.setInt(3, link_id);
			cs.registerOutParameter(4, OracleTypes.NUMBER);
			cs.execute();
			percentage = cs.getDouble(4);
			cs.clearBatch();
			cs.close();
			return percentage;
		} catch (SQLException e) {
			System.out.println("求百分比出现异常：" + "link_id为" + link_id
					+ e.getMessage());
			return percentage;
		}
	}

	public Struct getPrjPoint(double x, double y, long nearestLink) {

		Struct str_p = null;
		try {
			CallableStatement cs = conn.prepareCall("{call getprj_pt(?,?,?)}");
			double[] coord = { x, y };
			JGeometry geo = JGeometry.createPoint(coord, 2, 8307);
			Struct orgPt = JGeometry.storeJS(conn, geo);
			cs.setObject(1, orgPt);
			cs.setLong(2, nearestLink);

			// System.out.println(nearestLink);
			cs.registerOutParameter(3, OracleTypes.STRUCT, "MDSYS.SDO_GEOMETRY");

			cs.execute();

			str_p = (Struct) cs.getObject(3);
			// JGeometry j_pt=JGeometry.loadJS(str_p);
			// or=j_pt.getPoint();
			// System.out.println(or[0]);
			cs.close();
			return str_p;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("获取投影点出现问题！");
			return str_p;
		}
	}

	private int executeUpdate(Connection conn, String sql) {
		try {
			PreparedStatement stm = conn.prepareStatement(sql);
			int result = stm.executeUpdate();
			return result;
		} catch (SQLException e) {
			System.out.println("执行" + sql + "失败");
			e.printStackTrace();
			return -1;
		}

	}

	class POIString {
		public String name;
		public String address;
		public Double longitude;
		public Double latitude;
		public String phone;

		public POIString(String _name, String _address, Double _longitude,
				Double _latitude, String _phone) {
			this.name = _name;
			this.address = _address;
			this.longitude = _longitude;
			this.latitude = _latitude;
			this.phone = _phone;
		}
	}

	class POI {
		public int id;
		public String name;
		public String address;
		public Struct geo_org;
		public Struct geo_prj;
		public String phone;
		public int feat_elem_type;
		public int feat_elem_id;
		public double start_percentage;
		public long sequence;

		public POI(int _id, String _name, String _address, Struct _org,
				Struct _prj, String _phone, int _feat, int _linkid,
				double _percentage, long _sequence) {
			this.id = _id;
			this.name = _name;
			this.address = _address;
			this.geo_org = _org;
			this.geo_prj = _prj;
			this.phone = _phone;
			this.feat_elem_type = _feat;
			this.feat_elem_id = _linkid;
			this.start_percentage = _percentage;
			this.sequence = _sequence;
		}
	}
}
