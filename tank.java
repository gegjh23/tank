 
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
 
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
 
import tankwar.Tank.Direction;
import barrier.EnemyBorn;
import barrier.Gold;
import barrier.Home;
import barrier.Iron;
import barrier.SelfBorn;
import barrier.Wall;
 
public class TankWar implements KeyListener {
    static boolean TIMEOUT = false;
    private JFrame f;
    private JPanel gamePanel;
    private PanelShow messgePanel;
    private myPanel p;
    private Tank myTank;
    public static final int AREA_WIDTH = 830;
    public static final int AREA_HEIGHT = 800;
    private ArrayList<Missle> missles = new ArrayList<Missle>();
    private ArrayList<Tank> allTanks = new ArrayList<Tank>();
    private ArrayList<Boom> booms = new ArrayList<Boom>();
    private ArrayList<Wall> walls = new ArrayList<Wall>();
    private ArrayList<Iron> irons = new ArrayList<Iron>();
    private ArrayList<Gold> golds = new ArrayList<Gold>();
    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<EnemyBorn> enemyBorns = new ArrayList<EnemyBorn>();
    private SelfBorn selfBorn;
    private Home home;
    private Tank enemyTank;
    private Random r;
    private ImageIcon backGround;
    private final String map;
    private int tankMax;
    private boolean over = false;
    private static int selfMax = 3;
    private boolean win;
    private boolean flash = false;
    private TankWar tw = this;
    static int SCORE = 0;
 
    private final JFrame mainF;
    private int style;
 
    public TankWar(String map, int tankMax, JFrame mainF, int style)
            throws Exception {
        this.map = map;
        this.tankMax = tankMax;
        this.mainF = mainF;
        this.style = style;
        init();
    }
 
    private void init() {
        f = new JFrame("坦克大战 V3.0");
        gamePanel = new JPanel(null);
        p = new myPanel();
        p.setBackground(Color.WHITE);
        r = new Random();
        messgePanel = new PanelShow();
        initMap(new File("map/" + map));
 
        try {
            myTank = new Tank(selfBorn.getX(), selfBorn.getY(), true, allTanks,
                    walls, irons, golds, missles, home, booms, style);
        } catch (Exception e1) {
        }
        myTank.setDir(Direction.U);
 
        allTanks.add(myTank);
        addTank();
        try {
            backGround = new ImageIcon(
                    TankWar.class.getResource("/pic/whiteback.jpg"));
        } catch (Exception e) {
        }
 
        p.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.WHITE));
        p.setSize(AREA_WIDTH, AREA_HEIGHT);
        messgePanel.setBounds(AREA_WIDTH, 0, 200, AREA_HEIGHT);
        gamePanel.add(messgePanel);
        gamePanel.add(p);
        f.add(gamePanel);
        f.setBounds(0, 0, AREA_WIDTH + 200, AREA_HEIGHT);
        f.setDefaultCloseOperation(3);
        f.setResizable(true);
        f.setFocusable(true);
        f.addKeyListener(this);
        f.setVisible(true);
 
        new Thread(new Runnable() {
            public void run() {
                while (!over) {
                    if (!myTank.isLive()) {
                        selfMax--;
                        if (selfMax < 0) {
                            f.removeKeyListener(tw);
                            over = true;
                            win = false;
                            break;
                        } else {
                            myTank.setLevel(1);
                            myTank.setX(selfBorn.getX());
                            myTank.setY(selfBorn.getY());
                            myTank.setDir(Direction.U);
                            myTank.setHp(50);
                            myTank.setLive(true);
                        }
                    }
                    if (tankMax <= 0 && allTanks.size() == 1) {
                        f.removeKeyListener(tw);
                        over = true;
                        win = true;
                    }
                    if (!home.isLive()) {
                        f.removeKeyListener(tw);
                        over = true;
                        win = false;
                    }
                    p.repaint();
                    myTank.move();
                    for (int i = 1; i < allTanks.size(); i++) {
                        allTanks.get(i).move();
                        allTanks.get(i).setNoFire(myTank.getNoFire() + 1);
                        // if(allTanks.get(i).getX()%5==0&&allTanks.get(i).getY()%5==0)
                        aI(allTanks.get(i));
                    }
                    if (allTanks.size() <= enemyBorns.size() + 1)
                        addTank();
                    myTank.setNoFire(myTank.getNoFire() + 1);
                    messgePanel.setEnemyCount(tankMax);
                    messgePanel.setSelfCount(selfMax);
                    messgePanel.setScore(SCORE);
                    if (SCORE % 500 == 0) {
                        SCORE += 100;
                        Item item = new Item(allTanks, booms, irons, home);
                        items.add(item);
                        item.start();
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                    }
                }
 
                over();
            }
 
        }).start();
 
    }
 
    private class myPanel extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 4408440723797225328L;
 
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backGround.getImage(), 0, 0, null);
            for (int j = 0; j < walls.size(); j++) {
                walls.get(j).draw(g);
            }
            for (int j = 0; j < irons.size(); j++) {
                irons.get(j).draw(g);
            }
            for (int j = 0; j < golds.size(); j++) {
                golds.get(j).draw(g);
            }
            for (int j = 0; j < enemyBorns.size(); j++) {
                enemyBorns.get(j).draw(g);
            }
            home.draw(g);
            selfBorn.draw(g);
 
            for (int j = 0; j < allTanks.size(); j++) {
                allTanks.get(j).drawTank(g);
            }
            for (int j = 0; j < irons.size(); j++) {
                irons.get(j).draw(g);
            }
 
            for (int i = 0; i < missles.size(); i++) {
                missles.get(i).drawMissle(g);
                if (!missles.get(i).isLive())
                    missles.remove(i);
            }
            for (int i = 0; i < booms.size(); i++) {
                if (booms.get(i).isLive())
                    booms.get(i).drawBoom(g);
                else
                    booms.remove(i);
            }
            for (int j = 0; j < items.size(); j++) {
                if (!items.get(j).isLive()) {
                    items.remove(j);
                    continue;
                }
                items.get(j).draw(g);
            }
            if (over)
                drawOver(g);
            messgePanel.repaint();
        }
    }
 
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
 
    }
 
    @Override
    public void keyPressed(KeyEvent e) {
        if (over) {
            if (e.getKeyCode() == KeyEvent.VK_F1) {
                over = false;
                missles.clear();
                allTanks.clear();
                booms.clear();
                walls.clear();
                irons.clear();
                golds.clear();
                enemyBorns.clear();
                try {
                    init();
                } catch (Exception e1) {
                }
            } else {
                f.setVisible(false);
                mainF.setSize(800, 800);
                mainF.setVisible(true);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            myTank.fire();
        } else {
            myTank.keyPress(e);
        }
    }
 
    @Override
    public void keyReleased(KeyEvent e) {
 
        myTank.keyReleased(e);
    }
 
    public void aI(Tank tank) {
        if (TIMEOUT) {
            tank.setUp(false);
            tank.setLeft(false);
            tank.setDown(false);
            tank.setRight(false);
            return;
        }
