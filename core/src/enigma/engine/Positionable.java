package enigma.engine;

public abstract class Positionable extends Entity {
	public abstract void setPosition(float x, float y);
	public abstract void translate(float x, float y);
	
	public abstract float getX();
	public abstract float getY();
}
