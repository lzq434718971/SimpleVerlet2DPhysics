import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import engine.DRectangle;
import engine.Vector2D;
import engine.VerletUtil;

public class FunctionTest {
	public static void main(String args[])
	{
		
	}
}
/*
class ThreadA extends Thread
{
	private int[] _n;
	
	public ThreadA(int[] num)
	{
		_n=num;
	}
	
	public void run()
	{
		Lock lock=new ReentrantLock();
		lock.lock();
		for(int i=0;i<100;i++)
		{
			_n[0]++;
			System.out.print(_n[0]+"\n");
		}
		lock.unlock();
	}
}

class ThreadB extends TimerTask
{
	private int[] _n;
	
	public ThreadB(int[] num)
	{
		_n=num;
	}
	
	public void run()
	{
		_n[0]=-50000000;
		System.out.print("test\n");
	}
}

class ThreadC extends TimerTask
{
	private int[] _n;
	
	public ThreadC(int[] num)
	{
		_n=num;
	}
	
	public void run()
	{
		System.out.print(_n[0]+"\n");
	}
}
*/
