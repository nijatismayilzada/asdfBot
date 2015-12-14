package MKAgent;

import java.util.ArrayList;

public class Node {

  private int name;
  private Board board;
  private boolean isLegal;
  private MoveType moveType;
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

  public Node(int name, Board board, boolean isLegal) {
    this(name, board);
    this.setLegal(isLegal);
  }

  public Node(int name, Board board, boolean isLegal, MoveType moveType) {
    this(name, board, isLegal);
    this.setMoveType(moveType);
  }

  public Node(int name, Board board, boolean isLegal, MoveType moveType, int payoff) {
    this(name, board, isLegal, moveType);
    this.setPayoff(payoff);
  }

  public Node(int name, Board board, boolean isLegal, MoveType moveType, int payoff, Node parent) {
    this(name, board, isLegal, moveType, payoff);
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
    return "Node -> name: " + this.getName() + " / isMoveLegal: " + this.isLegal() + " / Payoff: " + this.getPayoff()
        + " / moveType: " + this.getMoveType() + " / parentName: " + this.getParent().getName() + " / nextMoves: "
        + this.getNextMoves().toString();
  }
}
