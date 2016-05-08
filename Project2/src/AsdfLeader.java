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

    ul.clear();
    ufr.clear();
    steps = p_steps;

    m_platformStub.log(PlayerType.LEADER, "startDay: " + startDay + " / steps: " + steps);

    m_platformStub.log(PlayerType.LEADER, "step: " + p_steps);

    for (int i = 1; i <= historyDays; i++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, i);

      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);
    }

    RLSInitialize();

    float maximisation = maximisation(thetaToReaction().getA(), thetaToReaction().getB());

    m_platformStub.log(PlayerType.LEADER, "startSimulation maximisation: " + maximisation);
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

    int endDay = startDay + steps;

    float sumLeader = 0;

    for (int day = startDay; day < endDay; day++) {
      Record l_newRecord = m_platformStub.query(PlayerType.FOLLOWER, day);

      m_platformStub.log(PlayerType.LEADER, "day " + day + " / leader price: " + l_newRecord.m_leaderPrice + " / " +
          "follower price: " + l_newRecord.m_followerPrice);

      sumLeader += (l_newRecord.m_leaderPrice - 1) * sl(l_newRecord.m_leaderPrice, l_newRecord.m_followerPrice);
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

    float publishPrice = RLS(p_date);
//    float publishPrice = OLS(p_date, 30, 0.96F);

    m_platformStub.log(PlayerType.LEADER, "publishPrice: " + publishPrice);
    m_platformStub.publishPrice(PlayerType.LEADER, publishPrice);
  }


  public static void main(final String[] p_args)
      throws RemoteException, NotBoundException {
    new AsdfLeader();
  }

  /* Ordinary Least Square Method */

  private float forgettingFactor;

  private float OLS(int p_date, int windowSize, float forgettingFactor) throws RemoteException {

    this.forgettingFactor = forgettingFactor;

    ArrayList<Float> ul = new ArrayList<>();
    ArrayList<Float> ufr = new ArrayList<>();

    for (int day = p_date - windowSize; day < p_date; day++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, day);
      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);
    }

    float value_of_b = b(ul, ufr);
    float value_of_a = a(ul, ufr);

    m_platformStub.log(PlayerType.LEADER, "a : " + value_of_a + " b: " + value_of_b);

    if (value_of_b > 3.33) {
      m_platformStub.log(PlayerType.LEADER, "Warning Warning!! b is " + value_of_b);
      return Float.MAX_VALUE;
    }

    return maximisation(value_of_a, value_of_b);
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

  private float sl(float ul, float uf) {
    return (float) (2 - ul + 0.3 * uf);
  }


  /* Recursive Least Square Method */

  private Matrix theta;
  private Matrix P;
  private Matrix phi;
  private int steps;
  private int startDay = 101;
  private float lambda = (float) 0.96;
  private int historyDays = 100;
  ArrayList<Float> ul = new ArrayList<>();
  ArrayList<Float> ufr = new ArrayList<>();

  private float RLS(int p_date) throws RemoteException {
    if (p_date != startDay) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, p_date - 1);
      RLSUpdate(l_newRecord.m_leaderPrice, l_newRecord.m_followerPrice);
    }

    return maximisation(thetaToReaction().getA(), thetaToReaction().getB());
  }

  private void RLSUpdate(float newUl, float newUf) {
    // Updated Theta = Old Theta + L_T+1 * [y(T+1) - Fi^ti[X(T+1)] * Theta_T]

    phi = this.assignPhi(newUl);

    Matrix numeratorL = P.times(phi);
    Matrix denumeratorL = phi.transpose().times(P).times(phi).plus(lambda);
    Matrix L = numeratorL.divide(denumeratorL.data[0][0]);

    theta = theta.plus(L.times(newUf - phi.transpose().times(theta).data[0][0]));

    Matrix numeratorP = P.times(phi).times(phi.transpose()).times(P);
    Matrix denumeratorP = phi.transpose().times(P).times(phi).plus(lambda);
    P = P.minus(numeratorP.divide(denumeratorP.data[0][0])).divide(lambda);
  }

  private void RLSInitialize() throws RemoteException {
    theta = new Matrix(2, 1);
    P = new Matrix(2, 2);
    phi = new Matrix(2, 1);

    Matrix sumPhi = new Matrix(2, 2);
    for (int day = 1; day <= historyDays; day++) {
      phi = this.assignPhi(ul.get(day - 1));

      sumPhi = sumPhi.plus(phi.times(phi.transpose()).times((float) Math.pow(lambda, historyDays - day)));
      theta = theta.plus(phi.times(ufr.get(day - 1)).times((float) Math.pow(lambda, historyDays - day)));
    }
    P = new Matrix(sumPhi);

    theta = P.invert().times(theta);
  }

  private Matrix assignPhi(float v) {
    Matrix A = new Matrix(2, 1);
    A.data[0][0] = 1;
    A.data[1][0] = v;

    return A;
  }

  private ReactionFunction thetaToReaction() {
    return new ReactionFunction(theta.data[0][0], theta.data[1][0]);
  }


}
