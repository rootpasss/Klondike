/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 06/03/2023, Time: 13:39:54
 */
package io.bitlab.api.model;

import java.util.Collections;
import java.util.Stack;

import io.bitlab.api.view.GameView;

public class GameEngine {
  private final String[]SUIT={"diamonds","clubs","hearts","spades"};
  private Stack<int[]>undoStack=new Stack<>();
  private int foundationSize;
  private int flipCount;
  private int score;

  private CardStack stack0=new CardStack(-1);//this is the Stock pile
  private CardStack stack1=new CardStack(-1);//this is the Waste pile
  private CardStack stack2=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack3=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack4=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack5=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack6=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack7=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack8=new CardStack(CardStack.TABLEAU_CELL);
  private CardStack stack9=new CardStack(CardStack.FOUNDATION_CELL);
  private CardStack stack10=new CardStack(CardStack.FOUNDATION_CELL);
  private CardStack stack11=new CardStack(CardStack.FOUNDATION_CELL);
  private CardStack stack12=new CardStack(CardStack.FOUNDATION_CELL);
  private CardStack[] stackArray={stack1,stack2,stack3,stack4,stack5,stack6,stack7,
                                  stack8,stack9,stack10,stack11,stack12,stack0};

  public GameEngine() {
    createDeck();
    //toFoundation();
    //fastGame();
  }

  //moves all remaining cards to the foundation automatically when the game is won
  //classic Solitaire does not use an automatic card move to foundations
  private void toFoundation() {
    do {
      foundationSize=stack9.size()+stack10.size()+stack11.size()+stack12.size();
      for(int i=0;i<stackArray.length-5;i++) {
        for(int j=0;j<stackArray[i].size();j++) {
          for(int k=8;k<12;k++) {
            if(stackArray[i].size()>0&&stackArray[k].size()>0&&stackArray[i].peek().getCardType().equals(stackArray[k].peek().getCardType())) {
              if(stackArray[i].peek().getCardValue()==13)
                stackArray[k].push(stackArray[i].pop());
              else
                stackArray[k].insertElementAt(stackArray[i].pop(),0);
              break;
            } else if(stackArray[i].size()>0&&stackArray[k].isEmpty()&&stackArray[i].peek().getCardValue()==1) {
                stackArray[k].push(stackArray[i].pop());
              break;
            }
          }
        }
      }
    } while(foundationSize<52);
  }

  //creates all 52 cards with its respective values and suits
  private void createDeck() {
    for(int i=0;i<4;i++) {
      for(int j=13;j>0;j--) {
        stack0.push(new Card(SUIT[i],j));
      }
    }
    createStacks();
  }

  //distribute cards
  private void createStacks() {
    Collections.shuffle(stack0);
    flipCount=21;
    for(int i=1;i<stackArray.length-5;i++) {
      for(int j=0;j<i;j++) {
        stackArray[i].push(stack0.pop());
      }
      ((Card)stackArray[i].peek()).setFlipState(true);
    }
  }

  //performs the placement of the elements between stacks
  public void moveCard(int from,int count,int to) {
    //int from=3;move from stack 3
    //int count=1;# of cards to move from stack 3
    //int to=2;move to stack 2

    //move one or more cards...
    if(stackArray[from].size()>0&&count<=stackArray[from].size()) {
      Object[]obj=stackArray[from].toArray();
      Card card=(Card)obj[obj.length-count];
      int wasFlip=0;
      if(stackArray[to].checkCard(card)) {
        for(int i=0;i<count;i++) {
          stackArray[to].push((Card)obj[obj.length-count+i]);
          stackArray[from].pop();
        }

        if(stackArray[from].size()>0&&!((Card)stackArray[from].peek()).getFlipState()) {
          ((Card)stackArray[from].peek()).setFlipState(true);
          if(from>0) {
            flipCount--;
            score+=5;
          }
          wasFlip=1;
        }
        if(to>=8)
          score+=10;
        else if(from==0&&to>0&&to<8)
          score+=5;
        else if(from>=8&&to<8)
          score-=10;
        undoStack.push(new int[]{to,count,from,wasFlip});

        /*DISABLED BECAUSE THE CLASSIC SOLITAIRE DOES NOT USE AN AUTOMATIC CARD MOVE
          TO FOUNDATIONS WHEN THE GAME IS WON.*/
        //determines if the game is done
        /*if(flipCount==0&&stackArray[0].size()<3&&stack0.empty()) {
          toFoundation();
        }*/
      } else {audioAlert();System.out.print(" (NOT VALID)");}
    } else {audioAlert();}
  }

  //Steps back one movement
  public void moveCardUndo() {
    if(!undoStack.isEmpty()) {
      int from=undoStack.peek()[0];
      int count=undoStack.peek()[1];
      int to=undoStack.peek()[2];
      int wasFlip=undoStack.peek()[3];
      Object[]obj=stackArray[from].toArray();

      if(to>0&&to<12&&wasFlip==1) {//undo when flip-over tableau card
        ((Card)stackArray[to].peek()).setFlipState(false);
        flipCount++;
        score-=7;
      }

      for(int i=0;i<count;i++) {
        if(to>0&&to<12) {
          stackArray[to].push((Card)obj[obj.length-count+i]);
        } else {
          Card card=stackArray[from].peek();
          card.setFlipState(to==0);
          stackArray[to].push(card);
        }
        stackArray[from].pop();
      }

      if(from>=8&&to>0)//undo from foundation to tableau
        score-=12;
      else if(to==0&&from>=8&&from<12)//undo from foundation to waste
        score-=12;
      else if(to==0&&from<8)//undo from tableau to waste
        score-=7;
      else if(from>0&&from<7&&to>=8)//undo from tableau to foundation
        score+=8;

      foundationSize=stack9.size()+stack10.size()+stack11.size()+stack12.size();
      undoStack.pop();
    }
  }

  //Move a valid card to foundations when a double click event is triggered
  public void moveCardDoubleClick(int from) {
    boolean passed=false;
    int f=0;
    if(stackArray[from].size()>0) {
      Card card=((Card)stackArray[from].peek());
      for(int i=8;i<12;i++) {
        if(stackArray[i].checkCard(card)) {
          stackArray[i].push(card);
          stackArray[from].pop();
          if(stackArray[from].size()>0&&!((Card)stackArray[from].peek()).getFlipState()) {
            ((Card)stackArray[from].peek()).setFlipState(true);
            score+=5;
            f=1;
          }
          undoStack.push(new int[]{i,1,from,f});
          passed=true;
          score+=10;
          break;
        }
      }
      System.out.print("\nFast move from pile "+from+" to foundations");

      if(!passed) {
        audioAlert();
        System.out.print(" (NOT VALID)");
      }
    }
  }

  public Stack[] getStacks() {
    return stackArray;
  }

  public int getScore() {
    return score;
  }

  public int subtractScore() {
    score-=2;
    return score;
  }

  //returns a representative text to show the actual game progress
  public String getDeckState() {
    String output="";
    for(int i=1;i<stackArray.length-5;i++) {
      output+="Stack "+(i)+" "+stackArray[i]+"\n";
    }

    output+="\n";

    for(int i=8;i<stackArray.length-1;i++) {
      output+=stackArray[i].size()>0?"Stack "+(i)+" ["+stackArray[i].peek()+"]\n":
                    "Stack "+(i)+" "+stackArray[i]+"\n";
    }

    //show only the latest 3 cards in the stack (or show 2, 1 or none if stack size < 3)
    int top=stackArray[0].size()>3?3:stackArray[0].size();
    String str="";
    for(int i=0;i<top;i++) {
      str+=stackArray[0].elementAt(stackArray[0].size()-top+i)+"    ";
    }
    output+="\nDEAL "+stack0.size()+" ["+str.trim()+"] "+stackArray[0].size();
    return output;
  }

  //feedbacks an audible warning when a card or group of cards cannot be moved
  private void audioAlert() {
    java.awt.Toolkit.getDefaultToolkit().beep();
  }

  //the game is won if foundationSize (total size of the foundation piles) is equal to 52
  public boolean isWinner() {
    return stack9.size()+stack10.size()+stack11.size()+stack12.size()==52;
  }

  //returns true if the undo stack is empty or false if its non empty; used for enable/disable undo button
  public boolean isEmptyStack() {
    return undoStack.empty();
  }

  //returns true if stock and waste stacks are empty, false if not; used for enable/disable waste button
  public boolean isEmptyWasteStock() {
    return stack0.empty()&&stack1.empty();
  }

  //creates a new game
  public void newGame() {
    System.out.printf("%n%n%5c%6c%6c%6c%nA new game has been started%n%5c%6c%6c%6c%n",
    '\u2667','\u2662','\u2661','\u2664','\u2664','\u2661','\u2662','\u2667');
    for(int i=0;i<stackArray.length;i++) {
      for(int j=stackArray[i].size()-1;j>=0;j--) {
        Card card=(Card)((CardStack)stackArray[i]).pop();
        card.setFlipState(false);//flip-down
        stack0.push(card);
      }
    }
    score=0;
    foundationSize=0;
    undoStack.removeAllElements();
    createStacks();
  }

  //populates the stack #1 by adding 3, 2 or 1 card(s) when possible
  public void spinStock() {
    if(stack0.size()>0) {
      int top=stack0.size()>3?3:stack0.size();
      for(int i=0;i<top;i++) {
        stackArray[0].push(stack0.pop());
        ((Card)stackArray[0].peek()).setFlipState(true);
      }
      undoStack.push(new int[]{0,top,12,1});
    } else if(stack1.size()>0) {
      //if the card list is empty then retrieve all cards from stack 1 to the stock pile
      for(int i=stackArray[0].size()-1;i>=0;i--) {
        Card card=(Card)((CardStack)stackArray[0]).pop();
        card.setFlipState(false);
        stack0.push(card);
      }
      undoStack.push(new int[]{12,stack0.size(),0,1});
    }
  }

  //quickly stacks elements (debug only)
  private void fastGame() {
    //infinite loop case 1
    /*stack1.push(new Card("diamonds",8));
    stack1.push(new Card("clubs",12));
    stack1.push(new Card("diamonds",6));

    stack2.push(new Card("spades",13));

    stack3.push(new Card("diamonds",13));
    stack3.push(new Card("spades",12));
    stack3.push(new Card("hearts",11));
    stack3.push(new Card("spades",10));
    stack3.push(new Card("hearts",9));
    stack3.push(new Card("spades",8));
    stack3.push(new Card("hearts",7));

    stack5.push(new Card("hearts",13));

    stack6.push(new Card("diamonds",11));
    stack6.push(new Card("clubs",10));
    stack6.push(new Card("diamonds",9));
    stack6.push(new Card("clubs",8));
    stack6.push(new Card("diamonds",7));

    stack7.push(new Card("hearts",12));
    stack7.push(new Card("spades",11));
    stack7.push(new Card("hearts",10));
    stack7.push(new Card("clubs",9));
    stack7.push(new Card("hearts",8));
    stack7.push(new Card("spades",7));
    stack7.push(new Card("hearts",6));

    stack8.push(new Card("clubs",13));
    stack8.push(new Card("diamonds",12));
    stack8.push(new Card("clubs",11));
    stack8.push(new Card("diamonds",10));
    stack8.push(new Card("spades",9));

    stack9.push(new Card("clubs",1));
    stack9.push(new Card("clubs",2));
    stack9.push(new Card("clubs",3));
    stack9.push(new Card("clubs",4));
    stack9.push(new Card("clubs",5));
    stack9.push(new Card("clubs",6));
    stack9.push(new Card("clubs",7));

    stack10.push(new Card("spades",1));
    stack10.push(new Card("spades",2));
    stack10.push(new Card("spades",3));
    stack10.push(new Card("spades",4));
    stack10.push(new Card("spades",5));
    stack10.push(new Card("spades",6));

    stack11.push(new Card("diamonds",1));
    stack11.push(new Card("diamonds",2));
    stack11.push(new Card("diamonds",3));
    stack11.push(new Card("diamonds",4));
    stack11.push(new Card("diamonds",5));

    stack12.push(new Card("hearts",1));
    stack12.push(new Card("hearts",2));
    stack12.push(new Card("hearts",3));
    stack12.push(new Card("hearts",4));
    stack12.push(new Card("hearts",5));*/

    //infinite loop case 2
    /*stack1.push(new Card("spades",2));
    stack1.push(new Card("clubs",13));
    stack1.push(new Card("clubs",10));

    stack2.push(new Card("hearts",13));
    stack2.push(new Card("spades",12));
    stack2.push(new Card("hearts",11));
    stack2.push(new Card("spades",10));
    stack2.push(new Card("hearts",9));
    stack2.push(new Card("spades",8));
    stack2.push(new Card("hearts",7));
    stack2.push(new Card("spades",6));
    stack2.push(new Card("hearts",5));

    stack3.push(new Card("spades",13));
    stack3.push(new Card("diamonds",12));
    stack3.push(new Card("spades",11));

    stack4.push(new Card("spades",4));

    stack5.push(new Card("diamonds",13));
    stack5.push(new Card("clubs",12));
    stack5.push(new Card("diamonds",11));

    stack6.push(new Card("diamonds",10));
    stack6.push(new Card("spades",9));
    stack6.push(new Card("hearts",8));
    stack6.push(new Card("clubs",7));
    stack6.push(new Card("hearts",6));
    stack6.push(new Card("spades",5));
    stack6.push(new Card("diamonds",4));
    stack6.push(new Card("spades",3));

    stack7.push(new Card("hearts",12));
    stack7.push(new Card("clubs",11));
    stack7.push(new Card("hearts",10));
    stack7.push(new Card("clubs",9));
    stack7.push(new Card("diamonds",8));
    stack7.push(new Card("spades",7));
    stack7.push(new Card("diamonds",6));
    stack7.push(new Card("clubs",5));
    stack7.push(new Card("hearts",4));

    stack8.push(new Card("diamonds",9));
    stack8.push(new Card("clubs",8));
    stack8.push(new Card("diamonds",7));
    stack8.push(new Card("clubs",6));
    stack8.push(new Card("diamonds",5));

    stack9.push(new Card("clubs",1));
    stack9.push(new Card("clubs",2));
    stack9.push(new Card("clubs",3));
    stack9.push(new Card("clubs",4));

    stack10.push(new Card("hearts",1));
    stack10.push(new Card("hearts",2));
    stack10.push(new Card("hearts",3));

    stack11.push(new Card("spades",1));

    stack12.push(new Card("diamonds",1));
    stack12.push(new Card("diamonds",2));
    stack12.push(new Card("diamonds",3));*/

    //infinite loop case 3
    /*stack1.push(new Card("diamonds",4));

    stack2.push(new Card("clubs",13));
    stack2.push(new Card("hearts",12));
    stack2.push(new Card("spades",11));
    stack2.push(new Card("diamonds",10));
    stack2.push(new Card("clubs",9));
    stack2.push(new Card("hearts",8));
    stack2.push(new Card("spades",7));
    stack2.push(new Card("hearts",6));
    stack2.push(new Card("spades",5));
    stack2.push(new Card("hearts",4));
    stack2.push(new Card("spades",3));

    stack3.push(new Card("hearts",7));
    stack3.push(new Card("spades",6));
    stack3.push(new Card("diamonds",5));

    stack4.push(new Card("hearts",13));
    stack4.push(new Card("spades",12));
    stack4.push(new Card("diamonds",11));
    stack4.push(new Card("spades",10));
    stack4.push(new Card("hearts",9));
    stack4.push(new Card("clubs",8));

    stack5.push(new Card("clubs",7));
    stack5.push(new Card("diamonds",6));

    stack6.push(new Card("diamonds",12));
    stack6.push(new Card("clubs",11));
    stack6.push(new Card("hearts",10));
    stack6.push(new Card("spades",9));
    stack6.push(new Card("diamonds",8));

    stack7.push(new Card("diamonds",13));
    stack7.push(new Card("clubs",12));
    stack7.push(new Card("hearts",11));
    stack7.push(new Card("clubs",10));
    stack7.push(new Card("diamonds",9));
    stack7.push(new Card("spades",8));
    stack7.push(new Card("diamonds",7));
    stack7.push(new Card("clubs",6));
    stack7.push(new Card("hearts",5));
    stack7.push(new Card("spades",4));
    stack7.push(new Card("diamonds",3));
    stack7.push(new Card("spades",2));

    stack8.push(new Card("spades",13));

    stack9.push(new Card("clubs",1));
    stack9.push(new Card("clubs",2));
    stack9.push(new Card("clubs",3));
    stack9.push(new Card("clubs",4));
    stack9.push(new Card("clubs",5));

    stack10.push(new Card("diamonds",1));
    stack10.push(new Card("diamonds",2));

    stack11.push(new Card("spades",1));

    stack12.push(new Card("hearts",1));
    stack12.push(new Card("hearts",2));
    stack12.push(new Card("hearts",3));*/

    //infinite loop case 4
    /*stack1.push(new Card("spades",11));
    stack1.push(new Card("spades",4));
    stack1.push(new Card("clubs",5));

    stack2.push(new Card("spades",13));
    stack2.push(new Card("hearts",12));
    stack2.push(new Card("clubs",11));
    stack2.push(new Card("diamonds",10));
    stack2.push(new Card("spades",9));
    stack2.push(new Card("hearts",8));
    stack2.push(new Card("spades",7));
    stack2.push(new Card("hearts",6));
    stack2.push(new Card("spades",5));
    stack2.push(new Card("hearts",4));
    stack2.push(new Card("spades",3));

    stack3.push(new Card("clubs",13));
    stack3.push(new Card("diamonds",12));

    stack4.push(new Card("hearts",10));
    stack4.push(new Card("clubs",9));
    stack4.push(new Card("diamonds",8));
    stack4.push(new Card("clubs",7));
    stack4.push(new Card("diamonds",6));

    stack5.push(new Card("hearts",13));
    stack5.push(new Card("spades",12));
    stack5.push(new Card("hearts",11));
    stack5.push(new Card("clubs",10));
    stack5.push(new Card("diamonds",9));
    stack5.push(new Card("spades",8));
    stack5.push(new Card("diamonds",7));
    stack5.push(new Card("spades",6));
    stack5.push(new Card("diamonds",5));

    stack6.push(new Card("diamonds",13));
    stack6.push(new Card("clubs",12));
    stack6.push(new Card("diamonds",11));
    stack6.push(new Card("spades",10));
    stack6.push(new Card("hearts",9));
    stack6.push(new Card("clubs",8));
    stack6.push(new Card("hearts",7));
    stack6.push(new Card("clubs",6));
    stack6.push(new Card("hearts",5));
    stack6.push(new Card("clubs",4));
    stack6.push(new Card("hearts",3));

    stack7.push(new Card("clubs",3));

    stack8.push(new Card("spades",2));

    stack9.push(new Card("spades",1));

    stack10.push(new Card("diamonds",1));
    stack10.push(new Card("diamonds",2));
    stack10.push(new Card("diamonds",3));
    stack10.push(new Card("diamonds",4));

    stack11.push(new Card("clubs",1));
    stack11.push(new Card("clubs",2));

    stack12.push(new Card("hearts",1));
    stack12.push(new Card("hearts",2));*/

    //infinite loop case 5
    stack1.push(new Card("spades",7));
    stack1.push(new Card("hearts",4));

    stack2.push(new Card("diamonds",13));
    stack2.push(new Card("spades",12));
    stack2.push(new Card("hearts",11));
    stack2.push(new Card("spades",10));
    stack2.push(new Card("diamonds",9));
    stack2.push(new Card("spades",8));
    stack2.push(new Card("diamonds",7));
    stack2.push(new Card("spades",6));
    stack2.push(new Card("hearts",5));
    stack2.push(new Card("clubs",4));
    stack2.push(new Card("diamonds",3));
    stack2.push(new Card("clubs",2));
    stack2.push(new Card("diamonds",1));

    stack3.push(new Card("spades",13));

    stack4.push(new Card("hearts",13));
    stack4.push(new Card("clubs",12));
    stack4.push(new Card("diamonds",11));
    stack4.push(new Card("clubs",10));
    stack4.push(new Card("hearts",9));
    stack4.push(new Card("clubs",8));
    stack4.push(new Card("hearts",7));
    stack4.push(new Card("clubs",6));
    stack4.push(new Card("diamonds",5));

    stack5.push(new Card("hearts",12));
    stack5.push(new Card("spades",11));
    stack5.push(new Card("diamonds",10));
    stack5.push(new Card("spades",9));
    stack5.push(new Card("diamonds",8));
    stack5.push(new Card("clubs",7));
    stack5.push(new Card("hearts",6));

    stack6.push(new Card("clubs",13));
    stack6.push(new Card("diamonds",12));
    stack6.push(new Card("clubs",11));
    stack6.push(new Card("hearts",10));
    stack6.push(new Card("clubs",9));
    stack6.push(new Card("hearts",8));

    stack7.push(new Card("diamonds",6));
    stack7.push(new Card("clubs",5));
    stack7.push(new Card("diamonds",4));
    stack7.push(new Card("clubs",3));

    stack8.push(new Card("diamonds",2));

    stack9.push(new Card("clubs",1));

    stack10.push(new Card("hearts",1));
    stack10.push(new Card("hearts",2));
    stack10.push(new Card("hearts",3));

    stack11.push(new Card("spades",1));
    stack11.push(new Card("spades",2));
    stack11.push(new Card("spades",3));
    stack11.push(new Card("spades",4));
    stack11.push(new Card("spades",5));

    //bug case 1
    /*stack1.push(new Card("clubs",13));
    stack1.push(new Card("clubs",11));

    stack2.push(new Card("diamonds",13));
    stack2.push(new Card("spades",12));
    stack2.push(new Card("diamonds",11));
    stack2.push(new Card("clubs",10));
    stack2.push(new Card("diamonds",9));
    stack2.push(new Card("clubs",8));
    stack2.push(new Card("hearts",7));
    stack2.push(new Card("clubs",6));
    stack2.push(new Card("diamonds",5));
    stack2.push(new Card("clubs",4));
    stack2.push(new Card("diamonds",3));

    stack3.push(new Card("hearts",13));
    stack3.push(new Card("clubs",12));
    stack3.push(new Card("hearts",11));
    stack3.push(new Card("spades",10));
    stack3.push(new Card("hearts",9));
    stack3.push(new Card("spades",8));
    stack3.push(new Card("diamonds",7));

    stack4.push(new Card("spades",13));
    stack4.push(new Card("hearts",12));
    stack4.push(new Card("spades",11));
    stack4.push(new Card("diamonds",10));
    stack4.push(new Card("clubs",9));
    stack4.push(new Card("hearts",8));
    stack4.push(new Card("clubs",7));
    stack4.push(new Card("diamonds",6));
    stack4.push(new Card("clubs",5));

    stack5.push(new Card("hearts",10));
    stack5.push(new Card("spades",9));
    stack5.push(new Card("diamonds",8));
    stack5.push(new Card("spades",7));

    stack7.push(new Card("diamonds",4));

    stack8.push(new Card("diamonds",12));

    stack9.push(new Card("hearts",1));
    stack9.push(new Card("hearts",2));
    stack9.push(new Card("hearts",3));
    stack9.push(new Card("hearts",4));
    stack9.push(new Card("hearts",5));
    stack9.push(new Card("hearts",6));

    stack10.push(new Card("clubs",1));
    stack10.push(new Card("clubs",2));
    stack10.push(new Card("clubs",3));

    stack11.push(new Card("diamonds",1));
    stack11.push(new Card("diamonds",2));

    stack12.push(new Card("spades",1));
    stack12.push(new Card("spades",2));
    stack12.push(new Card("spades",3));
    stack12.push(new Card("spades",4));
    stack12.push(new Card("spades",5));
    stack12.push(new Card("spades",6));*/

    //reveals each element of each stack
    for(int i=0;i<stackArray.length;i++) {
      for(int j=0;j<stackArray[i].size();j++) {
        (stackArray[i].elementAt(j)).setFlipState(true);
      }
    }
  }
}
