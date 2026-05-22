import java.awt.*;

/**
 * FallingObject is a concrete subclass of GameObject.
 * Represents falling hazards the player must dodge.
 * Has four types defined as int constants.
 */
public class FallingObject extends GameObject {
    // Object types
    public static final int NORMAL = 0;
    public static final int FAST   = 1;
    public static final int BOMB   = 2;
    public static final int ZIGZAG = 3;

    private int type;
    private Color color;
    private int zigzagTimer;
    private static final int ZIGZAG_INTERVAL = 20;

    public FallingObject(int x, int y, int speed, int type) {
        super(x, y, 28, 28);
        this.type = type;
        this.velocityY = speed;
        this.zigzagTimer = 0;

        // Set properties based on type
        if (type == NORMAL) {
            color = new Color(220, 60, 60);
        } else if (type == FAST) {
            color = new Color(255, 140, 0);
            this.velocityY += 3;
            this.width = 22;
            this.height = 22;
        } else if (type == BOMB) {
            color = new Color(80, 80, 80);
            this.width = 36;
            this.height = 36;
        } else if (type == ZIGZAG) {
            color = new Color(180, 50, 220);
            this.velocityX = 3;
        }
    }

    @Override
    public void move() {
        y += velocityY;

        if (type == ZIGZAG) {
            zigzagTimer++;
            x += velocityX;
            if (zigzagTimer >= ZIGZAG_INTERVAL) {
                velocityX = -velocityX;
                zigzagTimer = 0;
            }
            if (x < 0) {
                x = 0;
                velocityX = Math.abs(velocityX);
            }
            if (x + width > GamePanel.PANEL_WIDTH) {
                x = GamePanel.PANEL_WIDTH - width;
                velocityX = -Math.abs(velocityX);
            }
        }

        if (y > GamePanel.PANEL_HEIGHT + 50) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (type == BOMB) {
            // Bomb body
            g.setColor(new Color(50, 50, 50));
            g.fillOval(x, y + 4, width, height - 4);
            g.setColor(new Color(100, 100, 100));
            g.drawOval(x, y + 4, width, height - 4);
            // Fuse
            g.setColor(new Color(160, 120, 60));
            g.drawLine(x + width / 2, y + 4, x + width / 2 + 5, y);
            // Spark
            g.setColor(Color.YELLOW);
            g.fillOval(x + width / 2 + 4, y - 3, 6, 6);
        } else {
            // Regular orb
            g.setColor(color);
            g.fillOval(x, y, width, height);
            g.setColor(color.darker());
            g.drawOval(x, y, width, height);
            // Highlight dot
            g.setColor(new Color(255, 255, 255, 120));
            g.fillOval(x + 5, y + 3, width / 4, height / 5);
        }
    }

    public int getType() { return type; }
}