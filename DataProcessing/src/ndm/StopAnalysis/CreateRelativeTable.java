package ndm.StopAnalysis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import ndm.*;

public class CreateRelativeTable {
	public static void main(String[] args) {
		CreateRelativeTable create = new CreateRelativeTable();
		Connection conn = DataConnection.GetConnection();

		String sql_trjstop = "create table trj_stop(tid number,sid number,linkid number,posit number,"
				+ "begintime varchar2(48),endtime varchar2(48), constraint trj_stop_pk PRIMARY KEY(tid,sid,linkid))";

		int result_trjstop = create.executeUpdate(conn, sql_trjstop);
		if (result_trjstop == -1) {
			System.out.println("trj_stop创建失败！");
			return;
		}
		String sql_trjmove = "create table trj_move(tid number,mid number,segid number,linkid number,posit number,"
				+ "entertime varchar2(48),leavetime varchar2(48), constraint trj_move_pk PRIMARY KEY(tid,mid,segid))";

		int result_trjmove = create.executeUpdate(conn, sql_trjmove);
		if (result_trjmove == -1) {
			System.out.println("trj_move创建失败！");
			return;
		}

		//String sql_move = "create table move(mid number,tid number,sid1 number,linkid number,sid2 number,"
		//		+ " constraint move_pk PRIMARY KEY(mid,tid))";
		String sql_move = "create table move(mid number,tid number,sid1 number,sid2 number,"
				+ " constraint move_pk PRIMARY KEY(mid,tid))";

		int result_move = create.executeUpdate(conn, sql_move);
		if (result_move == -1) {
			System.out.println("move创建失败！");
			return;
		}

		String sql_pass = "create table pass(mid number,tid number,seqid number,poiid number,featurelayer_id number,"
				+ "time varchar2(48),description varchar2(30), constraint pass_pk PRIMARY KEY(mid,tid,seqid))";

		int result_pass = create.executeUpdate(conn, sql_pass);
		if (result_pass == -1) {
			System.out.println("pass创建失败！");
			return;
		}

		String sql_stop = "create table stop(sid number,tid number,begintime varchar2(48),endtime varchar2(48),"
				+ " constraint stop_pk PRIMARY KEY(sid,tid))";

		int result_stop = create.executeUpdate(conn, sql_stop);
		if (result_stop == -1) {
			System.out.println("stop创建失败！");
			return;
		}

		String sql_stay = "create table stay(sid number,tid number,poiid number,featurelayer_id number,"
				+ "description varchar2(30), constraint stay_pk PRIMARY KEY(sid,tid,poiid,featurelayer_id))";

		int result_stay = create.executeUpdate(conn, sql_stay);
		if (result_stay == -1) {
			System.out.println("stay创建失败！");
			return;
		}

		String sql_close = "create table close(sid number,tid number,poiid number,featurelayer_id number,"
				+ "description varchar2(30), constraint close_pk PRIMARY KEY(sid,tid,poiid,featurelayer_id))";

		int result_close = create.executeUpdate(conn, sql_close);
		if (result_close == -1) {
			System.out.println("close创建失败！");
			return;
		}

	}

	private int executeUpdate(Connection conn, String sql) {
		try {
			Statement stm = conn.createStatement();
			int result = stm.executeUpdate(sql);
			return result;
		} catch (SQLException e) {
			System.out.println("执行：" + sql + "语句错误");
			e.printStackTrace();
			return -1;
		}

	}
}
