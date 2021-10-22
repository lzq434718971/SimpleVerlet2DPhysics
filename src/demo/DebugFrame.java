package demo;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.BVHNode;
import engine.VerletScene;
import engine.Vertex;
import engine.VertexConstraint;

import java.util.Timer;
import java.util.TimerTask;

public class DebugFrame extends JFrame{
	private VisibleEngine _ve;
	
	public DebugFrame()
	{
		setTitle("debug");
		setSize(600, 640);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setVerletScene(VerletScene scene)
	{
		VisibleEngine ve=new VisibleEngine();
		ve.setVerletScene(scene);
		this.getContentPane().add(ve);
		//ve.playScene();
		_ve=ve;
	}
	
	public void playScene()
	{
		_ve.playScene();
	}
	
	public void setFrameRate(double value)
	{
		_ve.setFrameRate(value);
	}
}

class VisibleEngine extends JPanel
{
	private VerletScene _scene;
	private double _fRate;
	private Timer t;
	
	VisibleEngine()
	{
		t=new Timer();
		setBackground(Color.white);
	}
	
	public void playScene()
	{
		t.scheduleAtFixedRate(new FlushScreen(this),0L,(long)(1000/_fRate));
	}
	
	public void setVerletScene(VerletScene scene)
	{
		_scene=scene;
		_fRate=scene.getFrameRate();
	}
	
	public VerletScene getScene()
	{
		return _scene;
	}
	
	public void setFrameRate(double value)
	{
		_fRate=value;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		g.setColor(Color.lightGray);
		for(int i=0;i<=getHeight();i+=100)
		{
			g.drawLine(0,i,getWidth(),i);
		}
		for(int i=0;i<=getWidth();i+=100)
		{
			g.drawLine(i,0,i,getHeight());
		}
		
		g.setColor(Color.blue);
		//paintBVHBox(_scene.getBVHTree(),g);
		
		for(Vertex v:_scene.getAllVertex())
		{
			int radius=5;
			
			g.setColor(Color.black);
			g.drawArc(
					(int)Math.round(v.getX()-radius/2),
					(int)Math.round(v.getY()-radius/2),
					(int)radius,(int)radius,
					0,360);
			
			g.setColor(Color.cyan);
			g.drawArc(
					(int)Math.round(v.getX()-v.getVx()-radius/2),
					(int)Math.round(v.getY()-v.getVy()-radius/2),
					(int)radius,(int)radius,
					0,360);
		}
		
		for(VertexConstraint c:_scene.getAllVertexConstraint())
		{
			g.setColor(Color.black);
			g.drawLine(
					(int)Math.round(c.getV1().getX()),
					(int)Math.round(c.getV1().getY()),
					(int)Math.round(c.getV2().getX()),
					(int)Math.round(c.getV2().getY())
					);
		}
	}
	private void paintBVHBox(BVHNode tree,Graphics g)
	{
		g.drawRect( (int)tree.getBorderBox().getX(),
					(int)tree.getBorderBox().getY(),
					(int)tree.getBorderBox().getWidth(),
					(int)tree.getBorderBox().getHeight()
				  );
		if(!tree.isLeaf())
		{
			paintBVHBox(tree.getLeft(),g);
			paintBVHBox(tree.getRight(),g);
		}
	}
	
	public void applyGravity(double g)
	{
		//System.out.print("1");
		for(Vertex v:_scene.getAllVertex())
		{
			//v.setVy(v.getVy()+g);
			v.setY(v.getY()+g);
		}
	}
	
	public void restrictInBox()
	{
		for(Vertex v:_scene.getAllVertex())
		{
			if(v.getY()>getParent().getHeight()&&v.getVy()>0)
			{
				double tempVy=v.getVy();
				v.setY(getParent().getHeight());
				v.setVy(-(0.2*tempVy));
			}
		}
	}
}

class FlushScreen extends TimerTask
{
	private VisibleEngine _ve;
	
	public FlushScreen(VisibleEngine ve)
	{
		_ve=ve;
	}

	@Override
	public void run() 
	{
		_ve.repaint();
		_ve.getScene().update();
	}
}
