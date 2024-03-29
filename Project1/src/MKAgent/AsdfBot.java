package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah asdfKalah;
  private Side ourSide;
  private Tree tree;
  private int DEPTH = 7;
  private int[][] pruning = new int[DEPTH + 1][2];

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
    for (int i = 0; i < DEPTH + 1; i++) {
      pruning[i][0] = Integer.MIN_VALUE;
      pruning[i][1] = Integer.MAX_VALUE;
    }
    assignNodes(root, 1);

    if (canSwap) {
      swap();
      Node swap = new Node(8, board);
      swap.setPlayerSide(this.getOurSide().opposite());

      assignNodes(swap, 1);

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

    if (bestMove == 8) {
      return -1;
    }
    return bestMove;
  }

  boolean firstRec = true;
  boolean secondRec = true;

  private void assignNodes(Node currentNode, int currentDepth) {
    if (currentDepth <= DEPTH) {
      for (int i = 1; i <= 7; i++) {
        Kalah currentKalah = new Kalah(new Board(currentNode.getBoard()));
        Move nextMove = new Move(currentNode.getPlayerSide(), i);
        if (currentKalah.isLegalMove(nextMove)) {
          Side nextSide = currentKalah.makeMove(nextMove);
          Node nextChild = new Node(i, currentKalah.getBoard(), nextSide, 0, currentNode, firstRec);
          currentNode.addNextMove(nextChild);
          if (secondRec)
            assignNodes(nextChild, currentDepth + 1);
          if (firstRec == false)
            secondRec = false;

          if (!nextChild.getInNode() && i != 1 && currentDepth != DEPTH - 1 && currentDepth != DEPTH && currentNode.getPlayerSide() == this.getOurSide().opposite()) {
            if (nextChild.getPayoff() > pruning[currentDepth][1]) {
              firstRec = false;
            }
            if ((nextChild.getPayoff() + currentNode.getPayoff()) < pruning[currentDepth][0]) {
              firstRec = false;
            }
          }
        } // if
      } // for
      firstRec = true;
      secondRec = true;

      int payoff = heuristic(currentNode, currentDepth);

      int minValue = Integer.MAX_VALUE;
      int maxValue = Integer.MIN_VALUE;

      if (currentNode.getPlayerSide() == this.getOurSide()) {
        for (Node nextNode : currentNode.getNextMoves()) {
          if (nextNode.getPayoff() > maxValue && !nextNode.getInNode())
            maxValue = nextNode.getPayoff();
        }
        currentNode.setPayoff(payoff + maxValue);
        currentNode.setInNode(false);
      } else {
        for (Node nextNode : currentNode.getNextMoves()) {
          if (nextNode.getPayoff() < minValue && !nextNode.getInNode())
            minValue = nextNode.getPayoff();
        }
        currentNode.setPayoff(payoff + minValue);
        currentNode.setInNode(false);
      } // ifelse
      if (pruning[currentDepth][0] < currentNode.getPayoff())
        pruning[currentDepth][0] = currentNode.getPayoff();
      if (pruning[currentDepth][1] > currentNode.getPayoff())
        pruning[currentDepth][1] = currentNode.getPayoff();
    } else {

      int payoff = heuristic(currentNode, currentDepth);
      currentNode.setPayoff(payoff);
      currentNode.setInNode(false);
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

    int ef, w1 = 1, w2 = 1, w3 = 0, w4 = 10, w5 = 1, w6 = 35, e1 = 0, e2 = 0, e3 = 0, e4 = 0, e5 = 0, e6 = 0;

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

    int asdf1 = ourSeedsInStore - parentOurSeedsInStore;
    int asdf2 = oppSeedsInStore - parentOppSeedsInStore;

    e6 = asdf1 - asdf2;

    for (int i = 1; i <= 7; i++) {
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
          e3 = (parentMove + parentOurSeedsInHouse) / 8;
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
          e3 = -1 * (parentMove + parentOppSeedsInHouse) / 8;
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
    ef = w1 * e1 + w2 * e2 + w3 * e3 + w4 * e4 + w5 * e5 + w6 * e6;
    return ef;
  }

  public void cuddleRamin() throws InvalidMessageException, IOException {
    String s;
    MsgType mt;
    // canSwap boolean being true if our bot is in north side
    boolean canSwap = false;

    while ((mt = Protocol.getMessageType(s = Main.recvMsg())) != MsgType.END) {
      switch (mt) {
        case START:
          // if true, our bot starts first
          boolean first = Protocol.interpretStartMsg(s);
          if (first) {
            // If we start first, set our side and "last player" variable
            this.setOurSide(Side.SOUTH);

            // Get best move
            int i = 1;//rightMove(canSwap, new Board(this.getAsdf().getBoard()));
            s = Protocol.createMoveMsg(i);
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

          if (gameMessage.move == -1) {
            swap();
          } else {
          }

          // if this turn is our turn
          if (gameMessage.again) {
            // Get best move
            int i = rightMove(canSwap, new Board(this.getAsdf().getBoard()));

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
          return;
      }
    }
  }
}