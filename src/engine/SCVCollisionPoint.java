package engine;

/**
 * 
 * @author 86158
 * Stick��vertex����ײ�㴢����
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
	
	/** ��ȡ��ײʱ����ķ����ٶȴ�С **/
	public double getNormalN()
	{
		return Math.abs(vn.getY());
	}
	/** ��ȡ��ײʱ����ķ����ٶ����� **/
	public Vector2D getNormalV()
	{
		Vector2D re=VerletUtil.getCoordinateOn(vn, oriAxisX);
		return re;
	}
	
	/** ��ȡ������ײ��stick **/
	public StickConstraint getStickConstraint()
	{
		return sc;
	}
	
	/** ��ȡ������ײ��vertex **/
	public Vertex getVertex()
	{
		return v;
	}
	
	/** ��������Ӧ��ײ�����Ż�,�ж�vertex��stick�Ƿ����������ײ��vertex���˶������˰�ȫ���룩 **/
	public boolean isDeepCollision() 
	{
		return isDeepC;
	}
}
