package engine;

/**
 * 
 * @author 86158
 * 本引擎的可run的任务类，继承后改写run运行用户代码
 *
 */
public abstract class VerletTask 
{
	public VerletTask() {};
	
	public abstract void run();
}
