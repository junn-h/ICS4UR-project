import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * GamePanel: the main game area.
 * Contains the game loop, all game objects, input handling,
 * collision detection, and rendering.
 * Also contains GameFrame as an inner class.
 */
public class GamePanel extends JPanel implements ActionListener {

    public static final int PANEL_WIDTH  = 600;
    public static final int PANEL_HEIGHT = 580;

    private Timer gameTimer;
    private static final int TICK_MS = 16;

    // Game states
    private static final int STATE_COUNTDOWN = 0;
    private static final int STATE_PLAYING   = 1;
    private static final int STATE_PAUSED    = 2;
    private static final int STATE_GAMEOVER  = 3;
    private int state;

    private int countdownValue;
    private int countdownTimer;

    // Game objects
    private PlayerObject player;
    private ArrayList<FallingObject>    fallers;
    private ArrayList<CollectibleObject> collectibles;

    // Input
    private boolean leftDown, rightDown;
    private int mouseTargetX = -1; // -1 means no mouse target active

    // Game stats
    private int score;
    private int lives;
    private int level;
    private int frameCount;
    private static final int MAX_LIVES = 3;

    // Spawning
    private int spawnInterval;
    private int spawnTimer;
    private int collectibleSpawnTimer;
    private Random rng;

    // Power-ups
    private boolean shieldActive;
    private int shieldTimer;
    private boolean slowTimeActive;
    private int slowTimeTimer;
    private static final int POWERUP_DURATION = 300;

    // Scrolling stars background
    private int[][] stars;
    private int[] starSpeeds;

    private String username;

    public GamePanel(String username) {
        this.username = username;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        rng = new Random();
        initStars();
        setupInput();
        initGame();
    }

    // ===================== INIT =====================

    private void initStars() {
        // Place 80 stars at random positions with random speeds
        stars = new int[80][2];
        starSpeeds = new int[80];
        for (int i = 0; i < 80; i++) {
            stars[i][0] = rng.nextInt(PANEL_WIDTH);
            stars[i][1] = rng.nextInt(PANEL_HEIGHT);
            starSpeeds[i] = rng.nextInt(2) + 1;
        }
    }

    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT  || e.getKeyCode() == KeyEvent.VK_A){
                    leftDown  = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
                    rightDown = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    togglePause();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER && state == STATE_GAMEOVER){
                    restartGame();
                }
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT  || e.getKeyCode() == KeyEvent.VK_A){
                    leftDown  = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
                    rightDown = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (state == STATE_PLAYING) {
                    // Set a target for the ship to slide toward at normal speed
                    int targetX = e.getX() - player.getWidth() / 2;
                    if (targetX < 0){
                        targetX = 0;
                    }
                    if (targetX > PANEL_WIDTH - player.getWidth()){
                        targetX = PANEL_WIDTH - player.getWidth();
                    }
                    mouseTargetX = targetX;
                }
                if (state == STATE_GAMEOVER){
                    restartGame();
                }
            }
        });
    }

    private void initGame() {
        player       = new PlayerObject(PANEL_WIDTH / 2 - 25, PANEL_HEIGHT - 80);
        fallers      = new ArrayList<FallingObject>();
        collectibles = new ArrayList<CollectibleObject>();

        score        = 0;
        lives        = MAX_LIVES;
        level        = 1;
        frameCount   = 0;
        spawnInterval = 60;
        spawnTimer   = 0;
        collectibleSpawnTimer = 0;

        shieldActive   = false;
        shieldTimer    = 0;
        slowTimeActive = false;
        slowTimeTimer  = 0;

        state          = STATE_COUNTDOWN;
        countdownValue = 3;
        countdownTimer = 0;

        gameTimer = new Timer(TICK_MS, this);
        gameTimer.start();
    }

    private void restartGame() {
        gameTimer.stop();
        fallers.clear();
        collectibles.clear();
        initGame();
    }

    // ===================== GAME LOOP =====================

    public void actionPerformed(ActionEvent e) {
        update();
        repaint();  
    }

    private void update() {
        scrollStars();

        if (state == STATE_COUNTDOWN) {
            countdownTimer++;
            if (countdownTimer >= 60) {
                countdownTimer = 0;
                countdownValue--;
                if (countdownValue <= 0){
                    state = STATE_PLAYING;
                }
            }
            return;
        }
        if (state != STATE_PLAYING){
            return;
        }

        frameCount++;

        // Player movement
        if (leftDown)  { player.moveLeft();  mouseTargetX = -1; }
        if (rightDown) { player.moveRight(); mouseTargetX = -1; }
        if (mouseTargetX >= 0) {
            int dx = mouseTargetX - player.getX();
            if (Math.abs(dx) <= 6) {
                player.setX(mouseTargetX);
                mouseTargetX = -1;
            } else {
                if (dx > 0) {
                    player.setX(player.getX() + 6);
                } else {
                    player.setX(player.getX() - 6);
                }
            }
        }
        player.move();

        // Update power-up timers
        if (shieldActive) {
            shieldTimer--;
            player.setShieldActive(true);
            if (shieldTimer <= 0) {
                shieldActive = false;
                player.setShieldActive(false);
            }
        }
        if (slowTimeActive) {
            slowTimeTimer--;
            if (slowTimeTimer <= 0){
                slowTimeActive = false;
            }
        }

        // Spawn fallers
        spawnTimer++;
        if (spawnTimer >= spawnInterval) {
            spawnFaller();
            spawnTimer = 0;
        }

        // Spawn collectibles
        collectibleSpawnTimer++;
        if (collectibleSpawnTimer >= 180) {
            spawnCollectible();
            collectibleSpawnTimer = 0;
        }

        // Update fallers
        for (int i = 0; i < fallers.size(); i++) {
            FallingObject f = fallers.get(i);
            f.move();
            if (!f.isActive()){
                continue;
            }
            if (!player.isInvincible() && f.collidesWith(player)) {
                if (shieldActive) {
                    // Shield absorbs the hit
                    shieldActive = false;
                    shieldTimer = 0;
                    player.setShieldActive(false);
                    f.setActive(false);
                } else {
                    // Bombs deal 2 lives of damage, everything else deals 1
                    if (f.getType() == FallingObject.BOMB) {
                        lives -= 2;
                    } else {
                        lives -= 1;
                    }
                    f.setActive(false);
                    player.triggerInvincibility();
                    if (lives <= 0) {
                        state = STATE_GAMEOVER;
                        gameTimer.stop();
                        FileManager.saveScore(username, score, level);
                    }
                }
            }
        }

        // Update collectibles
        for (int i = 0; i < collectibles.size(); i++) {
            CollectibleObject c = collectibles.get(i);
            c.move();
            if (!c.isActive()){
                continue;
            }
            if (c.collidesWith(player)) {
                applyCollectible(c);
                c.setActive(false);
            }
        }

        // Remove inactive objects
        ArrayList<FallingObject> keepFallers = new ArrayList<FallingObject>();
        for (int i = 0; i < fallers.size(); i++) {
            if (fallers.get(i).isActive()){
                keepFallers.add(fallers.get(i));
            }
        }
        fallers = keepFallers;

        ArrayList<CollectibleObject> keepCollectibles = new ArrayList<CollectibleObject>();
        for (int i = 0; i < collectibles.size(); i++) {
            if (collectibles.get(i).isActive()){
                keepCollectibles.add(collectibles.get(i));
            }
        }
        collectibles = keepCollectibles;

        // Score increases over time
        if (frameCount % 30 == 0){
            score++;
        }

        // Level up every 100 points
        int newLevel = score / 100 + 1;
        if (newLevel > level) {
            level = newLevel;
            if (spawnInterval > 20){
                spawnInterval -= 5;
            }
        }
    }

    private void scrollStars() {
        // Move each star down and wrap it back to the top when it goes off screen
        for (int i = 0; i < stars.length; i++) {
            stars[i][1] += starSpeeds[i];
            if (stars[i][1] > PANEL_HEIGHT) {
                stars[i][1] = 0;
                stars[i][0] = rng.nextInt(PANEL_WIDTH);
            }
        }
    }

    private void applyCollectible(CollectibleObject c) {
        if (c.getType() == CollectibleObject.COIN) {
            score += 10;
        } else if (c.getType() == CollectibleObject.SHIELD) {
            shieldActive = true;
            shieldTimer  = POWERUP_DURATION;
            player.setShieldActive(true);
        } else if (c.getType() == CollectibleObject.SLOW_TIME) {
            slowTimeActive = true;
            slowTimeTimer  = POWERUP_DURATION;
        } else if (c.getType() == CollectibleObject.EXTRA_LIFE) {
            if (lives < MAX_LIVES){
                lives++;
            }
        }
    }

    // ===================== SPAWNING =====================

    private void spawnFaller() {
        int x = rng.nextInt(PANEL_WIDTH - 40);
        int speed = 3 + level + rng.nextInt(3);
        int roll = rng.nextInt(100);
        int type;

        if (level < 3) {
            if (roll < 80){
                type = FallingObject.NORMAL;
            }
            else {
                type = FallingObject.FAST;
            }
        } else if (level < 5) {
            if (roll < 50){
                type = FallingObject.NORMAL;
            }
            else if (roll < 80) {
                type = FallingObject.FAST;
            }
            else {
                type = FallingObject.ZIGZAG;
            }
        } else {
            if (roll < 35){
                type = FallingObject.NORMAL;
            }
            else if (roll < 60) {
                type = FallingObject.FAST;
            }
            else if (roll < 80) {
                type = FallingObject.ZIGZAG;
            }
            else {
                type = FallingObject.BOMB;
            }
        }

        fallers.add(new FallingObject(x, -40, speed, type));
    }

    private void spawnCollectible() {
        int x = rng.nextInt(PANEL_WIDTH - 30);
        int roll = rng.nextInt(100);
        int type;

        if (roll < 60){
            type = CollectibleObject.COIN;
        }
        else if (roll < 80) {
            type = CollectibleObject.SHIELD;
        }
        else if (roll < 93) {
            type = CollectibleObject.SLOW_TIME;
        }
        else {
            type = CollectibleObject.EXTRA_LIFE;
        }

        collectibles.add(new CollectibleObject(x, -30, type));
    }

    // ===================== RENDERING =====================

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(new Color(5, 5, 25));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // Stars
        for (int i = 0; i < stars.length; i++) {
            g.setColor(new Color(200, 200, 200));
            int size = starSpeeds[i];
            g.fillOval(stars[i][0], stars[i][1], size, size);
        }

        // Slow-time blue tint
        if (slowTimeActive) {
            g.setColor(new Color(50, 100, 200, 30));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }

        // Game objects
        for (int i = 0; i < fallers.size(); i++) {
            if (fallers.get(i).isActive()){
                fallers.get(i).draw(g);
            }
        }
        for (int i = 0; i < collectibles.size(); i++) {
            if (collectibles.get(i).isActive()){
                collectibles.get(i).draw(g);
            }
        }
        player.draw(g);

        drawHUD(g);

        if (state == STATE_COUNTDOWN){
            drawCountdown(g);
        }
        if (state == STATE_PAUSED){
            drawPaused(g);
        }
        if (state == STATE_GAMEOVER){
            drawGameOver(g);
        }
    }

    private void drawHUD(Graphics g) {
        // HUD background bar
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(8, 8, PANEL_WIDTH - 16, 45, 12, 12);

        // Score
        g.setFont(new Font("Arial Black", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 60));
        g.drawString("Score: " + score, 20, 35);

        // Level
        g.setColor(new Color(100, 200, 255));
        g.drawString("Lv " + level, PANEL_WIDTH / 2 - 20, 35);

        // Lives (hearts)
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives){
                g.setColor(new Color(255, 80, 100));
            }
            else {
                g.setColor(new Color(60, 60, 80));
            }
            int hx0 = PANEL_WIDTH - 91 + i * 27;
            g.fillArc(hx0,     14, 10, 10, 0, 180);
            g.fillArc(hx0 + 6, 14, 10, 10, 0, 180);
            int[] hx = {hx0, hx0 + 16, hx0 + 8};
            int[] hy = {19, 19, 30};
            g.fillPolygon(hx, hy, 3);
        }

        // Power-up bars
        g.setFont(new Font("Arial", Font.BOLD, 12));
        if (shieldActive) {
            int barW = (int)(150.0 * shieldTimer / POWERUP_DURATION);
            g.setColor(new Color(60, 60, 130));
            g.fillRoundRect(10, PANEL_HEIGHT - 32, 155, 18, 6, 6);
            g.setColor(new Color(100, 180, 255));
            g.fillRoundRect(10, PANEL_HEIGHT - 32, barW, 18, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString("Shield", 18, PANEL_HEIGHT - 18);
        }
        if (slowTimeActive) {
            int barW = (int)(150.0 * slowTimeTimer / POWERUP_DURATION);
            int barY;
            if (shieldActive) {
                barY = PANEL_HEIGHT - 56;
            } else {
                barY = PANEL_HEIGHT - 32;
            }
            g.setColor(new Color(40, 90, 40));
            g.fillRoundRect(10, barY, 155, 18, 6, 6);
            g.setColor(new Color(100, 230, 100));
            g.fillRoundRect(10, barY, barW, 18, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString("Slow Time", 18, barY + 14);
        }

        // Username
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(new Color(160, 160, 200));
        g.drawString(username, PANEL_WIDTH - 75, PANEL_HEIGHT - 8);
    }

    private void drawCountdown(Graphics g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setFont(new Font("Arial Black", Font.BOLD, 100));
        g.setColor(new Color(200, 230, 255));
        String s = String.valueOf(countdownValue);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (PANEL_WIDTH - fm.stringWidth(s)) / 2, PANEL_HEIGHT / 2 + 30);
    }

    private void drawPaused(Graphics g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setFont(new Font("Arial Black", Font.BOLD, 42));
        g.setColor(Color.WHITE);
        String s = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (PANEL_WIDTH - fm.stringWidth(s)) / 2, PANEL_HEIGHT / 2 - 10);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(new Color(200, 200, 200));
        String s2 = "Press P or ESC to resume";
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(s2, (PANEL_WIDTH - fm2.stringWidth(s2)) / 2, PANEL_HEIGHT / 2 + 25);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g.setColor(new Color(20, 20, 50));
        g.fillRoundRect(100, 150, 400, 260, 20, 20);
        g.setColor(new Color(100, 100, 200));
        g.drawRoundRect(100, 150, 400, 260, 20, 20);

        g.setFont(new Font("Arial Black", Font.BOLD, 38));
        g.setColor(new Color(255, 80, 80));
        String go = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(go, (PANEL_WIDTH - fm.stringWidth(go)) / 2, 205);

        g.setFont(new Font("Arial Black", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 60));
        String sc = "Score: " + score;
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(sc, (PANEL_WIDTH - fm2.stringWidth(sc)) / 2, 245);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(180, 180, 220));
        String lv = "Level reached: " + level;
        FontMetrics fm3 = g.getFontMetrics();
        g.drawString(lv, (PANEL_WIDTH - fm3.stringWidth(lv)) / 2, 275);

        int best = FileManager.getBestScore(username);
        g.setColor(new Color(100, 200, 255));
        String bestStr = "Your best: " + best;
        FontMetrics fm4 = g.getFontMetrics();
        g.drawString(bestStr, (PANEL_WIDTH - fm4.stringWidth(bestStr)) / 2, 300);

        if (score >= best && !username.equals("Guest")) {
            g.setFont(new Font("Arial Black", Font.BOLD, 14));
            g.setColor(new Color(255, 215, 0));
            String nb = "New Personal Best!";
            FontMetrics fm5 = g.getFontMetrics();
            g.drawString(nb, (PANEL_WIDTH - fm5.stringWidth(nb)) / 2, 325);
        }

        g.setFont(new Font("Arial Black", Font.BOLD, 15));
        g.setColor(new Color(100, 230, 100));
        String restart = "[ ENTER or CLICK to Restart ]";
        FontMetrics fm6 = g.getFontMetrics();
        g.drawString(restart, (PANEL_WIDTH - fm6.stringWidth(restart)) / 2, 385);
    }

    // ===================== PUBLIC CONTROLS =====================

    public void togglePause() {
        if (state == STATE_PLAYING) {
            state = STATE_PAUSED;
            gameTimer.stop();
        } else if (state == STATE_PAUSED) {
            state = STATE_PLAYING;
            gameTimer.start();
            requestFocusInWindow();
        }
    }

    public void stopGame() {
        gameTimer.stop();
    }

    // ===================== INNER CLASS: GameFrame =====================

    /**
     * The main game window.
     * Holds the GamePanel and the top control buttons.
     */
    public static class GameFrame extends JFrame {
        private GamePanel gamePanel;

        public GameFrame(String username) {
            setTitle("Dodge Blitz - " + username);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);

            // Top button bar
            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            topBar.setBackground(new Color(10, 10, 30));

            JButton pauseBtn  = makeButton("Pause");
            JButton boardBtn  = makeButton("Leaderboard");
            JButton logoutBtn = makeButton("Logout");

            topBar.add(boardBtn);
            topBar.add(pauseBtn);
            topBar.add(logoutBtn);

            gamePanel = new GamePanel(username);

            pauseBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { gamePanel.togglePause(); }
            });
            logoutBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    gamePanel.stopGame();
                    dispose();
                    new LoginFrame();
                }
            });
            boardBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    java.util.List<String> top = FileManager.getTopScores(10);
                    String msg = "";
                    if (top.isEmpty()) {
                        msg = "No scores yet. Play to earn a spot!";
                    } else {
                        for (int i = 0; i < top.size(); i++) {
                            msg += top.get(i) + "\n";
                        }
                    }
                    JOptionPane.showMessageDialog(null, msg, "Top Scores", JOptionPane.PLAIN_MESSAGE);
                }
            });

            setLayout(new BorderLayout());
            add(topBar, BorderLayout.NORTH);
            add(gamePanel, BorderLayout.CENTER);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            gamePanel.requestFocusInWindow();
        }

        private JButton makeButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setBackground(new Color(40, 40, 80));
            btn.setForeground(new Color(200, 210, 255));
            btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            btn.setFocusPainted(false);
            return btn;
        }
    }
}