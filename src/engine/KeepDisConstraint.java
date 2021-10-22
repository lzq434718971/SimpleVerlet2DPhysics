package engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * 
 * @author 86158
 * 此类用于添加保持两vertex距离不小于distance的约束
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
	 * 需要保持的距离的get set方法
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
	 * 设定是否要根据质量计算各自的位移
	 */
	public boolean isMassAvailable() {
		return _massAva;
	}

	public void setMassAvailable(boolean value) 
	{
		_massAva = value;
	}
}
