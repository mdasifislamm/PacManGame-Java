
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Game extends JPanel implements ActionListener {
	
	private Dimension dim;
    private final Font font = new Font("a Astro Space", Font.BOLD, 16);
    private boolean gameRunning = false;
    private boolean pacmanAlive = false;

    private final int B_SIZE = 24;
    private final int NUM_BLOCK = 15;
    private final int SCREEN_SIZE = NUM_BLOCK * B_SIZE;
    private final int MAX_GHOSTS = 15;
    private final int PACMAN_SPEED = 4;

    private int N_GHOSTS = 6;
    private int lives, score;
    private int[] dx, dy;
    private int[] ghostX, ghostY, ghostDX, ghostDY, ghostSpeed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacmanX, pacmanY, pacmanDirX, pacmanDirY;
    private int reqDx, reqDy;

    private final int levelData[] = {
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 0, 25, 24, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  0, 0, 0, 17, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 18, 18, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 18,  18,  22,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16,  16,  20,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 24, 24, 24, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 20, 0, 0, 0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 20, 0, 0, 0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 18, 18, 18, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private int[] screenData;
    private Timer timer;

    public Game() {

        addImages();
        initializeVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initializeGame();
    }
    
    
    private void addImages() {
    	down = new ImageIcon("res/down.gif").getImage();
    	up = new ImageIcon("res/up.gif").getImage();
    	left = new ImageIcon("res/left.gif").getImage();
    	right = new ImageIcon("res/right.gif").getImage();
        ghost = new ImageIcon("res/ghost.gif").getImage();
        heart = new ImageIcon("res/heart.png").getImage();

    }
       private void initializeVariables() {

        screenData = new int[NUM_BLOCK * NUM_BLOCK];
        dim = new Dimension(400, 400);
        ghostX = new int[MAX_GHOSTS];
        ghostDX = new int[MAX_GHOSTS];
        ghostY = new int[MAX_GHOSTS];
        ghostDY = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    private void startGame(Graphics2D g) {

        if (pacmanAlive) {

            death();

        } else {

            movePacman();
            drawPacman(g);
            moveGhosts(g);
            checkMaze();
        }
    }

    private void showIntro(Graphics2D g2d) {
 
    	String start = "Press ENTER to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, 70, 180);
    }

    private void drawScore(Graphics2D g) {
        g.setFont(font);
        g.setColor(new Color(124, 211, 9));
        String s = "Score: " + score;
        g.drawString(s,  247,  376);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < NUM_BLOCK * NUM_BLOCK && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initializeLevel();
        }
    }

    private void death() {

    	lives--;

        if (lives == 0) {
            gameRunning = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g) {

        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghostX[i] % B_SIZE == 0 && ghostY[i] % B_SIZE == 0) {
                pos = ghostX[i] / B_SIZE + NUM_BLOCK * (int) (ghostY[i] / B_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghostDX[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghostDY[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghostDX[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghostDY[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghostDX[i] = 0;
                        ghostDY[i] = 0;
                    } else {
                        ghostDX[i] = -ghostDX[i];
                        ghostDY[i] = -ghostDY[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghostDX[i] = dx[count];
                    ghostDY[i] = dy[count];
                }

            }

            ghostX[i] = ghostX[i] + (ghostDX[i] * ghostSpeed[i]);
            ghostY[i] = ghostY[i] + (ghostDY[i] * ghostSpeed[i]);
            drawGhost(g, ghostX[i] + 1, ghostY[i] + 1);

            if (pacmanX > (ghostX[i] - 12) && pacmanX < (ghostX[i] + 12)
                    && pacmanY > (ghostY[i] - 12) && pacmanY < (ghostY[i] + 12)
                    && gameRunning) {

                pacmanAlive = true;
            }
        }
    }

    private void drawGhost(Graphics2D g, int x, int y) {
    	g.drawImage(ghost, x, y, this);
        }

    private void movePacman() {

        int pos;
        int ch;

        if (pacmanX % B_SIZE == 0 && pacmanY % B_SIZE == 0) {
            pos = pacmanX / B_SIZE + NUM_BLOCK * (int) (pacmanY / B_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (reqDx != 0 || reqDy != 0) {
                if (!((reqDx == -1 && reqDy == 0 && (ch & 1) != 0)
                        || (reqDx == 1 && reqDy == 0 && (ch & 4) != 0)
                        || (reqDx == 0 && reqDy == -1 && (ch & 2) != 0)
                        || (reqDx == 0 && reqDy == 1 && (ch & 8) != 0))) {
                    pacmanDirX = reqDx;
                    pacmanDirY = reqDy;
                }
            }

            // Check for standstill
            if ((pacmanDirX == -1 && pacmanDirY == 0 && (ch & 1) != 0)
                    || (pacmanDirX == 1 && pacmanDirY == 0 && (ch & 4) != 0)
                    || (pacmanDirX == 0 && pacmanDirY == -1 && (ch & 2) != 0)
                    || (pacmanDirX == 0 && pacmanDirY == 1 && (ch & 8) != 0)) {
                pacmanDirX = 0;
                pacmanDirY = 0;
            }
        } 
        pacmanX = pacmanX + PACMAN_SPEED * pacmanDirX;
        pacmanY = pacmanY + PACMAN_SPEED * pacmanDirY;
    }

    private void drawPacman(Graphics2D g2d) {

        if (reqDx == -1) {
        	g2d.drawImage(left, pacmanX + 1, pacmanY + 1, this);
        } else if (reqDx == 1) {
        	g2d.drawImage(right, pacmanX + 1, pacmanY + 1, this);
        } else if (reqDy == -1) {
        	g2d.drawImage(up, pacmanX + 1, pacmanY + 1, this);
        } else {
        	g2d.drawImage(down, pacmanX + 1, pacmanY + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) {

        int i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += B_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += B_SIZE) {

                g2d.setColor(new Color(200,82,1));
                g2d.setStroke(new BasicStroke(5));
                
                if ((levelData[i] == 0)) { 
                	g2d.fillRect(x, y, B_SIZE, B_SIZE);
                 }

                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + B_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + B_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + B_SIZE - 1, y, x + B_SIZE - 1,
                            y + B_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + B_SIZE - 1, x + B_SIZE - 1,
                            y + B_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) { 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }

    private void initializeGame() {

    	lives = 3;
        score = 0;
        initializeLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initializeLevel() {

        int i;
        for (i = 0; i < NUM_BLOCK * NUM_BLOCK; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

    	int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghostY[i] = 4 * B_SIZE; //start position
            ghostX[i] = 4 * B_SIZE;
            ghostDY[i] = 0;
            ghostDX[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacmanX = 7 * B_SIZE;  //start position
        pacmanY = 11 * B_SIZE;
        pacmanDirX = 0;	//reset direction move
        pacmanDirY = 0;
        reqDx = 0;		// reset direction controls
        reqDy = 0;
        pacmanAlive = false;
    }

 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, dim.width, dim.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (gameRunning) {
            startGame(g2d);
        } else {
            showIntro(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (gameRunning) {
            	
            	switch(key) {
            	case KeyEvent.VK_LEFT:
            		reqDx = -1;
                	reqDy = 0;
                	break;
            	case KeyEvent.VK_RIGHT:
            		reqDx = 1;
                	reqDy = 0;
                	break;
            	case KeyEvent.VK_UP:
            		reqDx = 0;
                	reqDy = -1;
                	break;
            	case KeyEvent.VK_DOWN:
            		reqDx = 0;
                	reqDy = 1;
                	break;
            	case KeyEvent.VK_ESCAPE:
            		gameRunning = false;
                	break;
            	}
            	
            } else {
                if (key == KeyEvent.VK_ENTER) {
                    gameRunning = true;
                    initializeGame();
                }
            }
        }
}

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
		
	}