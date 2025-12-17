import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.HashMap;
import java.io.*;

public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}

// -------------------------------------------------------
// LOGIN PAGE
// -------------------------------------------------------

class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel status;

    // set a hashmap named accounts to add and store all users
    private HashMap<String, String> accounts = new HashMap<>();

    public LoginPage() {
        setTitle("Spacey Dodgey - Login");
        loadUsersFromFile();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.GRAY);
        setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Spacey Dodgey");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        // used grid box layout to place everything. lots of codehs copy

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.WHITE);
        panel.add(userLbl, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);
        panel.add(passLbl, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setEchoChar('!');
        panel.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        registerButton = new JButton("Register");
        panel.add(registerButton, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        status = new JLabel("Dont fly to close to the Sun");
        status.setForeground(Color.WHITE);
        status.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(status, gbc);

        // add action listeners for login and register
        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> registerUser());

        setVisible(true);
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) { // if a field is empty
            status.setText("Fields cannot be empty");
            return;
        }

        if (!accounts.containsKey(username)) { // If the user is not in accounts
            status.setText("User not found");
            return;
        }

        if (accounts.get(username).equals(password)) { // if user is in accounts
            status.setText("Login successful!"); // successful and play game
            dispose();
            new GameFrame();
        } else {
            status.setText("Incorrect password"); // not valid
        }
    }

    private void registerUser() {
        String username = usernameField.getText().trim(); // get just user string
        String password = new String(passwordField.getPassword()).trim(); // get just password string

        if (username.isEmpty() || password.isEmpty()) { // if a field is empty
            status.setText("Fields cannot be empty");
            return;
        }

        if (accounts.containsKey(username)) { // if user is already in accounts
            status.setText("User already exists");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt", true))) {
            writer.println(username + "," + password); // write user to users.txt
        } catch (IOException e) {
            System.out.println("error file write users.txt");
        }

        accounts.put(username, password); // put the user into accounts, name and password
        status.setText("Register successful!"); // say successful and submit
        usernameField.setText("");
        passwordField.setText("");
    }

    private void loadUsersFromFile() { // this is needed to load users from users.txt into accounts when restarting
        File file = new File("users.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    accounts.put(parts[0], parts[1]); // load user into accounts
                }
            }
        } catch (IOException e) {
            System.out.println("error users.txt");
        }
    }
}

// -------------------------------------------------------
// GAME Frame
// -------------------------------------------------------

class GameFrame extends JFrame {

    public GameFrame() { // set up the game frame
        setTitle("Rectangle Dodge Game");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        setLayout(null);

        panel.setBounds(0, 0, 800, 600);
        add(panel);

        setVisible(true);
    }
}

// -------------------------------------------------------
// ABSTRACT CLASS
// -------------------------------------------------------

abstract class ShapeObject {
    protected Rectangle bounds; // make these protected so the subclasses can use
    protected Color color;

    public ShapeObject(int x, int y, int w, int h, Color color) {
        this.bounds = new Rectangle(x, y, w, h); // counstructor
        this.color = color;
    }

    public Rectangle getBounds() {
        return bounds; // for collision detection
    }

    public boolean intersects(Rectangle r) {
        return bounds.intersects(r); // check for intersection
    }

    public abstract void speed(); // abstract class to determine movement (enemy v point)

    public void draw(Graphics g) { // draw the object
        g.setColor(color); 
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}

class EnemyObject extends ShapeObject {
    private int speedX;

    public EnemyObject(int x, int y, int w, int h, Color color, int speedX) {
        super(x, y, w, h, color); // calls superclass constructor and add speed
        this.speedX = speedX;
    }

    @Override
    public void speed() { // override speed so enemies move at enemy speed
        bounds.x -= speedX; 
    }
}

class CollectibleObject extends ShapeObject {
    private int speedX;

    public CollectibleObject(int x, int y, int w, int h, Color color, int speedX) {
        super(x, y, w, h, color); // calls superclass constructor and add speed
        this.speedX = speedX;
    }

    @Override
    public void speed() {  // override speed so points move at collectible speed
        bounds.x -= speedX;
    }
}

// -------------------------------------------------------
// GAME PANEL
// -------------------------------------------------------

class GamePanel extends JPanel implements ActionListener, KeyListener {

    Image playerImg; // add these 3 for the image
    int playerWidth = 70;
    int playerHeight = 50;

    int px = 100, py = 250;
    int speed = 5;

    EnemyObject TheSun; // THE GIANT SUN THAT ENDS THE GAME 

    boolean up, down, left, right;
    boolean circle = false;

    Color EC = Color.RED;
    int score = 0;

    ArrayList<EnemyObject> enemies = new ArrayList<>(); // arratlist for objects
    ArrayList<CollectibleObject> collectibles = new ArrayList<>();

    Random rand = new Random();
    Timer timer;
    ArrayList<BackgroundCircle> bgCircles = new ArrayList<>(); // arraylist for background circles

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        playerImg = new ImageIcon("rocket.png").getImage(); // add image in game panel constructor
        timer = new Timer(16, this);
        timer.start();
    }

    class BackgroundCircle {
        int x, y, size; // make a constructor for background circles with size and colour which will be ranodmized
        Color color;

        BackgroundCircle(int x, int y, int size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        circles();
        movePlayer();
        spawnObjects();
        moveObjects();
        checkCollisions();
        repaint();
    }

    void circles() {
        if (!circle) { // Create 3 background circles
            for (int i = 0; i < 4; i++) {
                int size = rand.nextInt(250) + 150; // 150–350 px big
                int x = rand.nextInt(800) - 200;    // can go offscreen a bit
                int y = rand.nextInt(600) - 200;

                Color c = new Color(
                        rand.nextInt(255),
                        rand.nextInt(255),
                        rand.nextInt(255),
                        60 // this is RGBA and the first 3 produce random colour while the 60 is the (A) for transparency
                );
                bgCircles.add(new BackgroundCircle(x, y, size, c)); // draw on the screen
            }
            circle = true;
        }
    }

    void movePlayer() {
        if (up && py > 0) py -= speed; // replaced player size with height and width because its now a rectangle
        if (down && py + playerHeight < getHeight() - 25) py += speed;
        if (left && px > 0) px -= speed;
        if (right && px + playerWidth < getWidth() - 5) px += speed;

        if (px > 600) { // IF THE PLAYER FLIES TO CLOSE TO THE SUN
            int w = 800;
            int h = 600;
            int y = 0; 

            TheSun = new EnemyObject(getWidth(), y, w, h, Color.YELLOW, 5);
            enemies.add(TheSun);
        }
    }

    boolean isOverlapping(Rectangle r) {
        for (EnemyObject e : enemies) if (r.intersects(e.getBounds())) return true;
        for (CollectibleObject c : collectibles) if (r.intersects(c.getBounds())) return true;
        return false;
    }

    void spawnObjects() {
        // Enemy
        if (rand.nextInt(25) == 0) { // this spawns an enemy every 25 frames on average
            Rectangle test = new Rectangle(getWidth(), rand.nextInt(getHeight()), 40, 40);

            if (!isOverlapping(test)) { // if not overlapping add new enemy
                enemies.add(new EnemyObject(test.x, test.y, test.width, test.height, EC, 5));
            }
        }

        // Collectible
        if (rand.nextInt(60) == 0) { // this spawns a point every 25 frames on average
            Rectangle test = new Rectangle(getWidth(), rand.nextInt(getHeight()), 30, 30);

            if (!isOverlapping(test)) {

                collectibles.add(new CollectibleObject( // same as enemy but for points and uses random colour from RGB
                        test.x, test.y, test.width, test.height,
                        new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), 4));
            }
        }
    }

    void moveObjects() {
        Iterator<EnemyObject> enemyItr = enemies.iterator(); // for enemy
        while (enemyItr.hasNext()) {
            EnemyObject obj = enemyItr.next();
            obj.speed(); // calls subclass speed()

            Rectangle b = obj.getBounds();
            if (b.x + b.width < 0) { // if off screen then remove
                enemyItr.remove();
            }
        }

        Iterator<CollectibleObject> colItr = collectibles.iterator(); // for points
        while (colItr.hasNext()) {
            CollectibleObject obj = colItr.next();
            obj.speed(); // calls subclass speed()

            Rectangle b = obj.getBounds();
            if (b.x + b.width < 0) { // if off screen then remove
                colItr.remove();
            }
        }
    }

    void checkCollisions() {
        Rectangle player = new Rectangle(px, py, playerWidth, playerHeight); // wreplace sizee with width and height for collisions

        // Collectibles
        Iterator<CollectibleObject> colItr = collectibles.iterator();
        while (colItr.hasNext()) { // loop all point objects
            CollectibleObject c = colItr.next();
            if (c.intersects(player)) { // if player touches point
                colItr.remove();
                score++;
            }
        }

        // -------------------
        // IF PLAYER CRASH
        // -------------------
        for (EnemyObject e : enemies) {
            if (e.intersects(player)) { // if player crashes
                up = false;
                down = false;
                left = false;
                right = false;
                timer.stop();
                EC = Color.RED; // make obstacles red again

                try (PrintWriter writer = new PrintWriter(new FileWriter("output.txt", true))) {
                    writer.println(score); // print score in output.txt
                } catch (IOException z) {
                    System.out.println("error output.txt");
                }

                showRetryMenu();
                bgCircles.clear();   // delete circles
                circle = false;      // redraw circles
                break;
            }
        }
    }

    void resetGame() {
        px = 100;
        py = 250;
        score = 0;

        enemies.clear();
        collectibles.clear();
        TheSun = null;

        timer.start();
    }

    void showRetryMenu() {
        int option = JOptionPane.showOptionDialog(
                this,
                "Game Over! Score: " + score,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Retry", "Quit"},
                "Retry"
        );

        if (option == 0) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    // ----------------------------------------
    // DRAW EVERYTHING
    // ----------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw Background circles
        for (BackgroundCircle bc : bgCircles) {
            g.setColor(bc.color);
            g.fillOval(bc.x, bc.y, bc.size, bc.size);
        }

        // Score text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 30);

        // draw player image
        g.drawImage(playerImg, px, py, playerWidth, playerHeight, null);

        // Enemies
        for (EnemyObject obj : enemies) {
            obj.draw(g); // draw enemys

            // DRAW THE SUN
            if (obj == TheSun) {
                Rectangle r = obj.getBounds();
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                g.drawString("The sun", r.x + 30, r.y + (r.height / 2) + 12);
            }
        }

        // Collectibles
        for (CollectibleObject obj : collectibles) {
            obj.draw(g); // draw points
        }
    }

    // ----------------------------------------
    // KEYBOARD MOVEMENT
    // ----------------------------------------
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP) up = true;
        if (k == KeyEvent.VK_DOWN) down = true;
        if (k == KeyEvent.VK_LEFT) left = true;
        if (k == KeyEvent.VK_RIGHT) right = true;
        if (k == KeyEvent.VK_B) {
            if (EC == Color.RED) { // if obstacles are red make them black
                EC = Color.BLACK;
            } else {
                EC = Color.RED;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP) up = false;
        if (k == KeyEvent.VK_DOWN) down = false;
        if (k == KeyEvent.VK_LEFT) left = false;
        if (k == KeyEvent.VK_RIGHT) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
