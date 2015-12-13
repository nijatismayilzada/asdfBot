package MKAgent;

import java.util.ArrayList;

/**
 * Created by rj on 12/12/2015.
 */

public class Node {

  private ArrayList<Node> nextMoves;
  private MoveType moveType;
  private int payoff = 0;
  private boolean isLegal;

  public Node() {
    this.nextMoves = new ArrayList<>();
  }

  public void setLegal(boolean legal) {
    this.isLegal = legal;
  }

  public boolean isLegal() {
    return this.isLegal;
  }

  public void setMoveType(MoveType moveType) {
    this.moveType = moveType;
  }

  public MoveType getMoveType() {
    return this.moveType;
  }

  public void addNextMove(Node nextMove) {
    this.nextMoves.add(nextMove);
  }

  public Node getNextMove(int i) {
    return this.nextMoves.get(i);
  }

  public void removeNextMove(int i) {
    this.nextMoves.remove(i);
  }

  public void setPayoff(int payoff) {
    this.payoff = payoff;
  }

  public int getPayoff() {
    return this.payoff;
  }

  public String toString() {
    return "Legal move: " + this.isLegal() + " moves: " + this.nextMoves.toString();
  }
}
