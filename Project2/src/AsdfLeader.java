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
  private Matrix theta;
  private Matrix P;
  private Matrix phi;
  private float lambda = (float) 0.96;

  AsdfLeader()
      throws RemoteException, NotBoundException {
    super(PlayerType.LEADER, "AsdfLeader");
    theta = new Matrix(2,1);
    P = new Matrix(2,2);
    phi = new Matrix(1,2);
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

    m_platformStub.log(PlayerType.LEADER, "step: " + p_steps);

      m_platformStub.log(PlayerType.LEADER, "kamil got");

      ArrayList<Float> ul = new ArrayList<>();
      ArrayList<Float> ufr = new ArrayList<>();

      for (int i = 1; i < 31; i++) {
          Record l_newRecord = m_platformStub.query(PlayerType.LEADER, i);

          ul.add(l_newRecord.m_leaderPrice);
          ufr.add(l_newRecord.m_followerPrice);
      }


      float value_of_b = b(ul, ufr);
      m_platformStub.log(PlayerType.LEADER, "ul size: " + ul.size());
      m_platformStub.log(PlayerType.LEADER, "a : " + a(ul, ufr) + " b: " + value_of_b);
      if (value_of_b > 3.33) m_platformStub.log(PlayerType.LEADER, "Warning Warning!! b is " + value_of_b);

      float maximisation = maximisation(a(ul, ufr), b(ul, ufr));

      m_platformStub.log(PlayerType.LEADER, "training ul: " + maximisation);
//      m_platformStub.publishPrice(PlayerType.LEADER, maximisation);

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

    Record l_newRecord = null;
    int startDay = 101;
    int endDay = 130;

    float sumLeader = 0;
    // float sumFollower = 0;

    for (int day = startDay; day < endDay; day++) {
      l_newRecord = m_platformStub.query(PlayerType.FOLLOWER, day);

      sumLeader += (l_newRecord.m_leaderPrice - 1) * sl(l_newRecord.m_leaderPrice, l_newRecord.m_followerPrice);
      // sumFollower += (l_newRecord.m_followerPrice - 1) * sl(l_newRecord.m_leaderPrice, l_newRecord.m_followerPrice);
    }

    m_platformStub.log(PlayerType.LEADER, "sumLeader: " + sumLeader);

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

    m_platformStub.log(PlayerType.LEADER, "kamil got");

    ArrayList<Float> ul = new ArrayList<>();
    ArrayList<Float> ufr = new ArrayList<>();

    for (int i = p_date-30; i < p_date; i++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, i);

      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);
    }

    float value_of_b = b(ul, ufr);
    m_platformStub.log(PlayerType.LEADER, "ul size: " + ul.size());
    m_platformStub.log(PlayerType.LEADER, "a : " + a(ul, ufr) + " b: " + value_of_b);
    if (value_of_b > 3.33) m_platformStub.log(PlayerType.LEADER, "Warning Warning!! b is " + value_of_b);

//    float forget = (float) 0.95;
//    float sigma = 100;
//
//    ArrayList<Float> P = new ArrayList<>();
//    ArrayList<Float> w = new ArrayList<>();
//    ArrayList<Float> a = new ArrayList<>();
//    ArrayList<Float> g = new ArrayList<>();
//
//    P.add(0, 1/sigma * 1);
//    w.add(0, (float) 0);
//    a.add(0, (float) 0);
//    g.add(0, (float) 0);
//
//
//    for(int n=1; n<=ul.size(); n++)
//    {
//      //ul and ufr n-1 operation is because of arraylist starting index
//      a.add(n, ufr.get(n-1) - ul.get(n-1) * w.get(n-1));
//      g.add(n, P.get(n-1)*ul.get(n-1)*(1/(forget + ul.get(n-1)*P.get(n-1)*ul.get(n-1))));
//      P.add(n, 1/forget * P.get(n-1) - g.get(n)*ul.get(n-1)*(1/forget)*P.get(n-1));
//      w.add(n, w.get(n-1) + a.get(n) * g.get(n));
//    }
//    float delta = w.get(ul.size()) - w.get(ul.size()-1);
//    float newPrice = maximisation + delta;


    float maximisation = maximisation(a(ul, ufr), b(ul, ufr));
    m_platformStub.publishPrice(PlayerType.LEADER, maximisation);
  }



  public static void main(final String[] p_args)
      throws RemoteException, NotBoundException {
    new AsdfLeader();
  }

  public void RLSUpdate(float newUl, float newUf){
    // Updated Theta = Old Theta + L_T+1 * [y(T+1) - Fi^ti[X(T+1)] * Theta_T]

    Matrix numeratorL = P.times(phi);
    Matrix denumeratorL = phi.transpose().times(P).times(phi).plus(lambda);
    Matrix L = numeratorL.divide(denumeratorL.data[0][0]);

    theta = theta.plus(L.times(newUf - phi.transpose().times(theta).data[0][0]));

    Matrix numeratorP = P.times(phi).times(phi.transpose()).times(P);
    Matrix denumeratorP = phi.transpose().times(P).times(phi).plus(lambda);
    P = P.minus(numeratorP.divide(denumeratorP.data[0][0])).divide(lambda);

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
      sum_ul_power_two += Math.pow(ul.get(t), 2) ;
    }

    float mexrec = T * sum_ul_power_two - (float) Math.pow(sum_ul, 2);

    return suret / mexrec;
  }

  private float maximisation(float a, float b) {
    return (float) ((-3 - 0.3 * (a - b)) / (0.6 * b - 2));
  }

  private float sl(float ul, float uf) {
    return (float) (2 - ul + 0.3 * uf);
  }

}
