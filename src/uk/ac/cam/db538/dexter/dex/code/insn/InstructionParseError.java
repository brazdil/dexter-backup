package uk.ac.cam.db538.dexter.dex.code.insn;

public class InstructionParseError extends Error {

  private static final long serialVersionUID = 1741974236499526459L;

  public InstructionParseError(String message) {
    super(message);
  }
}
