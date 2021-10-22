package engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * 
 * @author 86158
 * ��Ҫʵ���������������
 *
 */
public class VerletScene extends TimeLineObject implements TaskConductor
{
	//֡��
	private double _fRate;
	
	private int _iterTime;
	
	private double _safeDis;
	
	private ArrayList<LinkedList<Vertex>> _vertex;
	private ArrayList<LinkedList<StickConstraint>> _collisionGroup;
	private LinkedList<VertexConstraint> _constraint;
	private ArrayList<LinkedList<Integer>> _collisionClass;
	private LinkedList<VerletTask> _taskList;
	
	private LinkedList<SCVCollisionPoint> _collisionP;
	
	private BVHNode _bvhTree;
	private Vector2D heu1;
	private Vector2D heu2;
	
	private int selfAdaptThreshold;
	private int deepCollisionCount;
	
	private int testCount;
	
	public VerletScene(double frameRate)
	{
		_fRate=frameRate;
		generalInit();
	}
	
	/** ֡��Ĭ��30 **/
	public VerletScene()
	{
		_fRate=30;
		generalInit();
	}
	//��ʼ��
	private void generalInit()
	{
		_safeDis=VerletGlobalConfig.SAFE_DIS;
		_vertex=new ArrayList<LinkedList<Vertex>>(0);
		_vertex.add(new LinkedList<Vertex>());
		_constraint=new LinkedList<VertexConstraint>();
		_collisionGroup=new ArrayList<LinkedList<StickConstraint>>();
		_collisionGroup.add(new LinkedList<StickConstraint>());
		_collisionClass=new ArrayList<LinkedList<Integer>>();
		_collisionClass.add(new LinkedList<Integer>());
		_collisionClass.get(0).add(Integer.valueOf(0));
		_taskList=new LinkedList<VerletTask>();
		_collisionP=new LinkedList<SCVCollisionPoint>();
		selfAdaptThreshold=VerletGlobalConfig.COLLISION_SELF_ADAPT_THRESHOLD;
		setIterationTime(1);
	}
	
	/** ʹ��update�ƽ�����ʱ�� **/
	public void update()
	{
		//ִ�������б��е�����,�������б��жԳ������в������Ա�֤ģ�����ȷ
		conductTask();
		
		//�����ײ���¼�������
		_collisionP.clear();
		
		//����Ӧ��ײ�����õ��ļ�����
		deepCollisionCount=0;
		
		//��ǰ����ײ���и��£����Լ��ٸ���Լ��ʱ��ɵĴ�͸����
		updateCollision();
		
		//��ͨ��ײ���
		for(int i=0;i<_iterTime;i++)
		{
			updateConstraint();
			updateCollision();
		}
		
		
		//����Ӧ��ײ��⣬���Խ�ʡһЩ��ײ�����������ڷ�����͸ʱ���ܻ���ɸ�Ϊ���ĺ��
		/*
		int sizeOff=deepCollisionCount;
		int preSize=deepCollisionCount;
		int peaceTime=sizeOff==0?1:0;
		while(peaceTime<=selfAdaptThreshold)
		{
			for(int i=0;i<_iterTime;i++)
			{
				updateConstraint();
			}
			updateCollision();
			sizeOff=deepCollisionCount-preSize;
			preSize=deepCollisionCount;
			if(sizeOff==0)
			{
				peaceTime++;
			}
			else
			{
				peaceTime=0;
			}
		}
		*/
		//���Զ����λ�ƽ��и���
		updateMove();
	}
	//ִ�г����е��Զ�������
	protected void conductTask()
	{
		LinkedList<VerletTask> tlCopy=(LinkedList<VerletTask>) _taskList.clone();
		Iterator<VerletTask> iterator=tlCopy.iterator();
		while(iterator.hasNext())
		{
			VerletTask task=iterator.next();
			task.run();
		}
	}
	//����λ��
	protected void updateMove()
	{
		for(LinkedList<Vertex> g:_vertex)
		{
			for(Vertex v:g)
			{
				v.update();
			}
		}
	}
	//������ײ
	protected void updateCollision()
	{	
		//��ȡ������ײ��
		for(int i=0;i<_vertex.size()&&i<_collisionGroup.size();i++)
		{
			//��ȡ����ײ����������ײ��vertex
			LinkedList<Vertex> vGroup=new LinkedList<Vertex>();
			for(int gid:_collisionClass.get(i))
			{
				vGroup.addAll(_vertex.get(gid));
			}
			//��ǰ��ײ���µ�stick
			LinkedList<StickConstraint> scGroup=_collisionGroup.get(i);
			
			//BVH��ײ���
			//����ײ�����н�����������ʱ��û����Ҫ������ײ
			if(vGroup.size()<=2)
			{
				continue;
			}
			//�����������ʱ���������������BVH����
			if((heu1!=null)&&(heu2!=null))
			{
				_bvhTree=buildBVH(vGroup,heu1,heu2);
				heu1=_bvhTree.getLeft().getBorderBox().getCenter();
				heu2=_bvhTree.getRight().getBorderBox().getCenter();
			}
			else //�������㲻����ʱ������ײ���е�ǰ��������Ϊ����������
			{
				heu1=vGroup.get(0).getPosVector();
				heu2=vGroup.get(1).getPosVector();
				_bvhTree=buildBVH(vGroup,heu1,heu2);
				heu1=_bvhTree.getLeft().getBorderBox().getCenter();
				heu2=_bvhTree.getRight().getBorderBox().getCenter();
			}
			//����ײ���е�����stick������ײ��⣬��ǰ����BVH�����㷨����
			for(StickConstraint c:scGroup)
			{
				collideWithTree(c,_bvhTree);
			}
		}
		
	}
	private void collideWithTree(StickConstraint sc,BVHNode tree)
	{
		//�жϵ�ǰ�ڵ��Ƿ��п�����stick��ײ
		if(tree.getBorderBox().collide(sc.getBorderBox()))
		{
			//�����ǰ�ڵ��Ѿ���Ҷ�ڵ㣨ָ��һ������Ŀ���ײ����
			if(tree.isLeaf())
			{
				/*
				 * �ų������������
				 * 1.stick������vertex���Լ�����Ҫ���Լ���ײ
				 * 2.stick���vertex���Ǿ�̬�ģ�����Ҫ������ײ���
				 */
				if(!sc.contain((Vertex)tree.getCollisionObj())&&(sc.isDynamic()||((Vertex)tree.getCollisionObj()).isDynamic()))
				{
					//�������ײ��Ϊ
					StickCollideWithVertex scv=new StickCollideWithVertex();
					scv.setIterTime(2);
					scv.setSafeDis(_safeDis);
					scv.setFrictionFactor(1);
					scv.setBounce(1);
					SCVCollisionPoint cp=scv.collide((Vertex)tree.getCollisionObj(), sc);
					
					if(cp!=null)
					{
						//��¼��ײ����Ϣ
						_collisionP.add(cp);
						
						//��������Ӧ������ײ��������Ϣ
						if(cp.isDeepCollision())
						{
							deepCollisionCount++;
						}
					}
					
					//��ײ������λ�ƣ���Ҫ����bvh������
					tree.updateUp();
					sc.getV1().getBvhNode().updateUp();
					sc.getV2().getBvhNode().updateUp();
				}
			}
			else
			{
				//��������������ײ���ĵ���
				collideWithTree(sc,tree.getLeft());
				collideWithTree(sc,tree.getRight());
			}
		}
	}
	
	//������ײ��Լ���ͷ���ײ��Լ��
	protected void updateConstraint()
	{
		//System.out.print("updateConstraint\n");
		for(VertexConstraint c:_constraint)
		{
			c.update();
		}
		for(LinkedList<StickConstraint> cGroup:_collisionGroup)
		{
			for(VertexConstraint c:cGroup)
			{
				c.update();
			}
		}
	}
	
	//��ɹ���BVH�������ĺ���
	private BVHNode buildBVH(LinkedList<Vertex> vGroup,Vector2D heu1,Vector2D heu2)
	{
		//���������ж�
		if(vGroup.size()==1)
		{
			return new BVHNode(vGroup.get(0));
		}
		else if(vGroup.size()==2)
		{
			return BVHNode.getTheMerged(new BVHNode(vGroup.get(0)),new BVHNode(vGroup.get(1)));
		}
		
		boolean isSteady;
		LinkedList<Vertex> g1=new LinkedList<Vertex>();
		LinkedList<Vertex> g2=new LinkedList<Vertex>();
		
		//��ʼ��
		Iterator<Vertex> iterator;
		iterator=vGroup.iterator();
		while (iterator.hasNext()) 
		{
		    Vertex v = iterator.next();
			Vector2D pos=v.getPosVector();
			if(
				pos.subtract(heu1).getLengthSQ()<pos.subtract(heu2).getLengthSQ() ||
				(!iterator.hasNext()&&g1.size()==0)
			  )
			{
				g1.add(v);
			}
			else
			{
				g2.add(v);
			}
		}
		
		//k-means����
		do
		{
			isSteady=true;
			Vector2D ave1=getCenter(g1);
			Vector2D ave2=getCenter(g2);
			LinkedList<Vertex> toG2=new LinkedList<Vertex>();
			LinkedList<Vertex> toG1=new LinkedList<Vertex>();
			
			iterator = g1.iterator();
			while (iterator.hasNext()) 
			{
			    Vertex v = iterator.next();
			    if(		
			    	v.getPosVector().subtract(ave1).getLengthSQ() >=
					v.getPosVector().subtract(ave2).getLengthSQ()
				  )
			    {
			    	isSteady=false;
			    	iterator.remove();
			    	toG2.add(v);
			    }
			}
			
			iterator = g2.iterator();
			while (iterator.hasNext()) 
			{
			    Vertex v = iterator.next();
			    if(		
			    	v.getPosVector().subtract(ave2).getLengthSQ() >=
					v.getPosVector().subtract(ave1).getLengthSQ()
				  )
			    {
			    	isSteady=false;
			    	iterator.remove();
			    	toG1.add(v);
			    }
			}
			
			g1.addAll(toG1);
			g2.addAll(toG2);
			
		}while(!isSteady);
		
		//����bvh��
		BVHNode sub1 = null,sub2=null;
		if(g1.size()>=2)
		{
			sub1=buildBVH(g1,g1.get(0).getPosVector(),g1.get(1).getPosVector());
		}
		else if(g1.size()==1)
		{
			sub1=new BVHNode(g1.get(0));
		}
		if(g2.size()>=2)
		{
			sub2=buildBVH(g2,g2.get(0).getPosVector(),g2.get(1).getPosVector());
		}
		else if(g2.size()==1)
		{
			sub2=new BVHNode(g2.get(0));
		}
		return BVHNode.getTheMerged(sub1, sub2);
	}
	
	/*
	 * һЩ�����û��ĳ�������ĺ���
	 */
	public void addVertex(Vertex v)
	{
		_vertex.get(0).add(v);
	}
	
	public void addVertex(Vertex v,int groupID)
	{
		if(groupID>=_vertex.size())
		{
			for(int i=_vertex.size();i<=groupID;i++)
			{
				_vertex.add(new LinkedList<Vertex>());
				_collisionGroup.add(new LinkedList<StickConstraint>());
				_collisionClass.add(new LinkedList<Integer>());
				_collisionClass.get(i).add(Integer.valueOf(i));
			}
		}
		_vertex.get(groupID).add(v);
	}
	
	public void addConstraint(VertexConstraint vc)
	{
		_constraint.add(vc);
	}
	
	public void addCollisionConstraint(StickConstraint value)
	{
		_collisionGroup.get(0).add(value);
	}
	public void addCollisionConstraint(StickConstraint value,int groupID)
	{
		if(groupID>=_collisionGroup.size())
		{
			for(int i=_collisionGroup.size();i<=groupID;i++)
			{
				_vertex.add(new LinkedList<Vertex>());
				_collisionGroup.add(new LinkedList<StickConstraint>());
				_collisionClass.add(new LinkedList<Integer>());
				_collisionClass.get(i).add(Integer.valueOf(i));
			}
		}
		_collisionGroup.get(groupID).add(value);
	}
	
	public void removeConstraint(VertexConstraint vc)
	{
		_constraint.remove(vc);
		
		if(vc instanceof StickConstraint)
		{
			for(LinkedList<StickConstraint> scg:_collisionGroup)
			{
				scg.remove(vc);
			}
		}
	}
	
	public double getFrameRate()
	{
		return _fRate;
	}
	public double setFrameRate(double value)
	{
		_fRate=value;
		return _fRate;
	}
	
	/** ����ָ�� **/
	public LinkedList<Vertex> getAllVertex()
	{
		LinkedList<Vertex> re=new LinkedList<Vertex>();
		for(LinkedList<Vertex> g:_vertex)
		{
			re.addAll(g);
		}
		return re;
	}
	
	/** ����ָ�� **/
	public LinkedList<VertexConstraint> getAllVertexConstraint()
	{
		LinkedList<VertexConstraint> re=(LinkedList<VertexConstraint>) _constraint.clone();
		for(LinkedList<StickConstraint> g:_collisionGroup)
		{
			re.addAll(g);
		}
		return re;
	}
	
	/** ֱ����������stick��vertex�᲻����ײ **/
	public void setCollisionGroup(int g1,int g2,boolean state)
	{
		setTargetVertex(g1,g2,state);
		setTargetVertex(g2,g1,state);
	}
	/** ���������gid���stick�᲻����tarG���vertex��ײ**/
	public void setTargetVertex(int gid,int tarG,boolean state)
	{
		if(state)
		{
			if(!_collisionClass.get(gid).contains(Integer.valueOf(tarG)))
			{
				_collisionClass.get(gid).add(Integer.valueOf(tarG));
			}
		}
		else
		{
			if(_collisionClass.get(gid).contains(Integer.valueOf(tarG)))
			{
				_collisionClass.get(gid).remove(Integer.valueOf(tarG));
			}
		}
	}
	
	public int setIterationTime(int value)
	{
		_iterTime=Math.max(value,1);
		return _iterTime;
	}
	public int getIterationTime()
	{
		return _iterTime;
	}
	
	
	private Vector2D getCenter(LinkedList<Vertex> group)
	{
		Vector2D re=new Vector2D();
		double totalX,totalY;
		totalX=0;
		totalY=0;
		for(Vertex v:group)
		{
			totalX+=v.getX();
			totalY+=v.getY();
		}
		re.setX(totalX/group.size());
		re.setY(totalY/group.size());
		return re;
	}
	
	public BVHNode getBVHTree()
	{
		return _bvhTree.clone();
	}

	@Override
	public void addTask(VerletTask task) {
		_taskList.add(task);
	}

	@Override
	public void removeTask(VerletTask task) {
		_taskList.remove(task);
	}

	@Override
	public void insertTask(VerletTask task, int index) {
		_taskList.add(index, task);
	}

	@Override
	public LinkedList<VerletTask> getTaskList() {
		return (LinkedList<VerletTask>) _taskList.clone();
	}

	public double getSafeDis() {
		return _safeDis;
	}

	public void setSafeDis(double value) {
		_safeDis = value;
	}
	
	/** �ȶ����������е������ٶȱ�Ϊ0 **/
	public void freeze(int iterT)
	{
		updateCollision();
		for(int i=0;i<iterT-1;i++)
		{
			updateConstraint();
			updateCollision();
		}
		for(Vertex v:getAllVertex())
		{
			v.setPosition(v.getX(),v.getY());
		}
	}
	
	public LinkedList<SCVCollisionPoint> getCollisionPoint()
	{
		return (LinkedList<SCVCollisionPoint>)_collisionP.clone();
	}
}