package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah      asdfKalah;
  private Side       ourSide;
  private int        lastPlayer;
  private static int ASDFBOT  = 1;
  private static int OPPONENT = 0;
  private int        holes;
  private int        seeds;

  public AsdfBot(int holes, int seeds) {
    this.asdfKalah = new Kalah(new Board(holes, seeds));
    this.holes = holes;
    this.seeds = seeds;
  }

  public AsdfBot(AsdfBot original) {
    this(original.getHoleCapacity(), original.getSeedCapacity());
    // no defensive copies are created here, since
    // there are no mutable object fields (String is immutable)
  }

  public int getHoleCapacity() {
    return this.holes;
  }

  public int getSeedCapacity() {
    return this.seeds;
  }

  public Kalah getAsdf() {
    return asdfKalah;
  }

  public void setAsdf(int holes, int seeds) {
    this.asdfKalah = new Kalah(new Board(holes, seeds));
  }

  public Side getOurSide() {
    return this.ourSide;
  }

  public void setOurSide(Side ourSide) {
    this.ourSide = ourSide;
  }

  public int getLastPlayer() {
    return lastPlayer;
  }

  public void setLastPlayer(int lastPlayer) {
    this.lastPlayer = lastPlayer;
  }

  private int rightMove(boolean canSwap) {
    int maxValue = Integer.MIN_VALUE;
    int bestMove = 0;
    if (!canSwap) {
      for (int i = 1; i <= 7; i++) {
        AsdfBot tempKalah = new AsdfBot(this);
        if (tempKalah.getAsdf().isLegalMove(new Move(this.getOurSide(), i))) {
          tempKalah.getAsdf().makeMove(new Move(this.getOurSide(), i));
          int newValue = heuristics(tempKalah.getAsdf().getBoard());
          if (newValue > maxValue) {
            maxValue = newValue;
            bestMove = i;
          }
        }
      }
    } else {
      for (int i = 0; i <= 7; i++) {
        if (i == 0) {
          swap();
          int newValue = heuristics(this.getAsdf().getBoard());
          if (newValue > maxValue) {
            maxValue = newValue;
            bestMove = -1;
          }
          swap();
        } else {
          AsdfBot tempKalah = new AsdfBot(this);
          if (tempKalah.getAsdf().isLegalMove(new Move(this.getOurSide(), i))) {
            tempKalah.getAsdf().makeMove(new Move(this.getOurSide(), i));
            int newValue = heuristics(tempKalah.getAsdf().getBoard());
            if (newValue > maxValue) {
              maxValue = newValue;
              bestMove = i;
            }
          }
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

  private void updateBoard(Protocol.MoveTurn gameMessage) {
    if (this.getLastPlayer() == ASDFBOT) {
      if (gameMessage.move != -1) {
        System.err.println("Last turn was ASDFBOT's turn with move: "
            + gameMessage.move);
        this.getAsdf().makeMove(new Move(this.getOurSide(), gameMessage.move));
      } else {
        // This actually doesn't happen. Game engine doesn't show our swap
        // message to us.
        System.err.println("Last turn was ASDFBOT's turn with swap");
        swap();
      }
    } else {
      if (gameMessage.move != -1) {
        System.err.println("Last turn was Opponent's turn with move: "
            + gameMessage.move);
        this.getAsdf().makeMove(
            new Move(this.getOurSide().opposite(), gameMessage.move));
      } else {
        System.err.println("Last turn was Opponent's turn with swap");
        swap();
      }
    }

  }

  public void doRamin() {
    try {
      String s;
      // canSwap boolean being true if our bot is in north side
      boolean canSwap = false;

      while (true) {
        System.err.println();
        s = Main.recvMsg();
        System.err.println("Received: " + s);
        try {
          MsgType mt = Protocol.getMessageType(s);
          switch (mt) {
            case START:
              // if true, our bot starts first
              boolean first = Protocol.interpretStartMsg(s);
              System.err.println("ASDFBOT is starting: " + first);
              if (first) {
                this.setOurSide(Side.SOUTH);
                this.setLastPlayer(ASDFBOT);

                int i = rightMove(canSwap);
                s = Protocol.createMoveMsg(i);
                System.err.println("Our start move: " + i);
                this.getAsdf().makeMove(new Move(ourSide, i));
                Main.sendMsg(s);

              } else {
                this.setOurSide(Side.NORTH);
                this.setLastPlayer(OPPONENT);
                canSwap = true;
              }
              break;

            case STATE:
              Protocol.MoveTurn gameMessage = Protocol.interpretStateMsg(s,
                  this.getAsdf().getBoard());

              updateBoard(gameMessage);
              System.err.println("The board:\n" + this.getAsdf().getBoard());
              System.err.println("ASDFBOT's turn: " + gameMessage.again);

              if (gameMessage.again) {
                int i = rightMove(canSwap);
                // if best right move send -1
                if (i == -1) {
                  s = Protocol.createSwapMsg();
                  swap();
                } else {
                  s = Protocol.createMoveMsg(i);
                }
                canSwap = false;
                this.setLastPlayer(ASDFBOT);
                Main.sendMsg(s);
              } else {
                this.setLastPlayer(OPPONENT);
              }

              if (gameMessage.end) {

              } else {

              }
              break;

            case END:
              System.err.println("Bye!");
              return;

          }
        } catch (InvalidMessageException e) {
          System.err.println("InvalidMessageException: " + e.getMessage());
        }

      }
    } catch (IOException e) {
      System.err.println("IOEx ception: " + e.getMessage());
    }
  }
}