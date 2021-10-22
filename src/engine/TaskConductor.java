package engine;

import java.util.LinkedList;

/**
 * 
 * @author 86158
 * �ܹ���֯�������Ӧʵ�ֵĽӿ�
 *
 */
public interface TaskConductor 
{
	public void addTask(VerletTask task);
	public void removeTask(VerletTask task);
	public void insertTask(VerletTask task,int index);
	public LinkedList<VerletTask> getTaskList();
}
