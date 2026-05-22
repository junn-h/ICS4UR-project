import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login and Registration screen.
 * Also contains the main() entry point to reduce total file count.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }

    public LoginFrame() {
        setTitle("Dodge Blitz - Login");
        setSize(420, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.add(buildLoginPanel(),    "LOGIN");
        cardPanel.add(buildRegisterPanel(), "REGISTER");

        add(cardPanel);
        setVisible(true);
    }

    // ==================== LOGIN PANEL ====================

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(15, 20, 50));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);

        JLabel title = new JLabel("DODGE BLITZ", SwingConstants.CENTER);
        title.setFont(new Font("Arial Black", Font.BOLD, 30));
        title.setForeground(new Color(100, 180, 255));
        title.setBounds(60, 40, 300, 45);
        panel.add(title);

        JLabel subtitle = new JLabel("Login to Play", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(new Color(160, 160, 200));
        subtitle.setBounds(60, 88, 300, 25);
        panel.add(subtitle);

        JLabel userLabel = makeLabel("Username:");
        userLabel.setBounds(80, 150, 260, 22);
        panel.add(userLabel);
        usernameField = makeTextField();
        usernameField.setBounds(80, 172, 260, 38);
        panel.add(usernameField);

        JLabel passLabel = makeLabel("Password:");
        passLabel.setBounds(80, 225, 260, 22);
        panel.add(passLabel);
        passwordField = makePasswordField();
        passwordField.setBounds(80, 247, 260, 38);
        panel.add(passwordField);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBounds(60, 295, 300, 22);
        panel.add(statusLabel);

        JButton loginBtn = makeButton("LOGIN", new Color(70, 130, 255));
        loginBtn.setBounds(80, 325, 260, 45);
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { handleLogin(); }
        });
        panel.add(loginBtn);

        JLabel regLink = new JLabel("No account? Register here", SwingConstants.CENTER);
        regLink.setFont(new Font("Arial", Font.PLAIN, 12));
        regLink.setForeground(new Color(100, 200, 255));
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.setBounds(80, 385, 260, 22);
        regLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(cardPanel, "REGISTER"); }
        });
        panel.add(regLink);

        JButton guestBtn = makeButton("Play as Guest", new Color(80, 80, 80));
        guestBtn.setBounds(80, 415, 260, 38);
        guestBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { launchGame("Guest"); }
        });
        panel.add(guestBtn);

        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { handleLogin(); }
        });

        return panel;
    }

    // ==================== REGISTER PANEL ====================

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(15, 20, 50));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);

        JLabel title = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        title.setFont(new Font("Arial Black", Font.BOLD, 24));
        title.setForeground(new Color(100, 255, 180));
        title.setBounds(60, 50, 300, 40);
        panel.add(title);

        JTextField regUser = makeTextField();
        JPasswordField regPass = makePasswordField();
        JPasswordField regConfirm = makePasswordField();

        JLabel u = makeLabel("Username:");
        u.setBounds(80, 120, 260, 22);
        panel.add(u);
        regUser.setBounds(80, 142, 260, 38);
        panel.add(regUser);

        JLabel p = makeLabel("Password:");
        p.setBounds(80, 195, 260, 22);
        panel.add(p);
        regPass.setBounds(80, 217, 260, 38);
        panel.add(regPass);

        JLabel c = makeLabel("Confirm Password:");
        c.setBounds(80, 265, 260, 22);
        panel.add(c);
        regConfirm.setBounds(80, 287, 260, 38);
        panel.add(regConfirm);

        JLabel regStatus = new JLabel("", SwingConstants.CENTER);
        regStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        regStatus.setForeground(new Color(255, 100, 100));
        regStatus.setBounds(60, 332, 300, 22);
        panel.add(regStatus);

        JButton regBtn = makeButton("REGISTER", new Color(60, 180, 100));
        regBtn.setBounds(80, 360, 260, 45);
        regBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String user    = regUser.getText().trim();
                String pass    = new String(regPass.getPassword());
                String confirm = new String(regConfirm.getPassword());

                if (user.isEmpty() || pass.isEmpty()) {
                    regStatus.setForeground(new Color(255, 100, 100));
                    regStatus.setText("Fields cannot be empty.");
                    return;
                }
                if (!pass.equals(confirm)) {
                    regStatus.setForeground(new Color(255, 100, 100));
                    regStatus.setText("Passwords do not match.");
                    return;
                }
                if (user.contains(":")) {
                    regStatus.setForeground(new Color(255, 100, 100));
                    regStatus.setText("Username cannot contain ':'");
                    return;
                }
                if (FileManager.registerUser(user, pass)) {
                    regStatus.setForeground(new Color(100, 255, 150));
                    regStatus.setText("Registered! You can now log in.");
                    usernameField.setText(user);
                    Timer t = new Timer(1500, new ActionListener() {
                        public void actionPerformed(ActionEvent e) { cardLayout.show(cardPanel, "LOGIN"); }
                    });
                    t.setRepeats(false);
                    t.start();
                } else {
                    regStatus.setForeground(new Color(255, 100, 100));
                    regStatus.setText("Username already taken.");
                }
            }
        });
        panel.add(regBtn);

        JLabel backLink = new JLabel("Back to Login", SwingConstants.CENTER);
        backLink.setFont(new Font("Arial", Font.PLAIN, 12));
        backLink.setForeground(new Color(100, 200, 255));
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.setBounds(80, 420, 260, 22);
        backLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(cardPanel, "LOGIN"); }
        });
        panel.add(backLink);

        return panel;
    }

    // ==================== HELPERS ====================

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(new Color(200, 200, 220));
        return lbl;
    }

    private JTextField makeTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(new Color(40, 40, 70));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private JPasswordField makePasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(new Color(40, 40, 70));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial Black", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (FileManager.loginUser(user, pass)) {
            launchGame(user);
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    private void launchGame(String username) {
        dispose();
        new GamePanel.GameFrame(username);
    }
}