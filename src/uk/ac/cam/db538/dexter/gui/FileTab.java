package uk.ac.cam.db538.dexter.gui;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;


import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;

import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tree.WebTree;

public class FileTab extends WebSplitPane {
  private static final long serialVersionUID = -2012258240775133554L;

  @Getter private final Dex OpenedFile;

  private final ClassPanel SelectedClassPanel;
  private final MethodPanel SelectedMethodPanel;

  @Getter private final TreeSelectionListener TreeListener;

  public FileTab(Dex file) {
    super();
    OpenedFile = file;

    val splitPane = this;

    splitPane.setDividerLocation(300);
    splitPane.setContinuousLayout (true);

    // create selection panels
    SelectedClassPanel = new ClassPanel();
    SelectedMethodPanel = new MethodPanel();

    // create list of classes
    val classTreeRoot = new DefaultMutableTreeNode(OpenedFile.getFilename().getName());
    addClassesToTree(classTreeRoot, OpenedFile.getClasses());
    val classTree = new WebTree(classTreeRoot);
    classTree.setShowsRootHandles(true);
    classTree.setVisibleRowCount(4);
    classTree.setCellRenderer(new ClassTreeRenderer());
    javax.swing.ToolTipManager.sharedInstance().registerComponent(classTree);
    val classTreeScroll = new WebScrollPane(classTree);
    classTreeScroll.setMinimumSize(new Dimension(300, 200));
    splitPane.setLeftComponent(classTreeScroll);

    // set selection listener
    classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    TreeListener = new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        val node = (DefaultMutableTreeNode) classTree.getLastSelectedPathComponent();
        if (node != null) {
          val obj = node.getUserObject();

          if (obj instanceof DexClass) {
            splitPane.setRightComponent(SelectedClassPanel);
            SelectedClassPanel.changeClass((DexClass) obj);
          } else if (obj instanceof DexMethod) {
            splitPane.setRightComponent(SelectedMethodPanel);
            SelectedMethodPanel.changeMethod((DexMethod) obj);
          }
        }
      }
    };
    classTree.addTreeSelectionListener(TreeListener);
  }

  static String getDisplayName(DefaultMutableTreeNode node) {
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
      for (val type : f.getArgumentTypes()) {
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

}
