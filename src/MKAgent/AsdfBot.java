package MKAgent;

import java.io.IOException;

public class AsdfBot {

  private Kalah  kalah;
  protected Side ourSide = Side.SOUTH;

  public AsdfBot(int holes, int seeds) {
    this.kalah = new Kalah(new Board(holes, seeds));
  }

  public void doMagic() {

    try {
      String s;
      while (true) {
        System.err.println();
        s = Main.recvMsg();

        System.err.println("Received: " + s);
        try {
          MsgType mt = Protocol.getMessageType(s);
          switch (mt) {
            case START:
              System.err.println("A start...");
              boolean first = Protocol.interpretStartMsg(s);
              System.err.println("Starting player? " + first);
              if (first) {
                ourSide = Side.SOUTH;
                Main.sendMsg(Protocol.createMoveMsg(1));
                Board moveBoard = new Board(this.kalah.getBoard());
                Kalah.makeMove((Board) moveBoard, (Move) new Move(ourSide, 1));
              } else {
                ourSide = Side.NORTH;
                Main.sendMsg(Protocol.createSwapMsg());
                ourSide = Side.SOUTH;
              }
              break;

            case STATE:
              System.err.println("A state...");
              Protocol.MoveTurn r = Protocol.interpretStateMsg(s,
                  kalah.getBoard());
              if (r.again) {

              } else {

              }
              Board thisBoard = new Board(this.kalah.getBoard());

              if (r.move != -1) {
                kalah.makeMove(new Move(ourSide, r.move));
              }

              Move newMove;
              int i = 1;
              while (!Kalah.isLegalMove(thisBoard, new Move(ourSide, i))) {
                i++;
              }
              Kalah.makeMove((Board) thisBoard, new Move(ourSide, i));
              Main.sendMsg(Protocol.createMoveMsg(i));
              i = 1;
              System.err.println("This was the move: " + r.move);
              System.err.println("Is the game over?: " + r.end);
              if (!r.end)
                System.err.println("Is it our turn again? " + r.again);
              System.err.println("The board:\n" + kalah.getBoard());
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