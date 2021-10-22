package engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * 
 * @author 86158
 * ����������ӱ�����vertex���벻С��distance��Լ��
 *
 */
public class KeepDisConstraint extends VertexConstraint 
{
	private double _d;
	private boolean _massAva;
	
	KeepDisConstraint(double distance,Vertex v1,Vertex v2)
	{
		_d=distance;
		setV1(v1);
		setV2(v2);
		
		setMassAvailable(false);
	}
	
	@Override
	public Vertex setV1(Vertex value) 
	{
		_v1=value;
		return value;
	}

	@Override
	public Vertex setV2(Vertex value) 
	{
		_v2=value;
		return value;
	}

	@Override
	public Vertex getV1() {
		return _v1;
	}

	@Override
	public Vertex getV2() {
		return _v2;
	}

	@Override
	public void update()
	{
		Vector2D o_v1=_v1.getPosVector().subtract(_v1.getVelocity());
		Vector2D o_v2=_v2.getPosVector().subtract(_v2.getVelocity());
		Vector2D p1=o_v2.subtract(o_v1);
		Vector2D p2=_v2.getPosVector().subtract(_v1.getPosVector());
		double totalM=_v1.getMass()+_v2.getMass();
		Vector<Vector2D> reP=VerletUtil.getIntersection(p1,p2,new Vector2D(0,0),_d);
		if(/*p2.getLengthSQ()<_d*_d&&*/reP.size()!=0)
		{
			Vector2D off=reP.get(0).subtract(p2);
			if(_massAva)
			{
				off.multiply(_v1.getMass()/totalM);
			}
			else
			{
				off.multiply(0.5);
			}
			_v2.appendOfferset(off);
			off.reverse();
			if(_massAva)
			{
				off.multiply(_v2.getMass()/_v1.getMass());
			}
			_v1.appendOfferset(off);
		}
	}
	
	/*
	 * ��Ҫ���ֵľ����get set����
	 */
	public double setDistance(double value)
	{
		_d=value;
		return value;
	}
	
	public double getDistance()
	{
		return _d;
	}
	
	/*
	 * �趨�Ƿ�Ҫ��������������Ե�λ��
	 */
	public boolean isMassAvailable() {
		return _massAva;
	}

	public void setMassAvailable(boolean value) 
	{
		_massAva = value;
	}
}
