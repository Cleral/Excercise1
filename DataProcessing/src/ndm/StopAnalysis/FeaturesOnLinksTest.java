package ndm.StopAnalysis;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import ndm.DataConnection;

public class FeaturesOnLinksTest {

	public static void main(String[] args) {
		Connection conn = DataConnection.GetConnection();
		int[] a = { 10000, 27857, 26887, 26885, 26884, 26883, 23027 };
		for (int i = 0; i < a.length; i++) {
			List<Feature> features = getFeatursOnLink(conn, a[i]);
			System.out.println(a[i] + ":" + features.size());

		}

	}

	private static List<Feature> getFeatursOnLink(Connection conn, int linkid) {
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

	private static Feature getFeatureInfo(Connection conn, int featureid,
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
}
