package uk.ac.cam.db538.dexter.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jf.dexlib.Util.AccessFlags;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexClass;

import com.alee.extended.breadcrumb.BreadcrumbElementType;
import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbButton;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

public class ClassPanel extends WebPanel {

	private static final long serialVersionUID = 1106819823568775169L;
	
	private TableLayout layout;
	private WebBreadcrumb breadcrumbs;
	private JTextField fieldName;
	private JTextField fieldSuperClass;
	private Map<AccessFlags, JCheckBox> checkboxesAccessField;
	
	private int getRow() {
		int row = layout.getNumRow();
		layout.insertRow(row, TableLayout.PREFERRED);
		return row;
	}
	
	private String getLeftColumnConstraint(int row) {
		return "0," + row + ",r,t";
	}
	
	private String getRightColumnConstraint(int row) {
		return "1," + row;
	}

	public ClassPanel() {
		layout = new TableLayout(new double[][] { {TableLayout.PREFERRED, TableLayout.FILL}, {} });
		layout.setHGap(5);
		layout.setVGap(5);
		this.setLayout(layout);
		
		int row;
		
		// create breadcrumbs
		row = getRow();
		breadcrumbs = new WebBreadcrumb(true);
		this.add(breadcrumbs, new TableLayoutConstraints(0, row, 1, row));
		
		// create class field
		row = getRow();
		this.add(new WebLabel("Class:"), getLeftColumnConstraint(row));
		fieldName = new WebTextField();
		fieldName.setEditable(false);
		this.add(fieldName, getRightColumnConstraint(row));
		
		// create superclass field
		row = getRow();
		this.add(new WebLabel("Super class:"), getLeftColumnConstraint(row));
		fieldSuperClass = new WebTextField();
		fieldSuperClass.setEditable(false);
		this.add(fieldSuperClass, getRightColumnConstraint(row));

		// create access flag checkboxes
		row = getRow();
		this.add(new WebLabel("Access flags:"), getLeftColumnConstraint(row));
		checkboxesAccessField = new HashMap<AccessFlags, JCheckBox>();
		val group = new GroupPanel(4, false);
		group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
		for (val flag : AccessFlags.getAccessFlagsForClass(-1)) {
			val checkbox = new WebCheckBox(flag.name());
			checkbox.setFocusable(false);
			checkboxesAccessField.put(flag, checkbox);
			group.add(checkbox);
		}
		this.add(group, getRightColumnConstraint(row));
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
		breadcrumbs.removeAll();
		String[] fullname = cls.getType().getPrettyName().split("\\.");
		for (val partname : fullname)
			breadcrumbs.add(new WebBreadcrumbButton(partname));
		
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
