package demo;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import engine.VerletScene;
import engine.VerletTask;

public class DollControler 
{
	private Doll _doll;
	private VerletScene _vs;
	
	private MoveOnYAxis upCon;
	private MoveOnXAxis leftCon;
	private MoveOnYAxis downCon;
	private MoveOnXAxis rightCon;
	
	public DollControler(Component listener,Doll target,VerletScene vs,double speed,double limit,int upKey,int leftKey,int downKey,int rightKey)
	{
		_doll=target;
		_vs=vs;
		
		upCon=new MoveOnYAxis(target,-speed,limit);
		leftCon=new MoveOnXAxis(target,-speed,limit);
		rightCon=new MoveOnXAxis(target,speed,limit);
		downCon=new MoveOnYAxis(target,speed,limit);
		
		vs.addTask(upCon);
		vs.addTask(leftCon);
		vs.addTask(downCon);
		vs.addTask(rightCon);
		
		listener.addKeyListener(
								new KeyAdapter()
									{
										public void keyPressed(KeyEvent e)
										{
											if(e.getKeyCode()==upKey)
											{
												upCon.setAvailable(true);
											}
											else if(e.getKeyCode()==leftKey)
											{
												leftCon.setAvailable(true);
											}
											else if(e.getKeyCode()==downKey)
											{
												downCon.setAvailable(true);
											}
											else if(e.getKeyCode()==rightKey)
											{
												rightCon.setAvailable(true);
											}
										}
			
										public void keyReleased(KeyEvent e)
										{
											if(e.getKeyCode()==upKey)
											{
												upCon.setAvailable(false);
											}
											else if(e.getKeyCode()==leftKey)
											{
												leftCon.setAvailable(false);
											}
											else if(e.getKeyCode()==downKey)
											{
												downCon.setAvailable(false);
											}
											else if(e.getKeyCode()==rightKey)
											{
												rightCon.setAvailable(false);
											}
										}
									}
								);
	}
	
	private class MoveOnYAxis extends VerletTask
	{
		private Doll _doll;
		private double _s;
		private boolean _ava;
		private double _l;
		
		public MoveOnYAxis(Doll doll,double speed,double limit)
		{
			_doll=doll;
			_s=speed;
			_l=limit;
		}

		@Override
		public void run() 
		{
			if(_ava&&((_doll.getHead().getVy()*_s<0)||Math.abs(_doll.getHead().getVy())<_l))
			{
				_doll.moveY(_s);
			}
		}

		public boolean isAvailable() {
			return _ava;
		}

		public void setAvailable(boolean value) {
			_ava = value;
		}
	}
	
	private class MoveOnXAxis extends VerletTask
	{
		private Doll _doll;
		private double _s;
		private boolean _ava;
		private double _l;
		
		public MoveOnXAxis(Doll doll,double speed,double limit)
		{
			_doll=doll;
			_s=speed;
			_l=limit;
		}

		@Override
		public void run() 
		{
			if(_ava&&((_doll.getHead().getVx()*_s<0)||Math.abs(_doll.getHead().getVx())<_l))
			{
				_doll.moveX(_s);
			}
		}
		
		public boolean isAvailable() {
			return _ava;
		}

		public void setAvailable(boolean value) {
			_ava = value;
		}
	}
}
