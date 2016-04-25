import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.Record;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * A pseudo leader. The members m_platformStub and m_type are declared
 * in the PlayerImpl, and feel free to use them. You may want to check
 * the implementation of the PlayerImpl. You will use m_platformStub to access
 * the platform by calling the remote method provided by it.
 *
 * @author Xin
 */
final class AsdfLeader
    extends PlayerImpl {
  /**
   * In the constructor, you need to call the constructor
   * of PlayerImpl in the first line, so that you don't need to
   * care about how to connect to the platform. You may want to throw
   * the two exceptions declared in the prototype, or you may handle it
   * by using "try {} catch {}". It's all up to you.
   *
   * @throws RemoteException
   * @throws NotBoundException
   */
  AsdfLeader()
      throws RemoteException, NotBoundException {
    super(PlayerType.LEADER, "AsdfLeader");
  }

  /**
   * You may want to delete this method if you don't want modify
   * the original connection checking behavior of the platform.
   * Actually I recommend you to delete this method from your own code
   *
   * @throws RemoteException If implemented, the RemoteException *MUST* be
   *                         thrown by this method
   */
  @Override
  public void checkConnection()
      throws RemoteException {
    super.checkConnection();
    //TO DO: delete the line above and put your own code here
  }

  /**
   * You may want to delete this method if you don't want the platform
   * to control the exit behavior of your leader class
   *
   * @throws RemoteException If implemented, the RemoteException *MUST* be
   *                         thrown by this method
   */
  @Override
  public void goodbye()
      throws RemoteException {
    super.goodbye();
    //TO DO: delete the line above and put your own exit code here
  }

  /**
   * You may want to delete this method if you don't want to do any
   * initialization
   *
   * @param p_steps Indicates how many steps will the simulation perform
   * @throws RemoteException If implemented, the RemoteException *MUST* be
   *                         thrown by this method
   */
  @Override
  public void startSimulation(int p_steps)
      throws RemoteException {
    super.startSimulation(p_steps);
    //TO DO: delete the line above and put your own initialization code here
  }

  /**
   * You may want to delete this method if you don't want to do any
   * finalization
   *
   * @throws RemoteException If implemented, the RemoteException *MUST* be
   *                         thrown by this method
   */
  @Override
  public void endSimulation()
      throws RemoteException {
    super.endSimulation();
    //TO DO: delete the line above and put your own finalization code here
  }

  /**
   * To inform this instance to proceed to a new simulation day
   *
   * @param p_date The date of the new day
   * @throws RemoteException This exception *MUST* be thrown by this method
   */
  @Override
  public void proceedNewDay(int p_date)
      throws RemoteException {
        /*
         * Check for new price
		 * Record l_newRecord = m_platformStub.query(m_type, p_date);
		 *
		 * Your own math model to compute the price here
		 * ...
		 * float l_newPrice = ....
		 *
		 * Submit your new price, and end your phase
		 * m_platformStub.publishPrice(m_type, l_newPrice);
		 */

    m_platformStub.log(PlayerType.LEADER, "kamil got");

    ArrayList<Float> ul = new ArrayList<>();
    ArrayList<Float> ufr = new ArrayList<>();

//    float ul[] = new float[101];
//    float ufr[] = new float[101];
    for (int i = p_date - 100; i < p_date; i++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, i);

      m_platformStub.log(PlayerType.LEADER, "Day " + i + " Leader Price: " + l_newRecord.m_leaderPrice +
          " Follower Price: " + l_newRecord.m_followerPrice);

      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);

//      m_platformStub.log(PlayerType.LEADER, "day " + i + " leader price is " + l_newRecord.m_leaderPrice);
//      m_platformStub.log(PlayerType.LEADER, "day " + i + " follower price is " + l_newRecord.m_followerPrice);
    }

    m_platformStub.log(PlayerType.LEADER, "ul size: " + ul.size());
    m_platformStub.log(PlayerType.LEADER, "a : " + a(ul, ufr) + " b: " + b(ul, ufr));

    float maximisation = maximisation(a(ul, ufr), b(ul, ufr));


    m_platformStub.publishPrice(PlayerType.LEADER, maximisation);
  }

  public static void main(final String[] p_args)
      throws RemoteException, NotBoundException {
    new AsdfLeader();
  }

  private float a(ArrayList<Float> ul, ArrayList<Float> ufr) {
    int T = ul.size();

    float sum_ul_power_two = 0;
    float sum_ufr = 0;

    for (int t = 0; t < T; t++) {
      sum_ul_power_two += Math.pow(ul.get(t), 2);
      sum_ufr += ufr.get(t);
    }

    float birinci = sum_ul_power_two * sum_ufr;

    float sum_ul = 0;
    float sum_ul_ufr_multip = 0;
    for (int t = 0; t < T; t++) {
      sum_ul += ul.get(t);
      sum_ul_ufr_multip += ul.get(t) * ufr.get(t);
    }

    float ikinci = sum_ul * sum_ul_ufr_multip;


    float suret = birinci - ikinci;

    float mexrec = T * sum_ul_power_two - (float) Math.pow(sum_ul, 2);


    return suret / mexrec;
  }

  private float b(ArrayList<Float> ul, ArrayList<Float> ufr) {

    int T = ul.size();

    float sum_ul_ufr_multip = 0;
    for (int t = 0; t < T; t++) {
      sum_ul_ufr_multip += ul.get(t) * ufr.get(t);
    }

    float birinci = T * sum_ul_ufr_multip;

    float sum_ul = 0;
    float sum_ufr = 0;
    for (int t = 0; t < T; t++) {
      sum_ul += ul.get(t);
      sum_ufr += ufr.get(t);
    }

    float ikinci = sum_ul * sum_ufr;

    float suret = birinci - ikinci;

    float sum_ul_power_two = 0;
    for (int t = 0; t < T; t++) {
      sum_ul_power_two += Math.pow(ul.get(t), 2);
    }

    float mexrec = T * sum_ul_power_two - (float) Math.pow(sum_ul, 2);

    return suret / mexrec;
  }

  private float maximisation(float a, float b) {
    return (float) ((-3 - 0.3 * (a - b)) / (0.6 * b - 2));
  }

}
