package uk.ac.cam.db538.dexter.gui;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_SparseSwitchData;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;

import com.alee.extended.label.WebHotkeyLabel;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

public class MethodPanel extends InfoPanel {

  private static final long serialVersionUID = -5520873485656895013L;

  private JTextField fieldName;
  private JTextField fieldParentClass;
  private JTextField fieldReturnType;
  private JTextField fieldParameters;
  private JTextField fieldAnnotations;
  private JCheckBox checkboxVirtual;
  private GroupPanel panelInstructions;

  public MethodPanel() {
    // create class field
    fieldName = new WebTextField();
    fieldName.setEditable(false);
    this.addRow(new WebLabel("Method:"), fieldName);

    // create superclass field
    fieldParentClass = new WebTextField();
    fieldParentClass.setEditable(false);
    this.addRow(new WebLabel("Parent class:"), fieldParentClass);

    // create superclass field
    fieldReturnType = new WebTextField();
    fieldReturnType.setEditable(false);
    this.addRow(new WebLabel("Return type:"), fieldReturnType);

    // create superclass field
    fieldParameters = new WebTextField();
    fieldParameters.setEditable(false);
    this.addRow(new WebLabel("Parameters:"), fieldParameters);

    // create interfaces field
    fieldAnnotations = new WebTextField();
    fieldAnnotations.setEditable(false);
    this.addRow(new WebLabel("Annotations:"), fieldAnnotations);

    // create virtual checkbox
    checkboxVirtual = new WebCheckBox();
    this.addRow(new WebLabel("Declared virtual:"), checkboxVirtual);

    // create access flag checkboxes
    this.addRow(new WebLabel("Access flags:"), createAccessFlagCheckboxes(AccessFlags.getAccessFlagsForMethod(-1)), true);

    // create group panel for method code
    panelInstructions = new GroupPanel();
    panelInstructions.setLayout(new BoxLayout(panelInstructions, BoxLayout.Y_AXIS));
    this.addRow(new WebLabel("Method code:"), panelInstructions, true);
  }

  public void changeMethod(DexMethod method) {
    this.setBreadcrumbs(method.getParentClass().getType().getPrettyName() + "." + method.getName());
    fieldName.setText(method.getName());
    fieldParentClass.setText(method.getParentClass().getType().getPrettyName());
    fieldReturnType.setText(method.getPrototype().getReturnType().getPrettyName());

    boolean first = true;
    val params = new StringBuilder();
    for (val param : method.getPrototype().getParameterTypes()) {
      if (first)
        first = false;
      else
        params.append(", ");
      params.append(param.getPrettyName());
    }
    fieldParameters.setText(params.toString());

    val strAnnotations = new StringBuilder();
    first = true;
    for (val a : method.getAnnotations()) {
      if (first)
        first = false;
      else
        strAnnotations.append(", ");
      strAnnotations.append(a.getType().getPrettyName());
    }
    fieldAnnotations.setText(strAnnotations.toString());

    this.setCheckboxValueUneditable(checkboxVirtual, method.isVirtual());
    this.setAccessFlagCheckboxes(method.getAccessFlagSet());

    // put instructions
    panelInstructions.removeAll();
    if (method instanceof DexMethodWithCode) {
//    	val allInstructions = new NoDuplicatesList<DexCodeElement>();
//    	allInstructions.addAll(((DexMethodWithCode) method).getParameterMoveInstructions().getInstructionList());
//    	allInstructions.addAll(((DexMethodWithCode) method).getCode().getInstructionList());

      for (val insn : ((DexMethodWithCode) method).getCode().getInstructionList()) {
        val label = new WebHotkeyLabel(insn.getOriginalAssembly());

        // set its colour based on being original element or not
        if (!insn.isOriginalElement())
          label.setForeground(Color.GRAY);

        if (insn.isAuxiliaryElement())
          label.setForeground(Color.LIGHT_GRAY);

        // indent instructions (not labels)
        if (insn instanceof DexInstruction)
          label.setMargin(new Insets(0, 20, 0, 0));

        // for const instructions, show the hex value in tooltip
        if (insn instanceof DexInstruction_Const)
          TooltipManager.setTooltip(label, "0x" + Long.toHexString(((DexInstruction_Const) insn).getValue()), TooltipWay.trailing, 0);
        else if (insn instanceof DexInstruction_ConstWide)
          TooltipManager.setTooltip(label, "0x" + Long.toHexString(((DexInstruction_ConstWide) insn).getValue()), TooltipWay.trailing, 0);
        else if (insn instanceof DexInstruction_ConstString)
          TooltipManager.setTooltip(label, ((DexInstruction_ConstString) insn).getStringConstant(), TooltipWay.up, 0);
        else if (insn instanceof DexCatch)
          TooltipManager.setTooltip(label, ((DexCatch) insn).getExceptionType().getPrettyName(), TooltipWay.trailing, 0);
        else if (insn instanceof DexTryBlockStart) {
          val tryBlockStart = (DexTryBlockStart) insn;
          val str = new StringBuilder();
          str.append("<html>");

          val catchAllBlock = tryBlockStart.getCatchAllHandler();
          if (catchAllBlock != null) {
            str.append("ALL => " + catchAllBlock.getOriginalAbsoluteOffset());
            str.append("<br>");
          }

          for (val catchBlock : tryBlockStart.getCatchHandlers()) {
            str.append(catchBlock.getExceptionType().getPrettyName() + " => " + catchBlock.getOriginalAbsoluteOffset());
            str.append("<br>");
          }
          str.append("</html>");
          TooltipManager.setTooltip(label, str.toString(), TooltipWay.right);
        } else if (insn instanceof DexInstruction_Invoke) {
          val str = new StringBuilder();
          val prototype = ((DexInstruction_Invoke) insn).getMethodPrototype();

          first = true;
          for (val paramType : prototype.getParameterTypes()) {
            if (first)
              first = false;
            else
              str.append(", ");
            str.append(paramType.getPrettyName());
          }

          str.append(" => ");
          str.append(prototype.getReturnType().getPrettyName());

          TooltipManager.setTooltip(label, str.toString(), TooltipWay.up);
        } else if (insn instanceof DexInstruction_PackedSwitchData) {
          val insnPSD = (DexInstruction_PackedSwitchData) insn;
          val str = new StringBuilder();
          str.append("<html>");

          int key = insnPSD.getFirstKey();
          for (val target : insnPSD.getTargets()) {
            str.append(key++ + " => L" + target.getOriginalAbsoluteOffset());
            str.append("<br>");
          }
          str.append("</html>");
          TooltipManager.setTooltip(label, str.toString(), TooltipWay.right);
        } else if (insn instanceof DexInstruction_SparseSwitchData) {
          val insnSSD = (DexInstruction_SparseSwitchData) insn;
          val str = new StringBuilder();
          str.append("<html>");

          for (val keyTarget : insnSSD.getKeyTargetPairs()) {
            str.append(keyTarget.getValA() + " => L" + keyTarget.getValB().getOriginalAbsoluteOffset());
            str.append("<br>");
          }
          str.append("</html>");
          TooltipManager.setTooltip(label, str.toString(), TooltipWay.right);
        } else if (insn instanceof DexInstruction_FillArrayData) {
          val insnFAD = (DexInstruction_FillArrayData) insn;
          val str = new StringBuilder();
          str.append("<html>");

          int index = 0;
          for (val target : insnFAD.getElementData()) {
            str.append(index++ + " => [");
            int number = 0;
            first = true;
            for (int i = 0; i < target.length; ++i) {
              if (first)
                first = false;
              else
                str.append(", ");
              str.append(target[i]);
              number |= ((int) target[i]) << (8 * i);
              if (i < 3)
                number &= (1 << (8 * (i + 1))) - 1;
            }
            str.append("] => ");
            str.append(number);
            str.append(", 0x");
            str.append(Integer.toHexString(number));
            str.append("<br>");
          }
          str.append("</html>");
          TooltipManager.setTooltip(label, str.toString(), TooltipWay.right);
        }
        panelInstructions.add(label);
      }
    }
  }
}
