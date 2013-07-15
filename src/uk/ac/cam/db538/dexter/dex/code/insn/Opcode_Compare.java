package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_Compare {
  CmplFloat("cmpl-float"),
  CmpgFloat("cmpg-float"),
  CmplDouble("cmpl-double"),
  CmpgDouble("cmpg-double"),
  CmpLong("cmp-long");

  @Getter private final String AsmName;

  private Opcode_Compare(String assemblyName) {
    AsmName = assemblyName;
  }
}