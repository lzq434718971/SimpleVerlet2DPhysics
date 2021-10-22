package demo;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JFrame;

import engine.RotationConstraint;
import engine.StickConstraint;
import engine.Vector2D;
import engine.VerletScene;
import engine.VerletTask;
import engine.VerletUtil;
import engine.Vertex;

public class VerletPhysics extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String args[])
	{
		VerletScene vs=new VerletScene();
		vs.setFrameRate(60);
		vs.setIterationTime(5);

		double testF=0.3;
		double testB=3;
		
		Vertex vl=new Vertex(100,250);
		Vertex vr=new Vertex(500,250);
		StickConstraint sc=new StickConstraint(400,vl,vr);
		//vl.setIsDynamic(false);
		//vr.setIsDynamic(false);
		vl.setMass(20);
		vr.setMass(20);
		vs.addVertex(vl);
		vs.addVertex(vr);
		vs.addCollisionConstraint(sc);
		
		Vertex c1=new Vertex(250,100);
		Vertex c2=new Vertex(300,100);
		vs.addVertex(c1);
		vs.addVertex(c2);
		c1.setIsDynamic(false);
		c2.setIsDynamic(false);
		c1.setFriction(1);
		c2.setFriction(1);
		
		addACircle((c1.getX()+c2.getX())/2,c1.getY(),30,32,vs);
		
		addABox(100,10,50,50,vs);
		
		//¾²Ì¬³¡¾°ÉèÖÃ
		
		//µ×
		setAWall(-1,600,601,600,vs);
		//¶¥
		setAWall(-1,0,601,0,vs);
		//×ó
		setAWall(10,-1,10,601,vs);
		//ÓÒ
		setAWall(600,-1,600,601,vs);
		
		//ÓÒÐ±ÆÂ
		setAWall(601,400,100,601,vs);
		//×óÐ±ÆÂ
		setAWall(-1,250,400,601,vs);
		
		TestTask task=new TestTask(vs);
		vs.addTask(task);
		DebugFrame df=new DebugFrame();
		df.setVerletScene(vs);
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
	private static void addABox(double x,double y,double w,double h,VerletScene vs)
	{
		Vertex v1=new Vertex(x,y);
		Vertex v2=new Vertex(x+w,y);
		Vertex v3=new Vertex(x+w,y+h);
		Vertex v4=new Vertex(x,y+h);
		StickConstraint sc1=new StickConstraint(w,v1,v2);
		StickConstraint sc2=new StickConstraint(h,v2,v3);
		StickConstraint sc3=new StickConstraint(w,v3,v4);
		StickConstraint sc4=new StickConstraint(h,v1,v4);
		StickConstraint sc5=new StickConstraint(VerletUtil.getDistance(v1, v3),v1,v3);
		StickConstraint sc6=new StickConstraint(VerletUtil.getDistance(v2, v4),v2,v4);
		vs.addVertex(v1);
		vs.addVertex(v2);
		vs.addVertex(v3);
		vs.addVertex(v4);
		vs.addCollisionConstraint(sc1);
		vs.addCollisionConstraint(sc2);
		vs.addCollisionConstraint(sc3);
		vs.addCollisionConstraint(sc4);
		vs.addConstraint(sc5);
		vs.addConstraint(sc6);
		v1.setFriction(0);
		v2.setFriction(0);
		v3.setFriction(0);
		v4.setFriction(0);
		v1.setMass(1000);
		v2.setMass(1000);
		v3.setMass(1000);
		v4.setMass(1000);
	}
	
	private static void addACircle(double x,double y,double radius,int n,VerletScene vs)
	{
		double pRadian=2*Math.PI/n;
		Vector<Vertex> chain=new Vector<Vertex>(n);
		for(int i=0;i<n;i++)
		{
			double rx=radius*Math.cos(pRadian*(i))+x;
			double ry=radius*Math.sin(pRadian*(i))+y;
			chain.add(new Vertex(rx,ry));
			chain.get(i).setFriction(0.2);
			chain.get(i).setMass(1);
			chain.get(i).setDamping(0.99);;
			vs.addVertex(chain.get(i));
		}
		for(int i=0;i<n-1;i++)
		{
			Vertex v1=chain.get(i);
			Vertex v2=chain.get(i+1);
			StickConstraint sc=new StickConstraint(VerletUtil.getDistance(v1,v2),v1,v2);
			vs.addCollisionConstraint(sc);
		}
		Vertex v1=chain.get(n-1);
		Vertex v2=chain.get(0);
		StickConstraint sc=new StickConstraint(VerletUtil.getDistance(v1,v2),v1,v2);
		vs.addCollisionConstraint(sc);
	}
}

class TestTask extends VerletTask
{
	private VerletScene _vs;
	
	public TestTask(VerletScene vs)
	{
		_vs=vs;
	}

	@Override
	public void run() 
	{
		applyGravity(0.1);
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