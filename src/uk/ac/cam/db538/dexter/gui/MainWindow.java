package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jf.dexlib.Util.AccessFlags;

import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.tree.WebTree;
import com.alee.laf.tree.WebTreeCellRenderer;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexMethod;
import uk.ac.cam.db538.dexter.dex.code.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

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
  private ClassPanel SelectedClassPanel;
  private MethodPanel SelectedMethodPanel;

  public MainWindow() {
    initialize();
  }

  private void initialize() {
    Frame = new JFrame("Dexter");
    Frame.setBounds(100, 100, 800, 600);
    Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    TabbedPane = new WebTabbedPane();
    Frame.add(TabbedPane);

    openFile(new File("metronome.dex"));
    openFile(new File("endomondo.dex"));
  }

  /*
   * Renderer that changes icons for class tree, and the text of leaves
   */
  private static class ClassTreeRenderer extends WebTreeCellRenderer {
    private static final long serialVersionUID = 1L;

    private static final ImageIcon pkgfolderIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/packagefolder.gif"));
    private static final ImageIcon pkgIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/package.gif"));
    private static final ImageIcon classIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/class.gif"));
    private static final ImageIcon interfaceIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/interface.gif"));
    private static final ImageIcon enumIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/enum.gif"));
    private static final ImageIcon annotationIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/annotation.gif"));
    private static final ImageIcon instanceDefaultIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_default.png"));
    private static final ImageIcon instancePublicIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_public.png"));
    private static final ImageIcon instancePrivateIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_private.png"));
    private static final ImageIcon instanceProtectedIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/instance_protected.png"));
    private static final ImageIcon staticDefaultIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_default.png"));
    private static final ImageIcon staticPublicIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_public.png"));
    private static final ImageIcon staticPrivateIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_private.png"));
    private static final ImageIcon staticProtectedIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/static_protected.png"));
    private static final ImageIcon virtualDefaultIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/virtual_default.png"));
    private static final ImageIcon virtualPublicIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/virtual_public.png"));
    private static final ImageIcon virtualPrivateIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/virtual_private.png"));
    private static final ImageIcon virtualProtectedIcon =
      new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/virtual_protected.png"));

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
        val cls = (DexClass) node.getUserObject();
        if (cls.isAnnotation())
          setIcon(annotationIcon);
        if (cls.isEnum())
          setIcon(enumIcon);
        else if (cls.isInterface())
          setIcon(interfaceIcon);
        else
          setIcon(classIcon);
        break;
      case 4:
        val obj = node.getUserObject();
        Set<AccessFlags> access = EnumSet.noneOf(AccessFlags.class);
        boolean isStatic = false;
        boolean isVirtual = false;

        if (obj instanceof DexField) {
          val field = (DexField) obj;
          access = field.getAccessFlagSet();
          isStatic = field.isStatic();
        } else if (obj instanceof DexMethod) {
          val method = (DexMethod) obj;
          access = method.getAccessFlagSet();
          isStatic = method.isStatic();
          isVirtual = method.isVirtual();
        }

        if (access.contains(AccessFlags.PUBLIC))
          setIcon(isStatic ? staticPublicIcon : isVirtual ? virtualPublicIcon : instancePublicIcon);
        else if (access.contains(AccessFlags.PROTECTED))
          setIcon(isStatic ? staticProtectedIcon : isVirtual ? virtualProtectedIcon : instanceProtectedIcon);
        else if (access.contains(AccessFlags.PRIVATE))
          setIcon(isStatic ? staticPrivateIcon : isVirtual ? virtualPrivateIcon : instancePrivateIcon);
        else
          setIcon(isStatic ? staticDefaultIcon : isVirtual ? virtualDefaultIcon : instanceDefaultIcon);
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
    } catch (UnknownTypeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    } catch (DexInstructionParsingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    val classTreeScroll = new WebScrollPane(classTree);
    classTreeScroll.setMinimumSize(new Dimension(300, 200));
    splitPane.setLeftComponent(classTreeScroll);

    // create selection panels
    SelectedClassPanel = new ClassPanel();
    SelectedMethodPanel = new MethodPanel();

    // set selection listener
    classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    classTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        val tree = (JTree) e.getSource();
        val node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
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
    });
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
