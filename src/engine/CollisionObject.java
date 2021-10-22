package engine;

import java.awt.Rectangle;

/**
 * 
 * @author 86158
 * ��update����ײ����
 *
 */
public abstract class CollisionObject extends TimeLineObject implements Colideable
{
	protected BVHNode _bvhNode;
	
	public abstract DRectangle getBorderBox();

	public abstract BVHNode getBvhNode();

	public abstract void setBvhNode(BVHNode _value);
}
