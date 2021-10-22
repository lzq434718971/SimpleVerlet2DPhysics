package engine;

/**
 * 
 * @author 86158
 * 实现顶点间的刚性约束的类，可以参与碰撞
 *
 */
public class StickConstraint extends VertexConstraint implements Colideable
{
	private double _len;
	
	//length为所约束的两vertex的距离
	public StickConstraint(double length)
	{	
		_len=length;
	}
	public StickConstraint(double length,Vertex v1,Vertex v2)
	{
		_len=length;
		_v1=v1;
		_v2=v2;
	}
	
	/*
	 * 一组get set方法
	 */
	public void setLength(double value)
	{
		_len=value;
	}
	
	public double getLength()
	{
		return _len;
	}
	
	public Vertex getV1()
	{
		return _v1;
	}
	public Vertex setV1(Vertex value)
	{
		_v1=value;
		return _v1;
	}
	
	public Vertex getV2()
	{
		return _v2;
	}
	public Vertex setV2(Vertex value)
	{
		_v2=value;
		return _v2;
	}
	
	public double getMass()
	{
		return _v1.getMass()+_v2.getMass();
	}
	
	/** 判断该约束是否包含某vertex **/
	public boolean contain(Vertex v)
	{
		return v==_v1||v==_v2;
	}
	
	/** 判断这个stick是否还在活动 **/
	public boolean isDynamic()
	{
		return _v1.isDynamic()||_v2.isDynamic();
	}
	
	/** 对该stick的两个顶点应用f力 **/
	public void appendForce(Vector2D f)
	{
		Vector2D f1=f.clone();
		f1.setLength(_v1.getMass()/getMass()*f.getLength());
		
		Vector2D f2=f.clone();
		f2.setLength(_v2.getMass()/getMass()*f.getLength());
		
		_v1.appendForce(f1);
		_v2.appendForce(f2);
	}
	
	@Override
	//维持距离
	public void update()
	{
		//当前两点距离（需要调整）
		Vector2D offerset=_v1.getPosVector().subtract(_v2.getPosVector());
		double clen=offerset.getLength();
		double totalM=_v1.getMass()+_v2.getMass();
		
		if(clen>_len)
		{
			Vector2D off1=offerset.clone();
			//静态顶点质量视为无穷
			double ratio1=_v2.isDynamic()?_v2.getMass()/totalM:1;
			off1.setLength((clen-_len)*ratio1);
			off1.reverse();
			
			Vector2D off2=offerset.clone();
			//静态顶点质量视为无穷
			double ratio2=_v1.isDynamic()?_v1.getMass()/totalM:1;
			off2.setLength((clen-_len)*ratio2);
			
			//根据动量守恒定理决定vertex的位移
			_v1.appendOfferset(off1);
			
			_v2.appendOfferset(off2);
		}
		else if(clen<_len)
		{
			Vector2D off1=offerset.clone();
			
			double ratio1=_v2.isDynamic()?_v2.getMass()/totalM:1;
			off1.setLength((_len-clen)*ratio1);
			
			Vector2D off2=offerset.clone();
			//静态顶点质量视为无穷
			double ratio2=_v1.isDynamic()?_v1.getMass()/totalM:1;
			off2.setLength((_len-clen)*ratio2);
			off2.reverse();
			
			//根据动量守恒定理决定vertex的位移
			_v1.appendOfferset(off1);
			
			_v2.appendOfferset(off2);
		}
		
	}
	
	@Override
	/** 获取碰撞盒 **/
	public DRectangle getBorderBox() 
	{
		return _v1.getBorderBox().getTheMerged(_v2.getBorderBox());
	}
}
