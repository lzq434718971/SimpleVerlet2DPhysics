package engine;

/**
 * 
 * @author 86158
 * ���͵�vertex��Լ���Ļ���
 *
 */
public abstract class VertexConstraint extends TimeLineObject
{
	protected Vertex _v1,_v2;
	
	public abstract Vertex setV1(Vertex value);
	public abstract Vertex setV2(Vertex value);
	public abstract Vertex getV1();
	public abstract Vertex getV2();
}
