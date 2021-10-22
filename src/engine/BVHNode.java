package engine;

/*
 * 
 * @author 86158
 * BVH�㷨�õ��Ķ��������ݽṹ������������Ҷ�ӽڵ����������Ҷ�ӽڵ�
 *
 */
public class BVHNode 
{
	private BVHNode _left;
	private BVHNode _right;
	private BVHNode _parent;
	private DRectangle _bdBox;
	private CollisionObject _cObj;
	
	/** ����BVH���ڵ㣬һ����ΪҶ�ڵ� **/
	public BVHNode(CollisionObject cObj)
	{
		_left=null;
		_right=null;
		_parent=null;
		_cObj=cObj;
		_bdBox=cObj.getBorderBox();
		cObj.setBvhNode(this);
	}
	
	/** �ù��캯��������n1��n2����Ϊnullʱʹ��,����һ���м�ڵ� **/
	private BVHNode(BVHNode n1,BVHNode n2)
	{
		_left=n1;
		_right=n2;
		n1._parent=this;
		n2._parent=this;
		_cObj=null;
		_bdBox=n1.getBorderBox().getTheMerged(n2.getBorderBox());
	}
	
	/** ����ľ�̬factory�����������ַ����������ܹ���֤����Ҷ�ڵ�ʱ��������Ҷ�ڵ� **/
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
	
	/** �ӵ�ǰ�ڵ㿪ʼ���ϸ����� **/
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
		//���������ڵ�
		if(_parent!=null)
		{
			_parent.updateUp();
		}
	}
	
	/*
	 * ����get set����,�ܹ���֤setʱ��BVH�����ܹ�����ʹ��
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
	
	/** �ж��Ƿ�ΪҶ�ڵ� **/
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
	
	/** ����collisionObj��������ʱ����ɸ���,����ֻ��Ҷ�ڵ��ܹ���ɴ˲��� **/
	public void setCollisionObj(CollisionObject value) 
	{
		if(isLeaf())
		{
			_cObj = value;
			updateUp();
		}
	}
	
	/** ��ȡ���ڵ� **/
	public BVHNode getParent() 
	{
		return _parent;
	}
	
	/** ��¡�Ե�ǰ�ڵ�Ϊ������ **/
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
