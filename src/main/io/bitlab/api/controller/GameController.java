/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 06/03/2023, Time: 14:03:43
 */
package io.bitlab.api.controller;

import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import io.bitlab.api.model.GameEngine;
import io.bitlab.api.model.RecordStore;
import io.bitlab.api.view.DeckArt;
import io.bitlab.api.view.GameView;
import io.bitlab.api.view.Stat;

public class GameController {
  private GameEngine ge;
  private GameView gv;
  private DeckArt da;
  private int deckIndex;
  private int played;
  private int playT;
  private int won;
  private int wonT;
  private int time;
  private int idx;
  private int ws;
  private int ls;
  private int wsT;
  private int lsT;
  private int wsC=0;
  private int lsC=0;
  private int wsCT=0;
  private int lsCT=0;
  private RecordStore rs=RecordStore.openRecordStore();
  private boolean inAction=false;
  private boolean deckChanged=false;
  //private boolean timed=true;
  private Timer t;
  private LocalDate date;
  private int[] data;

  public GameController(GameView gv,GameEngine ge,DeckArt da) {
    this.gv=gv;
    this.ge=ge;
    this.da=da;
    deckIndex=RecordStore.getRecord()[0];
    played=RecordStore.getRecord()[1];
    won=RecordStore.getRecord()[2];
    playT=RecordStore.getRecord()[3];
    wonT=RecordStore.getRecord()[4];
    ws=RecordStore.getRecord()[5];
    ls=RecordStore.getRecord()[6];
    wsT=RecordStore.getRecord()[7];
    lsT=RecordStore.getRecord()[8];
    date=LocalDate.now();
    gv.setTimedOption(rs.getRecord()[9]==1);
    gv.addClickListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        JComponent comp=((JComponent)evt.getSource());
        int from=Integer.parseInt(comp.getName());
        from=from+1>12?12:from;
        java.util.List<Rectangle2D.Double>cards=gv.getItems(from);
        if(evt.getClickCount()==2) {
          if(from>0&&from<8) {
            ge.moveCardDoubleClick(from);
            if(!inAction) {
              inAction=true;
              played++;
              gv.enableTimedOption(false);
              if(gv.isTimedGame()) {
                t=startTimer();
                playT++;
              }
            }
            showGameState();
          }
          GameView.pick=false;
        } else {
          if(!GameView.pick) {
            if(from==12) {
              ge.spinStock();
              gv.updateUI(ge.getStacks());
              gv.enableUndoButton(ge.isEmptyStack());
              GameView.pick=false;
              if(!inAction) {
                inAction=true;
                played++;
                gv.enableTimedOption(false);
                if(gv.isTimedGame()) {
                  t=startTimer();
                  playT++;
                }
              }
            } else if(from>0&&from<8) {
              for(Rectangle2D.Double r:cards) {
                if(r.contains(evt.getX(),evt.getY())) {
                  GameView.total=cards.size()-cards.indexOf(r);
                  GameView.from=from;
                  GameView.pick=true;
                }
              }
            } else if(cards.size()>0) {
              GameView.total=1;
              GameView.from=from;
              GameView.pick=true;
            }
          } else {
            System.out.print("\nMove "+GameView.total+" cards from pile "+GameView.from+" to pile "+from);
            GameView.to=from;
            GameView.pick=false;
            ge.moveCard(GameView.from,GameView.total,from);
            if(!inAction) {
              inAction=true;
              played++;
              gv.enableTimedOption(false);
              if(gv.isTimedGame()) {
                t=startTimer();
                playT++;
              }
            }
            showGameState();
          }
        }
      }
    });
    gv.addDealListener(e->createNewGame());
    gv.addUndoListener(e->undo());
    gv.addDeckListener(e->deck());
    gv.addDestroyListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt) {
        destroy();
      }
    });
    gv.addDealKeyListener(new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createNewGame();
      }
    });
    gv.addUndoKeyListener(new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        undo();
      }
    });
    gv.addDeckKeyListener(new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deck();
      }
    });
    gv.addStatsListener(new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<String>a=(ArrayList<String>)getBest();
        int p=playT>0?wonT*100/playT:0;
        wsT=wsCT>wsT?wsCT:wsT; lsT=lsCT>lsT?lsCT:lsT;
        Object[]d={a,playT,wonT,p+"%",wsT,lsT,wsCT};
        Object[]o={"Reset","Close"};
        int opt=JOptionPane.showOptionDialog(gv,Stat.getPane2(d),"Solitaire Statistics",
                                          JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,o,o[1]);
        if(opt==0)
          reset();
      }
    });
    da.addDeckDialogListener(e->changeDeck());
    showGameState();

    Stat.addListListener(new javax.swing.event.ListSelectionListener() {
      @Override
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        if(!evt.getValueIsAdjusting()) {
          idx=((javax.swing.JList)evt.getSource()).getSelectedIndex();
          int p;int w;int x;int y;int z;
          if(idx==0) {
            p=playT;w=wonT;
            x=wsCT>wsT?wsCT:wsT;
            y=lsCT>lsT?lsCT:lsT;
            z=wsCT;
          } else {
            p=played;w=won;
            x=wsC>ws?wsC:ws;
            y=lsC>ls?lsC:ls;
            z=wsC;
          }
          Stat.hide();
          int pp=p>0?w*100/p:0;
          Object[]d={p,w,pp+"%",x,y,z};
          Stat.updatePane(d);
        }
      }
    });
  }

  private void createNewGame() {
    if(JOptionPane.showConfirmDialog(gv,"Deal New?","Solitaire",JOptionPane.YES_NO_OPTION)==0) {
      if(gv.isTimedGame()) {
        t.cancel();t.purge();
        lsCT++;
      } else {
        lsC++;
      }
      storeData();
      ge.newGame();
      showGameState();
      inAction=false;
      gv.updateTime(0);
      gv.updateScore(0);
      gv.updateBonus(-1);
      gv.enableTimedOption(true);
    }
  }

  private void undo() {
    ge.moveCardUndo();
    showGameState();
  }

  private void deck() {
    da.setLocationRelativeTo(gv);
    da.setVisible(true);
  }

  private void destroy() {
    if(inAction) {
      if(gv.isTimedGame())
        lsCT++;
      else
        lsC++;
    }
    storeData();
  }

  private void reset() {
    if(idx==0) {
      playT=0;wonT=0;wsT=0;wsCT=0;lsT=0;lsCT=0;
      data=java.util.Arrays.copyOf(data,10);
    } else {
      played=0;won=0;ws=0;wsC=0;ls=0;lsC=0;
    }
    storeData();
  }

  private void changeDeck() {
    GameView.imagemap.remove("???");
    try {
      GameView.imagemap.put("???",ImageIO.read(getClass().getResourceAsStream("/suits/"+da.getDeckName())));
      deckIndex=da.getDeckIndex();
      gv.updateUI(ge.getStacks());
      da.setVisible(false);
      deckChanged=true;
    } catch(Exception e) {e.printStackTrace();}
  }

  private void showGameState() {
    gv.updateUI(ge.getStacks());
    gv.enableUndoButton(ge.isEmptyStack());
    gv.updateScore(ge.getScore());
    int opt;
    if(ge.isWinner()) {
      gv.enableUndoButton(true);
      lsC=0;
      if(gv.isTimedGame()) {
        t.cancel();t.purge();
        wonT++;
        wsCT++;
        storeData();
        int b=700000/time;
        int s=b+ge.getScore();
        int h=(int)getBest(0);
        String d=(String)getBest(1);
        Object[]da={ge.getScore(),time,b,ge.getScore()+b,h,d,playT,wonT,wonT*100/playT};
        Object[]o={"Exit","Play Again"};
        opt=JOptionPane.showOptionDialog(gv,Stat.getPane1(da),"Game Won",JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,null,o,o[1]);
        if(opt==0)
          System.exit(0);
      } else {
        won++;
        wsC++;
        storeData();
        opt=JOptionPane.showConfirmDialog(gv,"Deal Again?","Game Won",JOptionPane.YES_NO_OPTION);
      }

      if(gv.isTimedGame()&&opt==1||opt==0) {
        ge.newGame();
        gv.updateUI(ge.getStacks());
        gv.updateTime(0);
        gv.updateScore(0);
        gv.updateBonus(-1);
        gv.enableTimedOption(true);
      }

      deckChanged=false;
      inAction=false;
    }
  }

  private void storeData() {
    data[0]=deckIndex;
    data[1]=played;
    data[2]=won;
    data[3]=playT;
    data[4]=wonT;
    data[5]=wsC>ws?wsC:ws;
    data[6]=lsC>ls?lsC:ls;
    data[7]=wsCT>wsT?wsCT:wsT;
    data[8]=lsCT>lsT?lsCT:lsT;
    data[9]=gv.isTimedGame()?1:0;

    if(gv.isTimedGame()&&ge.isWinner()) {
      int b=700000/time;
      int s=b+ge.getScore();
      int y=date.getYear();
      int m=date.getMonthValue();
      int d=date.getDayOfMonth();
      int[]apnd=java.util.Arrays.copyOf(data,data.length+7);
      gv.updateBonus(b);
      gv.updateScore(s);
      apnd[data.length]=ge.getScore();
      apnd[data.length+1]=time;
      apnd[data.length+2]=b;
      apnd[data.length+3]=s;
      apnd[data.length+4]=y;
      apnd[data.length+5]=m;
      apnd[data.length+6]=d;
      rs.setRecord(apnd);
    } else {
      rs.setRecord(data);
    }
  }

  private void setLocalTiming(int time) {
    this.time=time;
  }

  private Object getBest(int index) {
    rs.openRecordStore();
    int[]data=rs.getRecord();
    Object[]val=new Object[2];
    int top=0;
    for(int i=10;i<data.length;i+=7) {
      if(data[i+3]>top) {
        top=data[i+3];
        val[0]=data[i+3];
        val[1]=new String(data[i+6]+"/"+data[i+5]+"/"+data[i+4]);
      }
    }
    return val[index];
  }

  private ArrayList<String> getBest() {
    rs.openRecordStore();
    int[]data=rs.getRecord();
    java.text.DecimalFormat df=new java.text.DecimalFormat("00");
    if(data.length>10) {
      java.util.Map<Integer,String>m=new java.util.HashMap<>();
      for(int i=10;i<data.length;i+=7) {
        m.put(data[i+3],df.format(data[i+6])+"/"+df.format(data[i+5])+"/"+data[i+4]);
      }

      ArrayList<String>d=new ArrayList<>();
      m.entrySet().stream()
       .sorted(java.util.Map.Entry.<Integer,String>comparingByKey().reversed()).limit(5)
       .forEach(entry->d.add(entry.getKey()+" "+entry.getValue()));
      return d;
    } else {
      return new ArrayList<String>(java.util.Arrays.asList(new String[]{"","","","",""}));
    }
  }

  private Timer startTimer() {
    Timer t=new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      int c=0;
      @Override
      public void run() {
        gv.updateTime(c);
        setLocalTiming(c);
        if(c%10==0&&c>0)
          gv.updateScore(ge.subtractScore());
        c++;
      }
    },0,1000);
    return t;
  }
}
