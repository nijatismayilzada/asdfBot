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
final class AsdfLeaderMk1
    extends PlayerImpl {

  private int WINDOW_SIZE = 100;
  private int START_DAY = 101;
  private double LAMBDA = 0.99;
  private int HISTORY_DAYS = 100;
  private int DIMENSION = 2;

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

  AsdfLeaderMk1()
      throws RemoteException, NotBoundException {
    super(PlayerType.LEADER, "AsdfLeaderMk1");
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

    m_platformStub.log(PlayerType.LEADER, "START_DAY: " + START_DAY + " / steps: " + steps);

    m_platformStub.log(PlayerType.LEADER, "step: " + p_steps);

    for (int i = 1; i <= HISTORY_DAYS; i++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, i);

      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);
    }

    RLSInitialize();
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

    int endDay = START_DAY + steps;

    double sumLeader = 0;

    for (int day = START_DAY; day < endDay; day++) {
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

    double publishPrice = RLS(p_date);
//    double publishPrice = OLS(p_date);

    m_platformStub.log(PlayerType.LEADER, "publishPrice: " + publishPrice);
    m_platformStub.publishPrice(PlayerType.LEADER, (float) publishPrice);
  }


  public static void main(final String[] p_args)
      throws RemoteException, NotBoundException {
    new AsdfLeaderMk1();
  }

  /* Ordinary Least Square Method */

  private double OLS(int p_date) throws RemoteException {

    ArrayList<Float> ul = new ArrayList<>();
    ArrayList<Float> ufr = new ArrayList<>();

    for (int day = p_date - WINDOW_SIZE; day < p_date; day++) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, day);
      ul.add(l_newRecord.m_leaderPrice);
      ufr.add(l_newRecord.m_followerPrice);
    }

    double value_of_b = b(ul, ufr);
    double value_of_a = a(ul, ufr);

    m_platformStub.log(PlayerType.LEADER, "a : " + value_of_a + " b: " + value_of_b);

    if (value_of_b > 3.33) {
      m_platformStub.log(PlayerType.LEADER, "Warning Warning!! b is " + value_of_b);
      return Double.MAX_VALUE;
    }

    return maximisation(value_of_a, value_of_b);
  }

  private double a(ArrayList<Float> ul, ArrayList<Float> ufr) {
    int T = ul.size();
    double sum_ul_power_two = 0;
    double sum_ufr = 0;
    for (int t = 0; t < T; t++) {
      double p_lambda = Math.pow(LAMBDA, T - t + 1);
      sum_ul_power_two += Math.pow(ul.get(t), 2) * p_lambda;
      sum_ufr += ufr.get(t) * p_lambda;
    }
    double birinci = sum_ul_power_two * sum_ufr;
    double sum_ul = 0;
    double sum_ul_ufr_multip = 0;
    for (int t = 0; t < T; t++) {
      double p_lambda = Math.pow(LAMBDA, T - t + 1);
      sum_ul += ul.get(t) * p_lambda;
      sum_ul_ufr_multip += ul.get(t) * ufr.get(t) * p_lambda;
    }
    double ikinci = sum_ul * sum_ul_ufr_multip;
    double suret = birinci - ikinci;
    double mexrec = T * sum_ul_power_two - Math.pow(sum_ul, 2);
    return suret / mexrec;
  }

  private double b(ArrayList<Float> ul, ArrayList<Float> ufr) {
    int T = ul.size();
    double sum_ul_ufr_multip = 0;
    for (int t = 0; t < T; t++) {
      double p_lambda = Math.pow(LAMBDA, T - t + 1);
      sum_ul_ufr_multip += ul.get(t) * ufr.get(t) * p_lambda;
    }
    double birinci = T * sum_ul_ufr_multip;
    double sum_ul = 0;
    double sum_ufr = 0;
    for (int t = 0; t < T; t++) {
      double p_lambda = Math.pow(LAMBDA, T - t + 1);
      sum_ul += ul.get(t) * p_lambda;
      sum_ufr += ufr.get(t) * p_lambda;
    }
    double ikinci = sum_ul * sum_ufr;
    double suret = birinci - ikinci;
    double sum_ul_power_two = 0;
    for (int t = 0; t < T; t++) {
      double p_lambda = Math.pow(LAMBDA, T - t + 1);
      sum_ul_power_two += Math.pow(ul.get(t), 2) * p_lambda;
    }
    double mexrec = T * sum_ul_power_two - Math.pow(sum_ul, 2);
    return suret / mexrec;
  }

  private double maximisation(double a, double b) {
    return (3 + 0.3 * (a - b)) / (2 - 0.6 * b);
  }

  private double maximisation(double a, double b, double c) {
    double val_max = 0;
    double uu_max = 1;
    for (double uu = 1; uu < 3.0; uu = uu + 0.001) {
      double magic = (uu - 1) * (2 - uu + 0.3 * (a + b * uu + c * uu * uu));
      if (magic > val_max) {
        val_max = magic;
        uu_max = uu;
      }
    }
    return uu_max;
  }


  private double sl(double ul, double uf) {
    return (2 - ul + 0.3 * uf);
  }

  /* Recursive Least Square Method */

  private Matrix theta;
  private Matrix tempTheta;
  private Matrix P;
  private Matrix phi;
  private int steps;
  ArrayList<Float> ul = new ArrayList<>();
  ArrayList<Float> ufr = new ArrayList<>();

  private double RLS(int p_date) throws RemoteException {
    if (p_date != START_DAY) {
      Record l_newRecord = m_platformStub.query(PlayerType.LEADER, p_date - 1);
      RLSUpdate(l_newRecord.m_leaderPrice, l_newRecord.m_followerPrice);
    }

    if (DIMENSION == 2) {
      return maximisation(thetaToReaction().getA(), thetaToReaction().getB());
    }
    else {
      return maximisation(thetaToReaction().getA(), thetaToReaction().getB(), thetaToReaction().getC());
    }
  }

  private void RLSUpdate(double newUl, double newUf) {
    // Updated Theta = Old Theta + L_T+1 * [y(T+1) - Fi^ti[X(T+1)] * Theta_T]

    phi = this.assignPhi(newUl);

    Matrix numeratorL = P.times(phi);
    Matrix denumeratorL = phi.transpose().times(P).times(phi).plus(LAMBDA);
    Matrix L = numeratorL.divide(denumeratorL.data[0][0]);

    theta = theta.plus(L.times(newUf - phi.transpose().times(theta).data[0][0]));

    Matrix numeratorP = P.times(phi).times(phi.transpose()).times(P);
    Matrix denumeratorP = phi.transpose().times(P).times(phi).plus(LAMBDA);
    P = P.minus(numeratorP.divide(denumeratorP.data[0][0])).divide(LAMBDA);
  }

  private void RLSInitialize() throws RemoteException {
    theta = new Matrix(DIMENSION, 1);
    tempTheta = new Matrix(DIMENSION, 1);
    P = new Matrix(DIMENSION, DIMENSION);
    phi = new Matrix(DIMENSION, 1);

    Matrix sumPhi = new Matrix(DIMENSION, DIMENSION);
    double reactY;
    double approxX;
    double y;
    double ySubReactY = 0;
    double RMSE;

    double ySubReactYDivideY = 0;
    double MAPE;

    for (int day = 1; day <= HISTORY_DAYS; day++) {
      phi = this.assignPhi(ul.get(day - 1));

      sumPhi = sumPhi.plus(phi.times(phi.transpose()).times(Math.pow(LAMBDA, HISTORY_DAYS - day)));
      tempTheta = tempTheta.plus(phi.times(ufr.get(day - 1)).times(Math.pow(LAMBDA, HISTORY_DAYS - day)));

      P = new Matrix(sumPhi);
      theta = P.invert().times(tempTheta);

      if (day > 1) {
        if (DIMENSION == 2) {
          approxX = maximisation(thetaToReaction().getA(), thetaToReaction().getB());
          reactY = thetaToReaction().getA() + thetaToReaction().getB() * approxX;
        }
        else {
          approxX = maximisation(thetaToReaction().getA(), thetaToReaction().getB(), thetaToReaction().getC());
          reactY = thetaToReaction().getA() + thetaToReaction().getB() * approxX + thetaToReaction().getC() * Math
              .pow(approxX, 2);
        }

        Record l_newRecord = m_platformStub.query(PlayerType.LEADER, day);
        y = l_newRecord.m_followerPrice;
        ySubReactY += Math.pow(Math.abs(y - reactY), 2);
        ySubReactYDivideY += Math.abs((y - reactY) / y);
      }
    }
    RMSE = Math.sqrt(((double) 1 / (double) (HISTORY_DAYS - 1)) * ySubReactY);
    MAPE = ((double) 1 / (double) (HISTORY_DAYS - 1)) * ySubReactYDivideY;
    m_platformStub.log(PlayerType.LEADER, "RMSE: " + RMSE + " MAPE: " + MAPE);
  }

  private Matrix assignPhi(double v) {
    Matrix A = new Matrix(DIMENSION, 1);
    for (int d = 0; d < DIMENSION; d++) {
      A.data[d][0] = Math.pow(v, d);
    }

    return A;
  }

  private ReactionFunction thetaToReaction() {
    if (DIMENSION == 2) return new ReactionFunction(theta.data[0][0], theta.data[1][0]);
    else return new ReactionFunction(theta.data[0][0], theta.data[1][0], theta.data[2][0]);
  }


}
