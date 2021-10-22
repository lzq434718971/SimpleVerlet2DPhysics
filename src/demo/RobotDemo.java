package demo;
import java.util.LinkedList;

import javax.swing.JFrame;

import engine.RotationConstraint;
import engine.StickConstraint;
import engine.Vector2D;
import engine.VerletScene;
import engine.VerletTask;
import engine.VerletUtil;
import engine.Vertex;

public class RobotDemo extends JFrame
{
	private static final long serialVersionUID = 1L;

	public static void main(String args[])
	{
		VerletScene vs=new VerletScene();
		vs.setFrameRate(120);
		vs.setIterationTime(30);
		vs.setSafeDis(1);
		
		setAWall(-50,600,650,650,vs);
		setAWall(600,550,300,650,vs);
		setAWall(0,-50,0,650,vs);
		setAWall(600,-50,600,650,vs);
		setAWall(-50,0,650,0,vs);
		
		double unit=0.7;
		//»ù´¡°å´´½¨
		Vertex b1=new Vertex(200,475,10);
		Vertex b2=new Vertex(b1.getX()-38*unit,b1.getY()+7.8*unit,10);
		Vertex b3=new Vertex(b1.getX()+38*unit,b1.getY()+7.8*unit,10);
		//b1.setIsDynamic(false);
		//b2.setIsDynamic(false);
		//b3.setIsDynamic(false);
		b1.setFriction(1);
		b2.setFriction(1);
		b3.setFriction(1);
		
		vs.addVertex(b1,5);
		vs.addVertex(b2,5);
		vs.addVertex(b3,5);
		
		addAStick(b1,b2,vs,5);
		addAStick(b1,b3,vs,5);
		addAStick(b2,b3,vs,5);
		
		double radius=15*unit;
		
		Vertex engine1=new Vertex(b1.getX(),b1.getY()-radius);
		Vertex engine2=new Vertex(b1.getX(),b1.getY()+radius);
		StickConstraint sc1=new StickConstraint(radius,b1,engine1);
		StickConstraint sc2=new StickConstraint(radius,b1,engine2);
		StickConstraint sc3=new StickConstraint(2*radius,engine1,engine2);
		
		vs.addVertex(engine1,3);
		vs.addVertex(engine2,4);
		vs.addConstraint(sc1);
		vs.addConstraint(sc2);
		vs.addConstraint(sc3);
		
		placeRobot(b1,b2,b3,engine1,1,vs,1);
		placeRobot(b1,b2,b3,engine2,-1,vs,2);
		
		vs.setCollisionGroup(0,1,true);
		vs.setCollisionGroup(0,2,true);
		vs.setCollisionGroup(0,3,true);
		vs.setCollisionGroup(0,4,true);
		vs.setCollisionGroup(0,5,true);
		
		b1.setIsDynamic(false);
		b2.setIsDynamic(false);
		b3.setIsDynamic(false);
		vs.freeze(100);
		b1.setIsDynamic(true);
		b2.setIsDynamic(true);
		b3.setIsDynamic(true);
		
		DemoTask task=new DemoTask(vs,engine1,engine2,b1,radius,-Math.PI/2,2*Math.PI/vs.getFrameRate()/vs.getIterationTime()*5);
		vs.addTask(task);
		DebugFrame df=new DebugFrame();
		df.setVerletScene(vs);
		//df.setFrameRate(60);
		df.playScene();
	}
	
	private static void setAWall(double x1,double y1,double x2,double y2,VerletScene vs)
	{
		Vertex v1=new Vertex(x1,y1);
		Vertex v2=new Vertex(x2,y2);
		v1.setIsDynamic(false);
		v2.setIsDynamic(false);
		StickConstraint sc=new StickConstraint(VerletUtil.getDistance(x1, y1, x2, y2),v1,v2);
		vs.addCollisionConstraint(sc);
		vs.addVertex(v1);
		vs.addVertex(v2);
	}
	private static void addAStick(Vertex v1,Vertex v2,VerletScene vs,int gid)
	{
		StickConstraint sc=new StickConstraint(VerletUtil.getDistance(v1, v2),v1,v2);
		vs.addCollisionConstraint(sc,gid);
	}
	private static void placeRobot(Vertex v1,Vertex v2,Vertex v3,Vertex engine,int side,VerletScene vs,int gid)
	{
		placeHalf(v3,engine,1,vs,gid);
		placeHalf(v2,engine,-1,vs,gid);
	}
	private static void placeHalf(Vertex board,Vertex engine,int side,VerletScene vs,int gid)
	{
		double unit=0.7;
		Vertex top=new Vertex(board.getX()+side*20*unit,board.getY()-30*unit);
		Vertex edge=new Vertex(board.getX()+side*50*unit,board.getY());
		top.setFriction(0.1);
		edge.setFriction(0.1);
		
		Vertex leg1=new Vertex(board.getX()+side*unit,board.getY()+40*unit);
		Vertex leg2=new Vertex(board.getX()+side*50*unit,board.getY()+40*unit);
		Vertex leg3=new Vertex(board.getX()+side*20*unit,board.getY()+80*unit);
		leg1.setFriction(0.1);
		leg2.setFriction(0.1);
		leg3.setFriction(0.5);
		
		StickConstraint topTri1=new StickConstraint(41.5*unit,board,top);
		StickConstraint topTri2=new StickConstraint(40.1*unit,board,edge);
		StickConstraint topTri3=new StickConstraint(55.8*unit,edge,top);
		
		StickConstraint conn1=new StickConstraint(39.3*unit,board,leg1);
		StickConstraint conn2=new StickConstraint(39.4*unit,edge,leg2);
		
		StickConstraint legTri1=new StickConstraint(40.1*unit,leg1,leg2);
		StickConstraint legTri2=new StickConstraint(65.7*unit,leg2,leg3);
		StickConstraint legTri3=new StickConstraint(49*unit,leg1,leg3);
		
		StickConstraint engEdge1=new StickConstraint(61.9*unit,engine,leg1);
		StickConstraint engEdge2=new StickConstraint(50*unit,engine,top);
		
		vs.addVertex(top,gid);
		vs.addVertex(edge,gid);
		
		vs.addVertex(leg1,gid);
		vs.addVertex(leg2,gid);
		vs.addVertex(leg3,gid);
		
		vs.addCollisionConstraint(topTri1,gid);
		
		vs.addCollisionConstraint(topTri2,gid);
		vs.addCollisionConstraint(topTri3,gid);
		
		vs.addConstraint(conn1);
		vs.addConstraint(conn2);
		
		vs.addCollisionConstraint(legTri1,gid);
		
		vs.addCollisionConstraint(legTri2,gid);
		vs.addCollisionConstraint(legTri3,gid);
		
		vs.addCollisionConstraint(engEdge1,gid);
		vs.addCollisionConstraint(engEdge2,gid);
	}
}

class DemoTask extends VerletTask
{
	private Vertex _e1,_e2,_b;
	private double _r,_phase,_w;
	private VerletScene _vs;
	
	public DemoTask(VerletScene vs,Vertex e1,Vertex e2,Vertex board,double radius,double initPhase,double w)
	{
		_vs=vs;
		_e1=e1;
		_e2=e2;
		_b=board;
		_r=radius;
		_phase=initPhase;
		_w=w;
	}

	@Override
	public void run() 
	{
		//_vs.freeze(1);
		//if(_b.getY()>535)
		//{
			engineRotation();
		//}
		applyGravity(10/_vs.getFrameRate());
	}
	private void engineRotation()
	{
		Vector2D rotate=new Vector2D(0,_r);
		rotate.rotate(_phase);
		_e1.appendOfferset(_b.getPosVector().add(rotate).subtract(_e1.getPosVector()));
		_e2.appendOfferset(_b.getPosVector().subtract(rotate).subtract(_e2.getPosVector()));
		_phase+=_w;
		_phase%=2*Math.PI;
	}
	private void applyGravity(double g) 
	{
		LinkedList<Vertex> vertex=_vs.getAllVertex();
		for(Vertex v:vertex)
		{
			v.setVy(v.getVy()+g);
		}
	}
}
