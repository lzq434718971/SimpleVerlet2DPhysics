package engine;

import java.awt.Rectangle;

/**
 * 
 * @author 86158
 * 可update的碰撞对象
 *
 */
public abstract class CollisionObject extends TimeLineObject implements Colideable
{
	protected BVHNode _bvhNode;
	
	public abstract DRectangle getBorderBox();

	public abstract BVHNode getBvhNode();

	public abstract void setBvhNode(BVHNode _value);
}
