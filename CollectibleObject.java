import java.awt.*;

/**
 * CollectibleObject is a concrete subclass of GameObject.
 * Represents collectible items that grant points or power-ups.
 * Has four types defined as int constants.
 */
public class CollectibleObject extends GameObject {
    // Collectible types
    public static final int COIN       = 0;
    public static final int SHIELD     = 1;
    public static final int SLOW_TIME  = 2;
    public static final int EXTRA_LIFE = 3;

    private int type;
    private int animFrame;

    public CollectibleObject(int x, int y, int type) {
        super(x, y, 24, 24);
        this.type = type;
        this.velocityY = 2;
        this.animFrame = 0;
    }

    @Override
    public void move() {
        y += velocityY;
        animFrame++;
        if (y > GamePanel.PANEL_HEIGHT + 50) {
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        // Slight bobbing offset using animFrame
        int bobY = y + (int)(Math.sin(animFrame * 0.15) * 3);

        if (type == COIN) {
            g.setColor(new Color(255, 200, 0));
            g.fillOval(x, bobY, width, height);
            g.setColor(new Color(200, 150, 0));
            g.drawOval(x, bobY, width, height);
            g.setColor(new Color(180, 130, 0));
            g.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            int strX = x + (width - fm.stringWidth("$")) / 2;
            int strY = bobY + (height + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString("$", strX, strY);

        } else if (type == SHIELD) {
            int[] sx = {x + width/2, x + width, x + width, x + width/2, x, x};
            int[] sy = {bobY, bobY + 4, bobY + 16, bobY + height, bobY + 16, bobY + 4};
            g.setColor(new Color(100, 180, 255));
            g.fillPolygon(sx, sy, 6);
            g.setColor(Color.WHITE);
            g.drawPolygon(sx, sy, 6);

        } else if (type == SLOW_TIME) {
            g.setColor(new Color(150, 230, 150));
            g.fillOval(x, bobY, width, height);
            g.setColor(new Color(50, 150, 50));
            g.drawOval(x, bobY, width, height);
            // Clock hands
            int cx = x + width / 2;
            int cy = bobY + height / 2;
            g.setColor(new Color(0, 80, 0));
            g.drawLine(cx, cy, cx, cy - 7);
            g.drawLine(cx, cy, cx + 5, cy + 3);

        } else if (type == EXTRA_LIFE) {
            g.setColor(new Color(255, 80, 100));
            int half = width / 2;
            g.fillArc(x,            bobY, half + 2, half + 2, 0, 180);
            g.fillArc(x + half - 2, bobY, half + 2, half + 2, 0, 180);
            int arcBottom = bobY + (half + 2) / 2;
            int[] hx = {x, x + width, x + width / 2};
            int[] hy = {arcBottom, arcBottom, bobY + height};
            g.fillPolygon(hx, hy, 3);
        }
    }

    public int getType() { return type; }

    public int getPointValue() {
        if (type == COIN){
            return 10;
        }
        return 0;
    }
}