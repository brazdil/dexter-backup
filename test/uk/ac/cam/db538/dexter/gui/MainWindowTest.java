package uk.ac.cam.db538.dexter.gui;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexClass;

public class MainWindowTest {

	private static void execAddClassesToTree(DefaultMutableTreeNode root, List<DexClass> classes) {
		try {
			Method m = MainWindow.class.getDeclaredMethod("addClassesToTree", DefaultMutableTreeNode.class, List.class);
			m.setAccessible(true);
			m.invoke(null, root, classes);
		} catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
			fail("Couldn't execute method: " + e.getClass().getSimpleName());
		}
	}
	
	@Test
	public void testAddClassesToTree_EmptyList() {
		val root = new DefaultMutableTreeNode("root");
		val classes = new LinkedList<DexClass>();
		execAddClassesToTree(root, classes);
		assertEquals(0, root.getChildCount());
	}

	@Test
	public void testAddClassesToTree_TwoPackages() {
		val root = new DefaultMutableTreeNode("root");
		val classes = new LinkedList<DexClass>();
		
		val cls11 = new DexClass("Lcom.example1.a;");
		val cls12 = new DexClass("Lcom.example1.b;");
		val cls21 = new DexClass("Lcom.example2.a;");
		
		classes.add(cls11);
		classes.add(cls12);
		classes.add(cls21);
		execAddClassesToTree(root, classes);
		
		assertEquals(2, root.getChildCount());
		
		val child1 = (DefaultMutableTreeNode) root.getChildAt(0);
		assertEquals("com.example1", (String) child1.getUserObject());
		assertEquals(false, child1.isLeaf());
		assertEquals(2, child1.getChildCount());
		{
			val child11 = (DefaultMutableTreeNode) child1.getChildAt(0);
			assertEquals(cls11, child11.getUserObject());
			assertEquals(true, child11.isLeaf());
	
			val child12 = (DefaultMutableTreeNode) child1.getChildAt(1);
			assertEquals(cls12, child12.getUserObject());
			assertEquals(true, child12.isLeaf());
		}

		val child2 = (DefaultMutableTreeNode) root.getChildAt(1);
		assertEquals("com.example2", (String) child2.getUserObject());
		assertEquals(false, child2.isLeaf());
		assertEquals(1, child2.getChildCount());
		{
			val child21 = (DefaultMutableTreeNode) child2.getChildAt(0);
			assertEquals(cls21, child21.getUserObject());
			assertEquals(true, child21.isLeaf());
		}
	}
	
	@Test
	public void testAddClassesToTree_DefaultPackage() {
		val root = new DefaultMutableTreeNode("root");
		val classes = new LinkedList<DexClass>();

		val cls = new DexClass("LTestClass;");
		classes.add(cls);

		execAddClassesToTree(root, classes);
		
		assertEquals(1, root.getChildCount());
		
		val child = (DefaultMutableTreeNode) root.getChildAt(0);
		assertEquals("(default package)", (String) child.getUserObject());
		assertEquals(false, child.isLeaf());
		assertEquals(1, child.getChildCount());
		
		val leaf = (DefaultMutableTreeNode) child.getChildAt(0);
		assertEquals(true, leaf.isLeaf());
		assertEquals(cls, leaf.getUserObject());
	}
}

