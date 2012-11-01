package uk.ac.cam.db538.dexter.dex.code;

import org.apache.commons.lang3.StringEscapeUtils;

import lombok.Getter;

public class DexInstruction_ConstString extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexStringConstant StringConstant;

  // CAREFUL: need to produce the Jumbo instruction if
  //          the resulting StringDataItem has more than 16-bit id

  public DexInstruction_ConstString(DexRegister to, DexStringConstant value) {
    RegTo = to;
    StringConstant = value;
  }

  @Override
  public String getOriginalAssembly() {
    String escapedVal = StringEscapeUtils.escapeJava(StringConstant.getValue());
    if (escapedVal.length() > 15)
      escapedVal = escapedVal.substring(0, 15) + "...";
    return "const-string v" + RegTo.getId() + ", \"" + escapedVal + "\"";
  }
}
