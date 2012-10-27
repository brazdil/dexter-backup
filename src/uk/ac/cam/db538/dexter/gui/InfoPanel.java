package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jf.dexlib.Util.AccessFlags;

import info.clearthought.layout.TableLayout;
import lombok.val;

import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbLabel;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.panel.WebPanel;

public abstract class InfoPanel extends WebPanel {

	private static final long serialVersionUID = 1106819823568775169L;
	
	private TableLayout layout;
	private WebBreadcrumb breadcrumbs;
	private Map<AccessFlags, JCheckBox> checkboxesAccessField;

	protected int getRow() {
		int row = layout.getNumRow();
		layout.insertRow(row, TableLayout.PREFERRED);
		return row;
	}
	
	protected String getLeftColumnConstraint(int row, boolean top) {
		return "0," + row + ",r" + (top ? ",t" : ",c");
	}
	
	protected String getRightColumnConstraint(int row) {
		return "1," + row;
	}

	protected String getBothColumnConstraint(int row) {
		return "0," + row + ",1," + row;
	}
	
	protected void addRow(Component left, Component right, boolean topLeft) {
		int row = getRow();
		this.add(left, getLeftColumnConstraint(row, topLeft));
		this.add(right, getRightColumnConstraint(row));
	}
	
	protected void addRow(Component left, Component right) {
		addRow(left, right, false);
	}

	protected void addRow(Component both) {
		this.add(both, getBothColumnConstraint(getRow()));
	}
	
	public InfoPanel() {
		layout = new TableLayout(new double[][] { {TableLayout.PREFERRED, TableLayout.FILL}, {} });
		layout.setHGap(5);
		layout.setVGap(5);
		this.setLayout(layout);
		
		// create breadcrumbs
		breadcrumbs = new WebBreadcrumb(true);
		this.addRow(breadcrumbs);
	}
	
	protected void setBreadcrumbs(String dotSeparatedName) {
		breadcrumbs.removeAll();
		String[] fullname = dotSeparatedName.split("\\.");
		for (val partname : fullname)
			breadcrumbs.add(new WebBreadcrumbLabel(partname));
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
	
	protected void setCheckboxValueUneditable(JCheckBox checkbox, boolean value) {
		checkbox.removeChangeListener(AccessFlagCheckboxChangeListener_True);
		checkbox.removeChangeListener(AccessFlagCheckboxChangeListener_False);
		checkbox.setSelected(value);
		checkbox.setFocusable(false);
		if (value)
			checkbox.addChangeListener(AccessFlagCheckboxChangeListener_True);
		else
			checkbox.addChangeListener(AccessFlagCheckboxChangeListener_False);
	}
	
	protected void setAccessFlagCheckboxes(Set<AccessFlags> flagset) {
		for (val entry : checkboxesAccessField.entrySet()) {
			val flag = entry.getKey();
			val checkbox = entry.getValue();
			setCheckboxValueUneditable(checkbox, flagset.contains(flag));
		}
	}

	protected GroupPanel createAccessFlagCheckboxes(AccessFlags[] flagList) {
		checkboxesAccessField = new HashMap<AccessFlags, JCheckBox>();
		val checkboxPanel = new GroupPanel();
		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
		for (val flag : flagList) {
			val checkbox = new WebCheckBox(flag.name());
			checkboxesAccessField.put(flag, checkbox);
			checkboxPanel.add(checkbox);
		}
		return checkboxPanel;
	}
}
