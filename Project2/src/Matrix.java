import java.util.Arrays;

// 2D Matrix library by Princeton University:
// http://introcs.cs.princeton.edu/java/95linear/Matrix.java.html
final public class Matrix {
  public final int M;             // number of rows
  public final int N;             // number of columns
  public final double[][] data;   // M-by-N array

  // create M-by-N matrix of 0's
  public Matrix(int M, int N) {
    this.M = M;
    this.N = N;
    data = new double[M][N];
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        this.data[i][j] = 0.0;
  }

  // create matrix based on 2d array
  public Matrix(double[][] data) {
    M = data.length;
    N = data[0].length;
    this.data = new double[M][N];
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        this.data[i][j] = data[i][j];
  }

  // copy constructor
  public Matrix(Matrix A) {
    this(A.data);
  }

  // create and return a random M-by-N matrix with values between 0 and 1
  public static Matrix random(int M, int N) {
    Matrix A = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[i][j] = Math.random();
    return A;
  }

  // create and return the N-by-N identity matrix
  public static Matrix identity(int N) {
    Matrix I = new Matrix(N, N);
    for (int i = 0; i < N; i++)
      I.data[i][i] = 1;
    return I;
  }

  // Adapted from https://www.physics.unlv.edu/~pang/comp4/Inverse.java

  public Matrix invert() {
    int n = data.length;
    double x[][] = new double[n][n];
    double b[][] = new double[n][n];
    int index[] = new int[n];
    for (int i = 0; i < n; ++i)
      b[i][i] = 1;

    // Transform the matrix into an upper triangle
    gaussian(data, index);

    // Update the matrix b[i][j] with the ratios stored
    for (int i = 0; i < n - 1; ++i)
      for (int j = i + 1; j < n; ++j)
        for (int k = 0; k < n; ++k)
          b[index[j]][k]
              -= data[index[j]][i] * b[index[i]][k];

    // Perform backward substitutions
    for (int i = 0; i < n; ++i) {
      x[n - 1][i] = b[index[n - 1]][i] / data[index[n - 1]][n - 1];
      for (int j = n - 2; j >= 0; --j) {
        x[j][i] = b[index[j]][i];
        for (int k = j + 1; k < n; ++k) {
          x[j][i] -= data[index[j]][k] * x[k][i];
        }
        x[j][i] /= data[index[j]][j];
      }
    }
    return new Matrix(x);
  }

  // Method to carry out the partial-pivoting Gaussian
  // elimination.  Here index[] stores pivoting order.

  public void gaussian(double a[][], int index[]) {
    int n = index.length;
    double c[] = new double[n];

    // Initialize the index
    for (int i = 0; i < n; ++i)
      index[i] = i;

    // Find the rescaling factors, one from each row
    for (int i = 0; i < n; ++i) {
      double c1 = 0;
      for (int j = 0; j < n; ++j) {
        double c0 = Math.abs(a[i][j]);
        if (c0 > c1) c1 = c0;
      }
      c[i] = c1;
    }

    // Search the pivoting element from each column
    int k = 0;
    for (int j = 0; j < n - 1; ++j) {
      double pi1 = 0;
      for (int i = j; i < n; ++i) {
        double pi0 = Math.abs(a[index[i]][j]);
        pi0 /= c[index[i]];
        if (pi0 > pi1) {
          pi1 = pi0;
          k = i;
        }
      }

      // Interchange rows according to the pivoting order
      int itmp = index[j];
      index[j] = index[k];
      index[k] = itmp;
      for (int i = j + 1; i < n; ++i) {
        double pj = a[index[i]][j] / a[index[j]][j];

        // Record pivoting ratios below the diagonal
        a[index[i]][j] = pj;

        // Modify other elements accordingly
        for (int l = j + 1; l < n; ++l)
          a[index[i]][l] -= pj * a[index[j]][l];
      }
    }
  }


  // swap rows i and j
  private void swap(int i, int j) {
    double[] temp = data[i];
    data[i] = data[j];
    data[j] = temp;
  }

  // create and return the transpose of the invoking matrix
  public Matrix transpose() {
    Matrix A = new Matrix(N, M);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[j][i] = this.data[i][j];
    return A;
  }

  // return C = A + B
  public Matrix plus(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] + B.data[i][j];
    return C;
  }

  public Matrix plus(double B) {
    Matrix A = this;
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] + B;
    return C;
  }


  // return C = A - B
  public Matrix minus(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] - B.data[i][j];
    return C;
  }

  public Matrix divide(double B) {
    Matrix A = this;
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] / B;
    return C;
  }

  // does A = B exactly?
  public boolean eq(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        if (A.data[i][j] != B.data[i][j]) return false;
    return true;
  }

  // return C = A * B
  public Matrix times(Matrix B) {
    Matrix A = this;
    if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(A.M, B.N);
    for (int i = 0; i < C.M; i++)
      for (int j = 0; j < C.N; j++)
        for (int k = 0; k < A.N; k++)
          C.data[i][j] += (A.data[i][k] * B.data[k][j]);
    return C;
  }

  public Matrix times(double B) {
    Matrix A = this;
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] * B;
    return C;
  }


  // return x = A^-1 b, assuming A is square and has full rank
  public Matrix solve(Matrix rhs) {
    if (M != N || rhs.M != N || rhs.N != 1)
      throw new RuntimeException("Illegal matrix dimensions.");

    // create copies of the data
    Matrix A = new Matrix(this);
    Matrix b = new Matrix(rhs);

    // Gaussian elimination with partial pivoting
    for (int i = 0; i < N; i++) {

      // find pivot row and swap
      int max = i;
      for (int j = i + 1; j < N; j++)
        if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i]))
          max = j;
      A.swap(i, max);
      b.swap(i, max);

      // singular
      if (A.data[i][i] == 0.0) throw new RuntimeException("Matrix is singular.");

      // pivot within b
      for (int j = i + 1; j < N; j++)
        b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

      // pivot within A
      for (int j = i + 1; j < N; j++) {
        double m = A.data[j][i] / A.data[i][i];
        for (int k = i + 1; k < N; k++) {
          A.data[j][k] -= A.data[i][k] * m;
        }
        A.data[j][i] = 0;
      }
    }

    // back substitution
    Matrix x = new Matrix(N, 1);
    for (int j = N - 1; j >= 0; j--) {
      float t = 0;
      for (int k = j + 1; k < N; k++)
        t += A.data[j][k] * x.data[k][0];
      x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
    }
    return x;

  }

  // print matrix to standard output
  public void show() {
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < N; j++)
        System.out.printf("%9.4f ", data[i][j]);
      System.out.println();
    }
  }


  // test client
  public static void main(String[] args) {
    double[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };
    Matrix D = new Matrix(d);
    D.show();
    System.out.println();


//
    Matrix A = D.invert();
    A.show();
    System.out.println();
//
//    A.swap(1, 2);
//    A.show();
//    System.out.println();
//
//    Matrix B = A.transpose();
//    B.show();
//    System.out.println();
//
//    Matrix C = Matrix.identity(5);
//    C.show();
//    System.out.println();
//
//    A.plus(B).show();
//    System.out.println();
//
//    B.times(A).show();
//    System.out.println();
//
//    // shouldn't be equal since AB != BA in general
//    System.out.println(A.times(B).eq(B.times(A)));
//    System.out.println();
//
//    Matrix b = Matrix.random(5, 1);
//    b.show();
//    System.out.println();
//
//    Matrix x = A.solve(b);
//    x.show();
//    System.out.println();
//
//    A.times(x).show();

  }

  @Override
  public String toString() {
    return "Matrix{" +
        "M=" + M +
        ", N=" + N +
        ", data=" + Arrays.toString(data) +
        '}';
  }
}
