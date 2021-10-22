package engine;

/**
 * 
 * @author 86158
 * 处理vertex与stick碰撞的类
 *
 */
public class StickCollideWithVertex
{
	private double _f;
	private double _b;
	private int _iterT;
	private double _safeDis;
	
	public StickCollideWithVertex()
	{
		setFrictionFactor(0);
		setBounce(0);
		setIterTime(2);
		setSafeDis(1);
	}
	
	/** 含有摩擦力参数的构造函数 **/
	public StickCollideWithVertex(double frictionFactor)
	{
		setFrictionFactor(frictionFactor);
		setBounce(0);
		setIterTime(2);
		setSafeDis(1);
	}
	
	/** 处理碰撞的主要函数 **/
	public SCVCollisionPoint collide(Vertex v,StickConstraint sc)
	{	
		//碰撞点记录变量
		SCVCollisionPoint record;
		
		//保持vertex间的安全距离
		KeepDisConstraint kdc1=new KeepDisConstraint(_safeDis,v,sc.getV1());
		KeepDisConstraint kdc2=new KeepDisConstraint(_safeDis,v,sc.getV2());
		kdc1.update();
		kdc2.update();
		
		//p1是oldPos p2是newPos
		Vector2D p1=v.getPosVector();
		Vector2D p2=p1.clone();
		Vector2D vertexV=v.getVelocity(); 
		p1=p1.subtract(vertexV);

		Vector2D p3=sc.getV1().getPosVector();
		Vector2D p4=sc.getV2().getPosVector();
		
		Vector2D o_p3=p3.subtract(sc.getV1().getVelocity());
		Vector2D o_p4=p4.subtract(sc.getV2().getVelocity());
		
		//p3与p4连线的中点，作为vertex的坐标原点进行坐标转换来防止穿透
		Vector2D mid1=new Vector2D((o_p3.getX()+o_p4.getX())/2,(o_p3.getY()+o_p4.getY())/2);
		Vector2D mid2=new Vector2D((p3.getX()+p4.getX())/2,(p3.getY()+p4.getY())/2);
		
		//坐标转换
		Vector2D oriXAxis=VerletUtil.getCoordinateOn(new Vector2D(1,0),p4.subtract(p3));
		p1=VerletUtil.getCoordinateOn(p1.subtract(mid1),o_p4.subtract(mid1));
		p2=VerletUtil.getCoordinateOn(p2.subtract(mid2),p4.subtract(mid2));
		p3=new Vector2D(-sc.getLength()/2,0);
		p4=new Vector2D(sc.getLength()/2,0);
		vertexV=p2.subtract(p1);
		
		//stick的向量（p4-p3）
		Vector2D stickVec=p4.subtract(p3);
				
		//点相对于stick的法向速度
		Vector2D vn=vertexV.getProjectionOn(stickVec.getNormal());
		//点相对于stick的径向速度
		Vector2D vr=vertexV.getProjectionOn(stickVec);
		
		//交点
		Vector2D inter=VerletUtil.getIntersection(p1, p2, p3, p4);
		if(inter==null)
		{
			//保持安全距离
			Vector2D dector=p2.subtract(p1).getProjectionOn(new Vector2D(0,1));
			dector.setLength(_safeDis);
			Vector2D dectedInter=VerletUtil.getIntersection(p2,p2.add(dector),p3,p4);
			if(dectedInter!=null)
			{
				record=new SCVCollisionPoint(v,sc,oriXAxis,vn,vr,dectedInter,false);
				
				double refuseDis=_safeDis-dectedInter.subtract(p2).getLength();
				if(refuseDis!=0)
				{
					dector.setLength(refuseDis);
					dector=VerletUtil.getCoordinateOn(dector,oriXAxis);
					sc.getV1().appendOfferset(dector);
					sc.getV2().appendOfferset(dector);
					dector.reverse();
					v.appendOfferset(dector);
					//计算摩擦力
					appendFrictionForce(v,sc,vr,vn,oriXAxis);
					//计算反弹
					appendBounce(v,sc,vn,oriXAxis);
				}
				return record;
			}
			return null;
		}
		
		//碰撞点记录
		record=new SCVCollisionPoint(v,sc,oriXAxis,vn,vr,inter,true);
		
		//vector起始点到碰撞点向量
		Vector2D collisionVec=inter.subtract(p1);
		//产生碰撞的距离比例(collsionRatio)
		double collr=1-collisionVec.getLength()/vertexV.getLength();
		//initPos是屏幕坐标系的向量
		Vector2D initPos=VerletUtil.getCoordinateOn(inter.add(vn).add(vr.multiply(collr)),oriXAxis).add(mid2);
		v.grab(initPos);
		
		//利用stickConstraint实现碰撞交互
		StickConstraint sc1=new StickConstraint(inter.subtract(p3).add(vr.multiply(collr)).getLength(),
												v,sc.getV1());
		StickConstraint sc2=new StickConstraint(inter.subtract(p4).add(vr.multiply(collr)).getLength(),
												v,sc.getV2());
		
		for(int i=0;i<_iterT;i++)
		{
			sc1.update();
			sc2.update();
			sc.update();
		}
		
		double remainD=VerletUtil.getDistance(v.getPosVector(),sc.getV1().getPosVector(),sc.getV2().getPosVector());
		stickVec=sc.getV2().getPosVector().subtract(sc.getV1().getPosVector());
		Vector2D offerset=stickVec.getNormal();
		offerset.setLength(remainD+_safeDis);
		Vector2D side=v.getPosVector().subtract(sc.getV1().getPosVector());
		
		if(side.crossProduct(stickVec)<=0)
		{
			sc.getV1().appendOfferset(offerset);
			sc.getV2().appendOfferset(offerset);
			offerset.reverse();
			v.appendOfferset(offerset);
		}
		else
		{
			v.appendOfferset(offerset);
			offerset.reverse();
			sc.getV1().appendOfferset(offerset);
			sc.getV2().appendOfferset(offerset);
		}
		
		//计算摩擦力
		appendFrictionForce(v,sc,vr,vn,oriXAxis);
		//计算反弹
		appendBounce(v,sc,vn,oriXAxis);
		
		return record;
	}
	
	//辅助函数，在碰撞函数中计算摩擦力
	private void appendFrictionForce(Vertex v,StickConstraint sc,Vector2D vr,Vector2D vn,Vector2D oriXAxis)
	{
		//摩擦系数
		double finalF=v.getFriction()*getFrictionFactor();
				
		//摩擦力
		Vector2D radialF=vr.clone();
		radialF.reverse();
		radialF.setLength(Math.min(
								   v.getMass()*finalF*vn.getLength(),
								   vr.getLength()*v.getMass()
								  )
						 );
		radialF=VerletUtil.getCoordinateOn(radialF, oriXAxis);
		v.appendForce(radialF);
		radialF.reverse();
		sc.appendForce(radialF);
	}
	
	//辅助函数，在碰撞函数中计算反弹力
	private void appendBounce(Vertex v,StickConstraint sc,Vector2D vn,Vector2D oriXAxis)
	{
		double finalB=v.getBounce()*getBounce();
		
		Vector2D normalF=vn.clone();
		normalF.reverse();
		normalF.setLength(vn.getLength()*finalB*v.getMass());
		normalF=VerletUtil.getCoordinateOn(normalF, oriXAxis);
		v.appendForce(normalF);
		normalF.reverse();
		sc.appendForce(normalF);
	}
	
	//获取整个系统的动量
	private Vector2D getTotalMomentum(Vertex v,StickConstraint sc)
	{
		Vector2D vm0=v.getVelocity().multiply(v.getMass());
		Vector2D vm1=sc.getV1().getVelocity().multiply(sc.getV1().getMass());
		Vector2D vm2=sc.getV2().getVelocity().multiply(sc.getV2().getMass());
		return vm0.add(vm1).add(vm2);
	}
	
	//获取整个系统的质量
	private double getTotalMass(Vertex v,StickConstraint sc)
	{
		return v.getMass()+sc.getMass();
	}
	 
	/*
	 * 一组属性的get set方法
	 */
	public double getFrictionFactor() 
	{
		return _f;
	}

	public double setFrictionFactor(double value) 
	{
		_f = value;
		return _f;
	}

	public int getIterTime() {
		return _iterT;
	}
	
	public double getSafeDis()
	{
		return _safeDis;
	}
	
	public void setSafeDis(double value)
	{
		_safeDis=value;
	}

	public void setIterTime(int value) {
		_iterT = value;
	}

	public double getBounce() {
		return _b;
	}

	public void setBounce(double value) 
	{
		_b = value;
	}
}
