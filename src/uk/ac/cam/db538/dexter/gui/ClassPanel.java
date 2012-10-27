package uk.ac.cam.db538.dexter.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jf.dexlib.Util.AccessFlags;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexClass;

import com.alee.extended.panel.GroupPanel;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;

public class ClassPanel extends InfoPanel {

	private static final long serialVersionUID = -5520873485656895013L;
	
	private JTextField fieldName;
	private JTextField fieldSuperClass;
	private Map<AccessFlags, JCheckBox> checkboxesAccessField;
	
	public ClassPanel() {
		// create class field
		fieldName = new WebTextField();
		fieldName.setEditable(false);
		this.addRow(new WebLabel("Class:"), fieldName);
		
		// create superclass field
		fieldSuperClass = new WebTextField();
		fieldSuperClass.setEditable(false);
		this.addRow(new WebLabel("Super class:"), fieldSuperClass);

		// create access flag checkboxes
		checkboxesAccessField = new HashMap<AccessFlags, JCheckBox>();
		val group = new GroupPanel(4, false);
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		for (val flag : AccessFlags.getAccessFlagsForClass(-1)) {
			val checkbox = new WebCheckBox(flag.name());
			checkbox.setFocusable(false);
			checkboxesAccessField.put(flag, checkbox);
			group.add(checkbox);
		}
		this.addRow(new WebLabel("Access flags:"), group);
	}
	
	private ChangeListener AccessFlagCheckboxChangeListener_True = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			((JCheckBox) (e.getSource())).setSelected(true);
		}
	};
	private ChangeListener AccessFlagCheckboxChangeListener_False = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			((JCheckBox) (e.getSource())).setSelected(false);
		}
	};
	
	public void changeClass(DexClass cls) {
		this.setBreadcrumbs(cls.getType().getPrettyName());
		fieldName.setText(cls.getType().getPrettyName());
		fieldSuperClass.setText(cls.getSuperType().getPrettyName());
		
		val flagset = cls.getAccessFlagSet();
		for (val entry : checkboxesAccessField.entrySet()) {
			val flag = entry.getKey();
			val checkbox = entry.getValue();
			
			checkbox.removeChangeListener(AccessFlagCheckboxChangeListener_True);
			checkbox.removeChangeListener(AccessFlagCheckboxChangeListener_False);
			if (flagset.contains(flag)) {
				checkbox.setSelected(true);
				checkbox.addChangeListener(AccessFlagCheckboxChangeListener_True);
			} else {
				checkbox.setSelected(false);
				checkbox.addChangeListener(AccessFlagCheckboxChangeListener_False);
			}
		}
	}
}
