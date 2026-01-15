import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ClientScreen extends JPanel implements ActionListener,KeyListener {
    private String ip;
	private	int portNumber = 1024;
	private Socket socket;
	private int[][] grid;
	private boolean onStart;
	private JButton start;
	private BufferedReader in;
	private PrintWriter out;
	private int x, y;
	private int player;
	private int bombUnder;
	private boolean dead;
	private boolean placedBomb;
	private int side;
	private String blueScore, redScore, screenText;
	
    public ClientScreen(){
        this.setLayout(null);
        this.setFocusable(true);
		this.addKeyListener(this);
		ip = "10.210.84.196";

		blueScore = "0";
		redScore = "0";
		screenText = "WASD controls, Y to add 1 pt, [space] to place bomb, get the opps";

		onStart = true;
        start = new JButton("START");
        start.setBounds(260, 345, 200, 30);
        start.addActionListener(this);
        this.add(start);

		try {
			socket = new Socket(ip, portNumber);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {}

		reset();
    }

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		g.drawString(screenText, 300, 300);

        if (!onStart) {
			start.setVisible(false);

			g.drawString("BLUE: " + blueScore, 740, 60);
			g.drawString("RED: " + redScore, 740, 700);

			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					switch (grid[j][i]) {
						case 0:
							g.setColor(Color.WHITE);
							break;
						case 1:
							g.setColor(Color.BLACK);
							break;
						case 2:
							g.setColor(Color.GRAY);
							break;
						case 3:
							g.setColor(Color.BLUE);
							break;
						case 4:
							g.setColor(Color.BLUE);
							break;
						case 5:
							g.setColor(Color.RED);
							break;
						case 6:
							g.setColor(Color.RED);
							break;
						case 7:
							g.setColor(new Color(250, 80, 213));
							break;
						case 8:
							g.setColor(Color.ORANGE);
							break;
					}
					
					g.fillRect(80*j, 80*i, 80, 80);
					
					g.setColor(Color.white);
					g.drawRect(80*j, 80*i, 80, 80);
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800,720);
	}

	public void connect() throws IOException {
		repaint();

		while (true) {
			String message = in.readLine();
			System.out.println("[ClientScreen.connect] Message is: " + message);

			if (message.substring(0,2).equals("01")) {
				System.out.println("[ClientScreen.connect] Got here");

				player = Integer.parseInt(message.substring(2));
				
				for (int i = 0; i < grid.length; i++) {
					for (int j = 0; j < grid[i].length; j++) {
						if (grid[j][i] == player) {
							y = i;
							x = j;

							break;
						}
					}
				}

				switch (player) {
					case 3:
						side = 1;
						break;
					case 4:
						side = 1;
						break;
					case 5:
						side = 2;
						break;
					case 6:
						side = 2;
						break;
					default:
						break;
				}
			} 
			else if (message.substring(0,2).equals("02")) {
				// if(!dead){
				// 	winCounter+=1;
				// } else{
				// 	dead = false;
				// 	deadCounter+=1;
				// }
				// roundCounter +=1;
				onStart = false;
				
				repaint();
			} 
			else if (message.substring(0,2).equals("03")) {
				int mPlayer = Integer.parseInt(message.substring(2,3));
				int mx1 = Integer.parseInt(message.substring(3,4));
				int my1 = Integer.parseInt(message.substring(4,5));
				int mx2 = Integer.parseInt(message.substring(5,6));
				int my2 = Integer.parseInt(message.substring(6,7));

				if (message.substring(7,8).equals("1")) {
					grid[mx1][my1] = 7;
				}
				else {
					grid[mx1][my1] = 0;
				}
				
				grid[mx2][my2] = mPlayer;

				repaint();

				if (player == mPlayer) {
					x = mx2;
					y = my2;

					if (message.substring(7,8).equals("1")) {
						bombUnder = 0;
					}
				}
			}
			else if (message.substring(0,2).equals("04")) {
				int mx1 = Integer.parseInt(message.substring(2,3));
				int my1 = Integer.parseInt(message.substring(3,4));

				grid[mx1][my1] = 7;

				repaint();
			}
			else if (message.substring(0,2).equals("06")) {
				if (this.player == Integer.parseInt(message.substring(4))) {
					placedBomb = false;
				}
				playSound("bomb.wav");
				int mx1 = Integer.parseInt(message.substring(2,3));
				int my1 = Integer.parseInt(message.substring(3,4));

				for (int i = 0; i < 3; i++) {
					if (mx1+i <= 8 && grid[mx1+i][my1] != 1) {
						if (grid[mx1+i][my1] == player) {
							playSound("samsung.wav");

							if (!dead) {
								dead = true;
								move("07" + side);
							}
						}

						if (grid[mx1+i][my1] == 2) {
							grid[mx1+i][my1] = 0;
							break;
						}
						else {
							grid[mx1+i][my1] = 0;
						}
					}
					else {
						break;
					}
				}

				for (int i = 0; i >= -2; i--) {
					if (mx1+i >= 0 && grid[mx1+i][my1] != 1) {
						if (grid[mx1+i][my1] == player) {
							playSound("samsung.wav");
							if (!dead) {
								dead = true;
								move("07" + side);
							}
						}

						if (grid[mx1+i][my1] == 2) {
							grid[mx1+i][my1] = 0;
							break;
						}
						else {
							grid[mx1+i][my1] = 0;
						}
					}
					else {
						break;
					}
				}

				for (int i = 0; i >= -2; i--) {
					if (my1+i >= 0 && grid[mx1][my1+i] != 1) {
						if (grid[mx1][my1+i] == player) {
							playSound("samsung.wav");
							if (!dead) {
								dead = true;
								move("07" + side);
							}
						}

						if (grid[mx1][my1+i] == 2) {
							grid[mx1][my1+i] = 0;
							break;
						}
						else {
							grid[mx1][my1+i] = 0;
						}
					}
					else {
						break;
					}
				}

				for (int i = 0; i < 3; i++) {
					if (my1+i <= 8 && grid[mx1][my1+i] != 1) {
						if (grid[mx1][my1+i] == player) {
							playSound("samsung.wav");
							if (!dead) {
								dead = true;
								move("07" + side);
							}
						}

						if (grid[mx1][my1+i] == 2) {
							grid[mx1][my1+i] = 0;
							break;
						}
						else {
							grid[mx1][my1+i] = 0;
						}
					}
					else {
						break;
					}
				}
				
				repaint();
			}
			else if (message.substring(0, 2).equals("07")) {
				reset();

				blueScore = message.substring(2, 3);
				redScore = message.substring(3, 4);
					
				repaint();
			}
			else if (message.substring(0,2).equals("08")) {
				onStart = true;
				start.setVisible(true);

				superReset();
				if (Integer.parseInt((message.substring(2,3))) == side) {
					screenText = "You won!";
				}
				else {
					screenText = "You lost HAH, so FAK";
				}

				repaint();
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == start) {
			out.println("02");
			start.setVisible(false);
			screenText = "";
        }

		repaint();
    }

	public void move(String direction) { // first character is player number, second character is  (1 = w, 2 = a, 3 = s, 4 = d)
		out.println(direction);
		// System.out.println("[ClientScreen.move] We got here");
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// System.out.println("key code: " + e.getKeyCode());

		String message = "";
		
		if (!dead) {
			if (e.getKeyCode() == 87 && y-1>=0 && (grid[x][y-1] == 0)) { // W
				message += "03" + player + x + y + + x + (y-1) + bombUnder;
				move(message);

				playSound("fart.wav");
			}
			else if (e.getKeyCode() == 65 && x-1>=0 && (grid[x-1][y] == 0)) { // A
				message += "03" + player + x + y + (x-1) + y + bombUnder;
				move(message);

				playSound("fart.wav");
			}
			else if (e.getKeyCode() == 83 && y+1<=8 && (grid[x][y+1] == 0)) { // S
				message += "03" + player + x + y + x + (y+1) + bombUnder;
				move(message);

				playSound("fart.wav");
			}
			else if (e.getKeyCode() == 68 && x+1<=8 && (grid[x+1][y] == 0)) { // D
				message += "03" + player + x + y + (x+1) + y + bombUnder;
				move(message);
				
				playSound("fart.wav");
			}
			else if (e.getKeyCode() == 32 && !placedBomb) { // [spacebar]
				bombUnder = 1;
		
				move("05" + x + y + player);

				placedBomb = true;
			}
			else if (e.getKeyCode() == 89) { // [spacebar]
				move("09" + side);
			}
		}
	}

	@Override
    public void keyReleased(KeyEvent e) {}

	@Override
    public void keyTyped(KeyEvent e) {}

	public void playSound(String thing) {
        try {
            URL url = this.getClass().getClassLoader().getResource(thing);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            clip.start();
        } catch (Exception e) {}
    }

	public void reset() {
		dead = false;
		bombUnder = 0; // 0 = false, 1 = true
		placedBomb = false;

        grid = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j] = 2;

                if ((i%2 == 1) && (j%2 == 1)) {
                    grid[i][j] = 1;
                }
            }
        }

        grid[0][0] = 3;
        grid[0][1] = 0;
        grid[1][0] = 0;
        grid[0][8] = 4;
        grid[0][7] = 0;
        grid[1][8] = 0;
        grid[7][8] = 0;
        grid[7][0] = 0;
        grid[8][1] = 0;
        grid[8][0] = 5;
        grid[8][7] = 0;
        grid[8][8] = 6;

		if (player != 0) {
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[i].length; j++) {
					if (grid[j][i] == player) {
						y = i;
						x = j;

						break;
					}
				}
			}
		}
	}

	public void superReset() {
		reset();
		blueScore = "0";
		redScore = "0";
	}
}