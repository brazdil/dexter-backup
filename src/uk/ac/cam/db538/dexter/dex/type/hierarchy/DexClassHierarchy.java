package uk.ac.cam.db538.dexter.dex.type.hierarchy;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.apache.bcel.classfile.ClassParser;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexClassHierarchy {

  private final Map<DexClassType, ClassEntry> classes;

  public DexClassHierarchy() {
    this.classes = new HashMap<DexClassType, ClassEntry>();
  }

  public void addClass(DexClassType classType, DexClassType superclassType) {
    if (classes.containsKey(classType))
      throw new ClassHierarchyException("Class " + classType.getPrettyName() + " defined multiple times");

    val classEntry = new ClassEntry(classType, superclassType);
    classes.put(classType, classEntry);
  }

  public void addAllClassesFromJAR(File file, DexParsingCache cache) throws IOException {
    val jarFile = new JarFile(file);

    for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements();) {
      val jarEntry = jarEntryEnum.nextElement();

      if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {

        val jarClass = new ClassParser(jarFile.getInputStream(jarEntry), jarEntry.getName()).parse();
        addClass(
          cache.getClassType(createDescriptor(jarClass.getClassName())),
          cache.getClassType(createDescriptor(jarClass.getSuperclassName())));
      }
    }
  }

  public DexClassType getSuperclassType(DexClassType clazz) {
    return classes.get(clazz).getSuperclassType();
  }

  public void checkConsistentency() {
    boolean foundTopObject = false;
    for (val entry : classes.entrySet()) {
      val clazz = entry.getKey();
      val superclazz = entry.getValue().getSuperclassType();

      // hierarchy is consistent if all classes have their parents in the hierarchy as well
      if (!classes.containsKey(superclazz))
        throw new ClassHierarchyException("Class hierarchy not consistent (" +
                                          clazz.getPrettyName() + " needs its parent " +
                                          superclazz.getPrettyName() + ")");

      // Object's parent is Object, there can be only one class like that
      if (clazz == superclazz) {
        if (foundTopObject)
          throw new ClassHierarchyException("Class hierarchy not consistent (cannot have multiple root classes)");
        else
          foundTopObject = true;
      }
    }
  }

  public boolean isAncestor(DexClassType clazz, DexClassType ancestor) {
    // start at clazz and work our way up the hierarchy tree
    // checking equality at each level

    DexClassType prevClazz = null;
    do {
      if (clazz == ancestor)
        return true;

      prevClazz = clazz;
      clazz = getSuperclassType(clazz);
    } while (clazz != prevClazz);

    return false;
  }

  private static String createDescriptor(String className) {
    return "L" + className.replace('.', '/') + ";";
  }

  @AllArgsConstructor
  @Getter
  private static class ClassEntry {
    private final DexClassType classType;
    private final DexClassType superclassType;
  }
}
