package ndm;

import java.awt.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.PseudoColumnUsage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ndm.NetworkAnalyse.MatchProcess;
import ndm.domain.LinkTras;
import ndm.domain.ModifiedTra;
import oracle.jdbc.OracleTypes;
import oracle.net.aso.a;
import oracle.spatial.network.AnalysisInfo;
import oracle.spatial.network.lod.LODAnalysisInfo;
import oracle.spatial.network.lod.LODNetworkConstraint;
import oracle.spatial.network.lod.LODNetworkException;
import oracle.spatial.network.lod.LODNetworkManager;
import oracle.spatial.network.lod.LogicalPath;
import oracle.spatial.network.lod.LogicalSubPath;
import oracle.spatial.network.lod.NetworkAnalyst;
import oracle.spatial.network.lod.NetworkIO;
import oracle.spatial.network.lod.PointOnNet;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ManageTime {
	private static Logger logger = Logger.getLogger(ManageTime.class);
	static String netName = "WH_ROADS";
	static Connection conn = DataConnection.GetConnection();

	public static void main(String[] args) {
		ManageTime manageTime = new ManageTime();
		manageTime.dealModifiedTra();
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO: handle exception
		}
	}

	public void dealModifiedTra() {

		String sql = "select * from wh_modifiedtra where file_id<500 order by file_id,tra_id ";
		ResultSet rs = null;
		PreparedStatement pstm = null;
		try {
			ModifiedTra[] analyseNode = new ModifiedTra[2];
			ArrayList<LinkTras> tras = new ArrayList<LinkTras>();
			int traid = 0;

			PreparedStatement stm_traid = null;
			// 当进行另一段轨迹的停留提取时，sid等不是重新开始
			if (tras.size() == 0) {

				String sql_traid = "select * from (select max(tra_id) max from link_tras) where max is not null";
				stm_traid = conn.prepareStatement(sql_traid);
				ResultSet rs_traid = query(sql_traid, stm_traid);

				while (rs_traid.next()) {
					traid = rs_traid.getInt(1) + 1;

				}
				try {
					rs_traid.close();
				} catch (SQLException e) {
					// TODO: handle exception
				}
			}

			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			// rs=query(sql, pstm);

			while (rs.next()) {
				int fileid = rs.getInt("file_id");
				int nodeid = rs.getInt("tra_id");
				int linkid = rs.getInt("roadid");
				String time = rs.getString("time");
				// String oneway=rs.getString("bidirected");
				Struct struct = (Struct) rs.getObject("new");

				ModifiedTra node = new ModifiedTra();
				node.setFile_id(fileid);
				node.setNode_id(nodeid);
				node.setRoad_id(linkid);
				node.setTime(time);
				// node.setOneway(oneway);
				node.setStruct(struct);

				if (linkid == -1) {
					continue;
				}

				if (analyseNode[0] == null) {
					analyseNode[0] = node;
					continue;
				}

				if (analyseNode[1] == null) {
					analyseNode[1] = node;
				}

				if (analyseNode[0].getFile_id() == analyseNode[1].getFile_id()) {
					int timespan = subtractTime(analyseNode[0].getTime(),
							analyseNode[1].getTime());
					// 对于同一个link上的点，处理下一个点
					if (analyseNode[0].getRoad_id() == analyseNode[1]
							.getRoad_id()) {
						// 如果analyseNode[0]是这个link上的第一个投影点，根据这个点计算link的进入时间，并改变上一个link的离开时间
						if (!analyseNode[0].isPreNodeOnSameLink()) {

						}

						// 如果link上有多个点，且这个link是轨迹的第一个link，根据第一个和第二个先计算出一个entertime和leavetime，之后再修改leavetime
						if (tras.size() > 0 && timespan > 15 * 60) {
							traid++;
							analyseNode[0] = analyseNode[1];
							analyseNode[1] = null;
							continue;
						} else {
							dealLinkHasSomeTra(analyseNode, tras, traid);

						}
						/*
						 * if(tras.size()==0){ dealLinkHasSomeTra(analyseNode,
						 * tras, traid); }
						 * //新的轨迹，但是tras的最后一个link是相同的，则entertime相同
						 * ，leavetime重新计算一个临时值 else
						 * if(tras.size()>0&&timespan>15*60){ traid++;
						 * analyseNode[0]=analyseNode[1]; analyseNode[1]=null;
						 * continue; } else {
						 * if(tras.get(tras.size()-1).getLink_id
						 * ()==analyseNode[0].getRoad_id()){
						 * dealLinkHasSomeTra(analyseNode, tras, traid); } else
						 * { dealLinkHasSomeTra(analyseNode, tras, traid); } }
						 */
						// analyseNode[1].setPreNodeOnSameLink(true);
						analyseNode[0] = analyseNode[1];
						analyseNode[1] = null;
						continue;
					} else {
						// 时间差超过15min，作断开处理

						if (timespan > 15 * 60) {
							traid++;
							analyseNode[0] = analyseNode[1];
							analyseNode[1] = null;
							continue;
						} else {
							double sper = getPercentage(netName,
									analyseNode[0].getRoad_id(),
									analyseNode[0].getStruct(), conn);
							double eper = getPercentage(netName,
									analyseNode[1].getRoad_id(),
									analyseNode[1].getStruct(), conn);
							LogicalSubPath logicalSubPath = getsubPath(
									analyseNode[0].getRoad_id(), sper,
									analyseNode[1].getRoad_id(), eper,
									timespan, conn, netName);
							if (logicalSubPath != null) {
								if (dealLink(logicalSubPath, analyseNode, tras,
										fileid, traid, sper, eper)) {
									analyseNode[0] = analyseNode[1];
									analyseNode[1] = null;
								} else {
									System.out.println("计算轨迹时间等步骤出错！");
								}

							} else// 说明应该是速度限制导致不能匹配，记录下来，可能是因为oneway属性导致的
							{
								String error = "fileid:"
										+ analyseNode[0].getFile_id()
										+ ",nodeid:"
										+ analyseNode[0].getNode_id() + "有问题："
										+ analyseNode[0].getRoad_id() + "到"
										+ analyseNode[1].getRoad_id()
										+ "计算最短路径有问题";
								BufferedWriter out;
								FileWriter fw;
								try {
									out = new BufferedWriter(
											new OutputStreamWriter(
													new FileOutputStream(
															"D:/study/javast/DataProcessing/src/ndm/error_shortestpath",
															true)));
									// fw=new
									// FileWriter("D:/study/javast/DataProcessing/src/ndm/error_shortestpath");

									out.write(error);
									out.write("\n");

									out.close();
								} catch (IOException e) {
									System.out.println("最短路径问题写入失败！");
								}
								// System.out.println(analyseNode[0].getRoad_id()+"到"+analyseNode[1].getRoad_id()+"计算最短路径有问题");
								analyseNode[1] = null;
								continue;
							}
						}

					}

				}
				// 说明不是同一个轨迹
				else {
					traid++;
					analyseNode[0] = node;
					analyseNode[1] = null;
					if (traid % 300 == 0) {
						insertTras(tras);
						tras.clear();
					}
				}

			}
			insertTras(tras);
			System.out.println("插入完成！");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				pstm.close();
				conn.close();
			} catch (SQLException e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}

	}

	// 针对tras.size()=0且每一个轨迹的第一条link可能存在多个轨迹点的情形，先以前两个点计算出link的进入时间个离开时间
	// 或是处理tras.size()>0，且第一个link和前面的link不相同，计算后添加到tras中即可
	// 或是处理tras.size()>0，且第一个link和前面的link相同，进入时间以前一段轨迹的最后一个相同的link为主，再计算后添加到tras中即可
	private void dealLinkHasSomeTra(ModifiedTra[] analyseNode,
			ArrayList<LinkTras> tras, int traid) {
		LinkTras analyseLink = new LinkTras();
		double sper = getPercentage(netName, analyseNode[0].getRoad_id(),
				analyseNode[0].getStruct(), conn);
		double eper = getPercentage(netName, analyseNode[1].getRoad_id(),
				analyseNode[1].getStruct(), conn);
		String entertime = null;
		String leavetime = null;
		int pretime = 0;
		int posttime = 0;

		int seqid = 0;

		if (tras.size() == 0 || tras.get(tras.size() - 1).getTra_id() != traid) {
			seqid = 0;

		} else {
			// seqid接着上面的继续
			seqid = tras.get(tras.size() - 1).getSeq_id() + 1;
		}

		try {
			// 如果sper<eper，说明行驶方向和link的方向一致
			if (sper < eper) {
				pretime = (int) Math.round(sper
						* (subtractTime(analyseNode[0].getTime(),
								analyseNode[1].getTime()) / (eper - sper)));
				entertime = addTime(analyseNode[0].getTime(), -1 * pretime);
				posttime = (int) Math.round((1 - sper)
						* subtractTime(analyseNode[0].getTime(),
								analyseNode[1].getTime()) / (eper - sper));
				leavetime = addTime(analyseNode[0].getTime(), posttime);
				analyseLink.setConsistency("Y");
				analyseLink.setEntertime(entertime);
				analyseLink.setLeavetime(leavetime);
				analyseLink.setFile_id(analyseNode[0].getFile_id());
				analyseLink.setTra_id(traid);
				analyseLink.setSeq_id(seqid);
				analyseLink.setLink_id(analyseNode[0].getRoad_id());
				// 如果tras.size()==0，直接添加analyseLink
				if (tras.size() == 0) {

					tras.add(analyseLink);
				} else {
					if (tras.get(tras.size() - 1).getTra_id() == traid
							&& tras.get(tras.size() - 1).getLink_id() == analyseNode[0]
									.getRoad_id()) {
						if (tras.get(tras.size() - 1).getConsistency()
								.equals("N")
								|| tras.get(tras.size() - 1).getConsistency()
										.equals("DN")) {
							tras.get(tras.size() - 1).setConsistency("DY");
							// 更改link的离开时间
							tras.get(tras.size() - 1).setLeavetime(leavetime);
						}

					}
					// 如果是新的轨迹的第一个link，直接添加analyseLink
					else {

						tras.add(analyseLink);
					}
				}
			} else if (sper > eper) {
				pretime = (int) Math.round((1 - sper)
						* (subtractTime(analyseNode[0].getTime(),
								analyseNode[1].getTime()) / (sper - eper)));
				entertime = addTime(analyseNode[0].getTime(), -1 * pretime);
				posttime = (int) Math.round(sper
						* subtractTime(analyseNode[0].getTime(),
								analyseNode[1].getTime()) / (sper - eper));
				leavetime = addTime(analyseNode[0].getTime(), posttime);

				analyseLink.setConsistency("N");
				analyseLink.setEntertime(entertime);
				analyseLink.setLeavetime(leavetime);

				analyseLink.setFile_id(analyseNode[0].getFile_id());
				analyseLink.setTra_id(traid);
				analyseLink.setSeq_id(seqid);
				analyseLink.setLink_id(analyseNode[0].getRoad_id());
				// 如果tras.size()==0，直接添加analyseLink
				if (tras.size() == 0) {

					tras.add(analyseLink);
				} else {
					if (tras.get(tras.size() - 1).getTra_id() == traid
							&& tras.get(tras.size() - 1).getLink_id() == analyseNode[0]
									.getRoad_id()) {
						if (tras.get(tras.size() - 1).getConsistency()
								.equals("Y")
								|| tras.get(tras.size() - 1).getConsistency()
										.equals("DY")) {
							tras.get(tras.size() - 1).setConsistency("DN");
						}

					} else {
						tras.add(analyseLink);
					}
				}
			} else if (Math.abs(sper - eper) < 0.1) {
				// analyseNode[0]=analyseNode[1];
				// analyseNode[1]=null;
				return;
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println(tras.size());
		}

		/*
		 * //sper>eper说明逆向的 if(sper>eper){ pretime=(int)Math.round((1-sper)*
		 * (subtractTime(analyseNode[0].getTime(),
		 * analyseNode[1].getTime())/(sper-eper)));
		 * entertime=addTime(analyseNode[0].getTime(), -1*pretime);
		 * posttime=(int)Math.round(sper*subtractTime(analyseNode[0].getTime(),
		 * analyseNode[1].getTime())/(sper-eper));
		 * leavetime=addTime(analyseNode[0].getTime(), posttime);
		 * if(analyseLink.getConsistency()==null){
		 * analyseLink.setConsistency("N"); } else {
		 * if(analyseLink.getConsistency
		 * ().equals("Y")||analyseLink.getConsistency().equals("DY")){
		 * analyseLink.setConsistency("DN"); } }
		 * 
		 * } else { pretime=(int)Math.round(sper*
		 * (subtractTime(analyseNode[0].getTime(),
		 * analyseNode[1].getTime())/(eper-sper)));
		 * entertime=addTime(analyseNode[0].getTime(), -1*pretime);
		 * posttime=(int)Math.round((1-sper)*
		 * subtractTime(analyseNode[0].getTime(),
		 * analyseNode[1].getTime())/(eper-sper));
		 * leavetime=addTime(analyseNode[0].getTime(), posttime);
		 * if(analyseLink.getConsistency()==null){
		 * analyseLink.setConsistency("Y"); } else {
		 * if(analyseLink.getConsistency
		 * ().equals("N")||analyseLink.getConsistency().equals("DN")){
		 * analyseLink.setConsistency("DY"); } }
		 * //analyseLink.setConsistency("Y"); }
		 * 
		 * analyseLink.setFile_id(analyseNode[0].getFile_id());
		 * analyseLink.setTra_id(traid); analyseLink.setSeq_id(0);
		 * analyseLink.setLink_id(analyseNode[0].getRoad_id());
		 * if(tras.size()==0){ analyseLink.setEntertime(entertime);
		 * analyseLink.setLeavetime(leavetime); tras.add(analyseLink);
		 * 
		 * } //当tras.size()>0且最后一个link和新的轨迹的第一个link相同，
		 * 此时link的entertime用之前轨迹的最后一个轨迹的进入时间，离开时间重新计算 else
		 * if(tras.get(tras.size()-1).getLink_id()==analyseNode[0].getRoad_id())
		 * { analyseLink.setLeavetime(leavetime); }
		 * //说明前面有计算此link的进入时间，离开时间，只需改变离开时间即可 else {
		 * analyseLink.setEntertime(tras.get(tras.size()-1).getEntertime());
		 * analyseLink.setLeavetime(leavetime); tras.add(analyseLink); }
		 */
	}

	/**
	 * 处理两点之间最短路径上的每个link的entertime和leavetime
	 * 
	 * @param logicalSubPath
	 * @param analyseNode
	 *            用于分析的前后两个点
	 * @param tras
	 *            用于存储最终结果的arraylist
	 * @param fileid
	 * @param traid
	 * @param sper
	 * @param eper
	 * @return
	 */

	private boolean dealLink(LogicalSubPath logicalSubPath,
			ModifiedTra[] analyseNode, ArrayList<LinkTras> tras, int fileid,
			int traid, double sper, double eper) {
		double totallength = logicalSubPath.getCosts()[0];
		int timespan = subtractTime(analyseNode[0].getTime(),
				analyseNode[1].getTime());
		double speed = totallength / timespan;
		LogicalPath logicalPath = logicalSubPath.getReferencePath();
		long[] linkids = logicalPath.getLinkIds();

		int seqid = 0;

		if (tras.size() == 0 || tras.get(tras.size() - 1).getTra_id() != traid) {
			seqid = 0;

		} else {
			// seqid接着上面的继续
			seqid = tras.get(tras.size() - 1).getSeq_id() + 1;
		}
		String entertime = null;
		String leavetime = null;

		// 保存第二条link的entertime和leavetime
		String entertime_second = null;
		String leavetime_second = null;

		// 用于分析的links
		LinkTras[] analyseLinks = new LinkTras[3];

		StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < linkids.length; i++) {
			sb.append(String.valueOf(linkids[i]));
			sb2.append("'" + linkids[i] + "'" + "," + (i + 1));
			if (i != linkids.length - 1) {
				sb.append(",");
				sb2.append(",");
			}
		}
		String sql = "select * from links where link_id in (" + sb.toString()
				+ ")" + "order by decode(link_id," + sb2.toString() + ")";
		ResultSet rs = null;
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			// rs=query(sql, pstm);
			while (rs.next()) {
				int startNode = rs.getInt("start_node_id");
				int endNode = rs.getInt("end_node_id");
				String oneway = rs.getString("bidirected");
				int link_temp = rs.getInt("link_id");
				double length = rs.getDouble("street_length");

				double time_temp = length / speed;
				LinkTras analyseLink = new LinkTras();
				analyseLink.setLink_id(link_temp);
				analyseLink.setStartNode(startNode);
				analyseLink.setEndNode(endNode);
				analyseLink.setOneway(oneway);
				analyseLink.setLength(length);
				analyseLink.setFile_id(fileid);
				analyseLink.setTra_id(traid);
				analyseLink.setSeq_id(seqid++);

				// 个别情况下，前面一个link的oneway并没有被计算(在dealLinkHasSomeTra方法里面可能未处理此种情况)，此处修改
				if (tras.size() > 0
						&& tras.get(tras.size() - 1).getOneway() == null) {
					tras.get(tras.size() - 1).setOneway(oneway);
				}

				// 先处理前两个link
				if (analyseLinks[0] == null) {
					analyseLinks[0] = analyseLink;
					continue;
				}

				if (analyseLinks[1] == null) {
					analyseLinks[1] = analyseLink;

					// 开始处理前两个link
					// 下面判断第一个link的一致性
					if (analyseLinks[0].getEndNode() == analyseLinks[1]
							.getStartNode()
							|| analyseLinks[0].getEndNode() == analyseLinks[1]
									.getEndNode()) {
						analyseLinks[0].setConsistency("Y");
					} else if (analyseLinks[0].getStartNode() == analyseLinks[1]
							.getStartNode()
							|| analyseLinks[0].getStartNode() == analyseLinks[1]
									.getEndNode()) {
						analyseLinks[0].setConsistency("N");
					}

					// 对于第一个link，如果方向一致，则汽车实际行驶距离是1-sper，如果方向不一致，而汽车实际行驶距离是sper
					if (analyseLinks[0].getConsistency().equals("Y")) {

						int pretime = (int) Math.round(analyseLinks[0]
								.getLength() * sper / speed);
						int posttime = (int) Math.round(analyseLinks[0]
								.getLength() * (1 - sper) / speed);
						entertime = addTime(analyseNode[0].getTime(), -1
								* pretime);
						leavetime = addTime(analyseNode[0].getTime(), posttime);
						analyseLinks[0].setEntertime(entertime);
						analyseLinks[0].setLeavetime(leavetime);

						if (analyseLink.getStartNode() == analyseLinks[0]
								.getEndNode()) {
							analyseLinks[1].setConsistency("Y");
						} else {
							analyseLinks[1].setConsistency("N");
						}

						// 计算第二个link的时间，如果第二个link就是analyseNode[1]的link即通过eper计算link的离开时间
						if (analyseNode[1].getRoad_id() == link_temp) {
							if (analyseLinks[1].getConsistency().equals("Y")) {
								entertime_second = analyseLinks[0]
										.getLeavetime();
								int posttimespan = (int) Math
										.round(analyseLinks[1].getLength()
												* (1 - eper) / speed);
								leavetime_second = addTime(
										analyseNode[1].getTime(), posttimespan);

							} else {
								entertime_second = analyseLinks[0]
										.getLeavetime();
								int posttimespan = (int) Math
										.round(analyseLinks[1].getLength()
												* eper / speed);
								leavetime_second = addTime(
										analyseNode[1].getTime(), posttimespan);
							}
						}
						// 计算第二个link的时间
						else {
							entertime_second = analyseLinks[0].getLeavetime();
							int posttimespan = (int) Math.round(analyseLinks[1]
									.getLength() / speed);
							leavetime_second = addTime(entertime_second,
									posttimespan);
						}
						analyseLinks[1].setEntertime(entertime_second);
						analyseLinks[1].setLeavetime(leavetime_second);

					} else {
						int pretime = (int) Math.round(analyseLinks[0]
								.getLength() * (1 - sper) / speed);
						int posttime = (int) Math.round(analyseLinks[0]
								.getLength() * sper / speed);
						entertime = addTime(analyseNode[0].getTime(), -1
								* pretime);
						leavetime = addTime(analyseNode[0].getTime(), posttime);
						analyseLinks[0].setEntertime(entertime);
						analyseLinks[0].setLeavetime(leavetime);

						// 计算第二个link的一致性、进入和离开时间
						if (analyseLink.getStartNode() == analyseLinks[0]
								.getStartNode()) {
							analyseLinks[1].setConsistency("Y");
						} else {
							analyseLinks[1].setConsistency("N");
						}
						// 计算第二个link的时间，如果第二个link就是analyseNode[1]的link即通过eper计算link的离开时间
						if (analyseNode[1].getRoad_id() == link_temp) {
							if (analyseLinks[1].getConsistency().equals("Y")) {
								entertime_second = analyseLinks[0]
										.getLeavetime();
								int posttimespan = (int) Math
										.round(analyseLinks[1].getLength()
												* (1 - eper) / speed);
								leavetime_second = addTime(
										analyseNode[1].getTime(), posttimespan);

							} else {
								entertime_second = analyseLinks[0]
										.getLeavetime();
								int posttimespan = (int) Math
										.round(analyseLinks[1].getLength()
												* eper / speed);
								leavetime_second = addTime(
										analyseNode[1].getTime(), posttimespan);
							}
						}
						// 计算第二个link的时间
						else {
							entertime_second = analyseLinks[0].getLeavetime();
							int posttimespan = (int) Math.round(analyseLinks[1]
									.getLength() / speed);
							leavetime_second = addTime(entertime_second,
									posttimespan);
						}
						analyseLinks[1].setEntertime(entertime_second);
						analyseLinks[1].setLeavetime(leavetime_second);
					}

					// 如果tras.size()>0且tras中最后一个link和analyseLinks[0]相同，且属于同一个轨迹即轨迹没分段
					if (tras.size() > 0
							&& tras.get(tras.size() - 1).getLink_id() == analyseLinks[0]
									.getLink_id()
							&& tras.get(tras.size() - 1).getTra_id() == traid) {
						LinkTras tempLinkTras = tras.get(tras.size() - 1);
						// 如果tras中最后一个link的一致性和analyseLinks[0]记录的不相同
						if (!tempLinkTras.getConsistency().equals(
								analyseLinks[0].getConsistency())) {
							if (tempLinkTras.getConsistency().equals("Y")
									|| tempLinkTras.getConsistency().equals(
											"DY")) {
								if (analyseLinks[0].getConsistency()
										.equals("N")) {
									tras.get(tras.size() - 1).setConsistency(
											"DN");
								}

							} else {
								if (analyseLinks[0].getConsistency()
										.equals("Y")) {
									tras.get(tras.size() - 1).setConsistency(
											"DY");
								}
							}

						}
						// 需要更改tras中的最后一个link的离开时间
						tras.get(tras.size() - 1).setLeavetime(
								analyseLinks[0].getLeavetime());
						// 还需更改第二个link的seqid及当前seqid
						analyseLinks[1]
								.setSeq_id(analyseLinks[1].getSeq_id() - 1);
						seqid--;
					} else {
						tras.add(analyseLinks[0]);
					}
					tras.add(analyseLinks[1]);

				}

				// 路段上第三条link开始处理如下
				else {
					// 判断当前link的一致性

					if (tras.get(tras.size() - 1).getConsistency().equals("Y")) {
						if (tras.get(tras.size() - 1).getEndNode() == analyseLink
								.getStartNode()) {
							analyseLink.setConsistency("Y");
						} else if (tras.get(tras.size() - 1).getEndNode() == analyseLink
								.getEndNode()) {
							analyseLink.setConsistency("N");
						}
						// 如果前两个link形成了环，可能出现计算一致性异常
						else if (tras.get(tras.size() - 1).getStartNode() == analyseLink
								.getStartNode()) {
							logger.debug(tras.get(tras.size() - 1).getFile_id()
									+ ","
									+ tras.get(tras.size() - 1).getEntertime()
									+ ","
									+ tras.get(tras.size() - 1).getLeavetime());
							analyseLink.setConsistency("Y");
							//因为有环，所以之前计算的一致性出现问题，现修正
							tras.get(tras.size() - 1).setConsistency("N");
						} else if (tras.get(tras.size() - 1).getStartNode() == analyseLink
								.getEndNode()) {
							logger.debug(tras.get(tras.size() - 1).getFile_id()
									+ ","
									+ tras.get(tras.size() - 1).getEntertime()
									+ ","
									+ tras.get(tras.size() - 1).getLeavetime());
							analyseLink.setConsistency("N");
							//因为有环，所以之前计算的一致性出现问题，现修正
							tras.get(tras.size() - 1).setConsistency("N");
						}
						/*
						 * else {
						 * //用N表示在此link上有调头，实际计算还是跟不一致（N）一样,第一次行驶方向一致，第二次行驶方向相反
						 * //analyseLink.setConsistency("DN");
						 * tras.get(tras.size()-1).setConsistency("DN");
						 * if(tras.
						 * get(tras.size()-1).getStartNode()==analyseLink
						 * .getStartNode()){ analyseLink.setConsistency("Y"); }
						 * else
						 * if(tras.get(tras.size()-1).getStartNode()==analyseLink
						 * .getEndNode()) { analyseLink.setConsistency("N"); } }
						 */
					} else if (tras.get(tras.size() - 1).getConsistency()
							.equals("N")) {
						if (tras.get(tras.size() - 1).getStartNode() == analyseLink
								.getStartNode()) {
							analyseLink.setConsistency("Y");
						} else if (tras.get(tras.size() - 1).getStartNode() == analyseLink
								.getEndNode()) {
							analyseLink.setConsistency("N");
						}
						// 如果前两个link形成了环，可能出现计算一致性异常
						else if (tras.get(tras.size() - 1).getEndNode() == analyseLink
								.getStartNode()) {
							logger.debug(tras.get(tras.size() - 1).getFile_id()
									+ ","
									+ tras.get(tras.size() - 1).getEntertime()
									+ ","
									+ tras.get(tras.size() - 1).getLeavetime());
							analyseLink.setConsistency("Y");
							//因为有环，所以之前计算的一致性出现问题，现修正
							tras.get(tras.size() - 1).setConsistency("Y");
						} else if (tras.get(tras.size() - 1).getEndNode() == analyseLink
								.getEndNode()) {
							logger.debug(tras.get(tras.size() - 1).getFile_id()
									+ ","
									+ tras.get(tras.size() - 1).getEntertime()
									+ ","
									+ tras.get(tras.size() - 1).getLeavetime());
							analyseLink.setConsistency("N");
							//因为有环，所以之前计算的一致性出现问题，现修正
							tras.get(tras.size() - 1).setConsistency("Y");
						}
						/*
						 * else {
						 * //用D表示在此link上有调头，实际计算还是跟一致（Y）一样，第一次行驶方向相反，第二次行驶方向一致
						 * //analyseLink.setConsistency("DY");
						 * tras.get(tras.size()-1).setConsistency("DY");
						 * if(tras.
						 * get(tras.size()-1).getEndNode()==analyseLink.getStartNode
						 * ()){ analyseLink.setConsistency("Y"); } else
						 * if(tras.get
						 * (tras.size()-1).getEndNode()==analyseLink.getEndNode
						 * ()) { analyseLink.setConsistency("N"); } }
						 */
					}

					int time = (int) Math
							.round(analyseLink.getLength() / speed);
					entertime = tras.get(tras.size() - 1).getLeavetime();
					leavetime = addTime(entertime, time);
					analyseLink.setEntertime(entertime);
					analyseLink.setLeavetime(leavetime);

					tras.add(analyseLink);
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("处理两个不同link上的点经过link的时间错误！");
			return false;
		} finally {
			try {
				rs.close();
				pstm.close();
			} catch (SQLException e2) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 处理两点之间最短路径上的每个link的entertime和leavetime
	 * 
	 * @param logicalSubPath
	 * @param analyseNode
	 *            用于分析的前后两个点
	 * @param tras
	 *            用于存储最终结果的arraylist
	 * @param fileid
	 * @param traid
	 * @param sper
	 * @param eper
	 * @return
	 */
	/*
	 * private boolean dealLink(LogicalSubPath logicalSubPath,ModifiedTra[]
	 * analyseNode, ArrayList<LinkTras> tras,int fileid,int traid,double
	 * sper,double eper){ double totallength=logicalSubPath.getCosts()[0]; int
	 * timespan=subtractTime(analyseNode[0].getTime(),
	 * analyseNode[1].getTime()); double speed=totallength/timespan; LogicalPath
	 * logicalPath=logicalSubPath.getReferencePath(); long[]
	 * linkids=logicalPath.getLinkIds();
	 * 
	 * int seqid=0;
	 * 
	 * if(tras.size()==0||tras.get(tras.size()-1).getTra_id()!=traid){ seqid=0;
	 * 
	 * } else { //seqid接着上面的继续 seqid=tras.get(tras.size()-1).getSeq_id()+1; }
	 * String entertime=null; String leavetime=null;
	 * 
	 * //保存第二条link的entertime和leavetime String entertime_second=null; String
	 * leavetime_second=null;
	 * 
	 * //用于分析的links LinkTras[] analyseLinks=new LinkTras[3];
	 * 
	 * StringBuffer sb = new StringBuffer(); StringBuffer sb2 = new
	 * StringBuffer(); for (int i = 0; i < linkids.length; i++) {
	 * sb.append(String.valueOf(linkids[i]));
	 * sb2.append("'"+linkids[i]+"'"+","+(i+1)); if (i != linkids.length-1) {
	 * sb.append(","); sb2.append(","); } } String sql =
	 * "select * from links where link_id in ("+sb.toString()+")" +
	 * "order by decode(link_id,"+sb2.toString()+")"; ResultSet rs=null;
	 * PreparedStatement pstm=null; try{ pstm=conn.prepareStatement(sql);
	 * rs=pstm.executeQuery(); //rs=query(sql, pstm); while(rs.next()){ int
	 * startNode=rs.getInt("start_node_id"); int
	 * endNode=rs.getInt("end_node_id"); String
	 * oneway=rs.getString("bidirected"); int link_temp = rs.getInt("link_id");
	 * double length = rs.getDouble("street_length");
	 * 
	 * double time_temp = length/speed; LinkTras analyseLink=new LinkTras();
	 * analyseLink.setLink_id(link_temp); analyseLink.setStartNode(startNode);
	 * analyseLink.setEndNode(endNode); analyseLink.setOneway(oneway);
	 * analyseLink.setLength(length); analyseLink.setFile_id(fileid);
	 * analyseLink.setTra_id(traid);
	 * 
	 * 
	 * 
	 * //如果满足下面条件，说明这是一个断开后的新的一段轨迹，从第一条link开始判断方向
	 * //先判断前两个link的一致性，然后根据一致性计算link的entertime和leavetime
	 * if(tras.size()==0||(tras
	 * .size()>0&&traid!=tras.get(tras.size()-1).getTra_id())
	 * ||tras.get(tras.size()-1).getConsistency().equals("DY")
	 * ||tras.get(tras.size()-1).getConsistency().equals("DN")){
	 * 
	 * if(tras.size()==0||(tras.size()>0&&traid!=tras.get(tras.size()-1).getTra_id
	 * ())){ if(analyseLinks[0]==null){ seqid=0; }
	 * 
	 * } if(analyseLinks[0]==null){
	 * 
	 * 
	 * 
	 * // if(tras.size()>0&&tras.get(tras.size()-1).getLink_id()==analyseLink.
	 * getLink_id()){ // analyseLink.setSeq_id(seqid); // } // else { //
	 * analyseLink.setSeq_id(seqid++); // }
	 * 
	 * analyseLink.setSeq_id(seqid++); analyseLinks[0]=analyseLink;
	 * 
	 * continue; } if(analyseLinks[1]==null){ analyseLink.setSeq_id(seqid++);
	 * analyseLinks[1]=analyseLink; //下面判断第一个link的一致性
	 * if(analyseLinks[0].getEndNode()==analyseLinks[1].getStartNode()||
	 * analyseLinks[0].getEndNode()==analyseLinks[1].getEndNode()){
	 * analyseLinks[0].setConsistency("Y"); } else
	 * if(analyseLinks[0].getStartNode()==analyseLinks[1].getStartNode()||
	 * analyseLinks[0].getStartNode()==analyseLinks[1].getEndNode()) {
	 * analyseLinks[0].setConsistency("N"); }
	 * 
	 * } //开始计算link的entertime和leavetime
	 * 
	 * 
	 * 
	 * //对于第一个link，如果方向一致，则汽车实际行驶距离是1-sper，如果方向不一致，而汽车实际行驶距离是sper
	 * if(analyseLinks[0].getConsistency().equals("Y")){
	 * 
	 * int pretime=(int)Math.round(analyseLinks[0].getLength()*sper/speed); int
	 * posttime=(int)Math.round(analyseLinks[0].getLength()*(1-sper)/speed);
	 * entertime=addTime(analyseNode[0].getTime(), -1*pretime);
	 * leavetime=addTime(analyseNode[0].getTime(), posttime);
	 * analyseLinks[0].setEntertime(entertime);
	 * analyseLinks[0].setLeavetime(leavetime);
	 * 
	 * if(analyseLink.getStartNode()==analyseLinks[0].getEndNode()){
	 * analyseLinks[1].setConsistency("Y"); } else {
	 * analyseLinks[1].setConsistency("N"); }
	 * 
	 * //计算第二个link的时间，如果第二个link就是analyseNode[1]的link即通过eper计算link的离开时间
	 * if(analyseNode[1].getRoad_id()==link_temp){
	 * if(analyseLinks[1].getConsistency().equals("Y")){
	 * entertime_second=analyseLinks[0].getLeavetime(); int
	 * posttimespan=(int)Math.round(analyseLinks[1].getLength()*(1-eper)/speed);
	 * leavetime_second=addTime(analyseNode[1].getTime(), posttimespan);
	 * 
	 * 
	 * } else { entertime_second=analyseLinks[0].getLeavetime(); int
	 * posttimespan=(int)Math.round(analyseLinks[1].getLength()*eper/speed);
	 * leavetime_second=addTime(analyseNode[1].getTime(), posttimespan); } }
	 * //计算第二个link的时间 else { entertime_second=analyseLinks[0].getLeavetime();
	 * int posttimespan=(int)Math.round(analyseLinks[1].getLength()/speed);
	 * leavetime_second=addTime(entertime_second, posttimespan); }
	 * analyseLinks[1].setEntertime(entertime_second);
	 * analyseLinks[1].setLeavetime(leavetime_second);
	 * 
	 * } else{ int
	 * pretime=(int)Math.round(analyseLinks[0].getLength()*(1-sper)/speed); int
	 * posttime=(int)Math.round(analyseLinks[0].getLength()*sper/speed);
	 * entertime=addTime(analyseNode[0].getTime(), -1*pretime);
	 * leavetime=addTime(analyseNode[0].getTime(), posttime);
	 * analyseLinks[0].setEntertime(entertime);
	 * analyseLinks[0].setLeavetime(leavetime);
	 * 
	 * //计算第二个link的一致性、进入和离开时间
	 * if(analyseLink.getStartNode()==analyseLinks[0].getStartNode()){
	 * analyseLinks[1].setConsistency("Y"); } else {
	 * analyseLinks[1].setConsistency("N"); }
	 * //计算第二个link的时间，如果第二个link就是analyseNode[1]的link即通过eper计算link的离开时间
	 * if(analyseNode[1].getRoad_id()==link_temp){
	 * if(analyseLinks[1].getConsistency().equals("Y")){
	 * entertime_second=analyseLinks[0].getLeavetime(); int
	 * posttimespan=(int)Math.round(analyseLinks[1].getLength()*(1-eper)/speed);
	 * leavetime_second=addTime(analyseNode[1].getTime(), posttimespan);
	 * 
	 * 
	 * } else { entertime_second=analyseLinks[0].getLeavetime(); int
	 * posttimespan=(int)Math.round(analyseLinks[1].getLength()*eper/speed);
	 * leavetime_second=addTime(analyseNode[1].getTime(), posttimespan); } }
	 * //计算第二个link的时间 else { entertime_second=analyseLinks[0].getLeavetime();
	 * int posttimespan=(int)Math.round(analyseLinks[1].getLength()/speed);
	 * leavetime_second=addTime(entertime_second, posttimespan); }
	 * analyseLinks[1].setEntertime(entertime_second);
	 * analyseLinks[1].setLeavetime(leavetime_second); }
	 * 
	 * //对于之前有记录的link，更改离开时间即可 //
	 * if(tras.size()>0&&tras.get(tras.size()-1).getLink_id
	 * ()==analyseLinks[0].getLink_id()){ //
	 * tras.get(tras.size()-1).setLeavetime(analyseLinks[0].getLeavetime()); //
	 * } // else { // tras.add(analyseLinks[0]); // //seqid++; // }
	 * 
	 * if(tras.size()>0&&tras.get(tras.size()-1).getLink_id()==analyseLinks[0].
	 * getLink_id()){
	 * //tras.get(tras.size()-1).setLeavetime(analyseLinks[1].getLeavetime());
	 * analyseLinks[0].setEntertime(tras.get(tras.size()-1).getEntertime());
	 * //tras.add(analyseLinks[0]); //seqid++; }
	 * if((tras.size()>0&&traid==tras.get(tras.size()-1).getTra_id())
	 * &&(tras.get(tras.size()-1).getConsistency().equals("DY")
	 * ||tras.get(tras.size()-1).getConsistency().equals("DN"))){
	 * tras.get(tras.size()-1).setLeavetime(analyseLinks[0].getLeavetime());
	 * analyseLinks[1].setSeq_id(analyseLinks[1].getSeq_id()-1); seqid--; } else
	 * { tras.add(analyseLinks[0]); }
	 * 
	 * tras.add(analyseLinks[1]); continue; }
	 * //常规情况下，根据tras中记录的最后一条link的一致性判断当前link的一致性
	 * //要不要考虑当traid不相等时，linkid相等的情况，好像在考虑linkid相等的时候已考虑
	 * //考虑tras中存储的最后一个link的一致性是否正确？ else
	 * if(tras.size()>0&&traid==tras.get(tras.size()-1).getTra_id()
	 * &&(tras.get(tras
	 * .size()-1).getConsistency().equals("Y")||tras.get(tras.size
	 * ()-1).getConsistency().equals("N"))){ //seqid接着上面的继续
	 * //seqid=tras.get(tras.size()-1).getSeq_id()+1;
	 * //analyseLink.setSeq_id(seqid++); int pretime=0; int posttime=0;
	 * 
	 * //如果是第一个link,需要更改之前记录的
	 * if(analyseNode[0].getRoad_id()==analyseLink.getLink_id()){
	 * if(tras.get(tras.size()-1).getLink_id()==analyseNode[0].getRoad_id()){
	 * //因为在处理同一个link上存在多个轨迹的情况中，未存储startnode，endnode信息，现在处理
	 * tras.get(tras.size()-1).setStartNode(startNode);
	 * tras.get(tras.size()-1).setEndNode(endNode); //行驶方向一致的情况
	 * //if(tras.get(tras.size()-1).getConsistency().equals("Y")|| //
	 * tras.get(tras.size()-1).getConsistency().equals("DY")){
	 * if(tras.get(tras.size()-1).getConsistency().equals("Y")){
	 * posttime=(int)Math.round(analyseLink.getLength()*(1-sper)/speed);
	 * leavetime=addTime(analyseNode[0].getTime(), posttime);
	 * tras.get(tras.size()-1).setLeavetime(leavetime); } else
	 * if(tras.get(tras.size()-1).getConsistency().equals("N")) {
	 * posttime=(int)Math.round(analyseLink.getLength()*sper/speed);
	 * leavetime=addTime(analyseNode[0].getTime(), posttime);
	 * tras.get(tras.size()-1).setLeavetime(leavetime); } } else {
	 * System.out.println
	 * ("第一个点的linkid和tras中的最后一个linkid不相等，traid，seqid，linkid分别是"
	 * +tras.get(tras.size
	 * ()-1).getTra_id()+","+tras.get(tras.size()-1).getSeq_id()
	 * +","+tras.get(tras.get(tras.size()-1).getLink_id())); }
	 * 
	 * //个别情况下，前面一个link的oneway并没有被计算，此处修改
	 * if(tras.get(tras.size()-1).getOneway()==null){
	 * tras.get(tras.size()-1).setOneway(oneway); } continue; }
	 * 
	 * analyseLink.setSeq_id(seqid++); //判断当前link的一致性
	 * if(tras.get(tras.size()-1).
	 * getConsistency().equals("Y")||tras.get(tras.size
	 * ()-1).getConsistency().equals("DY")){
	 * if(tras.get(tras.size()-1).getEndNode()==analyseLink.getStartNode()){
	 * analyseLink.setConsistency("Y"); } else
	 * if(tras.get(tras.size()-1).getEndNode()==analyseLink.getEndNode()){
	 * analyseLink.setConsistency("N"); } else {
	 * //用N表示在此link上有调头，实际计算还是跟不一致（N）一样,第一次行驶方向一致，第二次行驶方向相反
	 * //analyseLink.setConsistency("DN");
	 * tras.get(tras.size()-1).setConsistency("DN");
	 * if(tras.get(tras.size()-1).getStartNode()==analyseLink.getStartNode()){
	 * analyseLink.setConsistency("Y"); } else
	 * if(tras.get(tras.size()-1).getStartNode()==analyseLink.getEndNode()) {
	 * analyseLink.setConsistency("N"); } } } else
	 * if(tras.get(tras.size()-1).getConsistency
	 * ().equals("N")||tras.get(tras.size()-1).getConsistency().equals("DN")) {
	 * if(tras.get(tras.size()-1).getStartNode()==analyseLink.getStartNode()){
	 * analyseLink.setConsistency("Y"); } else
	 * if(tras.get(tras.size()-1).getStartNode()==analyseLink.getEndNode()){
	 * analyseLink.setConsistency("N"); } else {
	 * //用D表示在此link上有调头，实际计算还是跟一致（Y）一样，第一次行驶方向相反，第二次行驶方向一致
	 * //analyseLink.setConsistency("DY");
	 * tras.get(tras.size()-1).setConsistency("DY");
	 * if(tras.get(tras.size()-1).getEndNode()==analyseLink.getStartNode()){
	 * analyseLink.setConsistency("Y"); } else
	 * if(tras.get(tras.size()-1).getEndNode()==analyseLink.getEndNode()) {
	 * analyseLink.setConsistency("N"); } } } //考虑“DY” “DN”的情形
	 * 
	 * 
	 * //如果是最后一个link if(analyseNode[1].getRoad_id()==analyseLink.getLink_id()){
	 * 
	 * if(analyseLink.getConsistency().equals("Y")){
	 * pretime=(int)Math.round(analyseLink.getLength()*eper/speed);
	 * posttime=(int)Math.round(analyseLink.getLength()*(1-eper)/speed);
	 * //entertime用此方法还是重新计算值得思考
	 * entertime=tras.get(tras.size()-1).getLeavetime();
	 * leavetime=addTime(analyseNode[1].getTime(), posttime);
	 * analyseLink.setEntertime(entertime); analyseLink.setLeavetime(leavetime);
	 * //analyseLink.setSeq_id(seqid++); } else
	 * if(analyseLink.getConsistency().equals("N")) {
	 * pretime=(int)Math.round(analyseLink.getLength()*(1-eper)/speed);
	 * posttime=(int)Math.round(analyseLink.getLength()*eper/speed);
	 * entertime=tras.get(tras.size()-1).getLeavetime();
	 * leavetime=addTime(analyseNode[1].getTime(), posttime);
	 * analyseLink.setEntertime(entertime); analyseLink.setLeavetime(leavetime);
	 * //analyseLink.setSeq_id(seqid++); } //掉头的情况，还没考虑 } //不是最后一个link else {
	 * int time=(int)Math.round(analyseLink.getLength()/speed);
	 * entertime=tras.get(tras.size()-1).getLeavetime();
	 * leavetime=addTime(entertime, time); analyseLink.setEntertime(entertime);
	 * analyseLink.setLeavetime(leavetime); //analyseLink.setSeq_id(seqid++); }
	 * tras.add(analyseLink); }
	 * 
	 * } return true; } catch(SQLException e){
	 * 
	 * return false; } finally{ try { rs.close(); pstm.close(); } catch
	 * (Exception e2) { // TODO: handle exception } }
	 * 
	 * 
	 * }
	 */

	/**
	 * 返回查询sql语句的结果集
	 * 
	 * @param sql
	 * @param pst
	 * @return 返回查询sql语句的结果集
	 */
	private ResultSet query(String sql, PreparedStatement pst) {
		// PreparedStatement pst=null;
		ResultSet rs = null;
		try {
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("执行" + sql + "语句出错！");
			rs.close();
			pst.close();
		} finally {
			/*
			 * try { pst.close(); } catch (Exception e2) { // TODO: handle
			 * exception }
			 */
			return rs;
		}
	}

	/**
	 * @param tras
	 *            轨迹数据
	 */
	private void insertTras(ArrayList<LinkTras> tras) {
		String sql_insert = "insert into link_tras(link_id,tra_id,seq_id,entertime,leavetime,bidirected,"
				+ "file_id,consistency) values(?,?,?,?,?,?,?,?)";
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql_insert);
			for (int i = 0; i < tras.size(); i++) {
				ps.setInt(1, tras.get(i).getLink_id());
				ps.setInt(2, tras.get(i).getTra_id());
				ps.setInt(3, tras.get(i).getSeq_id());
				ps.setString(4, tras.get(i).getEntertime());
				ps.setString(5, tras.get(i).getLeavetime());
				ps.setString(6, tras.get(i).getOneway());
				ps.setInt(7, tras.get(i).getFile_id());
				ps.setString(8, tras.get(i).getConsistency());
				ps.addBatch();
				if ((i + 1) % 10000 == 0) {
					ps.executeBatch();
				}
				ps.executeBatch();
				/*
				 * try{
				 * 
				 * } catch(Exception e1){ System.out.println(i); }
				 */

			}
			ps.executeBatch();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("插入时回滚失败！");
			}
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
	 * @param origin_time
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

	/**
	 * @param slinkid
	 *            起点所匹配到的link id
	 * @param sper
	 *            起点在link上所占的比例
	 * @param elinkid
	 *            终点所匹配到的link id
	 * @param eper
	 *            终点在link上所占的比例
	 * @param timespan
	 *            两点之间的时间差
	 * @param conn
	 *            数据库连接对象
	 * @param netName
	 *            路网名称
	 * @return 起点到终点的最短路径
	 */
	private LogicalSubPath getsubPath(long slinkid, double sper, long elinkid,
			double eper, int timespan, Connection conn, String netName) {
		try {
			NetworkIO networkIO = LODNetworkManager.getCachedNetworkIO(conn,
					netName, netName, null);
			NetworkAnalyst networkAnalyst = LODNetworkManager
					.getNetworkAnalyst(networkIO);
			LogicalSubPath path = networkAnalyst.shortestPathDijkstra(
					new PointOnNet(slinkid, sper),
					new PointOnNet(elinkid, eper), new LengthConstraint(
							timespan * 33.33));
			return path;
		} catch (LODNetworkException e) {
			e.printStackTrace();
			System.out.println(slinkid + "," + elinkid);
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
	 * @param conn
	 *            数据库连接对象
	 * @return 从起点到pt所占的百分比
	 */
	private double getPercentage(String networkname, int link_id, Struct pt,
			Connection conn) {
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
}

class LengthConstraint implements LODNetworkConstraint {
	private double cost;

	public LengthConstraint(double cost) {
		this.cost = cost;
	}

	@Override
	public int getNumberOfUserObjects() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getUserDataCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCurrentNodePartiallyExpanded(LODAnalysisInfo arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfied(LODAnalysisInfo info) {
		double[] costs = info.getCurrentCosts();
		if (costs[0] > cost) {
			return false;
		}
		return true;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkAnalyst(NetworkAnalyst arg0) {
		// TODO Auto-generated method stub

	}

}
