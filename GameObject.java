import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Abstract base class for all moving game objects.
 * Defines common properties: position, velocity, size, and core methods.
 */
public abstract class GameObject {
    protected int x, y;
    protected int width, height;
    protected int velocityX, velocityY;
    protected boolean active;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }

    public abstract void move();
    public abstract void draw(Graphics g);

    public boolean collidesWith(GameObject other) {
        Rectangle a = new Rectangle(x, y, width, height);
        Rectangle b = new Rectangle(other.x, other.y, other.width, other.height);
        return a.intersects(b);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setX(int x) { this.x = x; }
}
