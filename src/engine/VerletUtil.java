package engine;

import java.util.Vector;

/**
 * 
 * @author 86158
 * 引擎用到的常用的数学函数
 *
 */
public class VerletUtil 
{
	/** 由四个坐标值获取两点的距离 **/
	public static double getDistance(double x1,double y1,double x2,double y2)
	{
		double dx=x1-x2;
		double dy=y1-y2;
		return Math.sqrt(dx*dx+dy*dy);
	}
	/** 求v0与经过v1，v2的直线的距离 **/
	public static double getDistance(Vector2D v0,Vector2D v1,Vector2D v2)
	{
		Vector2D mLine=v2.subtract(v1);
		Vector2D oLine=v0.subtract(v1);
		return Math.abs(oLine.crossProduct(mLine)/mLine.getLength());
	}
	/** 求vertex间的距离 **/
	public static double getDistance(Vertex v1,Vertex v2)
	{
		double dx=v1.getX()-v2.getX();
		double dy=v1.getY()-v2.getY();
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	/** 求两线段交点 **/
	public static Vector2D getIntersection(Vector2D p1,Vector2D p2,Vector2D p3,Vector2D p4)
	{
		double aboutN=1e-5;
		boolean isInf1=(p2.getX()-p1.getX())<aboutN;
		boolean isInf2=(p4.getX()-p3.getX())<aboutN;
		
		double k1=(p2.getY()-p1.getY())/(p2.getX()-p1.getX());
		double k2=(p4.getY()-p3.getY())/(p4.getX()-p3.getX());
		
		double b1=p1.getY()-k1*p1.getX();
		double b2=p3.getY()-k2*p3.getX();
		
		//当直线长度为0时
		if(p1.subtract(p2).isZero()&&p3.subtract(p4).isZero())
		{
			if(p1.subtract(p3).isZero())
			{
				return p1.clone();
			}
			else
			{
				return null;
			}
		}
		else if(p1.subtract(p2).isZero())
		{
			if((k2*p1.getX()+b2)==p1.getY())
			{
				double factor1=(p1.getX()-p3.getX())*(p1.getX()-p4.getX());
				double factor2=(p1.getY()-p3.getY())*(p1.getY()-p4.getY());
				if((factor1<0||Math.abs(factor1)<aboutN) &&
				   (factor2<0||Math.abs(factor2)<aboutN)
				  )
				{
					return p1.clone();
				}
				return null;
			}
			else
			{
				return null;
			}
		}
		else if(p3.subtract(p4).isZero())
		{
			if((k1*p3.getX()+b1)==p3.getY())
			{
				double factor1=(p3.getX()-p1.getX())*(p3.getX()-p2.getX());
				double factor2=(p3.getY()-p1.getY())*(p3.getY()-p2.getY());
				if((factor1<0||Math.abs(factor1)<aboutN) &&
				   (factor2<0||Math.abs(factor2)<aboutN)
				  )
				{
					return p3.clone();
				}
				return null;
			}
			else
			{
				return null;
			}
		}
		
		//处理斜率为无穷的特殊情况
		if(isInf1&&isInf2)
		{
			return null;
		}
		else if(isInf1)
		{
			double rx=p1.getX();
			double ry=k2*rx+b2;
			double factor1=(ry-p1.getY())*(ry-p2.getY());
			double factor2=(ry-p3.getY())*(ry-p4.getY());
			double factor3=(rx-p3.getX())*(rx-p4.getX());
			if(factor1>aboutN||factor2>aboutN||factor3>aboutN)
			{
				return null;
			}
			else
			{
				return new Vector2D(rx,ry);
			}
		}
		else if(isInf2)
		{
			double rx=p3.getX();
			double ry=k1*rx+b1;
			double factor1=(ry-p1.getY())*(ry-p2.getY());
			double factor2=(ry-p3.getY())*(ry-p4.getY());
			double factor3=(rx-p1.getX())*(rx-p2.getX());
			if(factor1>aboutN||factor2>aboutN||factor3>aboutN)
			{
				return null;
			}
			else
			{
				return new Vector2D(rx,ry);
			}
		}
		
		if(Math.abs(k1-k2)<aboutN)
		{
			if(Math.abs(b1-b2)<aboutN)
			{
				int factor1=p1.getX()>=p3.getX()?1:-1;
				int factor2=p1.getX()>=p4.getX()?1:-1;
				int factor3=p2.getX()>=p3.getX()?1:-1;
				int factor4=p2.getX()>=p4.getX()?1:-1;
				int ffactor=factor1+factor2+factor3+factor4;
				if(ffactor==-4)
				{
					return null;
				}
				else if(ffactor==-2)
				{
					double rx=Math.max(p1.getX(), p2.getX())+Math.min(p3.getX(), p4.getX())/2;
					double ry=k1*rx+b1;
					return new Vector2D(rx,ry);
				}
				else if(ffactor==0)
				{
					if((factor1+factor2)==0)
					{
						double rx=(p1.getX()+p2.getX())/2;
						double ry=k1*rx+b1;
						return new Vector2D(rx,ry);
					}
					else
					{
						double rx=(p3.getX()+p4.getX())/2;
						double ry=k1*rx+b1;
						return new Vector2D(rx,ry);
					}
				}
				else if(ffactor==2)
				{
					double rx=Math.min(p1.getX(), p2.getX())+Math.max(p3.getX(), p4.getX())/2;
					double ry=k1*rx+b1;
					return new Vector2D(rx,ry);
				}
				else if(ffactor==4)
				{
					if(Math.min(p1.getX(), p2.getX())==Math.max(p3.getX(), p4.getX())/2)
					{
						double rx=Math.min(p1.getX(), p2.getX());
						double ry=k1*rx+b1;
						return new Vector2D(rx,ry);
					}
					else
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			double rx=(b2-b1)/(k1-k2);
			double ry=k1*rx+b1;
			//判断交点是否在线段内
			double factor1=(rx-p1.getX())*(rx-p2.getX());
			double factor2=(rx-p3.getX())*(rx-p4.getX());
			double factor3=(ry-p1.getY())*(ry-p2.getY());
			double factor4=(ry-p3.getY())*(ry-p4.getY());
			if(
				(factor1<0||Math.abs(factor1)<aboutN) &&
				(factor2<0||Math.abs(factor2)<aboutN) &&
				(factor3<0||Math.abs(factor3)<aboutN) &&
				(factor4<0||Math.abs(factor4)<aboutN)
			  )
			{
				return new Vector2D(rx,ry);
			}
			else
			{
				return null;
			}
		}
	}
	
	/** 求圆与线段的交点,且返回结果将会按照与线段距离远近排列在结果中 **/
	public static Vector<Vector2D> getIntersection(Vector2D p1,Vector2D p2,Vector2D center,double radius)
	{
		double aboutN=1e-5;
		double dis;
		Vector<Vector2D> re=new Vector<Vector2D>();
		
		boolean isInf=Math.abs(p1.getX()-p2.getX())<aboutN;
		double k=(p1.getY()-p2.getY())/(p1.getX()-p2.getX());
		double b=p1.getY()-k*p1.getX();
		
		//两者都为零向量或其中一个为零向量时的相交判断
		if(p2.subtract(p1).isZero()&&radius==0)
		{
			if(p1.subtract(center).isZero())
			{
				re.add(p1.clone());
				return re;
			}
			else
			{
				return re;
			}
		}
		else if(p2.subtract(p1).isZero())
		{
			if(Math.abs(p1.subtract(center).getLengthSQ()-radius*radius)<aboutN)
			{
				re.add(p1);
				return re;
			}
			else
			{
				return re;
			}
		}
		else if(radius==0)
		{
			if(isInf)
			{
				if(Math.abs(center.getX()-p1.getX())<aboutN)
				{
					double factor=(center.getY()-p1.getY())*(center.getY()-p2.getY());
					if(factor>0)
					{
						return re;
					}
					re.add(center.clone());
					return re;
				}
				else
				{
					return re;
				}
			}
			else
			{
				if(Math.abs(center.getY()-(k*center.getX()+b))<aboutN)
				{
					re.add(center.clone());
					return re;
				}
				else
				{
					return re;
				}
			}
		}
		
		//斜率正无穷
		if(isInf)
		{
			dis=Math.abs(center.getX()-p1.getX());
			if(dis>radius)
			{
				return re;
			}
			else if(Math.abs(dis-radius)<aboutN)
			{
				double factor=(center.getY()-p1.getY())*(center.getY()-p2.getY());
				if(factor>aboutN)
				{
					return re;
				}
				if(p1.getX()<center.getX())
				{
					Vector2D rp=new Vector2D(center.getX()-radius,center.getY());
					re.add(rp);
					return re;
				}
				else
				{
					Vector2D rp=new Vector2D(center.getX()+radius,center.getY());
					re.add(rp);
					return re;
				}
			}
			else
			{
				double radDis=Math.sqrt(radius*radius-dis*dis);
				Vector2D radV=p2.subtract(p1).getRadial();
				Vector2D norV=p2.subtract(p1).getNormal();
				radV.setLength(radDis);
				norV.setLength(dis);
				Vector2D side=center.subtract(p1);
				double factor=side.crossProduct(p2.subtract(p1));
				if(factor<-aboutN)
				{
					norV.reverse();
				}
				Vector2D rp1=center.add(norV).subtract(radV);
				Vector2D rp2=center.add(norV).add(radV);
				double factor1=(rp1.getY()-p1.getY())*(rp1.getY()-p2.getY());
				double factor2=(rp2.getY()-p1.getY())*(rp2.getY()-p2.getY());
				if(factor1<0||Math.abs(factor1)<aboutN)
				{
					re.add(rp1);
				}
				if(factor2<0||Math.abs(factor2)<aboutN)
				{
					re.add(rp2);
				}
				return re;
			}
		}
		else	//普通情况
		{
			dis=VerletUtil.getDistance(center, p1, p2);
			if(dis>radius)
			{
				return re;
			}
			double radDis=Math.sqrt(radius*radius-dis*dis);
			Vector2D radV=p2.subtract(p1).getRadial();
			Vector2D norV=p2.subtract(p1).getNormal();
			radV.setLength(radDis);
			norV.setLength(dis);
			Vector2D side=center.subtract(p1);
			double factor=side.crossProduct(p2.subtract(p1));
			if(factor<-aboutN)
			{
				norV.reverse();
			}
			//相切的情况
			if(Math.abs(dis-radius)<aboutN)
			{
				Vector2D rp=center.add(norV);
				double factor1=(rp.getX()-p1.getX())*(rp.getX()-p2.getX());
				double factor2=(rp.getY()-p1.getY())*(rp.getY()-p2.getY());
				if(
					(factor1<0||Math.abs(factor1)<aboutN) &&
					(factor2<0||Math.abs(factor2)<aboutN)
				  )
				{
					re.add(rp);
				}
				return re;
			}
			Vector2D rp1=center.add(norV).subtract(radV);
			Vector2D rp2=center.add(norV).add(radV);
			double factor1=(rp1.getX()-p1.getX())*(rp1.getX()-p2.getX());
			double factor2=(rp2.getX()-p1.getX())*(rp2.getX()-p2.getX());
			double factor3=(rp1.getY()-p1.getY())*(rp1.getY()-p2.getY());
			double factor4=(rp2.getY()-p1.getY())*(rp2.getY()-p2.getY());
			if(
				(factor1<0||Math.abs(factor1)<aboutN) &&
				(factor3<0||Math.abs(factor3)<aboutN)
			  )
			{
				re.add(rp1);
			}
			if(
				(factor2<0||Math.abs(factor2)<aboutN) &&
				(factor4<0||Math.abs(factor4)<aboutN)
			  )
			{
				re.add(rp2);
			}
			return re;
		}
	}
	
	/**以给定的向量作为左手螺旋系的x正方向，得到在该坐标下的坐标值**/
	public static Vector2D getCoordinateOn(Vector2D pos,Vector2D cooSys)
	{
		double rx=pos.dotProduct(cooSys.getRadial());
		double ry=pos.dotProduct(cooSys.getNormal());
		return new Vector2D(rx,ry);
	}
	
	//弧度 = 角度乘以PI后再除以180、 推理可得弧度换算角度的公式
    /**  弧度转角度  **/
    public static double radianToAngle(double radian)
    {
        return radian / Math.PI * 180;
    }
    
    public static double angleToRadian(double angle)
    {
    	return angle / 180 * Math.PI;
    }
	
	/** 判断点是否在p1，p2构成的直线上**/
	/*
	public static boolean isOnLine()
	{
		
	}
	*/
}
