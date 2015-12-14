package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah    asdfKalah;
  private Side     ourSide;
  private MoveType lastPlayer;
  private Tree tree;
  private int depth = 5;

  public AsdfBot(int holes, int seeds) {
    this.asdfKalah = new Kalah(new Board(holes, seeds));
    this.tree = new Tree();
  }

  public Kalah getAsdf() {
    return asdfKalah;
  }

  public Side getOurSide() {
    return ourSide;
  }

  public void setOurSide(Side ourSide) {
    this.ourSide = ourSide;
  }

  public MoveType getLastPlayer() {
    return lastPlayer;
  }

  public void setLastPlayer(MoveType lastPlayer) {
    this.lastPlayer = lastPlayer;
  }

  //TODO: choose best from current possible moves
  private int rightMove(boolean canSwap, Board board) {
    Node root = new Node(0, board);
    tree.setRoot(root);
    assignNodes(root, 1);
    return 0;
  }

  //TODO: build tree more efficiently
  private void assignNodes(Node currentNode, int currentDepth) {

    if (currentDepth <= depth) {
      for (Node child : currentNode.getNextMoves()) {
        assignNodes( child, currentDepth + 1);
      }
      //TODO: alphabeta pruning
    } else {
      // TODO: calculate heuristic for each node
    }
  }


  private void swap() {
    this.ourSide = ourSide.opposite();
  }

  private int heuristics(Node node) {

    int ef, w1=1, w2=1, w3=1, w4=2, w5=2, e1, e2, e3, e4, e5;

    int ourSeeds = node.getBoard().getSeedsInStore(ourSide);
    int oppSeeds = node.getBoard().getSeedsInStore(ourSide.opposite());

    for (int i = 1; i <= 7; i++) {
      ourSeeds += node.getBoard().getSeeds(ourSide, i);
      oppSeeds += node.getBoard().getSeeds(ourSide.opposite(), i);
    }
    e1 = ourSeeds - oppSeeds;

    int ourFreeHouse = 0;
    int oppFreeHouse = 0;

    for (int i = 1; i <=7; i++) {
      if (node.getBoard().getSeeds(ourSide, i) == 0)
        ourFreeHouse++;
      if (node.getBoard().getSeeds(ourSide.opposite(), i) == 0)
        oppFreeHouse++;
    }
    e2 = ourFreeHouse - oppFreeHouse;

    if (node.getBoard().getSeedsInStore(ourSide) > node.getBoard().getSeedsInStore(ourSide.opposite()))
      e3 = 1;
    else
      e3 = 0;

    //TODO: update e4 and e5;
    if (node.getParent().getName() - node.getParent().getBoard().getSeeds(ourSide, node.getParent().getName()) == 0)
      e4 = 1;
    else
      e4 = 0;

    e5 = 0;
    ef = e1 + e2 + e3 +e4 + e5;
    return ef;
  }

  public void cuddleRamin() throws InvalidMessageException, IOException {
    String s;
    MsgType mt;
    // canSwap boolean being true if our bot is in north side
    boolean canSwap = false;

    while ((mt = Protocol.getMessageType(s = Main.recvMsg())) != MsgType.END) {
      System.err.println();
      System.err.println("Received: " + s);
      switch (mt) {
        case START:
          // if true, our bot starts first
          boolean first = Protocol.interpretStartMsg(s);
          System.err.println("ASDFBOT is starting: " + first);
          if (first) {
            // If we start first, set our side and "last player" variable
            this.setOurSide(Side.SOUTH);
            this.setLastPlayer(MoveType.ASDFBOT);

            // Get best move
            //int i = rightMove(canSwap);
            //s = Protocol.createMoveMsg(i);
            //System.err.println("Asdf start decision: " + i);
            Main.sendMsg(s);
          } else {
            // Set opponent side and last player
            this.setOurSide(Side.NORTH);
            this.setLastPlayer(MoveType.OPPONENT);

            // Asdfbot can swipe on its turn
            canSwap = true;
          }
          break;

        case STATE:
          Protocol.MoveTurn gameMessage = Protocol.interpretStateMsg(s, this
              .getAsdf().getBoard());

          System.err.println("The board:\n" + this.getAsdf().getBoard());
          System.err.println("ASDFBOT's turn: " + gameMessage.again + ". Last move: " + gameMessage.move);

          if (gameMessage.move == -1){
            swap();
          } else {
          }

          // if this turn is our turn
          if (gameMessage.again) {
            // Get best move
            int i = rightMove(canSwap, this.getAsdf().getBoard());
            System.err.println("Asdf decision: " + i);

            // if best right move is -1, it means asdfbot should swap
            if (i == -1) {
              s = Protocol.createSwapMsg();
              swap();
              // Otherwise, do move
            } else {
              s = Protocol.createMoveMsg(i);
            }
            canSwap = false;
            // The whole operation is done by asdfbot, so set last player
            // asdfbot
            this.setLastPlayer(MoveType.ASDFBOT);
            Main.sendMsg(s);
          } else {
            // If this is opponent's turn, set last player
            this.setLastPlayer(MoveType.OPPONENT);
          }

          if (gameMessage.end) {
          } else {
          }

          break;
          
        case END:
          System.err.println("Bye!");
          return;
      }
    }
  }
}