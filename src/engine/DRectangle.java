package engine;

/**
 * 
 * @author 86158
 * 支持double类型且支持与其他DRectangle融合的矩形工具类
 * 
 */
public class DRectangle 
{
	private double _x,_y;
	private double _w,_h;
	
	public DRectangle()
	{
		_x=_y=0;
		_w=_h=0;
	}
	
	public DRectangle(double x,double y,double width,double height)
	{
		_x=x;
		_y=y;
		_w=width;
		_h=height;
	}
	
	/** 判断是否与obj相交 **/
	public boolean collide(DRectangle obj)
	{
		Vector2D c1=getCenter();
		Vector2D c2=obj.getCenter();
		double aveW=(getWidth()+obj.getWidth())/2;
		double aveH=(getHeight()+obj.getHeight())/2;
		if(
				Math.abs(c1.getX()-c2.getX())<aveW &&
				Math.abs(c1.getY()-c2.getY())<aveH
		  )
		{
			return true;
		}
		return false;
	}
	
	/** 判断p是否在矩形内 **/
	public boolean contain(Vector2D p)
	{
		if(
				p.getX()<=getX()+getWidth() &&
				p.getY()<=getY()+getHeight() &&
				p.getX()>=getX() &&
				p.getY()>=getY()
		  )
		{
			return true;
		}
		return false;
	}
	
	/** 获取与obj矩形共同构成的边界框 **/
	public DRectangle getTheMerged(DRectangle obj)
	{
		double minx=Math.min(getX(),obj.getX());
		double miny=Math.min(getY(),obj.getY());
		Vector2D rb1=getRightBottom();
		Vector2D rb2=obj.getRightBottom();
		double maxx=Math.max(rb1.getX(),rb2.getX());
		double maxy=Math.max(rb1.getY(),rb2.getY());
		
		return new DRectangle(minx,miny,maxx-minx,maxy-miny);
	}
	
	/*
	 *  一组get set方法
	 */
	public Vector2D getCenter()
	{
		return new Vector2D(getX()+getWidth()/2,getY()+getHeight()/2);
	}
	
	public Vector2D getLeftTop()
	{
		return new Vector2D(getX(),getY());
	}
	
	public Vector2D getLeftBottom()
	{
		return new Vector2D(getX(),getY()+getHeight());
	}
	
	public Vector2D getRightTop()
	{
		return new Vector2D(getX()+getWidth(),getY());
	}
	
	public Vector2D getRightBottom()
	{
		return new Vector2D(getX()+getWidth(),getY()+getHeight());
	}

	public double getX() {
		return _x;
	}
	
	public double setX(double value) 
	{
		_x = value;
		return _x;
	}
	
	public double getY() {
		return _y;
	}
	
	public double setY(double value) {
		_y = value;
		return _y;
	}

	public double getWidth() 
	{
		return _w;
	}

	public double setWidth(double value) 
	{
		_w = value;
		return _w;
	}

	public double getHeight() 
	{
		return _h;
	}

	public double setHeight(double value) {
		_h = value;
		return _h;
	}
	
	public DRectangle clone()
	{
		return new DRectangle(getX(),getY(),getWidth(),getHeight());
	}
	
	public String toString()
	{
		return "[DRectangle("+getX()+","+getY()+","+getWidth()+","+getHeight()+")]";
	}
}
