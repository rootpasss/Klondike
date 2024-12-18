/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 06/03/2023, Time: 13:31:53
 */
package io.bitlab.api.view;

import io.bitlab.api.model.RecordStore;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class GameView extends JFrame {
  public static Map<String,BufferedImage> imagemap=new HashMap<>();
  private List<Area> arealist=new ArrayList<>();
  public static boolean pick=false;
  public static int total;
  public static int from;
  public static int to;
  private JPanel contentPane;
  private JPanel statusPane;
  private BufferedImage back;
  private JButton btnDeal;
  private JButton btnDeck;
  private JButton btnUndo;
  private JCheckBox cbTimed;
  private RecordStore rs=RecordStore.openRecordStore();
  private JLabel lblBonus;
  private JLabel lblScore;
  private JLabel lblTime;

  public GameView() {
    super("Solitaire");
    java.awt.Dimension d=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int width=695; int height=540;
    int x=d.width/2-width/2;
    setBounds(x,10,width,height);
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    contentPane=new JPanel(null) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.drawImage(back,0,0,null);
      }
    };
    add(contentPane);
    loadCards();
    createArea();
    addButtons();
    setVisible(true);
    addStatusBar();
    System.out.printf("%c %c %c %c  Welcome to Solitaire! %c %c %c %c%n",
    '\u2667','\u2662','\u2661','\u2664','\u2664','\u2661','\u2662','\u2667');
  }

  private void addStatusBar() {
    GridBagConstraints c=new GridBagConstraints();
    c.fill=GridBagConstraints.HORIZONTAL;c.weightx=1;
    statusPane=new JPanel(new GridBagLayout());
    statusPane.setBackground(Color.decode("#FFFFFF"));
    statusPane.setBounds(0,getHeight()-(getInsets().top+20),getWidth(),20);
    lblBonus=new JLabel();
    lblScore=new JLabel("Score: 0");
    lblTime=new JLabel(" Time: 0  ");
    lblBonus.setForeground(Color.decode("#000000"));
    lblScore.setForeground(lblBonus.getForeground());
    lblTime.setForeground(lblBonus.getForeground());
    lblScore.setHorizontalAlignment(JLabel.RIGHT);
    statusPane.add(lblBonus,c);c.gridx=1;
    statusPane.add(lblScore,c);c.gridx=2;c.weightx=0;
    statusPane.add(lblTime,c);
    contentPane.add(statusPane);
  }

  private void addButtons() {
    JPanel btnPane=new JPanel();
    btnPane.setOpaque(false);
    btnPane.setBounds(getWidth()/2-180,getHeight()-95,370,50);
    btnDeal=new JButton("Deal New");
    btnUndo=new JButton("Undo");
    btnDeck=new JButton("Deck");
    cbTimed=new JCheckBox("Timed");
    cbTimed.setForeground(UIManager.getLookAndFeel().toString().contains("metal")?Color.DARK_GRAY:
                          Color.decode("#FFFFFF"));
    cbTimed.addActionListener(e->statusPane.setVisible(cbTimed.isSelected()));
    btnPane.add(btnDeal);
    btnPane.add(btnUndo);
    btnPane.add(btnDeck);
    btnPane.add(cbTimed);
    contentPane.add(btnPane);
  }

  private void loadCards() {
    try {
      InputStream is=getClass().getResourceAsStream("/suits/background.png");
      back=ImageIO.read(is);
      String path;
      String card;
      for(int i=0;i<53;i++) {
        if(i<13) {
          card="spades"+(i+1);
        } else if(i<26) {
          card="hearts"+(i+1-13);
        } else if(i<39) {
          card="clubs"+(i+1-26);
        } else if(i<52) {
          card="diamonds"+(i+1-39);
        } else {
          card="???";
        }
        int index=rs.getRecord()[0];
        is=getClass().getResourceAsStream("/suits/"+(card.equals("???")?DeckArt.getDeckName(index):card)+".png");
        imagemap.put(card,ImageIO.read(is));
      }
    } catch(IOException e) {e.printStackTrace();JOptionPane.showMessageDialog(null,e);System.exit(0);}
  }

  private void createArea() {
    //adding waste zone
    Area area=new Area(95,20,0);
    arealist.add(area);
    contentPane.add(area);

    //adding tableau zones
    int x=10; int y=140;
    for(int i=0;i<7;i++) {
      area=new Area(x,y,i+1);
      x+=100;
      arealist.add(area);
      contentPane.add(area);
    }

    //adding foundation zones
    x=310;
    for(int i=0;i<4;i++) {
      area=new Area(x,20,8+i);
      arealist.add(area);
      contentPane.add(area);
      x+=100;
    }

    //adding stock zone
    area=new Area(10,20,-1);
    arealist.add(area);
    contentPane.add(area);
  }

  public void updateUI(java.util.Stack[] stack) {
    for(int i=0;i<stack.length;i++) {
      arealist.get(i).flush();
      int total=stack[i].size();
      if(i==0) {//waste area overlap only
        total=stack[i].size()>3?3:stack[i].size();
        for(int j=0;j<total;j++) {
          String card=stack[i].elementAt(stack[i].size()-total+j).toString();
          arealist.get(i).addCard(new Vcard(card));
        }
      } else {
        if(i>=8&&i<12)//foundations area overlap only
          total=stack[i].empty()?0:stack[i].size()<5?1:stack[i].size()<9?2:stack[i].size()<13?3:4;
        else if(i==12)//stock area overlap only
          total=stack[i].empty()?0:stack[i].size()<12?1:stack[i].size()<20?2:3;
        for(int j=0;j<total;j++) {
          String card=stack[i].elementAt(stack[i].size()-total+j).toString();
          arealist.get(i).addCard(new Vcard(card));
        }
      }
      //System.out.printf("%d png to area %d%n",arealist.get(i).getCardList().size(),i);//debug only
    }
    repaint();
    //System.out.println();//debug only
  }

  public void updateTime(int t) {
    lblTime.setText(" Time: "+t+"  ");
  }

  public void updateScore(int s) {
    lblScore.setText("Score: "+s);
  }

  public void updateBonus(int b) {
    lblBonus.setText(b>=0?"  Bonus: "+b:"");
  }

  public void addClickListener(MouseListener l) {
    for(int i=0;i<contentPane.getComponentCount();i++)
      ((javax.swing.JComponent)contentPane.getComponent(i)).addMouseListener(l);
  }

  public void addDealListener(ActionListener l) {
    btnDeal.addActionListener(l);
  }

  public void addUndoListener(ActionListener l) {
    btnUndo.addActionListener(l);
  }

  public void addDeckListener(ActionListener l) {
    btnDeck.addActionListener(l);
  }

  public void enableUndoButton(boolean state) {
    btnUndo.setEnabled(!state);
  }

  public void enableTimedOption(boolean b) {
    cbTimed.setEnabled(b);
  }

  public void setTimedOption(boolean b) {
    cbTimed.setSelected(b);
    statusPane.setVisible(b);
  }

  public void addDestroyListener(WindowListener l) {
    addWindowListener(l);
  }

  public void addDealKeyListener(AbstractAction l) {
    applyKeyStroke(l,"N","deal");
  }

  public void addUndoKeyListener(AbstractAction l) {
    applyKeyStroke(l,"U","undo");
  }

  public void addDeckKeyListener(AbstractAction l) {
    applyKeyStroke(l,"D","deck");
  }

  public void addStatsListener(AbstractAction l) {
    applyKeyStroke(l,"S","stats");
  }

  public int getFromIndex() {
    return from;
  }

  public int getToIndex() {
    return to;
  }

  public int getCountIndex() {
    return total;
  }

  public List<Rectangle2D.Double> getItems(int index) {
    return arealist.get(index).getCardList();
  }

  public boolean isTimedGame() {
    return cbTimed.isSelected();
  }

  private void applyKeyStroke(javax.swing.AbstractAction a,String s,String d) {
    contentPane.getInputMap(contentPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
               .put(javax.swing.KeyStroke.getKeyStroke(s),d);
    contentPane.getActionMap().put(d,a);
  }
}

class Area extends JPanel {
  private List<Rectangle2D.Double> cardlist=new ArrayList<>();
  private int from;

  public Area(int x,int y,int from) {
    this.from=from;
    setName(String.valueOf(from<0?12:from));
    setOpaque(false);
    if(from<0)//stock area
      setBounds(x,y,78,105);
    else if(from==0)//waste area
      setBounds(x,y,103,105);
    else if(from<8)//tableau area
      setBounds(x,y,71,320);
    else//foundations area
      setBounds(x,y,78,105);
  }

  public List<Rectangle2D.Double> getCardList() {
    return cardlist;
  }

  public void addCard(Vcard card) {
    if(from==-1||from>7) {//horizontal and vertical overlapping for stock and foundations cards
      card.x+=cardlist.size()*2;
      card.y+=cardlist.size()*1;
    } else if(from==0) {//horizontal and vertical overlapping for waste cards
      card.x+=cardlist.size()*15;
      card.y+=cardlist.size()*4;
    } else if(!cardlist.isEmpty()&&from>0&&from<8) {//vertical overlapping for tableau cards
      double spc=cardlist.get(cardlist.size()-1).y;
      spc+=cardlist.get(cardlist.size()-1).toString().contains("?")?3:15;
      card.y=spc;
    }
    cardlist.add(card);
  }

  public void flush() {
    cardlist.removeAll(cardlist);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2=(Graphics2D)g;
    for(Rectangle2D.Double c:cardlist) {
      g2.drawImage(GameView.imagemap.get(c.toString()),(int)c.getX(),(int)c.getY(),null);
    }
  }
}

class Vcard extends Rectangle2D.Double {
  private String name;

  public Vcard(String name) {
    super(0,0,71,96);
    this.name=name;
  }

  @Override
  public String toString() {
    return name;
  }
}
