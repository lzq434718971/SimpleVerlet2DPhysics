package engine;

/**
 * 
 * @author 86158
 * Stick和vertex的碰撞点储存类
 *
 */
public class SCVCollisionPoint 
{
	private Vector2D vn,vr;
	private Vector2D inter;
	private StickConstraint sc;
	private Vertex v;
	private Vector2D oriAxisX;
	private boolean isDeepC;
	
	public SCVCollisionPoint(Vertex v,StickConstraint sc,Vector2D ox,Vector2D vn,Vector2D vr,Vector2D inter,boolean isDeep)
	{
		this.v=v;
		this.sc=sc;
		this.oriAxisX=ox;
		this.vn=vn;
		this.vr=vr;
		this.inter=inter;
		this.isDeepC=isDeep;
	}
	
	/** 获取碰撞时顶点的法向速度大小 **/
	public double getNormalN()
	{
		return Math.abs(vn.getY());
	}
	/** 获取碰撞时顶点的法向速度向量 **/
	public Vector2D getNormalV()
	{
		Vector2D re=VerletUtil.getCoordinateOn(vn, oriAxisX);
		return re;
	}
	
	/** 获取发生碰撞的stick **/
	public StickConstraint getStickConstraint()
	{
		return sc;
	}
	
	/** 获取发生碰撞的vertex **/
	public Vertex getVertex()
	{
		return v;
	}
	
	/** 用于自适应碰撞次数优化,判断vertex与stick是否发生了深度碰撞（vertex的运动超过了安全距离） **/
	public boolean isDeepCollision() 
	{
		return isDeepC;
	}
}
