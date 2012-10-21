package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import com.alee.extended.filechooser.FileListModel;
import com.alee.extended.filechooser.FilesView;
import com.alee.extended.filechooser.WebFileList;
import com.alee.laf.list.WebList;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tree.WebTree;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.DockController;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lombok.val;

public class MainWindow {

	private JFrame frame;
	private DockController dockController;
	private SplitDockStation dockStation;
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("Dexter");
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		dockController = new DockController();
		dockController.setRootWindow(frame);
		dockController.setTheme(new EclipseTheme());
		
		dockStation = new SplitDockStation();
		dockController.add(dockStation);
		frame.add(dockStation);
		
		openFile(new File("classes.dex"));
	}
	
	/*
	 * Renderer that changes icons for class tree, and the text of leaves
	 */
	private static class ClassTreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final ImageIcon pkgfolderIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/packagefolder.gif"));
		private static final ImageIcon pkgIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/package.gif"));
		private static final ImageIcon clsIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/class.gif"));

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			 super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			 
			 val node = (DefaultMutableTreeNode) value;
			 if (node.isLeaf()) {
				 val cls = (DexClass) node.getUserObject();
				 setIcon(clsIcon);
				 setText(cls.getShortName());
			 } else if (node.isRoot())
				 setIcon(pkgfolderIcon);
			 else
				 setIcon(pkgIcon);
			 
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
		
		// create dockable for the file
		val dockable = new DefaultDockable();
		dockable.setTitleText(filename.getName());
		dockStation.drop(dockable);

		// create split pane
		val splitPane = new WebSplitPane();
		splitPane.setDividerLocation(300);
		dockable.add(splitPane);

		// create class tree
		val classTreeRoot = new DefaultMutableTreeNode("classes.dex");
		addClassesToTree(classTreeRoot, dex.getClasses());
		
		// create list of classes
		val classTree = new WebTree(classTreeRoot);
		// classTree.setRootVisible(false);
		classTree.setCellRenderer(new ClassTreeRenderer());
		splitPane.setLeftComponent(new WebScrollPane(classTree));
	}
	
	private static void insertNodeAlphabetically(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
		String strNewChild = newChild.toString();
		
		int insertAt = 0;
		int parentChildren = parent.getChildCount();
		while (insertAt < parentChildren) {
			if (strNewChild.compareToIgnoreCase(parent.getChildAt(insertAt).toString()) < 0)
				break;
			++insertAt;
		}
		parent.insert(newChild, insertAt);		
	}
	
	private static void addClassesToTree(DefaultMutableTreeNode root, List<DexClass> classes) {
		val pkgNodes = new HashMap<String, DefaultMutableTreeNode>();
		
		for (val cls : classes) {
			String pkgName = cls.getPackageName();
			if (pkgName == null)
				pkgName = "(default package)";
			
			DefaultMutableTreeNode pkgNode = pkgNodes.get(pkgName);
			if (pkgNode == null) {
				pkgNode = new DefaultMutableTreeNode(pkgName);
				pkgNode.setAllowsChildren(true);
				insertNodeAlphabetically(root, pkgNode);
				pkgNodes.put(pkgName, pkgNode);
			}
			
			val clsLeaf = new DefaultMutableTreeNode(cls);
			clsLeaf.setAllowsChildren(false);
			insertNodeAlphabetically(pkgNode, clsLeaf);
		}
	}

	// MAIN FUNCTION
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
