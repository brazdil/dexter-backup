package uk.ac.cam.db538.dexter.merge;

import java.util.concurrent.Semaphore;

public class MethodCallHelper {

  public static final int[] ARG;
  public static int RES;

  public static final Semaphore S_ARG, S_RES;

  static {
    ARG = new int[256];
    S_ARG = new Semaphore(1);
    S_RES = new Semaphore(1);

    try {
      S_ARG.acquire();
    } catch (InterruptedException e) {
      throw new Error(e);
    }
  }

//  public static void S_ARG_acquire() {
//    try {
//      S_ARG.acquire();
//    } catch (InterruptedException e) {
//      throw new Error(e);
//    }
//  }
//
//  public static void S_RES_acquire() {
//    try {
//      S_RES.acquire();
//    } catch (InterruptedException e) {
//      throw new Error(e);
//    }
//  }
//
//  public static void main(String[] args) throws NoSuchMethodException, SecurityException, InterruptedException {
//    S_ARG.release();
//
//    int x = 5, y = 10;
//    int tx = 1, ty = 0;
//
//    S_ARG.acquire();
//    ARG[0] = tx;
//    ARG[1] = ty;
//    int z = add(x, y);
//    int tz = RES;
//    S_RES.release();
//
//    System.out.println(x + "|" + tx + " + " + y + "|" + ty + " = " + z + "|" + tz);
//
//    Object o = new Object();
//    Method m = o.getClass().getMethod("equals", Object.class, int.class);
//    if (m.getAnnotation(SafeVarargs.class) != null)
//      System.out.println("Has SafeVarargs annotation");
//  }
//
//  public static int add(int a, int b) throws InterruptedException {
//    int ta = ARG[0];
//    int tb = ARG[1];
//    S_ARG.release();
//
//    int res = a + b;
//    int tres = ta | tb;
//
//    S_RES.acquire();
//    RES = tres;
//    return res;
//  }
}
