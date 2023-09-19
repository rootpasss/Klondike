/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 06/03/2023, Time: 13:34:29
 */
package io.bitlab.api.launch;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import io.bitlab.api.controller.GameController;
import io.bitlab.api.model.GameEngine;
import io.bitlab.api.view.DeckArt;
import io.bitlab.api.view.GameView;

public class Solitaire {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        printHeader();
        try {
          if(System.getProperty("os.name").equals("Linux"))
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
          UIManager.put("OptionPane.questionIcon",new javax.swing.ImageIcon(getClass().getResource("/suits/zwarning.png")));
          new GameController(new GameView(),new GameEngine(),new DeckArt());
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private static void printHeader() {
    System.out.println("Solitaire-Klondike: a Java classic version");
    System.out.println("(c)2023 JNY");
    System.out.println("github.com/rootpasss\n");
  }
}
