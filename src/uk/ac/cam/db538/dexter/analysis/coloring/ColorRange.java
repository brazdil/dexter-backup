package uk.ac.cam.db538.dexter.analysis.coloring;

public enum ColorRange {
  Range_0_15,
  Range_0_255,
  Range_0_65535;

  public boolean isInRange(int i) {
    switch (this) {
    case Range_0_15:
      return i >= 0 && i <= 15;
    case Range_0_255:
      return i >= 0 && i <= 255;
    default:
    case Range_0_65535:
      return i >= 0 && i <= 65535;
    }
  }
}