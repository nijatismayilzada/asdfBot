package MKAgent;

import java.util.ArrayList;

public class Node {

  private int name;
  private Board board;
  private Side playerSide;
  private int payoff = 0;
  private Node parent;
  private ArrayList<Node> nextMoves;

  public Node() {
    this.nextMoves = new ArrayList<>();
  }

  public Node(int name) {
    this();
    this.setName(name);
  }

  public Node(int name, Board board) {
    this(name);
    this.setBoard(board);
  }

  public Node(int name, Board board, Side playerSide) {
    this(name, board);
    this.setPlayerSide(playerSide);
  }

  public Node(int name, Board board, Side playerSide, int payoff) {
    this(name, board, playerSide);
    this.setPayoff(payoff);
  }

  public Node(int name, Board board,  Side playerSide, int payoff, Node parent) {
    this(name, board, playerSide, payoff);
    this.setParent(parent);
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  public Board getBoard() {
    return this.board;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public Node getParent() {
    return this.parent;
  }

  public int getName() {
    return this.name;
  }

  public void setName(int name) {
    this.name = name;
  }

  public void setPlayerSide(Side playerSide) {
    this.playerSide = playerSide;
  }

  public Side getPlayerSide() {
    return this.playerSide;
  }

  public void addNextMove(Node nextMove) {
    this.nextMoves.add(nextMove);
  }

  public Node getNextMove(int i) {
    return this.nextMoves.get(i);
  }

  public ArrayList<Node> getNextMoves() {
    return this.nextMoves;
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


  @Override
  public String toString() {
    return "Node -> name: " + this.getName() + " / Payoff: " + this.getPayoff()
        + " / moveType: " + this.getPlayerSide() + " / nextMoves: "
        + this.getNextMoves().toString();
  }
}
