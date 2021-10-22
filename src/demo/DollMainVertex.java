package demo;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import engine.StickConstraint;

public class DollMainVertex extends DollVertex implements DollMain
{
	private Color _oriC;
	private Color _c;
	private double _r;
	private StickConstraint _psc;
	private StickConstraint _nsc;
	
	private Timer animCon;
	
	public DollMainVertex(double x,double y)
	{
		super(x,y);
		animCon=new Timer();
	}
	
	@Override
	public Color getColor() 
	{
		return _c;
	}

	@Override
	public void setColor(Color value) 
	{
		_oriC=value;
		_c=value;
	}

	@Override
	public double getRadius() 
	{
		return _r;
	}

	@Override
	public double setRadius(double value) 
	{
		_r=value;
		return value;
	}

	@Override
	public StickConstraint getPreConstraint() 
	{
		return _psc;
	}

	@Override
	public StickConstraint getNextConstraint() 
	{
		return _nsc;
	}

	@Override
	public void setPreConstraint(StickConstraint value) 
	{
		_psc=value;
	}

	@Override
	public void setNextConstraint(StickConstraint value) 
	{
		_nsc=value;
	}
	
	public void getHit(Color hitColor)
	{
		_c=_oriC;
		animCon.schedule(new ColorChange(hitColor,0.01,(Doll)getRoot()),0L,10L);
	}
	private Color mixColor(Color c1,Color c2,double w1,double w2)
	{
		int r=(int) ((double)c1.getRed()*w1+(double)c2.getRed()*w2);
		int g=(int) ((double)c1.getGreen()*w1+(double)c2.getGreen()*w2);
		int b=(int) ((double)c1.getBlue()*w1+(double)c2.getBlue()*w2);
		return new Color(r,g,b);
	}
	
	private class ColorChange extends TimerTask
	{
		private double weight=1;
		private double s;
		private Color targetC;
		private Doll doll;
		
		public ColorChange(Color tc,double speed,Doll d)
		{
			targetC=tc;
			s=speed;
			doll=d;
		}

		@Override
		public void run() 
		{
			_c=mixColor(_oriC,targetC,1-weight,weight);
			weight-=s;
			doll.setInvincible(true);
			if(weight<=0)
			{
				doll.setInvincible(false);
				this.cancel();
			}
		}
		
	}
}
