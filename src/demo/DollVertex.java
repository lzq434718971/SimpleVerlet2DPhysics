package demo;

import engine.Vertex;

public class DollVertex extends Vertex implements DollTree
{
	private DollTree _p;
	
	public DollVertex(double x,double y)
	{
		super(x,y);
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
			return this;
		}
	}
}
