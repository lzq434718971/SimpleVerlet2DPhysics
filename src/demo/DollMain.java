package demo;

import java.awt.Color;

import engine.StickConstraint;

public interface DollMain 
{
	public Color getColor();
	public void setColor(Color c);
	public double getRadius();
	public double setRadius(double value);
	public StickConstraint getPreConstraint();
	public StickConstraint getNextConstraint();
	public void setPreConstraint(StickConstraint value);
	public void setNextConstraint(StickConstraint value);
}
