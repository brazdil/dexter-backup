package uk.ac.cam.db538.dexter.gui;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.jf.dexlib.Util.AccessFlags;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexMethod;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;

public class MethodPanel extends InfoPanel {

  private static final long serialVersionUID = -5520873485656895013L;

  private JTextField fieldName;
  private JTextField fieldParentClass;
  private JTextField fieldReturnType;
  private JTextField fieldParameters;
  private JCheckBox checkboxVirtual;

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
  }

  public void changeMethod(DexMethod method) {
    this.setBreadcrumbs(method.getParentClass().getType().getPrettyName() + "." + method.getName());
    fieldName.setText(method.getName());
    fieldParentClass.setText(method.getParentClass().getType().getPrettyName());
    fieldReturnType.setText(method.getReturnType().getPrettyName());

    boolean first = true;
    val params = new StringBuilder();
    for (val param : method.getParameterTypes()) {
      if (first)
        first = false;
      else
        params.append(", ");
      params.append(param.getPrettyName());
    }
    fieldParameters.setText(params.toString());

    this.setCheckboxValueUneditable(checkboxVirtual, !method.isDirect());
    this.setAccessFlagCheckboxes(method.getAccessFlagSet());
  }
}
