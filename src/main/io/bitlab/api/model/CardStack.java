/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 20/03/2023, Time: 18:16:19
 */
package io.bitlab.api.model;

import java.util.Stack;

class CardStack extends Stack<Card> {
  public static final int FOUNDATION_CELL=0;
  public static final int TABLEAU_CELL=1;
  private int stackID;

  public CardStack(int stackID) {
    this.stackID=stackID;
  }

  public boolean checkCard(Card card) {
    if(stackID==TABLEAU_CELL) {
      return (size()==0&&card.getCardValue()==13)||
         size()>0&&(((Card)peek()).getCardValue()-card.getCardValue()==1&&
         !((Card)peek()).getCardColor().equals(card.getCardColor()));
    } else if(stackID==FOUNDATION_CELL) {
      return (size()==0&&card.getCardValue()==1)||
        (size()>0&&card.getCardValue()-((Card)peek()).getCardValue()==1&&
         card.getCardType().equals(((Card)peek()).getCardType()));
    } else {
      return false;
    }
  }
}
