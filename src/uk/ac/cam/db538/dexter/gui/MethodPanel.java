package uk.ac.cam.db538.dexter.gui;

import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.jf.dexlib.Util.AccessFlags;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
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

    this.setCheckboxValueUneditable(checkboxVirtual, method.isVirtual());
    this.setAccessFlagCheckboxes(method.getAccessFlagSet());

    // put instructions
    panelInstructions.removeAll();
    if (method instanceof DexMethodWithCode) {
      for (val insn : ((DexMethodWithCode) method).getCode().getInstructionList()) {
        val label = new WebHotkeyLabel(insn.getOriginalAssembly());

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

        panelInstructions.add(label);
      }
    }
  }
}
