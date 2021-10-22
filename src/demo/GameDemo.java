package demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.SCVCollisionPoint;
import engine.StickConstraint;
import engine.Vector2D;
import engine.VerletScene;
import engine.VerletTask;
import engine.VerletUtil;
import engine.Vertex;

public class GameDemo extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public static void main(String args[])
	{
		GameFrame gf=new GameFrame();
		gf.setFrameRate(30);
		VerletScene vs;
		vs=init(gf);
		
		/*
		DebugFrame df=new DebugFrame();
		df.setVerletScene(vs);
		df.setFrameRate(10);
		df.playScene();
		*/
	}
	
	private static VerletScene init(GameFrame gf)
	{
		double unit=10;
		
		GamePhyScene vs=new GamePhyScene();
		vs.setFrameRate(60);
		vs.setIterationTime(10);
		
		setAWall(0,0,gf.getWidth()-20,0,vs);
		setAWall(gf.getWidth()-20,0,gf.getWidth()-20,gf.getHeight()-50,vs);
		setAWall(0,gf.getHeight()-50,gf.getWidth()-20,gf.getHeight()-50,vs);
		setAWall(0,0,0,gf.getHeight()-50,vs);
		
		Doll doll1=new Doll(150,200,unit);
		doll1.setColor(Color.gray);
		DollControler dollCon1=new DollControler(gf, doll1, vs,0.5,3,KeyEvent.VK_W,KeyEvent.VK_A,KeyEvent.VK_S,KeyEvent.VK_D);
		
		Doll doll2=new Doll(550,200,unit);
		doll2.setColor(Color.LIGHT_GRAY);
		DollControler dollCon2=new DollControler(gf, doll2, vs,0.5,3,KeyEvent.VK_I,KeyEvent.VK_J,KeyEvent.VK_K,KeyEvent.VK_L);
		
		vs.addADoll(doll1,0);
		vs.addADoll(doll2,0);
		
		vs.addTask(new DrawCollision(vs));
		
		gf.setScene(vs);
		gf.play();
		return vs;
	}
	
	private static void setAWall(double x1,double y1,double x2,double y2,GamePhyScene vs)
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
}
class DrawCollision extends VerletTask
{
	private VerletScene _vs;
	
	public DrawCollision(VerletScene vs)
	{
		super();
		_vs=vs;
	}
	@Override
	public void run() 
	{
		LinkedList<SCVCollisionPoint> cp=_vs.getCollisionPoint();
		for(SCVCollisionPoint colp:cp)
		{
			if(colp==null||!(colp.getVertex() instanceof DollVertex))
			{
				return;
			}
			if(!(colp.getStickConstraint() instanceof DollConstraint))
			{
				return;
			}
			
			DollVertex v=(DollVertex)colp.getVertex();
			DollConstraint dc=(DollConstraint)colp.getStickConstraint();
			DollMainVertex mv1;
			DollMainVertex mv2;
			
			if(v instanceof DollMainVertex)
			{
				mv1=(DollMainVertex)v;
			}
			else
			{
				mv1=(DollMainVertex)v.getParent();
			}
			mv2=(DollMainVertex)dc.getParent();
			
			ArrayList<Doll> dlist=((GamePhyScene)_vs).getDollList();
			Doll d1=dlist.get(0);
			Doll d2=dlist.get(1);
			
			if(d1.contain(mv1))
			{
				if(d2.contain(mv2))
				{
					Vector2D bounceF=colp.getNormalV();
					bounceF.setLength(0.3);
					
					if(d2.isFragile(mv2))
					{
						Doll d=(Doll)mv2.getRoot();
						d.setHealth(d.getHealth()-1);
						mv2.getHit(Color.red);
						
						mv2.appendForce(bounceF.multiply(mv2.getMass()));
						
						if(d.getHealth()==0)
						{
							d.collapseIn(_vs);
						}
					}
					
					bounceF.reverse();
					if(d1.isFragile(mv1))
					{
						Doll d=(Doll)mv1.getRoot();
						d.setHealth(d.getHealth()-1);
						mv1.getHit(Color.red);
						
						mv1.appendForce(bounceF.multiply(mv1.getMass()));
						
						if(d.getHealth()==0)
						{
							d.collapseIn(_vs);
						}
					}
				}
			}
			else if(d2.contain(mv1))
			{
				if(d1.contain(mv2))
				{
					Vector2D bounceF=colp.getNormalV();
					bounceF.setLength(0.3);
					
					if(d1.isFragile(mv2))
					{
						Doll d=(Doll)mv2.getRoot();
						d.setHealth(d.getHealth()-1);
						mv2.getHit(Color.red);
						
						mv2.appendForce(bounceF.multiply(mv2.getMass()));
						
						if(d.getHealth()==0)
						{
							d.collapseIn(_vs);
						}
					}
					
					bounceF.reverse();
					if(d2.isFragile(mv1))
					{
						Doll d=(Doll)mv1.getRoot();
						d.setHealth(d.getHealth()-1);
						mv1.getHit(Color.red);
						
						mv1.appendForce(bounceF.multiply(mv1.getMass()));
						
						if(d.getHealth()==0)
						{
							d.collapseIn(_vs);
						}
					}
				}
			}
		}
	}
}

class GamePhyScene extends VerletScene
{
	private ArrayList<Doll> dollG;
	private ArrayList<DollMainVertex> _bc;
	
	public GamePhyScene()
	{
		super();
		dollG=new ArrayList<Doll>();
		_bc=new ArrayList<DollMainVertex>();
	}
	
	public void addADoll(Doll d,int gid)
	{
		d.addDollTo(this, gid);
		_bc.addAll(d.getAllMainVertex());
		dollG.add(d);
	}
	
	public ArrayList<DollMainVertex> getAllBallCenter()
	{
		return _bc;
	}
	
	public ArrayList<Doll> getDollList()
	{
		return dollG;
	}
}

class GameFrame extends JFrame
{
	private Timer t;
	private double _fr;
	private GamePhyScene _vs;
	private VisibleEngine _ve;
	
	public GameFrame()
	{
		t=new Timer();
		
		setTitle("Doll");
		setSize(960,800);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setFrameRate(double value)
	{
		_fr=value;
	}
	
	public void setScene(GamePhyScene value)
	{
		_vs=value;
		_ve=new VisibleEngine(_vs);
		add(_ve);
	}
	
	public void play()
	{
		t.scheduleAtFixedRate(new FlushScreen(_vs),0L,(long)(1000/_fr));
	}
	
	public void reset()
	{
		t.cancel();
		_vs=null;
	}
	
	private class FlushScreen extends TimerTask
	{
		private GamePhyScene _vs;
		
		public FlushScreen(GamePhyScene vs)
		{
			_vs=vs;
		}
		
		@Override
		public void run() 
		{
			_ve.repaint();;
			_vs.update();
		}
		
	}
	
	private class VisibleEngine extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private GamePhyScene _vs;

		public VisibleEngine(GamePhyScene vs)
		{
			this.setBackground(Color.black);
			_vs=vs;
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			
			ArrayList<DollMainVertex> ballCenter=_vs.getAllBallCenter();
			
			for(DollMainVertex v:ballCenter)
			{
				double radius=v.getRadius();
				g.setColor(v.getColor());
				
				g.fillArc(
						(int)Math.round(v.getX()-radius),
						(int)Math.round(v.getY()-radius),
						(int)(2*radius),(int)(2*radius),
						0,360);
			}
		}
	}
}