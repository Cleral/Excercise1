package ndm.domain;

import java.sql.Struct;

import oracle.spatial.geometry.JGeometry;

public class ClosestPoint {
	public JGeometry geo = null;
	public double dist= 0;
	public Struct struct = null;

	public ClosestPoint(JGeometry geo,double dist,Struct struct) 
	{
		this.geo = geo;
		this.dist = dist;
		this.struct = struct;
	}
}
