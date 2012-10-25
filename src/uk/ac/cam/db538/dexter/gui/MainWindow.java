package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jf.dexlib.Util.AccessFlags;

import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.tree.WebTree;
import com.alee.laf.tree.WebTreeCellRenderer;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexMethod;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.DockController;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import lombok.val;

public class MainWindow {

	private JFrame Frame;
	private JTabbedPane TabbedPane;  
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		Frame = new JFrame("Dexter");
		Frame.setBounds(100, 100, 800, 600);
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TabbedPane = new WebTabbedPane();
		Frame.add(TabbedPane);
		
		openFile(new File("classes.dex"));
	}
	
	/*
	 * Renderer that changes icons for class tree, and the text of leaves
	 */
	private static class ClassTreeRenderer extends WebTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final ImageIcon pkgfolderIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/packagefolder.gif"));
		private static final ImageIcon pkgIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/package.gif"));
		private static final ImageIcon clsIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/class.gif"));
		private static final ImageIcon instanceDefaultIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_default.png"));
		private static final ImageIcon instancePublicIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_public.png"));
		private static final ImageIcon instancePrivateIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_private.png"));
		private static final ImageIcon instanceProtectedIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_protected.png"));
		private static final ImageIcon staticDefaultIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_default.png"));
		private static final ImageIcon staticPublicIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_public.png"));
		private static final ImageIcon staticPrivateIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_private.png"));
		private static final ImageIcon staticProtectedIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_protected.png"));

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			 super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			 
			 val node = (DefaultMutableTreeNode) value;
			 setText(getDisplayName(node));
			 
			 switch (node.getLevel()) {
			 case 0:
				 setIcon(pkgfolderIcon);
				 break;
			 case 1:
				 setIcon(pkgIcon);
				 break;
			 case 2:
				 setIcon(clsIcon);
				 break;
			 case 4:
				 val obj = node.getUserObject();
				 Set<AccessFlags> access = EnumSet.noneOf(AccessFlags.class);
				 boolean isStatic = false;
				 
				 if (obj instanceof DexField) {
					 val field = (DexField) obj;
					 access = field.getAccessFlagSet();
					 isStatic = field.isStatic();
				 } else if (obj instanceof DexMethod) {
					 val method = (DexMethod) obj;
					 access = method.getAccessFlagSet();
					 isStatic = method.isStatic();
				 }

				 if (access.contains(AccessFlags.PUBLIC))
					 setIcon(isStatic ? staticPublicIcon : instancePublicIcon);
				 else if (access.contains(AccessFlags.PROTECTED))
					 setIcon(isStatic ? staticProtectedIcon : instanceProtectedIcon);
				 else if (access.contains(AccessFlags.PRIVATE))
					 setIcon(isStatic ? staticPrivateIcon : instancePrivateIcon);
				 else
					 setIcon(isStatic ? staticDefaultIcon : instanceDefaultIcon);
				 
				 TooltipManager.setTooltip(this, "You have waited 500ms to see this", TooltipWay.down);
			 }
			 
			 return this;
		}
	}
	
	private void openFile(File filename) {
		// load the file
		Dex dex;
		try {
			dex = new Dex(filename);
		} catch (IOException ex) {
			// TODO: handle
			return;
		}
		
		// create split pane
		val splitPane = new WebSplitPane();
		splitPane.setDividerLocation(300);
		splitPane.setContinuousLayout (true);
		
		// create tab
		TabbedPane.addTab(filename.getName(), splitPane);

		// create class tree
		val classTreeRoot = new DefaultMutableTreeNode("classes.dex");
		addClassesToTree(classTreeRoot, dex.getClasses());
		
		// create list of classes
		val classTree = new WebTree(classTreeRoot);
		classTree.setShowsRootHandles(true);
		classTree.setVisibleRowCount(4);
		classTree.setCellRenderer(new ClassTreeRenderer());
		javax.swing.ToolTipManager.sharedInstance().registerComponent(classTree);
		splitPane.setLeftComponent(new WebScrollPane(classTree));
		
		// create right panel
		val detailsPane = new WebTabbedPane() {
			private static final long serialVersionUID = 5866957665552637833L;

			{
				this.setTabPlacement(WebTabbedPane.BOTTOM);
				
				this.addTab("Methods", new JPanel());
			}			
		};
		splitPane.setRightComponent(detailsPane);
		
	}
	
	private static String getDisplayName(DefaultMutableTreeNode node) {
		Object obj = node.getUserObject();
		if (obj instanceof DexClass)
			return ((DexClass) obj).getType().getShortName();
		else if (obj instanceof DexField) {
			val f = (DexField) obj;
			return f.getName() + " : " + f.getType().getPrettyName();
		} else if (obj instanceof DexMethod) {
				val f = (DexMethod) obj;
				
				val str = new StringBuilder();
				str.append(f.getName());
				str.append("(");
				
				boolean first = true;
				for (val type : f.getParameterTypes()) {
					if (first)
						first = false;
					else
						str.append(", ");
					str.append(type.getPrettyName());
				}
				
				str.append(") : ");
				str.append(f.getReturnType().getPrettyName());
				
				return str.toString();
		} else
			return obj.toString();
	}
	
	private static void insertNodeAlphabetically(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
		String strNewChild = getDisplayName(newChild);
		
		int insertAt = 0;
		int parentChildren = parent.getChildCount();
		while (insertAt < parentChildren) {
			String strParentsChild = getDisplayName((DefaultMutableTreeNode) parent.getChildAt(insertAt));
			if (strNewChild.compareToIgnoreCase(strParentsChild) < 0)
				break;
			++insertAt;
		}
		parent.insert(newChild, insertAt);		
	}
	
	private static void addClassesToTree(DefaultMutableTreeNode root, List<DexClass> classes) {
		val pkgNodes = new HashMap<String, DefaultMutableTreeNode>();
		
		for (val cls : classes) {
			String pkgName = cls.getType().getPackageName();
			if (pkgName == null)
				pkgName = "(default package)";
			
			DefaultMutableTreeNode pkgNode = pkgNodes.get(pkgName);
			if (pkgNode == null) {
				pkgNode = new DefaultMutableTreeNode(pkgName);
				insertNodeAlphabetically(root, pkgNode);
				pkgNodes.put(pkgName, pkgNode);
			}
			
			val clsNode = new DefaultMutableTreeNode(cls);
			insertNodeAlphabetically(pkgNode, clsNode);
			
			if (!cls.getFields().isEmpty()) {
				val fieldsNode = new DefaultMutableTreeNode(StrFields);
				insertNodeAlphabetically(clsNode, fieldsNode);
				for (val field : cls.getFields()) {
					val fieldNode = new DefaultMutableTreeNode(field);
					insertNodeAlphabetically(fieldsNode, fieldNode);
				}
			}

			if (!cls.getMethods().isEmpty()) {
				val methodsNode = new DefaultMutableTreeNode(StrMethods);
				insertNodeAlphabetically(clsNode, methodsNode);
				for (val method : cls.getMethods()) {
					val methodNode = new DefaultMutableTreeNode(method);
					insertNodeAlphabetically(methodsNode, methodNode);
				}
			}
		}
	}
	
	private static final String StrFields = "Fields";
	private static final String StrMethods = "Methods";

	// MAIN FUNCTION
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.Frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
