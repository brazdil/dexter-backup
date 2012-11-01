package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jf.dexlib.Util.AccessFlags;

import com.alee.extended.window.WebProgressDialog;
import com.alee.laf.button.WebButton;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.laf.tree.WebTree;
import com.alee.laf.tree.WebTreeCellRenderer;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexMethod;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionParsingException;
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

    val menubar = new WebMenuBar();
    {
      val menuFile = new WebMenu("File");
      menuFile.setMnemonic(KeyEvent.VK_F);
      {
        val menuFileOpen = new WebMenuItem("Open", KeyEvent.VK_O);
        menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuFileOpen.addActionListener(new ActionListener() {
          private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

          @Override
          public void actionPerformed(ActionEvent arg0) {
            if (fc.showOpenDialog(Frame) == JFileChooser.APPROVE_OPTION) {
              openFileModal(fc.getSelectedFile());
            }
          }
        } );
        menuFile.add(menuFileOpen);
      }
      menubar.add(menuFile);
    }
    Frame.setJMenuBar(menubar);
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

  private void openFileModal(final File filename) {
    // Load dialog
    val progress = new WebProgressDialog(Frame, "");
    progress.setText("Loading " + filename.getName());
    progress.setIndeterminate(true);
    progress.setShowProgressText(false);

    // Starting updater thread
    new Thread(new Runnable() {
      public void run() {
        try {
          openFile(filename);
        } catch (IOException | UnknownTypeException
        | DexInstructionParsingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        progress.setVisible(false);
      }
    }).start();

    progress.setModal(true);
    progress.setVisible(true);
  }

  private void openFile(File filename) throws IOException, UnknownTypeException, DexInstructionParsingException {
    // load the file
    val dex = new Dex(filename);

    // create split pane
    val splitPane = new WebSplitPane();
    splitPane.setDividerLocation(300);
    splitPane.setContinuousLayout (true);

    // left pane
    val leftPane = new WebPanel();
    leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
    splitPane.setLeftComponent(leftPane);

    // create list of classes
    val classTreeRoot = new DefaultMutableTreeNode(filename.getName());
    addClassesToTree(classTreeRoot, dex.getClasses());
    val classTree = new WebTree(classTreeRoot);
    classTree.setShowsRootHandles(true);
    classTree.setVisibleRowCount(4);
    classTree.setCellRenderer(new ClassTreeRenderer());
    javax.swing.ToolTipManager.sharedInstance().registerComponent(classTree);
    val classTreeScroll = new WebScrollPane(classTree);
    classTreeScroll.setMinimumSize(new Dimension(300, 200));
    leftPane.add(classTreeScroll);

    // create selection panels
    SelectedClassPanel = new ClassPanel();
    SelectedMethodPanel = new MethodPanel();

    // set selection listener
    classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    val classTree_Listener = new TreeSelectionListener() {
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
    classTree.addTreeSelectionListener(classTree_Listener);

    // left toolbar
    val leftToolbar = new WebToolBar(WebToolBar.HORIZONTAL);
    leftToolbar.setFloatable(false);
    leftToolbar.setToolbarStyle(ToolbarStyle.attached);
    val leftToolbar_Instrument = new WebButton("Instrument");
    leftToolbar_Instrument.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dex.instrument();
        classTree_Listener.valueChanged(null);
      }
    });
    leftToolbar.add(leftToolbar_Instrument);
    leftPane.add(leftToolbar);


    // create tab
    TabbedPane.addTab(filename.getName(), splitPane);
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
