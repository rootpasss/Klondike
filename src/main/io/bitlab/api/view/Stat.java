/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 07/01/2024, Time: 12:21:27
 */
package io.bitlab.api.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("unchecked")
public class Stat {
  private static JList l=new JList<String>(new String[]{"Standard timed","Standard non-timed"});
  private static JPanel p1=new JPanel(new GridBagLayout());
  private static JPanel p2=new JPanel(new GridLayout(6,1));
  private static String[]t={"Games played: ","Games won: ","Win percentage: ","Longest winning streak: ",
                            "Longest losing streak: ","Current streak: "};
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

  public static JPanel getPane2(Object[]d) {
    JPanel contentPane=new JPanel(new GridBagLayout());
    GridBagConstraints c=new GridBagConstraints();
    GridBagConstraints c1=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTH;
    c1.anchor=GridBagConstraints.NORTH;
    c1.weighty=1;c1.weightx=1;

    l.setPreferredSize(new Dimension(190,60));
    l.setSelectedIndex(0);
    l.setBorder(new LineBorder(Color.LIGHT_GRAY,1,false));
    contentPane.add(l,c);c.gridy++;
    c.insets=new Insets(0,7,0,0);
    c1.insets=new Insets(0,-30,0,0);

    p1.setBorder(new CompoundBorder(new TitledBorder("High Scores"),new EmptyBorder(0,16,0,35)));
    p1.setPreferredSize(new Dimension(230,100));
    p1.removeAll();
    java.util.ArrayList<String>v=(java.util.ArrayList<String>)d[0];
    for(int i=0;i<v.size();i++) {
      String[]s=v.get(i).split(" ");
      c1.gridy=i;
      for(int j=0;j<s.length;j++) {
        c1.gridx=j;
        p1.add(new JLabel(s[j]),c1);
      }
    }
    contentPane.add(p1,c);

    p2.setPreferredSize(new Dimension(190,120));
    p2.removeAll();
    for(int i=1;i<7;i++) {
      p2.add(new JLabel(t[i-1]+d[i]));
    }
    contentPane.add(p2,c);

    return contentPane;
  }

  public static void updatePane(Object[]d) {
    for(int i=0;i<p2.getComponentCount();i++) {
      ((JLabel)p2.getComponent(i)).setText(t[i]+d[i]);
    }
  }

  public static void hide() {
    for(int i=0;i<p1.getComponentCount();i++)
      ((JLabel)p1.getComponent(i)).setVisible(l.getSelectedIndex()==0);
  }

  public static void addListListener(ListSelectionListener ll) {
    l.addListSelectionListener(ll);
  }
}
