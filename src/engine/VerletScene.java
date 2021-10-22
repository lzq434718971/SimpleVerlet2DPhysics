package engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * 
 * @author 86158
 * 主要实现物理场景管理的类
 *
 */
public class VerletScene extends TimeLineObject implements TaskConductor
{
	//帧数
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
	
	/** 帧率默认30 **/
	public VerletScene()
	{
		_fRate=30;
		generalInit();
	}
	//初始化
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
	
	/** 使用update推进场景时间 **/
	public void update()
	{
		//执行任务列表中的任务,在任务列表中对场景进行操作可以保证模拟的正确
		conductTask();
		
		//清空碰撞点记录方便更新
		_collisionP.clear();
		
		//自适应碰撞函数用到的计数器
		deepCollisionCount=0;
		
		//提前对碰撞进行更新，可以减少更新约束时造成的穿透现象
		updateCollision();
		
		//普通碰撞检测
		for(int i=0;i<_iterTime;i++)
		{
			updateConstraint();
			updateCollision();
		}
		
		
		//自适应碰撞检测，可以节省一些碰撞检测次数，但在发生穿透时可能会造成更为糟糕的后果
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
		//最后对顶点的位移进行更新
		updateMove();
	}
	//执行场景中的自定义任务
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
	//更新位置
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
	//计算碰撞
	protected void updateCollision()
	{	
		//获取所有碰撞组
		for(int i=0;i<_vertex.size()&&i<_collisionGroup.size();i++)
		{
			//获取该碰撞组允许发生碰撞的vertex
			LinkedList<Vertex> vGroup=new LinkedList<Vertex>();
			for(int gid:_collisionClass.get(i))
			{
				vGroup.addAll(_vertex.get(gid));
			}
			//当前碰撞组下的stick
			LinkedList<StickConstraint> scGroup=_collisionGroup.get(i);
			
			//BVH碰撞检测
			//当碰撞场景中仅有两个顶点时，没有需要检测的碰撞
			if(vGroup.size()<=2)
			{
				continue;
			}
			//当启发点存在时，根据启发点进行BVH划分
			if((heu1!=null)&&(heu2!=null))
			{
				_bvhTree=buildBVH(vGroup,heu1,heu2);
				heu1=_bvhTree.getLeft().getBorderBox().getCenter();
				heu2=_bvhTree.getRight().getBorderBox().getCenter();
			}
			else //当启发点不存在时，以碰撞组中的前两个点作为两个启发点
			{
				heu1=vGroup.get(0).getPosVector();
				heu2=vGroup.get(1).getPosVector();
				_bvhTree=buildBVH(vGroup,heu1,heu2);
				heu1=_bvhTree.getLeft().getBorderBox().getCenter();
				heu2=_bvhTree.getRight().getBorderBox().getCenter();
			}
			//对碰撞组中的所有stick进行碰撞检测，提前利用BVH划分算法过滤
			for(StickConstraint c:scGroup)
			{
				collideWithTree(c,_bvhTree);
			}
		}
		
	}
	private void collideWithTree(StickConstraint sc,BVHNode tree)
	{
		//判断当前节点是否有可能与stick碰撞
		if(tree.getBorderBox().collide(sc.getBorderBox()))
		{
			//如果当前节点已经是叶节点（指向一个具体的可碰撞对象）
			if(tree.isLeaf())
			{
				/*
				 * 排除掉两种情况：
				 * 1.stick包含该vertex，自己不需要与自己碰撞
				 * 2.stick与该vertex都是静态的，不需要进行碰撞检测
				 */
				if(!sc.contain((Vertex)tree.getCollisionObj())&&(sc.isDynamic()||((Vertex)tree.getCollisionObj()).isDynamic()))
				{
					//具体的碰撞行为
					StickCollideWithVertex scv=new StickCollideWithVertex();
					scv.setIterTime(2);
					scv.setSafeDis(_safeDis);
					scv.setFrictionFactor(1);
					scv.setBounce(1);
					SCVCollisionPoint cp=scv.collide((Vertex)tree.getCollisionObj(), sc);
					
					if(cp!=null)
					{
						//记录碰撞点信息
						_collisionP.add(cp);
						
						//用于自适应调整碰撞次数的信息
						if(cp.isDeepCollision())
						{
							deepCollisionCount++;
						}
					}
					
					//碰撞后发生了位移，需要更新bvh树数据
					tree.updateUp();
					sc.getV1().getBvhNode().updateUp();
					sc.getV2().getBvhNode().updateUp();
				}
			}
			else
			{
				//与整棵树进行碰撞检测的迭代
				collideWithTree(sc,tree.getLeft());
				collideWithTree(sc,tree.getRight());
			}
		}
	}
	
	//更新碰撞的约束和非碰撞的约束
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
	
	//完成构建BVH树工作的函数
	private BVHNode buildBVH(LinkedList<Vertex> vGroup,Vector2D heu1,Vector2D heu2)
	{
		//特殊条件判断
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
		
		//初始化
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
		
		//k-means分类
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
		
		//连接bvh树
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
	 * 一些面向用户的场景管理的函数
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
	
	/** 返回指针 **/
	public LinkedList<Vertex> getAllVertex()
	{
		LinkedList<Vertex> re=new LinkedList<Vertex>();
		for(LinkedList<Vertex> g:_vertex)
		{
			re.addAll(g);
		}
		return re;
	}
	
	/** 返回指针 **/
	public LinkedList<VertexConstraint> getAllVertexConstraint()
	{
		LinkedList<VertexConstraint> re=(LinkedList<VertexConstraint>) _constraint.clone();
		for(LinkedList<StickConstraint> g:_collisionGroup)
		{
			re.addAll(g);
		}
		return re;
	}
	
	/** 直接设置两组stick与vertex会不会碰撞 **/
	public void setCollisionGroup(int g1,int g2,boolean state)
	{
		setTargetVertex(g1,g2,state);
		setTargetVertex(g2,g1,state);
	}
	/** 单面地设置gid组的stick会不会与tarG组的vertex碰撞**/
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
	
	/** 稳定物体后令场景中的物体速度变为0 **/
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