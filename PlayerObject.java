import java.awt.*;

/**
 * PlayerObject is a concrete subclass of GameObject.
 * Represents the player-controlled spaceship at the bottom of the screen.
 */
public class PlayerObject extends GameObject {
    private int speed;
    private boolean isInvincible;
    private int invincibleTimer;
    private boolean shieldActive;
    private static final int INVINCIBLE_DURATION = 90;

    public PlayerObject(int x, int y) {
        super(x, y, 50, 30);
        this.speed = 6;
        this.isInvincible = false;
        this.invincibleTimer = 0;
        this.shieldActive = false;
    }

    @Override
    public void move() {
        // Keep player within screen bounds
        if (x < 0){
            x = 0;
        }
        if (x + width > GamePanel.PANEL_WIDTH){
            x = GamePanel.PANEL_WIDTH - width;
        }

        // Count down invincibility frames
        if (isInvincible) {
            invincibleTimer--;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        // Flicker when invincible
        if (isInvincible && (invincibleTimer / 5) % 2 == 0){
            return;
        }

        // Shield glow
        if (shieldActive) {
            g.setColor(new Color(100, 200, 255, 100));
            g.fillOval(x - 10, y - 10, width + 20, height + 20);
            g.setColor(new Color(100, 200, 255, 180));
            g.drawOval(x - 10, y - 10, width + 20, height + 20);
        }

        // Player body
        g.setColor(new Color(70, 130, 255));
        g.fillRoundRect(x, y, width, height, 10, 10);

        // Border
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, width, height, 10, 10);

        // Cockpit detail
        g.setColor(new Color(200, 240, 255));
        g.fillOval(x + width / 2 - 8, y + 5, 16, 10);
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0){
            x = 0;
        }
    }

    public void moveRight() {
        x += speed;
        if (x + width > GamePanel.PANEL_WIDTH){
            x = GamePanel.PANEL_WIDTH - width;
        }
    }

    public void triggerInvincibility() {
        isInvincible = true;
        invincibleTimer = INVINCIBLE_DURATION;
    }

    public boolean isInvincible() { return isInvincible; }
    public void setShieldActive(boolean shield) { this.shieldActive = shield; }
    public boolean isShieldActive() { return shieldActive; }
}
