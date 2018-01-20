package enigma.engine;

public interface Positionable {
	public void setPosition(float x, float y);
	public void translate(float x, float y);
	
	public float getX();
	public float getY();
}
