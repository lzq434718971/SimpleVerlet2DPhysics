package engine;

/*
 * 
 * @author 86158
 * BVH算法用到的二叉树数据结构，且满足若有叶子节点则必有两个叶子节点
 *
 */
public class BVHNode 
{
	private BVHNode _left;
	private BVHNode _right;
	private BVHNode _parent;
	private DRectangle _bdBox;
	private CollisionObject _cObj;
	
	/** 创建BVH树节点，一般作为叶节点 **/
	public BVHNode(CollisionObject cObj)
	{
		_left=null;
		_right=null;
		_parent=null;
		_cObj=cObj;
		_bdBox=cObj.getBorderBox();
		cObj.setBvhNode(this);
	}
	
	/** 该构造函数仅能在n1与n2均不为null时使用,建立一个中间节点 **/
	private BVHNode(BVHNode n1,BVHNode n2)
	{
		_left=n1;
		_right=n2;
		n1._parent=this;
		n2._parent=this;
		_cObj=null;
		_bdBox=n1.getBorderBox().getTheMerged(n2.getBorderBox());
	}
	
	/** 该类的静态factory方法，用这种方法生成树能够保证当有叶节点时必有两个叶节点 **/
	public static BVHNode getTheMerged(BVHNode n1,BVHNode n2)
	{
		if((n1==null)&&(n2==null))
		{
			return null;
		}
		else if(n1==null)
		{
			return n2;
		}
		else if(n2==null)
		{
			return n1;
		}
		else
		{
			return new BVHNode(n1,n2);
		}
	}
	
	/** 从当前节点开始往上更新树 **/
	public void updateUp()
	{
		if(isLeaf())
		{
			_bdBox=_cObj.getBorderBox();
		}
		else
		{
			_bdBox=_left.getBorderBox().getTheMerged(_right.getBorderBox());
		}
		//迭代到根节点
		if(_parent!=null)
		{
			_parent.updateUp();
		}
	}
	
	/*
	 * 两组get set方法,能够保证set时该BVH树仍能够正常使用
	 */
	public BVHNode getLeft() 
	{
		return _left;
	}
	
	public void setLeft(BVHNode value)
	{
		if(value==null)
		{
			_right._parent=_parent;
			_right.updateUp();
			_left._parent=null;
			_left=null;
			_right=null;
			_parent=null;
			_bdBox=null;
		}
		else
		{
			_left._parent=null;
			_left=value;
			updateUp();
		}
	}
	
	public DRectangle getBorderBox() 
	{
		return _bdBox.clone();
	}
	
	public BVHNode getRight() {
		return _right;
	}
	public void setRight(BVHNode value)
	{
		if(value==null)
		{
			_left._parent=_parent;
			_left.updateUp();
			_right._parent=null;
			_left=null;
			_right=null;
			_parent=null;
			_bdBox=null;
		}
		else
		{
			_right._parent=null;
			_right=value;
			updateUp();
		}
	}
	
	/** 判断是否为叶节点 **/
	public boolean isLeaf() 
	{
		return (_left==null)&&(_right==null);
	}
	
	public CollisionObject getCollisionObj() {
		if(isLeaf())
		{
			return _cObj;
		}
		return null;
	}
	
	/** 设置collisionObj，在设置时能完成更新,但是只有叶节点能够完成此操作 **/
	public void setCollisionObj(CollisionObject value) 
	{
		if(isLeaf())
		{
			_cObj = value;
			updateUp();
		}
	}
	
	/** 获取父节点 **/
	public BVHNode getParent() 
	{
		return _parent;
	}
	
	/** 克隆以当前节点为根的树 **/
	public BVHNode clone()
	{
		if(isLeaf())
		{
			return new BVHNode(_cObj);
		}
		else
		{
			return BVHNode.getTheMerged(_left.clone(), _right.clone());
		}
	}
}
