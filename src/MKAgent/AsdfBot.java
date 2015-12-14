package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah    asdfKalah;
  private Side     ourSide;
  private MoveType lastPlayer;
  private Tree tree;

  public AsdfBot(int holes, int seeds) {
    this.asdfKalah = new Kalah(new Board(holes, seeds));
    this.tree = new Tree(new Node());
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

  private int rightMove(boolean canSwap) {
    int maxValue = Integer.MIN_VALUE;
    int bestMove = 0;
    if (canSwap) {
      // do swap
      swap();
      // count heuristics
      int newValue = heuristics(this.getAsdf().getBoard());
      // compare and set best move
      if (newValue > maxValue) {
        maxValue = newValue;
        bestMove = -1;
      }
      // invert swap to turn the board to its original state
      swap();
    }
    
    Node root = new Node();
    root.setMoveType(MoveType.ASDFBOT);
    root.setPayoff(-1);

    for (int i = 1; i <= 7; i++) {
      // Check for possible moves, (not swap)
      // Copy original board to temporary board
      Kalah tempKalah = new Kalah(new Board(this.getAsdf().getBoard()));
      // Check the move, do the move and count heuristics
      Node treeNode = new Node();
      
      if (tempKalah.isLegalMove(new Move(this.getOurSide(), i))) {
        tempKalah.makeMove(new Move(this.getOurSide(), i));
        System.err.println("Rightmove board:\n" + tempKalah.getBoard());
        int newValue = heuristics(tempKalah.getBoard());
        
        treeNode.setPayoff(newValue);
        treeNode.setLegal(true);
        treeNode.setMoveType(MoveType.OPPONENT);
        
//        if (newValue > maxValue) {
//          maxValue = newValue;
//          bestMove = i;
//        }
      } else {
        // eger legal deyilse onda illegal set ele, sonra add ele.
        treeNode.setLegal(false);
      }
      root.addNextMove(treeNode);
    }
    
    for(int i = 1; i <= 7; i++) {
      Node move = root.getNextMove(i -1);
      System.err.println(move.toString());
      
      
      if(move.isLegal()) {
        if (move.getPayoff() > maxValue) {
          maxValue = move.getPayoff();
          bestMove = i;
        }
      }
    }
    
    return bestMove;
  }

  private void swap() {
    this.ourSide = ourSide.opposite();
  }

  private int heuristics(Board board) {
    int ourSeeds = board.getSeedsInStore(ourSide);
    int oppSeeds = board.getSeedsInStore(ourSide.opposite());

    for (int i = 1; i <= 7; i++) {
      ourSeeds += board.getSeeds(ourSide, i);
      oppSeeds += board.getSeeds(ourSide.opposite(), i);
    }
    int diff = ourSeeds - oppSeeds;
    return diff;
  }

  public void doRamin() throws InvalidMessageException, IOException {
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
            int i = rightMove(canSwap);
            s = Protocol.createMoveMsg(i);
            System.err.println("Asdf start decision: " + i);
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
            int i = rightMove(canSwap);
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