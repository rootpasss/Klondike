/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 06/03/2023, Time: 13:41:31
 */
package io.bitlab.api.model;

import java.awt.Color;

public class Card {
  private String cardType;//diamonds, clubs, hearts, spades
  private int cardValue;//A(1) to K(13)
  private Color cardColor;//red (diamonds and hearts) or black (clubs and spades)
  private boolean flipState=false;//false=flip-down, true=flip-up

  public Card(String cardType,int cardValue) {
    this.cardType=cardType;
    this.cardValue=cardValue;
    defineColor(cardType);
  }

  private void defineColor(String value) {
    cardColor=value.matches("diamonds|hearts")?Color.RED:Color.BLACK;
  }

  public String getCardType() {
    return cardType;
  }

  public int getCardValue() {
    return cardValue;
  }

  public Color getCardColor() {
    return cardColor;
  }

  public boolean getFlipState() {
    return flipState;
  }

  public void setFlipState(boolean flipState) {
    this.flipState=flipState;
  }

  @Override
  public String toString() {
    if(flipState) {
      String strColor=cardColor==Color.RED?"red":"black";
      String strValue=cardValue==13?"K":cardValue==12?"Q":cardValue==11?"J":cardValue==1?"A":cardValue+"";
      return cardType+cardValue;
    }
    return "???";
  }
}
