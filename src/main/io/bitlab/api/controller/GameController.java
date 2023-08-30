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

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import io.bitlab.api.model.GameEngine;
import io.bitlab.api.view.DeckArt;
import io.bitlab.api.view.GameView;

public class GameController {
  private GameEngine ge;
  private GameView gv;
  private DeckArt da;

  public GameController(GameView gv,GameEngine ge,DeckArt da) {
    this.gv=gv;
    this.ge=ge;
    this.da=da;
    gv.addClickListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        JComponent comp=((JComponent)evt.getSource());
        int from=Integer.parseInt(comp.getName());
        from=from+1>12?12:from;
        java.util.List<Rectangle2D.Double>cards=gv.getItems(from);
        if(!GameView.pick) {
          if(from==12) {
            ge.spinStock();
            gv.updateUI(ge.getStacks());
            gv.enableUndoButton(ge.isEmptyStack());
            GameView.pick=false;
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
          showGameState();
        }
      }
    });
    gv.addDealListener(e->createNewGame());
    gv.addUndoListener(e->undo());
    gv.addDeckListener(e->deck());
    da.addDeckDialogListener(e->changeDeck());
    showGameState();
  }

  private void createNewGame() {
    if(JOptionPane.showConfirmDialog(gv,"Deal New?","Solitaire",JOptionPane.YES_NO_OPTION)==0) {
      ge.newGame();
      showGameState();
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

  private void changeDeck() {
    GameView.imagemap.remove("???");
    try {
      GameView.imagemap.put("???",ImageIO.read(getClass().getResourceAsStream("/suits/"+da.getDeckName())));
      gv.updateUI(ge.getStacks());
      da.setVisible(false);
    } catch(Exception e) {e.printStackTrace();}
  }

  private void showGameState() {
    gv.updateUI(ge.getStacks());
    gv.enableUndoButton(ge.isEmptyStack());
    if(ge.isWinner()) {
      gv.enableUndoButton(true);
      if(JOptionPane.showConfirmDialog(gv,"Deal Again?","Solitaire",JOptionPane.YES_NO_OPTION)==0) {
        ge.newGame();
        gv.updateUI(ge.getStacks());
      }
    }
  }
}
