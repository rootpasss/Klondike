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

import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import io.bitlab.api.model.GameEngine;
import io.bitlab.api.model.RecordStore;
import io.bitlab.api.view.DeckArt;
import io.bitlab.api.view.GameView;

public class GameController {
  private GameEngine ge;
  private GameView gv;
  private DeckArt da;
  private int deckIndex;
  private int played;
  private int won;
  private int time;
  private RecordStore rs=RecordStore.openRecordStore();
  private boolean inAction=false;
  private boolean deckChanged=false;
  private boolean timed=true;
  private Timer t;

  public GameController(GameView gv,GameEngine ge,DeckArt da) {
    this.gv=gv;
    this.ge=ge;
    this.da=da;
    deckIndex=RecordStore.getRecord()[0];
    played=RecordStore.getRecord()[1];
    won=RecordStore.getRecord()[2];
    gv.setTimedOption(rs.getRecord()[3]==1);
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
              if(gv.isTimedGame())
                t=startTimer();
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
                if(gv.isTimedGame())
                  t=startTimer();
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
              if(gv.isTimedGame())
                t=startTimer();
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
        String stats="<html><h2><b>Games Played: "+played+"</b></h2><h2><b>Games Won: "+
                     won+"</b></h2></p></html>";
        JOptionPane.showMessageDialog(gv,stats,"Solitaire Stats",JOptionPane.PLAIN_MESSAGE);
      }
    });
    da.addDeckDialogListener(e->changeDeck());
    showGameState();
  }

  private void createNewGame() {
    if(JOptionPane.showConfirmDialog(gv,"Deal New?","Solitaire",JOptionPane.YES_NO_OPTION)==0) {
      rs.openRecordStore();
      int[]data=rs.getRecord();
      data[0]=deckIndex;
      data[1]=played;
      data[2]=won;
      rs.setRecord(data);
      ge.newGame();
      showGameState();
      inAction=false;
      if(gv.isTimedGame()) {
        t.cancel();
        t.purge();
      }
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
    rs.openRecordStore();
    int[]data=rs.getRecord();
    data[0]=deckIndex;
    data[1]=played;
    data[2]=won;
    data[3]=gv.isTimedGame()?1:0;
    rs.setRecord(data);
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
    if(ge.isWinner()) {
      gv.enableUndoButton(true);
      if(gv.isTimedGame()) {
        t.cancel();t.purge();
      }
      deckChanged=false;
      inAction=false;
      won++;
      storeData();
      if(JOptionPane.showConfirmDialog(gv,"Deal Again?","Solitaire",JOptionPane.YES_NO_OPTION)==0) {
        ge.newGame();
        gv.updateUI(ge.getStacks());
        gv.updateTime(0);
        gv.updateScore(0);
        gv.updateBonus(-1);
        gv.enableTimedOption(true);
      }
    }
  }

  private void storeData() {
    rs.openRecordStore();
    int[]data=rs.getRecord();
    data[0]=deckIndex;
    data[1]=played;
    data[2]=won;

    if(timed) {
      java.time.LocalDate date=java.time.LocalDate.now();
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
