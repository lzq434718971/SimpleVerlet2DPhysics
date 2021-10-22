package engine;

/**
 * 
 * @author 86158
 * !��ʱ����!����constraint�Զ��������ת���������һЩģ������
 * 
 */
public class RotationConstraint extends StickConstraint
{
	private double _iPhase;
	private double _cPhase;
	private double _radius;
	private double _w;
	
	/** �뾶����Ϊ���ĵ�v1����Ϊ�˵�v2�����ٶȣ����� **/
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
