package engine;

/**
 * 
 * @author 86158
 * 平面二维向量类工具类 
 *
 */
public class Vector2D implements Cloneable {
    private double x;
    private double y;
    
    //保存长度避免多余的开方运算
    private double _len;
    private boolean _isUpdated;

	//get
    public double getX() { return x;}
    public double getY() { return y;}
    
	//set
    public double setX(double x) { this.x = x;_isUpdated=false; return this.x;}
    public double setY(double y) { this.y = y;_isUpdated=false; return this.y;}

    @Override
    public String toString() {
        return "Vector2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    /** 空构造 默认值V(x,y)=>V(0,0) **/
    public Vector2D()
    {
        x = 0;
        y = 0;
        _isUpdated=false;
    }

    /** 赋值构造 V(x,y) **/
    public Vector2D(double _x, double _y)
    {
        x = _x;
        y = _y;
        _isUpdated=false;
    }

    /**  获取弧度  **/
    public double getRadian()
    {
        return Math.atan2(y, x);
    }

    /**  获取角度  **/
    public double getAngle()
    {
        return getRadian() / Math.PI * 180;
    }
    
    /** 将当前向量逆时针旋转radian弧度 **/
    public void rotate(double radian)
    {
    	radian=-radian;
    	Vector2D newXAxis=new Vector2D(Math.cos(radian),Math.sin(radian));
    	Vector2D newCood=VerletUtil.getCoordinateOn(this,newXAxis);
    	setX(newCood.getX());
    	setY(newCood.getY());
    }

    /**  克隆该对象  **/
    @Override
    public Vector2D clone()
    {
        return new Vector2D(x,y);
    }

    public double getLength()
    {
    	
    	if(_isUpdated)
    	{
    		return _len;
    	}
    	else
    	{
    		updateLen();
    		return _len;
    	}
    }

    public double getLengthSQ()
    {
        return x * x + y * y;
    }

    /**  向量置零  **/
    public Vector2D Zero()
    {
        x = 0;
        y = 0;
        _isUpdated=false;
        return this;
    }

    /** 该向量是否是置零的，是返回true，反之false **/
    public boolean isZero()
    {
    	return x==0&&y==0;
    }

    /** 向量的长度设置为我们期待的value  **/
    public void setLength(double value)
    {
        if(!_isUpdated)
    	{
    		updateLen();
    	}
    	if(_len==0)
    	{
    		x=value;
    		y=0;
    	}
    	else
    	{
    		x=x*value/_len;
        	y=y*value/_len;
    	}
    	
    	_len=Math.abs(value);
    	_isUpdated=true;
    }

    /**  向量的标准化（方向不变，长度为1）  **/
    public Vector2D normalize()
    {
    	
    	if(getLength()==0)
    	{
    		x=1;
    		y=0;
    		_len=1;
    		_isUpdated=true;
    		return this;
    	}
    	
        double length = getLength();
        x = x / length;
        y = y / length;
        _len=1;
        _isUpdated=true;
        return this;
    }

    /**  是否已经标准化  **/
    public boolean isNormalized()
    {
        return getLength() == 1.0;
    }

    /**  向量的方向翻转  **/
    public Vector2D reverse()
    {
        x = -x;
        y = -y;
        return this;
    }

    /**  2个向量的数量积(点积)  **/
    public double dotProduct(Vector2D v)
    {
        return x * v.x + y * v.y;
    }

    /**  2个向量的向量积(叉积)  **/
    public double crossProduct(Vector2D v)
    {
        return x * v.y - y * v.x;
    }

    /**  计算2个向量的夹角弧度  **/
    /**  参考点积公式:v1 * v2 = cos<v1,v2> * |v1| *|v2|  **/
    public static double radianBetween(Vector2D v1, Vector2D v2)
    {
        if(!v1.isNormalized()) {
            v1 = v1.clone().normalize(); // |v1| = 1
        }
        if(!v2.isNormalized()) {
            v2 = v2.clone().normalize(); // |v2| = 1
        }
        return Math.acos(v1.dotProduct(v2));
    }

    /** 向量加 **/
    public Vector2D add(Vector2D v)
    {
        return new Vector2D(x + v.x, y + v.y);
    }

    /** 向量减 **/
    public Vector2D subtract(Vector2D v)
    {
        return new Vector2D(x - v.x, y - v.y);
    }

    /** 向量乘 **/
    public Vector2D multiply(double value)
    {
        return new Vector2D(x * value, y * value);
    }

    /** 向量除 **/
    public Vector2D divide(double value)
    {
        return new Vector2D(x / value, y / value);
    }
    
    /** 返回径向标准向量 **/
    public Vector2D getRadial()
    {
    	Vector2D v=this.clone();
    	v.normalize();
    	return v;
    }
    
    /** 返回法向标准向量(该向量方向指向正向右边) **/
    public Vector2D getNormal()
    {
    	Vector2D temp=this.clone();
    	temp.normalize();
    	Vector2D v=temp.clone();
    	v.setX(-temp.getY());
    	v.setY(temp.getX());
    	return v;
    }
    
    /** 返回当前向量在v上的投影向量 **/
    public Vector2D getProjectionOn(Vector2D v)
    {
    	double len=dotProduct(v)/v.getLength();
    	Vector2D re=v.clone();
    	re.setLength(len);
    	return re;
    }
    
    
    private void updateLen()
    {
    	_len=Math.sqrt(getLengthSQ());
    	_isUpdated=true;
    }
    
}