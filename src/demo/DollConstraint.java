package demo;

import engine.StickConstraint;
import engine.Vertex;

public class DollConstraint extends StickConstraint implements DollTree
{
	private DollTree _p;
	
	public DollConstraint(double distance,Vertex v1,Vertex v2)
	{
		super(distance,v1,v2);
	}

	@Override
	public DollTree getParent() 
	{
		return _p;
	}

	@Override
	public void setParent(DollTree value) 
	{
		_p=value;
	}

	@Override
	public DollTree getRoot() 
	{
		if(_p!=null)
		{
			return _p.getRoot();
		}
		else
		{
			return null;
		}
	}
}
