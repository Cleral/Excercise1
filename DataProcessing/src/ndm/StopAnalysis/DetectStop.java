package ndm.StopAnalysis;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import oracle.jdbc.OracleTypes;
import oracle.net.aso.a;
import oracle.spatial.network.Link;
import oracle.spatial.network.Network;
import oracle.spatial.network.NetworkDataException;
import oracle.spatial.network.NetworkManager;
import oracle.spatial.network.Node;
import oracle.spatial.network.lod.Feature.FeatureType;
import ndm.*;

public class DetectStop {
	static Connection conn;
	static String netName = "wh_roads";
	static Network wh_roads = null;

	public static void main(String[] args) {
		try {
			conn = DataConnection.GetConnection();
			wh_roads = NetworkManager.readNetwork(conn, netName);
			DetectStop deal = new DetectStop();
			deal.getStop();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NetworkDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getStop() {
		try {
			// conn = DataConnection.GetConnection();
			String sqlString = "select * from link_tras where file_id>199 and file_id<500 order by 2,3";
			List<StopOnLink> list = new ArrayList<StopOnLink>();

			List<Trj_Stop> trj_stops = new ArrayList<Trj_Stop>();
			List<Trj_Move> trj_moves = new ArrayList<Trj_Move>();
			List<Stop> stops = new ArrayList<Stop>();
			List<Move> moves = new ArrayList<Move>();
			List<Pass> passes = new ArrayList<Pass>();
			List<Stay> stays = new ArrayList<Stay>();
			List<Close> closes = new ArrayList<Close>();

			AnalyseLink[] analyseLink = new AnalyseLink[2];
			// 存储可能停留的那些link
			List<AnalyseLink> analyseLinks = new ArrayList<AnalyseLink>();
			int sid = 0;
			int mid = 0;
			int segid = 0;

			ResultSet rs = executeQuery(conn, sqlString);
			while (rs.next()) {
				String enterTime = rs.getString("entertime");
				String leaveTime = rs.getString("leaveTime");
				int fileid = rs.getInt("file_id");
				int traid = rs.getInt("tra_id");
				int seqid = rs.getInt("seq_id");
				int linkid = rs.getInt("link_id");

				String sql_link = "select * from links where link_id=" + linkid;
				ResultSet rs_link = executeQuery(conn, sql_link);
				double link_length = 0.0;
				int startNode = 0;
				int endNode = 0;
				while (rs_link.next()) {
					link_length = rs_link.getDouble("street_length");
					startNode = rs_link.getInt("start_node_id");
					endNode = rs_link.getInt("end_node_id");

				}
				rs_link.close();

				// 判断是否是新的轨迹，然后对上一段轨迹的最后的点进行处理
				if (seqid == 0) {
					// 判断list是否为空，不为空，把最后一个list元素作为stop位置
					// if (list.size() > 0) {

					// }
					// list为空，判断trj_moves是否为空，为空则是选择集中第一个link，不为空的话按照trj_moves中最后一个元素为停留link处理
					// 如果analyseLink[0]不为空，说明这不是第一条轨迹，判断上一个轨迹的最后一个是否有停留
					// 有停留的话，去掉trj_moves中的最后一个元素
					// 没有停留的话，重新计算trj_moves中的结束点，passes，stops，closes，moves

					analyseLink[0] = new AnalyseLink(linkid, link_length,
							enterTime, leaveTime, fileid, traid, seqid,
							startNode, endNode, true);

					// 对于第一个link，先不计算pass等
					continue;
					/*
					 * if(trj_moves.size()>0){
					 * 
					 * } else{
					 * 
					 * }
					 */
				}

				if (seqid == 1) {
					// 对于第二条link，确定第一个link的点位置per的正负
					analyseLink[1] = new AnalyseLink(linkid, link_length,
							enterTime, leaveTime, fileid, traid, seqid,
							startNode, endNode, true);
					if (analyseLink[0].endNode != analyseLink[1].startNode
							&& analyseLink[0].endNode != analyseLink[1].endNode) {
						analyseLink[0].consist = false;
					}
					if (analyseLink[1].startNode != analyseLink[0].endNode
							&& analyseLink[1].startNode != analyseLink[0].startNode) {
						analyseLink[1].consist = false;
					}
					// computeFirstTwo(sid, mid, segid, analyseLink,
					// stops,trj_stops,
					// trj_moves, stays, closes, passes);

					// 当进行另一段轨迹的停留提取时，sid等不是重新开始
					if (trj_stops.size() == 0) {

						String sql_sid = "select * from (select max(sid) max from trj_stop) where max is not null";
						ResultSet rs_sid = executeQuery(conn, sql_sid);

						while (rs_sid.next()) {
							sid = rs_sid.getInt(1) + 1;

						}
						try {
							rs_sid.close();
						} catch (SQLException e) {
							// TODO: handle exception
						}
					}
					if (trj_moves.size() == 0) {

						String sql_mid = "select * from (select max(mid) max from trj_move) where max is not null";
						ResultSet rs_mid = executeQuery(conn, sql_mid);
						while (rs_mid.next()) {
							mid = rs_mid.getInt(1) + 1;
						}
						try {
							rs_mid.close();
						} catch (SQLException e) {
							// TODO: handle exception
						}
					}

					computeFirstTwo(sid, mid, segid, analyseLink, stops,
							trj_stops, trj_moves, stays, closes, passes);

					// 计算完后，sid等需要+1
					if (trj_stops.size() > 0) {
						sid = trj_stops.get(trj_stops.size() - 1).sid + 1;
					}

					if (trj_moves.size() > 0) {
						segid = trj_moves.get(trj_moves.size() - 1).seqid + 1;
						mid = trj_moves.get(trj_moves.size() - 1).mid;// 其他地方已有自增s
					}

					analyseLink[0] = analyseLink[1];
					analyseLink[1] = null;
					continue;
				}

				// 判断是不是结果集中最后一条记录，是的话就作为最后一个stop
				if (rs.isLast()) {
					/*
					 * String sql_p_o_l =
					 * "select o.speed,o.geom,m.geom,m.time,m.node_id from wh_modifiedtra m,day0529_tra_origin o,links l,link_tras2 lt "
					 * +
					 * "where m.roadid=l.link_id and m.roadid=lt.link_id and m.fileid=o.fileid and m.node_id=o.id and "
					 * +
					 * "m.time between lt.entertime and lt.leavetime and m.fileid="
					 * + fileid + " and lt.fid=" + fileid + " and lt.seq_id=" +
					 * seqid;
					 */
					String sql_p_o_l = "select o.speed,o.geo,m.new,m.time,m.tra_id from wh_modifiedtra m,wh_tras o,links l,link_tras lt "
							+ "where m.roadid=l.link_id and m.roadid=lt.link_id and m.file_id=o.fileid and m.tra_id=o.id"
							+ " and m.time between lt.entertime and lt.leavetime and m.file_id="
							+ fileid
							+ " and lt.file_id="
							+ fileid
							+ " and lt.seq_id=" + seqid;
					// System.out.println(enterTime+","+leaveTime);
					// analyseLink[1]是当前处理的link
					// analyseLinks.add(analyseLink[1]);

					ResultSet rs_p_o_l = executeQuery(conn, sql_p_o_l);
					while (rs_p_o_l.next()) {
						double speed = rs_p_o_l.getDouble("speed");
						Struct orgStruct = (Struct) rs_p_o_l.getObject(2);
						Struct mdfStruct = (Struct) rs_p_o_l.getObject(3);
						String time = rs_p_o_l.getString("time");
						int nodeid = rs_p_o_l.getInt("tra_id");
						list.add(new StopOnLink(linkid, analyseLink[0].consist,
								enterTime, leaveTime, time, link_length,
								orgStruct, mdfStruct, speed, traid, nodeid));
					}
					rs_p_o_l.close();
					if (list.size() > 0) {
						dealLastLink(mid, sid, segid, stays, stops, moves,
								closes, passes, trj_moves, trj_stops, list);
						list.clear();
						analyseLink[0] = null;
						analyseLink[1] = null;
					} else {
						System.out.println("这是查询结果集中的最后一条link失败！traid是" + traid
								+ ",seqid是" + seqid);
					}
					continue;
				}

				// 判断是不是某一个轨迹的最后一个link
				if (rs.next()) {
					String enterTime_n = rs.getString("entertime");
					String leaveTime_n = rs.getString("leaveTime");
					int fileid_n = rs.getInt("file_id");
					int traid_n = rs.getInt("tra_id");
					int seqid_n = rs.getInt("seq_id");
					int linkid_n = rs.getInt("link_id");

					// 如果seqid_n=0，则说明之前的位置是某条轨迹的最后一个link
					if (seqid_n == 0) {
						/*
						 * String sql_p_o_l =
						 * "select o.speed,o.geom,m.geom,m.time,m.node_id from wh_modifiedtra m,day0529_tra_origin o,links l,link_tras2 lt "
						 * +
						 * "where m.roadid=l.link_id and m.roadid=lt.link_id and m.fileid=o.fileid and m.node_id=o.id and "
						 * +
						 * "m.time between lt.entertime and lt.leavetime and m.fileid="
						 * + fileid + " and lt.fid=" + fileid +
						 * " and lt.seq_id=" + seqid;
						 */
						String sql_p_o_l = "select o.speed,o.geo,m.new,m.time,m.tra_id from wh_modifiedtra m,wh_tras o,links l,link_tras lt "
								+ "where m.roadid=l.link_id and m.roadid=lt.link_id and m.file_id=o.fileid and m.tra_id=o.id"
								+ " and m.time between lt.entertime and lt.leavetime and m.file_id="
								+ fileid
								+ " and lt.file_id="
								+ fileid
								+ " and lt.seq_id=" + seqid;
						// System.out.println(enterTime+","+leaveTime);
						// analyseLink[0]是当前处理的link
						// analyseLinks.add(analyseLink[1]);

						ResultSet rs_p_o_l = executeQuery(conn, sql_p_o_l);
						while (rs_p_o_l.next()) {
							double speed = rs_p_o_l.getDouble("speed");
							Struct orgStruct = (Struct) rs_p_o_l.getObject(2);
							Struct mdfStruct = (Struct) rs_p_o_l.getObject(3);
							String time = rs_p_o_l.getString("time");
							int nodeid = rs_p_o_l.getInt("tra_id");
							list.add(new StopOnLink(linkid,
									analyseLink[0].consist, enterTime,
									leaveTime, time, link_length, orgStruct,
									mdfStruct, speed, traid, nodeid));
						}
						rs_p_o_l.close();
						if (list.size() > 0) {
							dealLastLink(mid, sid, segid, stays, stops, moves,
									closes, passes, trj_moves, trj_stops, list);
							list.clear();
							if (trj_stops.size() > 0) {
								sid = trj_stops.get(trj_stops.size() - 1).sid + 1;
							}
							if (trj_moves.size() > 0) {
								segid = 0;
								mid = trj_moves.get(trj_moves.size() - 1).mid + 1;
							}
							analyseLink[0] = null;
							analyseLink[1] = null;
						} else {
							/*
							 * if(trj_stops.size()>0){
							 * sid=trj_stops.get(trj_stops.size()-1).sid+1; }
							 * if(trj_moves.size()>0){ segid=0;
							 * mid=trj_moves.get(trj_moves.size()-1).mid+1; }
							 */
							analyseLink[0] = null;
							analyseLink[1] = null;
							System.out.println("这是处理轨迹的最后一条link失败！traid是"
									+ traid + ",seqid是" + seqid);
						}

						// 计算下一条轨迹
						rs.previous();
						continue;
					}
					// 指针回到原来的位置
					rs.previous();
				}

				// 判定运动方向是否与画线方向
				if (analyseLink[0] == null) {
					analyseLink[0] = new AnalyseLink(linkid, link_length,
							enterTime, leaveTime, fileid, traid, seqid,
							startNode, endNode, true);
				} else if (analyseLink[1] == null) {
					analyseLink[1] = new AnalyseLink(linkid, link_length,
							enterTime, leaveTime, fileid, traid, seqid,
							startNode, endNode, true);
					if (analyseLink[1].startNode != analyseLink[0].endNode
							&& analyseLink[1].startNode != analyseLink[0].startNode) {
						analyseLink[1].consist = false;
					}
				}

				int timespan = subtractTime(enterTime, leaveTime);
				if (link_length / timespan < 3) {
					// 查询有轨迹点存在的link
					/*
					 * String sql_p_o_l =
					 * "select o.speed,o.geom,m.geom,m.time,m.node_id from wh_modifiedtra m,day0529_tra_origin o,links l,link_tras2 lt "
					 * +
					 * "where m.roadid=l.link_id and m.roadid=lt.link_id and m.fileid=o.fileid and m.node_id=o.id and "
					 * +
					 * "m.time between lt.entertime and lt.leavetime and m.fileid="
					 * + fileid + " and lt.fid=" + fileid + " and lt.seq_id=" +
					 * seqid;
					 */
					String sql_p_o_l = "select o.speed,o.geo,m.new,m.time,m.tra_id from wh_modifiedtra m,wh_tras o,links l,link_tras lt "
							+ "where m.roadid=l.link_id and m.roadid=lt.link_id and m.file_id=o.fileid and m.tra_id=o.id"
							+ " and m.time between lt.entertime and lt.leavetime and m.file_id="
							+ fileid
							+ " and lt.file_id="
							+ fileid
							+ " and lt.seq_id=" + seqid;
					// System.out.println(enterTime+","+leaveTime);
					// analyseLink[1]是当前处理的link
					// analyseLinks.add(analyseLink[1]);

					ResultSet rs_p_o_l = executeQuery(conn, sql_p_o_l);
					while (rs_p_o_l.next()) {
						double speed = rs_p_o_l.getDouble("speed");
						Struct orgStruct = (Struct) rs_p_o_l.getObject(2);
						Struct mdfStruct = (Struct) rs_p_o_l.getObject(3);
						String time = rs_p_o_l.getString("time");
						int nodeid = rs_p_o_l.getInt("tra_id");
						list.add(new StopOnLink(linkid, analyseLink[1].consist,
								enterTime, leaveTime, time, link_length,
								orgStruct, mdfStruct, speed, traid, nodeid));
					}
					rs_p_o_l.close();

					if (list.size() > 0) {
						findStop(sid, mid, segid, list, trj_stops, trj_moves,
								stops, stays, closes, moves, passes);

						if (trj_stops.size() > 0) {
							sid = trj_stops.get(trj_stops.size() - 1).sid + 1;
						}
						if (trj_moves.size() > 0) {
							segid = trj_moves.get(trj_moves.size() - 1).seqid + 1;
							;
							mid = trj_moves.get(trj_moves.size() - 1).mid;
						}
						if (list.get(0).consist
								&& (trj_moves.get(trj_moves.size() - 1).percentage - 1.0 == 0)) {
							mid++;
							segid = 0;
						} else if ((!list.get(0).consist)
								&& (trj_moves.get(trj_moves.size() - 1).percentage - 0.0 == 0)) {
							mid++;
							segid = 0;
						}
						list.clear();
					}

					analyseLink[0] = analyseLink[1];
					analyseLink[1] = null;
					continue;// 判断下一个link是否有停留
				}
				// 未停留，计算pass的POI
				else {
					// List<Integer> ints = new ArrayList<Integer>();
					findPass(mid, segid, linkid, analyseLink[1], passes,
							trj_moves);
					segid = trj_moves.get(trj_moves.size() - 1).seqid + 1;

					analyseLink[0] = analyseLink[1];
					analyseLink[1] = null;
				}
				// 此link未停留
				// if (list.size() > 0) {

				// }
			}
			// 通过list是否被清空来判断最后一些link是否有停留而未处理
			// if (list.size() > 0) {

			// }
			insertTable(trj_stops, trj_moves, stops, stays, closes, moves,
					passes);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("这是getStop函数中的错误");
		}

	}

	// 计算前两个link的stop与pass
	private void computeFirstTwo(int sid, int mid, int seqid,
			AnalyseLink[] analyseLink, List<Stop> stops,
			List<Trj_Stop> trj_stops, List<Trj_Move> trj_moves,
			List<Stay> stays, List<Close> closes, List<Pass> passes) {
		try {
			int pass_seq = 0;
			if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
				pass_seq = passes.get(passes.size() - 1).seqid + 1;
			}
			double per1 = 0.0;
			String time1 = null;// 第一个点的时间
			String sql = "select * from wh_modifiedtra whm,link_tras lt where lt.seq_id="
					+ analyseLink[0].seqid
					+ " and whm.file_id="
					+ analyseLink[0].fileid
					+ " and lt.link_id="
					+ analyseLink[0].linkid
					+ " and whm.file_id=lt.file_id"
					+ " and whm.roadid=lt.link_id and whm.time between lt.entertime and lt.leavetime order by whm.tra_id";
			ResultSet rs_firstPoint = executeQuery(conn, sql);
			if (rs_firstPoint.first()) {
				Struct point = (Struct) rs_firstPoint.getObject("new");
				per1 = getPercentage(netName, analyseLink[0].linkid, point);
				time1 = rs_firstPoint.getString("time");
			}
			rs_firstPoint.close();
			List<Feature> features = getFeatursOnLink(conn,
					analyseLink[0].linkid);

			if (!analyseLink[0].consist) {
				// Collections.reverse(features);
				features = features_reverse(features);
				per1 = -1 * per1;
			}

			if (per1 - 1 == 0 && analyseLink[0].consist) {

			} else if (per1 - 0 == 0 && (!analyseLink[0].consist)) {

			} else {
				trj_moves.add(new Trj_Move(analyseLink[0].traid, mid, seqid,
						analyseLink[0].linkid, per1, time1,
						analyseLink[0].leaveTime));
			}

			trj_stops.add(new Trj_Stop(analyseLink[0].traid, sid,
					analyseLink[0].linkid, per1, null, time1));
			stops.add(new Stop(sid, analyseLink[0].traid, null, time1));

			for (int i = 0; i < features.size(); i++) {
				// 在第一个点之前的feature处理如下
				if (features.get(i).startper < Math.abs(per1)) {
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(per1))
							* analyseLink[0].length;
					if (length_p_s < 30) {
						stays.add(new Stay(sid, analyseLink[0].traid, features
								.get(i).featureid,
								features.get(i).featurelayerid,
								features.get(i).featureType));
					} else if (length_p_s < 100) {
						closes.add(new Close(sid, analyseLink[0].traid,
								features.get(i).featureid,
								features.get(i).featurelayerid,
								features.get(i).featureType));
					} else {
						continue;
					}
				} else {
					// 在第一个点之后的feature处理如下
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(per1))
							* analyseLink[0].length;
					if (length_p_s < 30) {
						stays.add(new Stay(sid, analyseLink[0].traid, features
								.get(i).featureid,
								features.get(i).featurelayerid,
								features.get(i).featureType));
					} else if (length_p_s < 100) {
						closes.add(new Close(sid, analyseLink[0].traid,
								features.get(i).featureid,
								features.get(i).featurelayerid,
								features.get(i).featureType));
					} else {
						int totalspan = subtractTime(time1,
								analyseLink[0].leaveTime);
						int diff = new Double(totalspan
								* (1 - features.get(i).startper)
								/ (1 - Math.abs(per1))).intValue();
						String passtime = addTime(analyseLink[0].leaveTime, -1
								* diff);
						passes.add(new Pass(mid, analyseLink[0].traid,
								pass_seq++, features.get(i).featureid,
								passtime, features.get(i).featurelayerid,
								features.get(i).featureType));
					}
				}

			}
			sid++;
			seqid++;
			trj_moves.add(new Trj_Move(analyseLink[1].traid, mid, seqid,
					analyseLink[1].linkid, 1, analyseLink[1].enterTime,
					analyseLink[1].leaveTime));
			List<Feature> features2 = getFeatursOnLink(conn,
					analyseLink[0].linkid);
			List<Feature> features_reverse = new ArrayList<Feature>();
			if (analyseLink[1].consist) {

				for (int i = 0; i < features2.size(); i++) {
					int total = subtractTime(analyseLink[1].enterTime,
							analyseLink[1].leaveTime);
					int diff = new Double(features2.get(i).startper * total)
							.intValue();
					String passtime = addTime(analyseLink[1].enterTime, diff);
					passes.add(new Pass(mid, analyseLink[1].traid, pass_seq++,
							features2.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));

				}
			} else {
				// 先把features2反转
				features2 = features_reverse(features2);
				for (int i = 0; i < features2.size(); i++) {
					int total = subtractTime(analyseLink[1].enterTime,
							analyseLink[1].leaveTime);
					int diff = new Double(features2.get(i).startper * total)
							.intValue();
					String passtime = addTime(analyseLink[1].enterTime, diff);
					passes.add(new Pass(mid, analyseLink[1].linkid, pass_seq++,
							features2.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("这是处理前两个link的错误");
		}
	}

	// 从第三个link开始，计算pass过程中，经过的POI
	private void findPass(int mid, int seqid, int linkid,
			AnalyseLink analyseLink, List<Pass> passes, List<Trj_Move> trj_moves) {
		/*
		 * if (passes.size() > 0) { seqid = trj_moves.get(trj_moves.size() -
		 * 1).seqid + 1; }
		 */

		trj_moves.add(new Trj_Move(analyseLink.traid, mid, seqid, linkid, 1,
				analyseLink.enterTime, analyseLink.leaveTime));
		int pass_seq = 0;
		if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
			pass_seq = passes.get(passes.size() - 1).seqid + 1;
		}
		List<Feature> features = getFeatursOnLink(conn, linkid);
		List<Feature> f_reverse = new ArrayList<Feature>();
		// int traid;
		if (features.size() == 0) {
			return;
		}
		if (!analyseLink.consist) {
			// Collections.reverse(features);
			// 反转features的顺序，并改变feature的百分比
			features = features_reverse(features);
		}
		for (int i = 0; i < features.size(); i++) {
			String enterTime = analyseLink.enterTime;
			String leaveTime = analyseLink.leaveTime;
			// int fileid=analyseLink.fileid;
			int traid = analyseLink.traid;

			int featureid = features.get(i).featureid;
			double per = features.get(i).startper;
			String featureType = features.get(i).featureType;

			int timespent = new Double(subtractTime(enterTime, leaveTime) * per)
					.intValue();
			String passtime = addTime(enterTime, timespent);
			passes.add(new Pass(mid, traid, pass_seq++, featureid, passtime,
					features.get(i).featurelayerid, features.get(i).featureType));

		}

	}

	// 从第三个link开始，计算pass过程中，经过的POI，主要处理判断连续停留过程中的pass
	private void findPass(int mid, int seqid, int linkid,
			LinkHasPoint linkHasPoint, List<Pass> passes,
			List<Trj_Move> trj_moves) {
		/*
		 * if (passes.size() > 0) { seqid = trj_moves.get(trj_moves.size() -
		 * 1).seqid + 1; }
		 */

		trj_moves.add(new Trj_Move(linkHasPoint.traid, mid, seqid, linkid, 1,
				linkHasPoint.enterTime, linkHasPoint.leaveTime));

		int pass_seq = 0;
		if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
			pass_seq = passes.get(passes.size() - 1).seqid + 1;
		}
		List<Feature> features = getFeatursOnLink(conn, linkid);
		List<Feature> f_reverse = new ArrayList<Feature>();
		// int traid;
		if (features.size() == 0) {
			return;
		}
		if (!linkHasPoint.consist) {
			// Collections.reverse(features);
			// 反转features的顺序，并改变feature的百分比
			features = features_reverse(features);
		}
		for (int i = 0; i < features.size(); i++) {
			String enterTime = linkHasPoint.enterTime;
			String leaveTime = linkHasPoint.leaveTime;
			// int fileid=analyseLink.fileid;
			int traid = linkHasPoint.traid;

			int featureid = features.get(i).featureid;
			double per = features.get(i).startper;
			String featureType = features.get(i).featureType;

			int timespent = new Double(subtractTime(enterTime, leaveTime) * per)
					.intValue();
			String passtime = addTime(enterTime, timespent);
			passes.add(new Pass(mid, traid, pass_seq++, featureid, passtime,
					features.get(i).featurelayerid, features.get(i).featureType));

		}

	}

	private void findStop(int sid, int mid, int seqid, List<StopOnLink> list,
			List<Trj_Stop> trj_stops, List<Trj_Move> trj_moves,
			List<Stop> stops, List<Stay> stays, List<Close> closes,
			List<Move> moves, List<Pass> passes) {
		int pass_seq = 0;
		if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
			pass_seq = passes.get(passes.size() - 1).seqid + 1;
		}
		
		List<Stay> temp_stays=new  ArrayList<Stay>();//记录这个stop过程中可能的stay
		Stay crossing=null;//记录判断是路口停留的stop

		List<Double> percentages = new ArrayList<Double>();
		double avg = 0.0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).speed == 0) {
				double percentage = getPercentage(netName, list.get(i).linkid,
						list.get(i).mdf_point);
				percentages.add(percentage);
			}
		}
		if (percentages.size() > 0) {
			double sum = 0.0;
			for (int i = 0; i < percentages.size(); i++) {
				sum += percentages.get(i);
			}
			avg = sum / percentages.size();
		} else {
			avg = getPercentage(netName, list.get(list.size() - 1).linkid,
					list.get(list.size() - 1).mdf_point);
		}

		int pre;
		int post;
		if (list.get(0).length < 100) {
			pre = new Double(avg * list.get(0).length / 6).intValue();
			post = new Double((1 - avg) * list.get(0).length / 6).intValue();
		} else {
			pre = new Double(avg * list.get(0).length / 10).intValue();
			post = new Double((1 - avg) * list.get(0).length / 10).intValue();
		}
		if (pre == 0) {
			pre = 1;
		}
		if (post == 0) {
			post = 1;
		}
		String beginTime_stop = addTime(list.get(0).enterTime, pre);
		String endTime_stop = addTime(list.get(0).leaveTime, -1 * post);

		List<Feature> features = getFeatursOnLink(conn, list.get(0).linkid);
		if (!list.get(0).consist) {

			// 运动方向与link方向相反
			features = features_reverse(features);
			avg = -1 * avg;
			if (list.get(0).length < 100) {
				pre = new Double((1 + avg) * list.get(0).length / 6).intValue();
				post = new Double((avg) * list.get(0).length / 6).intValue();
			} else {
				pre = new Double((1 + avg) * list.get(0).length / 10)
						.intValue();
				post = new Double((avg) * list.get(0).length / 10).intValue();
			}
			if (pre == 0) {
				pre = 1;
			}
			if (post == 0) {
				post = -1;
			}
			// pre = new Double((1 + avg) * list.get(0).length / 15).intValue();
			// post = new Double((avg) * list.get(0).length / 15).intValue();
			beginTime_stop = addTime(list.get(0).enterTime, pre);
			endTime_stop = addTime(list.get(0).leaveTime, post);
		}
		
		// 判断是否因为有红绿灯才停留
		// 获取此Link
		Link thisLink = null;
		// 获取距离停留点较近的node
		Node thisNode = null;
		try {
			thisLink = wh_roads.getLink(list.get(0).linkid);
			if (Math.abs(avg) > 0.5) {
				thisNode = thisLink.getEndNode();
			} else {
				thisNode = thisLink.getStartNode();
			}

		} catch (NetworkDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!list.get(0).consist) {
			// 行驶方向从endnode到startnode
			if (Math.abs(avg) < 0.5) {
				thisNode = thisLink.getEndNode();
			} else {
				thisNode = thisLink.getStartNode();
			}
		}
		int degree = thisNode.getDegree();
		int stayTime = subtractTime(beginTime_stop, endTime_stop);
		double leaveLength=0.0;
		if(avg>0){
			leaveLength=(1-avg)*list.get(0).length;
		}
		else {
			leaveLength=Math.abs(avg)*list.get(0).length;
		}
		
		if(degree>2&&stayTime<120&&leaveLength<30){
			//判定为是在等红绿灯
			crossing=new Stay(sid, list.get(0).traid, -1, -1, "crossing");//用crossing记录在路口停留的stay
			for (int i = 0; i < features.size(); i++) {
				// 在第一个点之前的feature处理如下
				if (features.get(i).startper < Math.abs(avg)) {
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(avg))
							* list.get(0).length;
					if (length_p_s > 100) {
						// 正常速度行驶，从link的起点到feature的时间
						int diff = new Double(list.get(0).length
								* (features.get(i).startper) / 10).intValue();
						if (list.get(0).length < 100) {
							diff = new Double(list.get(0).length
									* (features.get(i).startper) / 6).intValue();
						}
						String passtime = addTime(list.get(0).enterTime, diff);
						passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
								features.get(i).featureid, passtime, features
										.get(i).featurelayerid,
								features.get(i).featureType));
					}
				} else {
					// 在第一个点之后的feature处理如下
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(avg))
							* list.get(0).length;
					if (length_p_s > 100) {
						// 正常速度行驶，从feature到link的终点的时间
						int diff = new Double(list.get(0).length
								* (1 - features.get(i).startper) / 10).intValue();
						if (list.get(0).length < 100) {
							diff = new Double(list.get(0).length
									* (1 - features.get(i).startper) / 6)
									.intValue();
						}
						String passtime = addTime(list.get(0).leaveTime, -1 * diff);
						passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
								features.get(i).featureid, passtime, features
										.get(i).featurelayerid,
								features.get(i).featureType));
					}
				}

			}
		}
		else {
			for (int i = 0; i < features.size(); i++) {
				// 在第一个点之前的feature处理如下
				if (features.get(i).startper < Math.abs(avg)) {
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(avg))
							* list.get(0).length;
					if (length_p_s < 30) {
						
						// 停留时间小于120秒且degree>2说明是个交叉路口
						if (degree > 2 && stayTime < 120) {
//							stays.add(new Stay(sid, list.get(0).traid, features
//									.get(i).featureid,
//									features.get(i).featurelayerid, "crossing"));
							temp_stays.add(new Stay(sid, list.get(0).traid, features
									.get(i).featureid,
									features.get(i).featurelayerid,
									features.get(i).featureType));
						} else {
//							stays.add(new Stay(sid, list.get(0).traid, features
//									.get(i).featureid,
//									features.get(i).featurelayerid,
//									features.get(i).featureType));
							temp_stays.add(new Stay(sid, list.get(0).traid, features
									.get(i).featureid,
									features.get(i).featurelayerid,
									features.get(i).featureType));
						}

					} else if (length_p_s < 100) {
						closes.add(new Close(sid, list.get(0).traid, features
								.get(i).featureid, features.get(i).featurelayerid,
								features.get(i).featureType));
					} else {
						// 正常速度行驶，从link的起点到feature的时间
						int diff = new Double(list.get(0).length
								* (features.get(i).startper) / 10).intValue();
						if (list.get(0).length < 100) {
							diff = new Double(list.get(0).length
									* (features.get(i).startper) / 6).intValue();
						}
						String passtime = addTime(list.get(0).enterTime, diff);
						passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
								features.get(i).featureid, passtime, features
										.get(i).featurelayerid,
								features.get(i).featureType));
					}
				} else {
					// 在第一个点之后的feature处理如下
					double length_p_s = Math.abs(features.get(i).startper
							- Math.abs(avg))
							* list.get(0).length;
					if (length_p_s < 30) {
//						stays.add(new Stay(sid, list.get(0).traid,
//								features.get(i).featureid,
//								features.get(i).featurelayerid,
//								features.get(i).featureType));
						temp_stays.add(new Stay(sid, list.get(0).traid, features
								.get(i).featureid,
								features.get(i).featurelayerid,
								features.get(i).featureType));
					} else if (length_p_s < 100) {
						closes.add(new Close(sid, list.get(0).traid, features
								.get(i).featureid, features.get(i).featurelayerid,
								features.get(i).featureType));
					} else {
						// 正常速度行驶，从feature到link的终点的时间
						int diff = new Double(list.get(0).length
								* (1 - features.get(i).startper) / 10).intValue();
						if (list.get(0).length < 100) {
							diff = new Double(list.get(0).length
									* (1 - features.get(i).startper) / 6)
									.intValue();
						}
						String passtime = addTime(list.get(0).leaveTime, -1 * diff);
						passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
								features.get(i).featureid, passtime, features
										.get(i).featurelayerid,
								features.get(i).featureType));
					}
				}

			}
		}
		
		
		
		if(crossing!=null){
			//说明上述stop是在路口停留
			stays.add(crossing);
		}
		else {
			//说明上述stop不是在路口停留，将temp_stays中的信息加入到stays中即可
			if(temp_stays.size()>0){
				stays.addAll(temp_stays);
			}
			
		}

		if (avg - 0.0 == 0 && list.get(0).consist) {

		} else if (Math.abs(avg) - 1.0 == 0 && (!list.get(0).consist)) {

		} else {
			trj_moves
					.add(new Trj_Move(list.get(0).traid, mid, seqid, list
							.get(0).linkid, avg, list.get(0).enterTime,
							beginTime_stop));
		}

		seqid = 0;
		moves.add(new Move(mid, list.get(0).traid, sid - 1, sid));
		mid++;
		trj_stops.add(new Trj_Stop(list.get(0).traid, sid, list.get(0).linkid,
				avg, beginTime_stop, endTime_stop));
		stops.add(new Stop(sid, list.get(0).traid, beginTime_stop, endTime_stop));
		sid++;

		if (avg - 1 == 0 && list.get(0).consist) {

		} else if (avg - 0 == 0 && (!list.get(0).consist)) {

		} else {
			trj_moves.add(new Trj_Move(list.get(0).traid, mid, seqid++, list
					.get(0).linkid, avg, endTime_stop, list.get(0).leaveTime));
			// 这里要不要判断有没有pass的poi？
		}

	}

	// 处理确认停留的link
	private void dealLinkWithStay(int mid, int sid, int seqid,
			StopOnLink stopOnLink, List<Stay> stays, List<Stop> stops,
			List<Move> moves, List<Close> closes, List<Pass> passes,
			List<Trj_Move> trj_moves, List<Trj_Stop> trj_stops) {

		int pass_seq = 0;
		if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
			pass_seq = passes.get(passes.size() - 1).seqid + 1;
		}
		// 判定运动方向与画线方向是否相反
		double per = getPercentage(netName, stopOnLink.linkid,
				stopOnLink.mdf_point);
		int pre = new Double(per * stopOnLink.length / 15).intValue();
		int post = new Double((1 - per) * stopOnLink.length / 15).intValue();
		String beginTime_stop = addTime(stopOnLink.enterTime, pre);
		String endTime_stop = addTime(stopOnLink.leaveTime, -1 * post);

		List<Feature> features = getFeatursOnLink(conn, stopOnLink.linkid);
		if (!stopOnLink.consist) {
			// 运动方向与link方向相反
			features = features_reverse(features);
			per = -1 * per;
			pre = new Double((1 + per) * stopOnLink.length / 15).intValue();
			post = new Double((per) * stopOnLink.length / 15).intValue();
			beginTime_stop = addTime(stopOnLink.enterTime, pre);
			endTime_stop = addTime(stopOnLink.leaveTime, post);
		}
		for (int i = 0; i < features.size(); i++) {
			// 在第一个点之前的feature处理如下
			if (features.get(i).startper < Math.abs(per)) {
				double length_p_s = Math.abs(features.get(i).startper
						- Math.abs(per))
						* stopOnLink.length;
				if (length_p_s < 30) {
					stays.add(new Stay(sid, stopOnLink.traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else if (length_p_s < 100) {
					closes.add(new Close(sid, stopOnLink.traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else {
					// 正常速度行驶，从link的起点到feature的时间
					int diff = new Double(stopOnLink.length
							* (features.get(i).startper) / 15).intValue();
					String passtime = addTime(stopOnLink.enterTime, diff);
					passes.add(new Pass(mid, stopOnLink.traid, pass_seq++,
							features.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));
				}
			} else {
				// 在第一个点之后的feature处理如下
				double length_p_s = Math.abs(features.get(i).startper
						- Math.abs(per))
						* stopOnLink.length;
				if (length_p_s < 30) {
					stays.add(new Stay(sid, stopOnLink.traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else if (length_p_s < 100) {
					closes.add(new Close(sid, stopOnLink.traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else {
					// 正常速度行驶，从feature到link的终点的时间
					int diff = new Double(stopOnLink.length
							* (1 - features.get(i).startper) / 15).intValue();
					String passtime = addTime(stopOnLink.leaveTime, -1 * diff);
					passes.add(new Pass(mid, stopOnLink.traid, pass_seq++,
							features.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));
				}
			}

		}

		trj_moves.add(new Trj_Move(stopOnLink.traid, mid, seqid,
				stopOnLink.linkid, per, stopOnLink.enterTime, beginTime_stop));
		seqid = 0;
		moves.add(new Move(mid, stopOnLink.traid, sid - 1, sid));
		mid++;
		trj_stops.add(new Trj_Stop(stopOnLink.traid, sid, stopOnLink.linkid,
				per, beginTime_stop, endTime_stop));
		stops.add(new Stop(sid, stopOnLink.traid, beginTime_stop, endTime_stop));
		sid++;
		trj_moves.add(new Trj_Move(stopOnLink.traid, mid, seqid++,
				stopOnLink.linkid, per, endTime_stop, stopOnLink.leaveTime));
	}

	// 当最后一个link平均速度过小时，处理最后一个link
	private void dealLastLink(int mid, int sid, int seqid, List<Stay> stays,
			List<Stop> stops, List<Move> moves, List<Close> closes,
			List<Pass> passes, List<Trj_Move> trj_moves,
			List<Trj_Stop> trj_stops, List<StopOnLink> list) {
		int pass_seq = 0;
		if (passes.size() > 0 && passes.get(passes.size() - 1).mid == mid) {
			pass_seq = passes.get(passes.size() - 1).seqid + 1;
		}

		List<Double> percentages = new ArrayList<Double>();
		double avg = 0.0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).speed == 0) {
				double percentage = getPercentage(netName, list.get(i).linkid,
						list.get(i).mdf_point);
				percentages.add(percentage);
			}
		}
		if (percentages.size() > 0) {
			double sum = 0.0;
			for (int i = 0; i < percentages.size(); i++) {
				sum += percentages.get(i);
			}
			avg = sum / percentages.size();
		} else {
			// 没有speed=0的轨迹点，把link上最后一个轨迹点作为stop点
			avg = getPercentage(netName, list.get(list.size() - 1).linkid,
					list.get(list.size() - 1).mdf_point);
		}

		int pre = new Double(avg * list.get(0).length / 15).intValue();
		int post = new Double((1 - avg) * list.get(0).length / 15).intValue();
		String beginTime_stop = addTime(list.get(0).enterTime, pre);
		// String endTime_stop=addTime(list.get(0).leaveTime, -1*post);

		List<Feature> features = getFeatursOnLink(conn, list.get(0).linkid);
		if (!list.get(0).consist) {
			// 运动方向与link方向相反
			features = features_reverse(features);
			avg = -1 * avg;
			pre = new Double((1 + avg) * list.get(0).length / 15).intValue();
			post = new Double((avg) * list.get(0).length / 15).intValue();
			beginTime_stop = addTime(list.get(0).enterTime, pre);
			// endTime_stop=addTime(list.get(0).leaveTime, post);
		}
		for (int i = 0; i < features.size(); i++) {
			// 在第一个点之前的feature处理如下
			if (features.get(i).startper < Math.abs(avg)) {
				double length_p_s = Math.abs(features.get(i).startper
						- Math.abs(avg))
						* list.get(0).length;
				if (length_p_s < 30) {
					stays.add(new Stay(sid, list.get(0).traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else if (length_p_s < 100) {
					closes.add(new Close(sid, list.get(0).traid, features
							.get(i).featureid, features.get(i).featurelayerid,
							features.get(i).featureType));
				} else {
					// 正常速度行驶，从link的起点到feature的时间
					int diff = new Double(list.get(0).length
							* (features.get(i).startper) / 15).intValue();
					String passtime = addTime(list.get(0).enterTime, diff);
					passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
							features.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));
				}
			} else {
				// 在第一个点之后的feature处理如下
				double length_p_s = Math.abs(features.get(i).startper
						- Math.abs(avg))
						* list.get(0).length;
				if (length_p_s < 30) {
					stays.add(new Stay(sid, list.get(0).traid,
							features.get(i).featureid,
							features.get(i).featurelayerid,
							features.get(i).featureType));
				} else if (length_p_s < 100) {
					closes.add(new Close(sid, list.get(0).traid, features
							.get(i).featureid, features.get(i).featurelayerid,
							features.get(i).featureType));
				} else {
					// 正常速度行驶，从feature到link的终点的时间
					int diff = new Double(list.get(0).length
							* (1 - features.get(i).startper) / 15).intValue();
					String passtime = addTime(list.get(0).leaveTime, -1 * diff);
					passes.add(new Pass(mid, list.get(0).traid, pass_seq++,
							features.get(i).featureid, passtime, features
									.get(i).featurelayerid,
							features.get(i).featureType));
				}
			}

		}

		if (list.get(0).consist && (avg - 0.0 == 0)) {

		} else if ((!list.get(0).consist) && (Math.abs(avg) - 1.0 == 0)) {

		} else {
			trj_moves
					.add(new Trj_Move(list.get(0).traid, mid, seqid, list
							.get(0).linkid, avg, list.get(0).enterTime,
							beginTime_stop));
		}

		seqid = 0;
		moves.add(new Move(mid, list.get(0).traid, sid - 1, sid));
		mid++;
		trj_stops.add(new Trj_Stop(list.get(0).traid, sid, list.get(0).linkid,
				avg, beginTime_stop, null));
		stops.add(new Stop(sid, list.get(0).traid, beginTime_stop, null));
		sid++;
	}

	// 将数据填充到数据库中
	private void insertTable(List<Trj_Stop> trj_stops,
			List<Trj_Move> trj_moves, List<Stop> stops, List<Stay> stays,
			List<Close> closes, List<Move> moves, List<Pass> passes) {
		try {
			String sql_trjstops = "insert into trj_stop(tid,sid,linkid,posit,begintime,endtime) "
					+ "values(?,?,?,?,?,?)";
			PreparedStatement stm_trjstops = conn
					.prepareStatement(sql_trjstops);
			for (int i = 0; i < trj_stops.size(); i++) {
				stm_trjstops.setInt(1, trj_stops.get(i).tid);
				stm_trjstops.setInt(2, trj_stops.get(i).sid);
				stm_trjstops.setInt(3, trj_stops.get(i).linkid);
				stm_trjstops.setDouble(4, trj_stops.get(i).percentage);
				stm_trjstops.setString(5, trj_stops.get(i).beginTime);
				stm_trjstops.setString(6, trj_stops.get(i).endTime);
				stm_trjstops.addBatch();
			}
			stm_trjstops.executeBatch();
			stm_trjstops.close();

			String sql_trjmoves = "insert into trj_move(tid,mid,segid,linkid,posit,entertime,leavetime) "
					+ "values(?,?,?,?,?,?,?)";
			PreparedStatement stm_trjmoves = conn
					.prepareStatement(sql_trjmoves);
			for (int i = 0; i < trj_moves.size(); i++) {
				stm_trjmoves.setInt(1, trj_moves.get(i).tid);
				stm_trjmoves.setInt(2, trj_moves.get(i).mid);
				stm_trjmoves.setInt(3, trj_moves.get(i).seqid);
				stm_trjmoves.setInt(4, trj_moves.get(i).linkid);
				stm_trjmoves.setDouble(5, trj_moves.get(i).percentage);
				stm_trjmoves.setString(6, trj_moves.get(i).enterTime);
				stm_trjmoves.setString(7, trj_moves.get(i).leaveTime);
				stm_trjmoves.addBatch();

				if ((i + 1) % 300 == 0) {
					stm_trjmoves.executeBatch();
				}
			}
			stm_trjmoves.executeBatch();
			stm_trjmoves.close();

			String sql_moves = "insert into move(mid,tid,sid1,sid2) "
					+ "values(?,?,?,?)";
			PreparedStatement stm_moves = conn.prepareStatement(sql_moves);
			for (int i = 0; i < moves.size(); i++) {
				stm_moves.setInt(1, moves.get(i).mid);
				stm_moves.setInt(2, moves.get(i).tid);
				stm_moves.setInt(3, moves.get(i).sid1);
				stm_moves.setDouble(4, moves.get(i).sid2);

				stm_moves.addBatch();
			}
			stm_moves.executeBatch();
			stm_moves.close();

			String sql_passes = "insert into pass(mid,tid,seqid,poiid,featurelayer_id,time,description) "
					+ "values(?,?,?,?,?,?,?)";
			PreparedStatement stm_passes = conn.prepareStatement(sql_passes);
			for (int i = 0; i < passes.size(); i++) {
				stm_passes.setInt(1, passes.get(i).mid);
				stm_passes.setInt(2, passes.get(i).tid);
				stm_passes.setInt(3, passes.get(i).seqid);
				stm_passes.setInt(4, passes.get(i).POIid);
				stm_passes.setInt(5, passes.get(i).featurelayerid);
				stm_passes.setString(6, passes.get(i).time);
				stm_passes.setString(7, passes.get(i).description);

				stm_passes.addBatch();
				if ((i + 1) % 300 == 0) {
					stm_passes.executeBatch();
				}
			}
			stm_passes.executeBatch();
			stm_passes.close();

			String sql_stops = "insert into stop(sid,tid,begintime,endtime) "
					+ "values(?,?,?,?)";
			PreparedStatement stm_stops = conn.prepareStatement(sql_stops);
			for (int i = 0; i < stops.size(); i++) {
				stm_stops.setInt(1, stops.get(i).sid);
				stm_stops.setInt(2, stops.get(i).tid);
				stm_stops.setString(3, stops.get(i).beginTime);
				stm_stops.setString(4, stops.get(i).endTime);

				stm_stops.addBatch();
			}
			stm_stops.executeBatch();
			stm_stops.close();

			String sql_stays = "insert into stay(sid,tid,poiid,featurelayer_id,description) "
					+ "values(?,?,?,?,?)";
			PreparedStatement stm_stays = conn.prepareStatement(sql_stays);
			for (int i = 0; i < stays.size(); i++) {
				stm_stays.setInt(1, stays.get(i).sid);
				stm_stays.setInt(2, stays.get(i).tid);
				stm_stays.setInt(3, stays.get(i).POIid);
				stm_stays.setInt(4, stays.get(i).featurelayerid);
				stm_stays.setString(5, stays.get(i).description);

				stm_stays.addBatch();
			}
			stm_stays.executeBatch();
			stm_stays.close();

			String sql_closes = "insert into close(sid,tid,poiid,featurelayer_id,description) "
					+ "values(?,?,?,?,?)";
			PreparedStatement stm_closes = conn.prepareStatement(sql_closes);
			for (int i = 0; i < closes.size(); i++) {
				stm_closes.setInt(1, closes.get(i).sid);
				stm_closes.setInt(2, closes.get(i).tid);
				stm_closes.setInt(3, closes.get(i).POIid);
				stm_closes.setInt(4, closes.get(i).featurelayerid);
				stm_closes.setString(5, closes.get(i).description);

				stm_closes.addBatch();
			}
			stm_closes.executeBatch();
			stm_closes.close();

			Date date=new Date();
			DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time=format.format(date);
			System.out.println(time+":插入完成！");
		} catch (SQLException e) {
			System.out.println("这是填充数据库过程的错误！");
			e.printStackTrace();

		}

	}

	/**
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            查询语句
	 * @return 查询后的结果集
	 */
	private ResultSet executeQuery(Connection conn, String sql) {
		try {
			Statement stm = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stm.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			System.out.println("此处为查询数据库失败！");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param time1
	 *            时间1
	 * @param time2
	 *            时间2
	 * @return 时间差
	 */
	private int subtractTime(String time1, String time2) {
		Date date1 = getDate(time1);
		Date date2 = getDate(time2);
		if (date1 == null || date2 == null) {
			return -1;
		}
		long second1 = date1.getTime() / 1000;
		long second2 = date2.getTime() / 1000;
		return (int) Math.abs(second1 - second2);
	}

	/**
	 * @param time
	 *            时间字段
	 * @return 返回标准格式时间
	 */
	private Date getDate(String time) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone zone = TimeZone.getTimeZone("Asia/Shanghai");
		sdf.setTimeZone(zone);
		try {
			// System.out.println(time);
			date = sdf.parse(time);
			return date;
		} catch (ParseException e) {
			System.out.println("时间转换失败！");
			return null;
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
			// cs.clearBatch();
			cs.close();
			return percentage;
		} catch (SQLException e) {
			System.out.println("求百分比出现异常：" + "link_id为" + link_id
					+ e.getMessage());
			return percentage;
		}
	}

	/**
	 * @param date
	 *            标准格式的日期
	 * @param time
	 *            加的时间
	 * @return 字符串类型的时间类型
	 */
	private String addTime(String origin_time, int time) {
		Date date = new Date();
		date = getDate(origin_time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone zone = TimeZone.getTimeZone("Asia/Shanghai");
		sdf.setTimeZone(zone);
		long microSecond = date.getTime() + time * 1000;
		date.setTime(microSecond);
		String str_date = sdf.format(date);
		return str_date;
	}

	private List<Feature> getFeatursOnLink(Connection conn, int linkid) {
		List<Feature> features = new ArrayList<Feature>();
		try {
			CallableStatement call = conn
					.prepareCall("{?=call GETFEATURESONLINKS(?,?)}");
			call.registerOutParameter(1, Types.VARCHAR);
			for (int featurelayerid = 1; featurelayerid < 10; featurelayerid++) {

				call.setInt(2, featurelayerid);
				call.setInt(3, linkid);

				call.execute();
				String idsString = call.getString(1);
				// call.close();

				if (idsString == null) {
					continue;
				}
				String[] ids = idsString.split(",");
				for (int i = 0; i < ids.length; i++) {
					int featureid = Integer.valueOf(ids[i]);
					Feature feature = getFeatureInfo(conn, featureid,
							featurelayerid);
					features.add(feature);
				}
			}
			call.close();

			// 冒泡排序，让features按照per从小到大排序
			if (features.size() > 0) {
				for (int i = features.size() - 1; i > 0; i--) {

					for (int j = 0; j < i; j++) {
						Feature preFeature = features.get(j);
						Feature postFeature = features.get(j + 1);
						if (preFeature.startper > postFeature.startper) {
							features.set(j, postFeature);
							features.set(j + 1, preFeature);
						}
					}
				}
			}

			return features;

		} catch (SQLException e) {
			// TODO: handle exception
			System.out.println("这是获取features列表处错误！linkid=" + linkid);
			e.printStackTrace();
			return features;
		}
	}

	private Feature getFeatureInfo(Connection conn, int featureid,
			int featurelayerid) {
		Feature feature = null;
		try {
			CallableStatement call = conn
					.prepareCall("{?=call GETFEATUREBYID(?,?)}");
			call.setInt(2, featurelayerid);
			call.setInt(3, featureid);

			call.registerOutParameter(1, Types.VARCHAR);
			call.execute();
			String idsString = call.getString(1);
			String[] info = idsString.split(",");
			String featureLayerName = null;
			if (featurelayerid == 1) {
				featureLayerName = "restaurants";
			}
			if (featurelayerid == 2) {
				featureLayerName = "shops";
			}
			if (featurelayerid == 3) {
				featureLayerName = "traffic";
			}
			if (featurelayerid == 4) {
				featureLayerName = "interests";
			}
			if (featurelayerid == 5) {
				featureLayerName = "schools";
			}
			if (featurelayerid == 6) {
				featureLayerName = "hospitals";
			}
			if (featurelayerid == 7) {
				featureLayerName = "banks";
			}
			if (featurelayerid == 8) {
				featureLayerName = "offices";
			}
			if (featurelayerid == 9) {
				featureLayerName = "hotels";
			}

			feature = new Feature(featureid, Integer.valueOf(info[0]),
					Integer.valueOf(info[1]), Double.valueOf(info[2]),
					featurelayerid, featureLayerName);
			call.close();
			return feature;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("获取feature失败！");
			return feature;
		}
	}

	// 如果运动方向相反，改变link上features的顺序，并更改feature的百分比
	private List<Feature> features_reverse(List<Feature> features) {
		List<Feature> f_reverse = new ArrayList<Feature>();
		for (int i = features.size() - 1; i > -1; i--) {
			Feature feature = features.get(i);
			feature.startper = 1 - feature.startper;
			f_reverse.add(feature);
		}
		return f_reverse;
	}

}

// 出现停留的link
class StopOnLink {
	int linkid;
	boolean consist;
	String enterTime;
	String leaveTime;
	String time;
	double length;
	Struct org_point;
	Struct mdf_point;
	double speed;
	int traid;
	int nodeid;

	public StopOnLink(int linkid, boolean consist, String enterTime,
			String leaveTime, String time, double length, Struct org_point,
			Struct mdf_point, double speed, int traid, int nodeid) {
		this.linkid = linkid;
		this.consist = consist;
		this.enterTime = enterTime;
		this.leaveTime = leaveTime;
		this.time = time;
		this.length = length;
		this.org_point = org_point;
		this.mdf_point = mdf_point;
		this.speed = speed;
		this.traid = traid;
		this.nodeid = nodeid;
	}
}

class Trj_Stop {
	int tid;
	int sid;
	int linkid;
	double percentage;
	String beginTime;
	String endTime;

	public Trj_Stop(int tid, int sid, int linkid, double percentage,
			String beginTime, String endTime) {
		this.tid = tid;
		this.sid = sid;
		this.linkid = linkid;
		this.percentage = percentage;
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
}

class Trj_Move {
	int tid;
	int mid;
	int seqid;
	int linkid;
	double percentage;
	String enterTime;
	String leaveTime;

	public Trj_Move(int tid, int mid, int seqid, int linkid, double percentage,
			String enterTime, String leaveTime) {
		this.tid = tid;
		this.mid = mid;
		this.seqid = seqid;
		this.linkid = linkid;
		this.percentage = percentage;
		this.enterTime = enterTime;
		this.leaveTime = leaveTime;
	}
}

class Stop {
	int sid;
	int tid;
	String beginTime;
	String endTime;

	public Stop(int sid, int tid, String beginTime, String endTime) {
		this.sid = sid;
		this.tid = tid;
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
}

class Stay {
	int sid;
	int tid;
	int POIid;
	int featurelayerid;
	String description;
	
	public Stay(){
		
	}

	public Stay(int sid, int tid, int POIid, int featurelayerid,
			String description) {
		this.sid = sid;
		this.tid = tid;
		this.POIid = POIid;
		this.featurelayerid = featurelayerid;
		this.description = description;
	}
}

class Close {
	int sid;
	int tid;
	int POIid;
	int featurelayerid;
	String description;

	public Close(int sid, int tid, int POIid, int featurelayerid,
			String description) {
		this.sid = sid;
		this.tid = tid;
		this.POIid = POIid;
		this.featurelayerid = featurelayerid;
		this.description = description;
	}
}

class Move {
	int mid;
	int tid;
	int sid1;
	int sid2;

	public Move(int mid, int tid, int sid1, int sid2) {
		this.mid = mid;
		this.tid = tid;
		this.sid1 = sid1;
		this.sid2 = sid2;
	}
}

class Pass {
	int mid;
	int tid;
	int seqid;
	int POIid;
	String time;
	int featurelayerid;
	String description;

	public Pass(int mid, int tid, int seqid, int POIid, String time,
			int featurelayerid, String description) {
		this.mid = mid;
		this.tid = tid;
		this.seqid = seqid;
		this.POIid = POIid;
		this.time = time;
		this.featurelayerid = featurelayerid;
		this.description = description;
	}
}

// 用于计算运动方向是否与画线方向一致
class AnalyseLink {
	int linkid;
	double length;
	String enterTime;
	String leaveTime;
	int fileid;
	int traid;
	int seqid;
	int startNode;
	int endNode;

	boolean consist = true;// 判断运动方向是否与画线方向一致

	public AnalyseLink(int linkid, double length, String enterTime,
			String leaveTime, int fileid, int traid, int seqid, int startNode,
			int endNode, boolean consist) {
		this.linkid = linkid;
		this.length = length;
		this.enterTime = enterTime;
		this.leaveTime = leaveTime;
		this.fileid = fileid;
		this.traid = traid;
		this.seqid = seqid;

		this.startNode = startNode;
		this.endNode = endNode;

		this.consist = consist;
	}
}

//
class LinkHasPoint {
	int traid;
	int linkid;
	String enterTime;
	String leaveTime;
	boolean consist;

	public LinkHasPoint(int traid, int linkid, String enterTime,
			String leaveTime, boolean consist) {
		this.traid = traid;
		this.linkid = linkid;
		this.enterTime = enterTime;
		this.leaveTime = leaveTime;
		this.consist = consist;
	}

}

// POI信息类
class Feature {
	int featureid;
	int fea_elem_type;
	int net_elem_id;
	double startper;
	int featurelayerid;
	String featureType;

	public Feature(int featureid, int fea_elem_type, int net_elem_id,
			double startper, int featurelayerid, String featureType) {
		this.featureid = featureid;
		this.fea_elem_type = fea_elem_type;
		this.net_elem_id = net_elem_id;
		this.startper = startper;
		this.featurelayerid = featurelayerid;
		this.featureType = featureType;
	}
}
