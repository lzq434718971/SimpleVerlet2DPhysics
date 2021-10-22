package engine;

/**
 * 
 * @author 86158
 * 物理引擎的核心vertex类
 *
 */
public class Vertex extends CollisionObject{
	private double _newX,_newY;
	private double _oldX,_oldY;
	private double _m;
	private double _fri;
	private double _bou;
	private boolean _isDynamic;
	private double _safeDis;
	private double _damping;
	
	public Vertex()
	{
		setPosition(0,0);
		setMass(1);
		generalInit();
	}
	
	public Vertex(double x,double y)
	{
		setPosition(x,y);
		setMass(1);
		generalInit();
	}
	
	public Vertex(double x,double y,double mass)
	{
		setPosition(x,y);
		setMass(mass);
		generalInit();
	}
	
	public Vertex(Vector2D vec)
	{
		setPosition(vec.getX(),vec.getY());
		setMass(1);
		generalInit();
	}
	
	private void generalInit()
	{
		setSafeDis(VerletGlobalConfig.SAFE_DIS);
		_isDynamic=true;
		setFriction(0);
		setBounce(0);
		setDamping(1);
	}
	
	/** 移动位置，同时速度置0 **/
	public void setPosition(double x,double y)
	{
		_newX=_oldX=x;
		_newY=_oldY=y;
	}
	public void setPosition(Vector2D v)
	{
		_newX=_oldX=v.getX();
		_newY=_oldY=v.getY();
	}
	
	/** 移动位置，此举动参与物理模拟 **/
	public void grab(Vector2D pos)
	{
		setX(pos.getX());
		setY(pos.getY());
	}

	@Override
	/** 更新顶点位置 **/
	public void update() {
		if(!_isDynamic)
		{
			setPosition(_newX,_newY);
			return;
		}
		
		double tempVX=_newX-_oldX;
		double tempVY=_newY-_oldY;
		double maxAV=VerletGlobalConfig.AXIS_SPEED_MAX;
		tempVX=tempVX>0?Math.min(tempVX,maxAV):Math.max(tempVX,-maxAV);
		tempVY=tempVY>0?Math.min(tempVY,maxAV):Math.max(tempVY,-maxAV);
		_newX=_oldX+tempVX;
		_newY=_oldY+tempVY;
		_oldX=_newX;
		_oldY=_newY;
		_newX+=tempVX*_damping;
		_newY+=tempVY*_damping;
	}
	
	/*
	 * 以下各函数用于获取和设置vertex的各项参数
	 */
	public double getX()
	{
		return _newX;
	}
	public double setX(double value)
	{
		if(_isDynamic)
		{
			_newX=value;
			return value;
		}
		else
		{
			return value;
		}
	}
	
	public double getY()
	{
		return _newY;
	}
	public double setY(double value)
	{
		if(_isDynamic)
		{
			_newY=value;
			return value;
		}
		else
		{
			return value;
		}
	}
	
	public Vector2D getPosVector()
	{
		Vector2D v2d=new Vector2D(_newX,_newY);
		return v2d;
	}
	
	public double getVx()
	{
		return _newX-_oldX;
	}
	public double setVx(double value)
	{
		setX(value+_oldX);
		return value;
	}
	
	public double getVy()
	{
		return _newY-_oldY;
	}
	public double setVy(double value)
	{
		setY(value+_oldY);
		return value;
	}
	
	public Vector2D getVelocity()
	{
		return new Vector2D(getVx(),getVy());
	}
	
	public Vector2D setVelocity(Vector2D value)
	{
		setVx(value.getX());
		setVy(value.getY());
		return value;
	}
	
	public double getMass()
	{
		return _m;
	}
	public double setMass(double value)
	{
		_m=value;
		return _m;
	}
	
	public double getFriction() {
		return _fri;
	}

	public void setFriction(double value) {
		_fri = value;
	}

	public void appendForce(Vector2D f)
	{
		double ax=f.getX()/_m;
		double ay=f.getY()/_m;
		setVx(getVx()+ax);
		setVy(getVy()+ay);
	}
	
	public void appendOfferset(Vector2D off)
	{
		setX(getX()+off.getX());
		setY(getY()+off.getY());
	}
	
	/*
	//将点平移，将不符合物理规则
	public void appendTranslation(Vector2D off)
	{
		_newX+=off.getX();
		_newY+=off.getY();
		_oldX+=off.getX();
		_oldY+=off.getY();
	}
	*/
	
	/** 返回边界盒 **/
	@Override
	public DRectangle getBorderBox() 
	{
		DRectangle rect=new DRectangle();
		rect.setX(Math.min(_newX-getSafeDis(), _oldX-getSafeDis()));
		rect.setY(Math.min(_newY-getSafeDis(), _oldY-getSafeDis()));
		rect.setWidth(Math.abs(_newX-_oldX)+2*getSafeDis());
		rect.setHeight(Math.abs(_newY-_oldY)+2*getSafeDis());
		return rect;
	}

	public boolean isDynamic() {
		return _isDynamic;
	}

	public void setIsDynamic(boolean value) {
		_isDynamic = value;
	}

	public double getBounce() {
		return _bou;
	}

	public void setBounce(double value) {
		_bou = value;
	}

	public double getSafeDis() {
		return _safeDis;
	}

	public void setSafeDis(double value) {
		_safeDis = value;
	}

	public double getDamping() {
		return _damping;
	}

	public void setDamping(double value) {
		_damping = value;
	}

	@Override
	public BVHNode getBvhNode() {
		return _bvhNode;
	}

	@Override
	public void setBvhNode(BVHNode value) {
		_bvhNode=value;
	}
}
