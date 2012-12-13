package uk.ac.cam.db538.dexter.gui;

import javax.swing.JTextField;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexClass;

import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;

public class ClassPanel extends InfoPanel {

  private static final long serialVersionUID = -5520873485656895013L;

  private JTextField fieldName;
  private JTextField fieldSuperClass;
  private JTextField fieldInterfaces;

  public ClassPanel() {
    // create class field
    fieldName = new WebTextField();
    fieldName.setEditable(false);
    this.addRow(new WebLabel("Class:"), fieldName);

    // create superclass field
    fieldSuperClass = new WebTextField();
    fieldSuperClass.setEditable(false);
    this.addRow(new WebLabel("Superclass:"), fieldSuperClass);

    // create interfaces field
    fieldInterfaces = new WebTextField();
    fieldInterfaces.setEditable(false);
    this.addRow(new WebLabel("Interfaces:"),fieldInterfaces);

    // create access flag checkboxes
    this.addRow(new WebLabel("Access flags:"), createAccessFlagCheckboxes(AccessFlags.getAccessFlagsForClass(-1)), true);
  }

  public void changeClass(DexClass cls) {
    this.setBreadcrumbs(cls.getType().getPrettyName());
    fieldName.setText(cls.getType().getPrettyName());
    fieldSuperClass.setText(cls.getSuperclassType().getPrettyName());

    val strInterfaces = new StringBuilder();
    boolean first = true;
    for (val i : cls.getInterfaces()) {
      if (first)
        first = false;
      else
        strInterfaces.append(", ");
      strInterfaces.append(i.getPrettyName());
    }
    fieldInterfaces.setText(strInterfaces.toString());

    this.setAccessFlagCheckboxes(cls.getAccessFlagSet());
  }
}
