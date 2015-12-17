package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah asdfKalah;
  private Side ourSide;
  private Tree tree;
  private int DEPTH = 7;

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
    assignNodes(root, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);

    if (canSwap) {
      swap();
      Node swap = new Node(8, board);
      swap.setPlayerSide(this.getOurSide().opposite());
      assignNodes(swap, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
      System.err.println("Swap payoff!: " + swap.getPayoff());
      root.addNextMove(swap);
      swap();
    }

    int bestMove = 0;
    int maxValue = Integer.MIN_VALUE;

    for (Node possibleMove : this.tree.getRoot().getNextMoves()) {
      if (possibleMove.getPayoff() > maxValue) {
        maxValue = possibleMove.getPayoff();
        bestMove = possibleMove.getName();
      }
    }
    System.err.println("Best payoff!: " + maxValue);

    if (bestMove == 8) {
      return -1;
    }
    return bestMove;
  }



  // TODO: build tree more efficiently
  private int assignNodes(Node currentNode, int currentDepth, int alpha, int beta) {
    if (currentDepth <= DEPTH) {
      for (int i = 1; i <= 7; i++) {
        Kalah currentKalah = new Kalah(new Board(currentNode.getBoard()));
        Move nextMove = new Move(currentNode.getPlayerSide(), i);
        if (currentKalah.isLegalMove(nextMove)) {
          Side nextSide = currentKalah.makeMove(nextMove);
          Node nextChild = new Node(i, currentKalah.getBoard(), nextSide, 0, currentNode);
          currentNode.addNextMove(nextChild);
          int po = assignNodes(nextChild, currentDepth + 1, alpha, beta);

            if (nextChild.getPlayerSide() == this.getOurSide()) {
              if (po > alpha)
                alpha = po;
              if (alpha >= beta)
                return po;
            }
            else {
              if (po > beta)
                beta = po;
              if (alpha >= beta)
                return po;
            }
        } // if
      } // for

      int payoff = heuristic(currentNode, currentDepth);

      int minValue = Integer.MAX_VALUE;
      int maxValue = Integer.MIN_VALUE;

      if (currentNode.getPlayerSide() == this.getOurSide()) {
        for (Node nextNode : currentNode.getNextMoves()) {
          if (nextNode.getPayoff() > maxValue)
            maxValue = nextNode.getPayoff();
        }
        currentNode.setPayoff(payoff + maxValue);
      } else {
        for (Node nextNode : currentNode.getNextMoves()) {
          if (nextNode.getPayoff() < minValue)
            minValue = nextNode.getPayoff();
        }
        currentNode.setPayoff(payoff + minValue);
      } // ifelse

      return currentNode.getPayoff();
      // TODO: alphabeta pruning
    } else {

      int payoff = heuristic(currentNode, currentDepth);
      currentNode.setPayoff(payoff);
      return payoff;
    } // ifelse
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
  private int heuristic(Node node, int currentDepth) {

    int ef, w1 = 1, w2 = 1, w3 = 1, w4 = 10, w5 = 2, e1 = 0, e2 = 0, e3 = 0, e4 = 0, e5 = 0;

    int ourSeedsInStore = node.getBoard().getSeedsInStore(ourSide);
    int oppSeedsInStore = node.getBoard().getSeedsInStore(ourSide.opposite());


    // hardcode fix.
    int parentOurSeedsInStore = 0;
    int parentOppSeedsInStore = 0;
    int parentMove = 0;
    int parentOurSeedsInHouse = 0;
    int parentOppSeedsInHouse = 0;

    if (currentDepth != 1) {
      parentOurSeedsInStore = node.getParent().getBoard().getSeedsInStore(ourSide);
      parentOppSeedsInStore = node.getParent().getBoard().getSeedsInStore(ourSide.opposite());
      parentMove = node.getName();
      parentOurSeedsInHouse = node.getParent().getBoard().getSeeds(ourSide, parentMove);
      parentOppSeedsInHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), parentMove);
    }

    int ourFreeHouse = 0;
    int oppFreeHouse = 0;
    int ourSeeds = ourSeedsInStore;
    int oppSeeds = oppSeedsInStore;

    for (int i = 1; i <= 4; i++) {
      ourSeeds += node.getBoard().getSeeds(ourSide, i);
      oppSeeds += node.getBoard().getSeeds(ourSide.opposite(), i);
    }

    for (int i = 1; i <= 7; i++) {
      if (node.getBoard().getSeeds(ourSide, i) == 0)
        ourFreeHouse++;
      if (node.getBoard().getSeeds(ourSide.opposite(), i) == 0)
        oppFreeHouse++;
    }

    e1 = ourSeeds - oppSeeds;
    e2 = ourFreeHouse - oppFreeHouse;

    // hardcode fix.
    if (currentDepth != 1) {
      if (node.getParent().getPlayerSide() == this.getOurSide()) {
        // if last move puts seed into store
        if ((parentMove + parentOurSeedsInHouse) % 8 == 0) {
          e4 = 1;
          e3 = 1;
        } else if (parentMove + parentOurSeedsInStore > 8) {
          e4 = 0;
          e3 = (parentMove + parentOurSeedsInStore) / 8;
        }

        for (int i = 1; i <= 7; i++) {
          int parentOurSeedsInCurrentHouse = node.getParent().getBoard().getSeeds(ourSide, i);
          int parentOppSeedsInFrontHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), 8 - i);
          int oppSeedsInFrontHouse = node.getBoard().getSeeds(ourSide.opposite(), 8 - i);
          // if there's no seed in current house && there is seed right in front of us
          if ((parentOurSeedsInCurrentHouse == 0 || parentOurSeedsInCurrentHouse == 15) && parentOppSeedsInFrontHouse != 0 && oppSeedsInFrontHouse == 0) {
            if (currentDepth != 2) {
              if (node.getParent().getParent().getPlayerSide() == this.getOurSide()) {
                w5 = 5;
                e5 = parentOppSeedsInFrontHouse;
                break;
              }
            }
            e5 = parentOppSeedsInFrontHouse;
            break;
          }

        }
      } else if (node.getParent().getPlayerSide() == this.getOurSide().opposite()) {

        if (parentMove + parentOppSeedsInHouse == 8) {
          e4 = -1;
          e3 = -1;
        } else if (parentMove + parentOppSeedsInStore > 8) {
          e4 = 0;
          e3 = -(parentMove + parentOppSeedsInStore) / 8;
        }


        for (int i = 1; i <= 7; i++) {
          int parentOppSeedsInCurrentHouse = node.getParent().getBoard().getSeeds(ourSide.opposite(), i);
          int parentOurSeedsInFrontHouse = node.getParent().getBoard().getSeeds(ourSide, 8 - i);
          int oppSeedsInCurrentHouse = node.getBoard().getSeeds(ourSide, 8 - i);

          if ((parentOppSeedsInCurrentHouse == 0 || parentOppSeedsInCurrentHouse == 15) && parentOurSeedsInFrontHouse != 0 && oppSeedsInCurrentHouse == 0) {
            if (currentDepth != 2) {
              if (node.getParent().getParent().getPlayerSide() == this.getOurSide()) {
                w5 = 5;
                e5 = -parentOurSeedsInFrontHouse;
                break;
              }
            }
            w5 = 5;
            e5 = -parentOurSeedsInFrontHouse;
            break;
          }
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