package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;

public class DexMacro_GetServiceTaint extends DexMacro {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regServiceName;

  public DexMacro_GetServiceTaint(DexCode methodCode, DexRegister regTo, DexRegister regServiceName) {
    super(methodCode);
    this.regTo = regTo;
    this.regServiceName = regServiceName;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();

    val methodQueryTaint = dex.getTaintConstants_ServiceTaint();

    return Arrays.asList(new DexCodeElement[] {
                           // regTo = TaintConstants.managerTaint(regManagerName)
                           new DexInstruction_Invoke(code, methodQueryTaint, Arrays.asList(new DexRegister[] { regServiceName })),
                           new DexInstruction_MoveResult(code, regTo, false),
                           // print debug info
                           new DexMacro_PrintStringConst(code, "$ system service request: ", false),
                           new DexMacro_PrintString(code, regServiceName, false),
                           new DexMacro_PrintStringConst(code, " => ", false),
                           new DexMacro_PrintInteger(code, regTo, true)
                         });
  }
}
