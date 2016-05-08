public class ReactionFunction {
  private double a;
  private double b;
  private double c;

  public ReactionFunction(double a, double b) {
    this.a = a;
    this.b = b;
  }

  public ReactionFunction(double a, double b, double c) {
    this(a, b);
    this.c = c;
  }

  public double getA() {
    return a;
  }

  public void setA(double a) {
    this.a = a;
  }

  public double getB() {
    return b;
  }

  public void setB(double b) {
    this.b = b;
  }

  public double getC() {
    return c;
  }

  public void setC(double c) {
    this.c = c;
  }
}
