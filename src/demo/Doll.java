package demo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

import engine.StickConstraint;
import engine.Vector2D;
import engine.VerletScene;
import engine.VerletUtil;
import engine.Vertex;

public class Doll implements DollTree
{
	//各躯干的头部
	private DollMainVertex head;
	private DollMainVertex body;
	private DollMainVertex legL;
	private DollMainVertex legR;
	private DollMainVertex handL;
	private DollMainVertex handR;
	private ArrayList<Vertex> allVertex;
	private ArrayList<StickConstraint> allCollisionConstraint;
	private ArrayList<StickConstraint> allConstraint;
	
	//游戏设定属性
	private int health;
	private boolean invincible;
	
	public Doll(double x,double y,double radius)
	{
		allVertex=new ArrayList<Vertex>();
		allCollisionConstraint=new ArrayList<StickConstraint>();
		allConstraint=new ArrayList<StickConstraint>();
		
		double r=radius;
		head=addABall(x,y,r*2,12);
		head.setMass(10);
		head.setParent(this);
		
		int chainSeg=5;
		int ballSeg=6;
		
		ArrayList<DollMainVertex> bodyG=addAChain(x,y+3*r+2,0,1,r,chainSeg,ballSeg);
		
		ArrayList<DollMainVertex> handLG=addAChain(x-2*r-2,y+3*r+2,-1,1,r,chainSeg,ballSeg);
		ArrayList<DollMainVertex> handRG=addAChain(x+2*r+2,y+3*r+2,1,1,r,chainSeg,ballSeg);
		
		ArrayList<DollMainVertex> legLG=addAChain(x-2*r-2,y+11*r+10,-1,1,r,chainSeg,ballSeg);
		ArrayList<DollMainVertex> legRG=addAChain(x+2*r+2,y+11*r+10,1,1,r,chainSeg,ballSeg);
		
		body=bodyG.get(0);
		handL=handLG.get(0);
		handR=handRG.get(0);
		legL=legLG.get(0);
		legR=legRG.get(0);
		
		body.setParent(head);
		handL.setParent(body);
		handR.setParent(body);
		legL.setParent(body);
		legR.setParent(body);
		
		StickConstraint conn1=new StickConstraint(3*r+2,bodyG.get(0),head);
		
		StickConstraint conn2=new StickConstraint(2*r+2,bodyG.get(0),handLG.get(0));
		StickConstraint conn3=new StickConstraint(2*r+2,bodyG.get(0),handRG.get(0));
		
		StickConstraint conn4=new StickConstraint(2*r+2,bodyG.get(4),legLG.get(0));
		StickConstraint conn5=new StickConstraint(2*r+2,bodyG.get(4),legRG.get(0));
		
		allConstraint.add(conn1);
		
		allConstraint.add(conn2);
		allConstraint.add(conn3);
		
		allConstraint.add(conn4);
		allConstraint.add(conn5);
		
		head.setNextConstraint(conn1);
		bodyG.get(0).setPreConstraint(conn1);
		
		handLG.get(0).setPreConstraint(conn2);
		handRG.get(0).setPreConstraint(conn3);
		
		legLG.get(0).setPreConstraint(conn4);
		legRG.get(0).setPreConstraint(conn5);
		
		setHealth(10);
	}
	
	//d1d2控制方向,返回各球体中心
	private ArrayList<DollMainVertex> addAChain(double x,double y,double d1,double d2,double radius,int chainSeg,int ballSeg)
	{
		ArrayList<DollMainVertex> center=new ArrayList<DollMainVertex>();
		Vector2D direction=new Vector2D(d1,d2);
		direction.setLength(radius*2+2);
		Vector2D pos=new Vector2D(x,y);
		DollMainVertex chainHead=addABall(pos.getX(),pos.getY(),radius,ballSeg);
		center.add(chainHead);
		pos=pos.add(direction);
		for(int i=1;i<chainSeg;i++)
		{
			DollMainVertex c=addABall(pos.getX(),pos.getY(),radius,ballSeg);
			c.setParent(chainHead);
			center.add(c);
			pos=pos.add(direction);
		}
		for(int i=0;i<chainSeg-1;i++)
		{
			StickConstraint sc=new StickConstraint(radius*2+2,center.get(i),center.get(i+1));
			allConstraint.add(sc);
			center.get(i).setNextConstraint(sc);
			center.get(i+1).setPreConstraint(sc);
		}
		return center;
	}
	
	private DollMainVertex addABall(double x,double y,double radius,double n)
	{
		DollMainVertex center=new DollMainVertex(x,y);
		center.setRadius(radius);
		
		ArrayList<DollVertex> outer=new ArrayList<DollVertex>();
		double perR=2*Math.PI/(double)n;
		
		//设置第一个顶点
		outer.add(new DollVertex(radius+x,0+y));
		outer.get(0).setParent(center);
		allConstraint.add(new StickConstraint(radius,center,outer.get(0)));
		allVertex.add(center);
		allVertex.add(outer.get(0));
		
		for(int i=1;i<n;i++)
		{
			outer.add(new DollVertex(Math.cos(perR*i)*radius+x,Math.sin(perR*i)*radius+y));
			outer.get(i).setParent(center);
			allVertex.add(outer.get(i));
			
			//设置外部碰撞边
			DollConstraint sco=new DollConstraint(VerletUtil.getDistance(outer.get(i),outer.get(i-1)),outer.get(i),outer.get(i-1));
			sco.setParent(center);
			allCollisionConstraint.add(sco);
			
			//设置内部支持边
			StickConstraint scr=new StickConstraint(radius,center,outer.get(i));
			allCollisionConstraint.add(scr);
		}
		DollConstraint scLast=new DollConstraint(
													VerletUtil.getDistance(
																			outer.get(0),
																			outer.get(outer.size()-1)
																		  ),
													outer.get(0),
													outer.get(outer.size()-1)
												 );
		scLast.setParent(center);
		allCollisionConstraint.add(scLast);
		
		return center;
	}
	
	public void addDollTo(VerletScene vs,int gid)
	{
		for(Vertex v:allVertex)
		{
			vs.addVertex(v,gid);
		}
		for(StickConstraint sc:allConstraint)
		{
			vs.addConstraint(sc);
		}
		for(StickConstraint sc:allCollisionConstraint)
		{
			vs.addCollisionConstraint(sc,gid);
		}
	}
	
	public DollMainVertex getHead()
	{
		return head;
	}
	
	public boolean contain(DollTree obj)
	{
		return obj.getRoot()==this;
	}
	
	public boolean isHead(DollMainVertex obj)
	{
		return obj==head;
	}
	
	public boolean isBody(DollMainVertex obj)
	{
		if(obj==body)
		{
			return true;
		}
		else if(obj==handL||obj==handR||obj==legL||obj==legR)
		{
			return false;
		}
		else if(obj.getParent()==body)
		{
			return true;
		}
		return false;
	}
	
	public boolean isHand(DollMainVertex obj)
	{
		if(obj==handL||obj==handR)
		{
			return true;
		}
		else
		{
			if(obj.getParent()==handL||obj.getParent()==handR)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public boolean isLeg(DollMainVertex obj)
	{
		if(obj==legL||obj==legR)
		{
			return true;
		}
		else
		{
			if(obj.getParent()==legL||obj.getParent()==legR)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public boolean isFragile(DollMainVertex obj)
	{
		return isHead(obj)||isBody(obj);
	}
	
	public void moveX(double v)
	{
		head.setVx(head.getVx()+v);
	}
	
	public void moveY(double v)
	{
		head.setVy(head.getVy()+v);
	}
	
	public void setColor(Color c)
	{
		head.setColor(c);
		
		setChainColor(body,c);
		setChainColor(handL,c);
		setChainColor(handR,c);
		setChainColor(legL,c);
		setChainColor(legR,c);
	}
	private void setChainColor(DollMainVertex v,Color c)
	{
		v.setColor(c);
		while(v.getNextConstraint()!=null)
		{
			StickConstraint nsc=v.getNextConstraint();
			DollMainVertex v1=(DollMainVertex)nsc.getV1();
			DollMainVertex v2=(DollMainVertex)nsc.getV2();
			if(v==v1)
			{
				v2.setColor(c);
				v=v2;
			}
			else
			{
				v1.setColor(c);
				v=v1;
			}
		}
	}
	
	public ArrayList<DollMainVertex> getAllMainVertex()
	{
		ArrayList<DollMainVertex> re=new ArrayList<DollMainVertex>();
		re.add(head);
		re.addAll(getChainMV(body));
		re.addAll(getChainMV(handL));
		re.addAll(getChainMV(handR));
		re.addAll(getChainMV(legL));
		re.addAll(getChainMV(legR));
		
		return re;
	}
	private ArrayList<DollMainVertex> getChainMV(DollMainVertex v)
	{
		ArrayList<DollMainVertex> re=new ArrayList<DollMainVertex>();
		re.add(v);
		while(v.getNextConstraint()!=null)
		{
			StickConstraint nsc=v.getNextConstraint();
			DollMainVertex v1=(DollMainVertex)nsc.getV1();
			DollMainVertex v2=(DollMainVertex)nsc.getV2();
			if(v==v1)
			{
				re.add(v2);
				v=v2;
			}
			else
			{
				re.add(v1);
				v=v1;
			}
		}
		return re;
	}

	@Override
	public DollTree getParent() 
	{
		return null;
	}

	@Override
	public void setParent(DollTree value) {}

	@Override
	public DollTree getRoot() 
	{
		return this;
	}
	
	/** 断开doll的所有连边，用作死亡特效 **/
	public void collapseIn(VerletScene vs)
	{
		LinkedList<StickConstraint> allConn=getConnection();
		for(StickConstraint sc:allConn)
		{
			vs.removeConstraint(sc);
		}
	}
	
	/** 获取整个doll的连边 **/
	public LinkedList<StickConstraint> getConnection()
	{
		LinkedList<StickConstraint> re=new LinkedList<StickConstraint>();
		re.add(body.getPreConstraint());
		re.add(handL.getPreConstraint());
		re.add(handR.getPreConstraint());
		re.add(legL.getPreConstraint());
		re.add(legR.getPreConstraint());
		
		re.addAll(getChainConnection(body));
		re.addAll(getChainConnection(handL));
		re.addAll(getChainConnection(handR));
		re.addAll(getChainConnection(legL));
		re.addAll(getChainConnection(legR));
		
		return re;
	}
	//获取一条链的所有连边
	private LinkedList<StickConstraint> getChainConnection(DollMainVertex v)
	{
		LinkedList<StickConstraint> re=new LinkedList<StickConstraint>();
		while(v.getNextConstraint()!=null)
		{
			StickConstraint nsc=v.getNextConstraint();
			DollMainVertex v1=(DollMainVertex)nsc.getV1();
			DollMainVertex v2=(DollMainVertex)nsc.getV2();
			re.add(v.getNextConstraint());
			v=(v==v1)?v2:v1;
		}
		return re;
	}

	public int getHealth() 
	{
		return health;
	}

	public void setHealth(int value) 
	{
		if(!invincible)
		{
			health = Math.max(value,0);
		}
	}
	
	public boolean isInvincible()
	{
		return invincible;
	}
	public void setInvincible(boolean value)
	{
		invincible=value;
	}
}
