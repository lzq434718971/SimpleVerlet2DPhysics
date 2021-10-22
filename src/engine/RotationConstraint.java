package engine;

/**
 * 
 * @author 86158
 * !暂时弃用!利用constraint对顶点进行旋转操作会造成一些模拟问题
 * 
 */
public class RotationConstraint extends StickConstraint
{
	private double _iPhase;
	private double _cPhase;
	private double _radius;
	private double _w;
	
	/** 半径，作为中心的v1，作为端的v2，角速度，初相 **/
	public RotationConstraint(double radius,Vertex v1,Vertex v2,double w,double initPhase)
	{
		super(radius,v1,v2);
		
		Vector2D v2InitPos=new Vector2D(Math.cos(initPhase)*radius,Math.sin(initPhase)*radius);
		v2.setPosition(v1.getPosVector().add(v2InitPos));
		_w=w;
		_iPhase=initPhase;
		_cPhase=initPhase;
	}

	@Override
	public void update() 
	{
		super.update();
		Vector2D pos=getV2().getPosVector().subtract(getV1().getPosVector());
		pos.rotate(_cPhase-pos.getRadian());
		pos=pos.divide(2);
		//pos.rotate(_w);
		Vector2D v2Pos=getV2().getPosVector().subtract(getV1().getPosVector());
		Vector2D mid=v2Pos.divide(2);
		getV1().appendOfferset(mid.subtract(pos));
		getV2().appendOfferset(mid.add(pos).subtract(v2Pos));
		_cPhase+=_w;
		_cPhase%=2*Math.PI;
	}
	
}
