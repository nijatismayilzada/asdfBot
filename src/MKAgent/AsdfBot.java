package MKAgent;

import java.io.IOException;

public class AsdfBot {

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
              Main.sendMsg(Protocol.createMoveMsg(1));
              break;

            case STATE:
              System.err.println("A state...");
              Board b = new Board(7, 7);
              Protocol.MoveTurn r = Protocol.interpretStateMsg(s, b);
              System.err.println("This was the move: " + r.move);
              System.err.println("Is the game over?: " + r.end);
              if (!r.end) System.err.println("Is it our turn again? " + r.again);
              System.err.println("The board:\n" + b);
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
      System.err.println("IOException: " + e.getMessage());
    }
  }

}