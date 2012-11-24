package uk.ac.cam.db538.dexter.analysis.coloring;

public enum ColorRange {
  RANGE_4BIT,
  RANGE_8BIT,
  RANGE_16BIT;

  public boolean isInRange(int i) {
    switch (this) {
    case RANGE_4BIT:
      return i >= 0 && i <= 15;
    case RANGE_8BIT:
      return i >= 0 && i <= 255;
    default:
    case RANGE_16BIT:
      return i >= 0 && i <= 65535;
    }
  }
}