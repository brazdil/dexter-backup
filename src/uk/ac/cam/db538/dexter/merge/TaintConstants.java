package uk.ac.cam.db538.dexter.merge;

public class TaintConstants {
  public static final int TAINT_SINK_OUT = 1 << 31;

  static {
    ObjectTaintStorage.set(System.out, TAINT_SINK_OUT);
  }
}
