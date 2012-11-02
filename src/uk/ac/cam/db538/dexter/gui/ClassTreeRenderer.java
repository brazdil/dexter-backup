package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexMethod;

import com.alee.laf.tree.WebTreeCellRenderer;

/*
 * Renderer that changes icons for class tree, and the text of leaves
 */
class ClassTreeRenderer extends WebTreeCellRenderer {
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
    setText(FileTab.getDisplayName(node));

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