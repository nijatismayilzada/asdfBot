package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah asdfKalah;
  private Side ourSide;
  private Tree tree;
  private int DEPTH = 5;

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

  private int rightMove(boolean canSwap, Board board) {
    Node root = new Node(0, board);
    root.setPlayerSide(this.getOurSide());
    tree.setRoot(root);
    assignNodes(root, 1);

    // TODO check if swap is needed?

    int bestMove = 0;
    int maxValue = Integer.MIN_VALUE;

    for (Node possibleMove : this.tree.getRoot().getNextMoves()) {
      if (possibleMove.getPayoff() > maxValue) {
        maxValue = possibleMove.getPayoff();
        bestMove = possibleMove.getName();
      }
    }

    return bestMove;
  }

  // TODO: build tree more efficiently
  private void assignNodes(Node currentNode, int currentDepth) {
    if (currentDepth <= DEPTH) {
      for (int i = 1; i <= 7; i++) {
        Kalah currentKalah = new Kalah(new Board(currentNode.getBoard()));
        Move nextMove = new Move(currentNode.getPlayerSide(), i);
        if (currentKalah.isLegalMove(nextMove)) {
          Side nextSide = currentKalah.makeMove(nextMove);
          Node nextChild = new Node(i, currentKalah.getBoard(), nextSide, 0, currentNode);
          currentNode.addNextMove(nextChild);
          assignNodes(nextChild, currentDepth + 1);
        }
      }
      // TODO: alphabeta pruning
    } else {

      int payoff = heuristic(currentNode);
      currentNode.setPayoff(payoff);
    }
  }

  private void swap() {
    this.ourSide = ourSide.opposite();
  }

  /* Heuristic method calculates Evaluation Function for given node and return score.
      Evaluation Function: w1 * e1 + w2 * e2 + w3 * e3 + w4 * e4 + w5 * e5 where w is weight and e is different
      evaluation function components (measures)
      e1 - difference between the number of nodes in each side
      e2 - difference between the number of free houses in each side
      e3 - the number of seeds added to store by passing store in a turn
      e4 - last seed of house added to store in turn (1 or -1 or 0) - has more weights as user will have one
           more turn to play
      e5 - the number of seeds added to store by getting nodes from opponent's house - has more weights as user might
           get more than one seed to store in one turn
      SPECIAL CASE: if parent's last node of house added to store, and in current turn, some seeds added to
                    store by getting nodes from opponent's house - change weight from 2 to 50.
    */
  private int heuristic(Node node) {

    int ef, w1 = 1, w2 = 1, w3 = 1, w4 = 2, w5 = 2, e1 = 0, e2 = 0, e3 = 0, e4 = 0, e5 = 0;

    int ourSeedsinStore = node.getBoard().getSeedsInStore(ourSide);
    int oppSeedsinStore = node.getBoard().getSeedsInStore(ourSide.opposite());
    int parentOurSeedsinStore = node.getParent().getBoard().getSeedsInStore(ourSide);
    int parentOppSeedsinStore = node.getParent().getBoard().getSeedsInStore(ourSide);
    int parentMove = node.getParent().getName();
    int ourSeedsinHouse = node.getParent().getBoard().getSeeds(ourSide, parentMove);
    int oppSeedsinHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), parentMove);

    int ourFreeHouse = 0;
    int oppFreeHouse = 0;
    int ourSeeds = ourSeedsinStore;
    int oppSeeds = oppSeedsinStore;

    for (int i = 1; i <= 7; i++) {
      ourSeeds += node.getBoard().getSeeds(ourSide, i);
      oppSeeds += node.getBoard().getSeeds(ourSide.opposite(), i);

      if (node.getBoard().getSeeds(ourSide, i) == 0)
        ourFreeHouse++;
      if (node.getBoard().getSeeds(ourSide.opposite(), i) == 0)
        oppFreeHouse++;
    }
    e1 = ourSeeds - oppSeeds;
    e2 = ourFreeHouse - oppFreeHouse;

    if (node.getParent().getPlayerSide() == this.getOurSide()) {

      if (parentMove + ourSeedsinHouse == 8) {
        e4 = 1;
        e3 = 1;
      } else if (parentMove + parentOurSeedsinStore > 8) {
        e4 = 0;
        e3 = (parentMove + parentOurSeedsinStore) / 8;
      }

      for (int i = 1; i <= 7; i++) {
        int parentOurSeedsinCurrentHouse = node.getParent().getBoard().getSeeds(ourSide, i);
        int parentOppSeedsinFrontHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), 8 - i);
        int oppSeedsinFrontHouse = node.getBoard().getSeeds(ourSide.opposite(), 8 - i);
        if (parentOurSeedsinCurrentHouse == 0 && parentOppSeedsinFrontHouse != 0 && oppSeedsinFrontHouse == 0) {
          if (node.getParent().getParent().getPlayerSide() == this.getOurSide()) {
            w5 = 50;
            e5 = parentOppSeedsinFrontHouse;
            break;
          }
          e5 = parentOppSeedsinFrontHouse;
          break;
        }
      }
    } else if (node.getParent().getPlayerSide() == this.getOurSide().opposite()) {

      if (parentMove + oppSeedsinHouse == 8) {
        e4 = -1;
        e3 = -1;
      } else if (parentMove + parentOppSeedsinStore > 8) {
        e4 = 0;
        e3 = -(parentMove + parentOppSeedsinStore) / 8;
      }
      for (int i = 1; i <= 7; i++) {
        int parentOppSeedsinCurrentHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), i);
        int parentOurSeedsinFrontHouse = node.getParent().getBoard().getSeeds(ourSide, 8 - i);
        int oppSeedsinCurrentHouse = node.getBoard().getSeeds(ourSide, 8 - i);

        if (parentOppSeedsinCurrentHouse == 0 && parentOurSeedsinFrontHouse != 0 && oppSeedsinCurrentHouse == 0) {
          if (node.getParent().getParent().getPlayerSide() == this.getOurSide()) {
            w5 = 50;
            e5 = -parentOurSeedsinFrontHouse;
            break;
          }
          e5 = -parentOurSeedsinFrontHouse;
          break;
        }
      }
    }

//    System.err.println("Difference between the number of nodes in each side: " + e1);
//    System.err.println("difference between the number of free houses in each side: " + e2);
//    System.err.println("The number of seeds added to store by passing store in a turn: " + e3);
//    System.err.println("Last seed of house added to store in turn (1 or -1 or 0): " + e4);
//    System.err.println("The number of seeds added to store by getting nodes from opponent's house: " + e5);
//    System.err.println("w5 (if parent's last node of house added to store, and in current turn, some seeds added to " +
//        "store by getting nodes from opponent's house - so weight will be 50: " + w5);
//    System.err.println();

    ef = w1 * e1 + w2 * e2 + w3 * e3 + w4 * e4 + w5 * e5;
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
          System.err.println("asdfBot is starting: " + first);
          if (first) {
            // If we start first, set our side and "last player" variable
            this.setOurSide(Side.SOUTH);

            // Get best move
            int i = 1;//rightMove(canSwap, new Board(this.getAsdf().getBoard()));
            s = Protocol.createMoveMsg(i);
            System.err.println("asdfBot start decision: " + i);
            Main.sendMsg(s);
          } else {
            // Set opponent side and last player
            this.setOurSide(Side.NORTH);

            // Asdfbot can swipe on its turn
            canSwap = true;
          }
          break;

        case STATE:
          Protocol.MoveTurn gameMessage = Protocol.interpretStateMsg(s, this
              .getAsdf().getBoard());

          System.err.println("The board:\n" + this.getAsdf().getBoard());
          System.err.println("asdfBot's turn: " + gameMessage.again + ". Last move: " + gameMessage.move);

          if (gameMessage.move == -1) {
            swap();
          } else {
          }

          // if this turn is our turn
          if (gameMessage.again) {
            // Get best move
            int i = rightMove(canSwap, new Board(this.getAsdf().getBoard()));
            System.err.println("asdfBot decision: " + i);

            // if best right move is -1, it means asdfbot should swap
            if (i == -1) {
              s = Protocol.createSwapMsg();
              swap();
              // Otherwise, do move
            } else {
              s = Protocol.createMoveMsg(i);
            }
            canSwap = false;
            Main.sendMsg(s);
          } else {
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