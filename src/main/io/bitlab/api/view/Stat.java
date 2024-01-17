/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 07/01/2024, Time: 12:21:27
 */
package io.bitlab.api.view;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Stat {
  public static JPanel getPane1(Object[]d) {
    JPanel contentPane=new JPanel(new BorderLayout(0,15));
    JPanel centerPane=new JPanel(new GridLayout(2,1));

    JLabel lbl=new JLabel("Congratulations, you won the game!");
    lbl.setHorizontalAlignment(JLabel.CENTER);

    JPanel scorePane=new JPanel(new GridBagLayout());
    scorePane.setPreferredSize(new java.awt.Dimension(350,90));
    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.LINE_START;
    scorePane.setBorder(new TitledBorder("Game Score"));
    c.insets=new java.awt.Insets(5,15,0,0);
    scorePane.add(new JLabel("Score: "+d[0]),c);
    c.weightx=1;
    c.gridy++;
    scorePane.add(new JLabel("Time: "+d[1]+" seconds"),c);
    c.gridy++;
    scorePane.add(new JLabel("Time Bonus: "+d[2]),c);
    c.gridy++;
    scorePane.add(new JLabel("Total Score: "+d[3]),c);

    JPanel statPane=new JPanel(new GridBagLayout());
    c.gridy=0;
    statPane.add(new JLabel("High score: "+d[4]),c);
    statPane.add(new JLabel("Date: "+d[5]),c);
    c.gridy++;
    statPane.add(new JLabel("Games played: "+d[6]),c);
    c.gridy++;
    statPane.add(new JLabel("Games won: "+d[7]),c);
    statPane.add(new JLabel("Win percentage: "+d[8]+"%"),c);

    centerPane.add(scorePane);
    centerPane.add(statPane);
    contentPane.add(lbl,BorderLayout.NORTH);
    contentPane.add(centerPane,BorderLayout.CENTER);
    return contentPane;
  }
}
