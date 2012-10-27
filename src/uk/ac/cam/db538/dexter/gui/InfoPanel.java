package uk.ac.cam.db538.dexter.gui;

import info.clearthought.layout.TableLayout;
import lombok.val;

import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbLabel;
import com.alee.laf.panel.WebPanel;

public abstract class InfoPanel extends WebPanel {

	private static final long serialVersionUID = 1106819823568775169L;
	
	private TableLayout layout;
	private WebBreadcrumb breadcrumbs;
	
	protected int getRow() {
		int row = layout.getNumRow();
		layout.insertRow(row, TableLayout.PREFERRED);
		return row;
	}
	
	protected String getLeftColumnConstraint(int row) {
		return "0," + row + ",r,t";
	}
	
	protected String getRightColumnConstraint(int row) {
		return "1," + row;
	}

	protected String getBothColumnConstraint(int row) {
		return "0," + row + ",1," + row;
	}

	public InfoPanel() {
		layout = new TableLayout(new double[][] { {TableLayout.PREFERRED, TableLayout.FILL}, {} });
		layout.setHGap(5);
		layout.setVGap(5);
		this.setLayout(layout);
		
		// create breadcrumbs
		breadcrumbs = new WebBreadcrumb(true);
		this.add(breadcrumbs, getBothColumnConstraint(getRow()));
	}
	
	protected void setBreadcrumbs(String dotSeparatedName) {
		breadcrumbs.removeAll();
		String[] fullname = dotSeparatedName.split("\\.");
		for (val partname : fullname)
			breadcrumbs.add(new WebBreadcrumbLabel(partname));
	}
}
