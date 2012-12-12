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
  private final DexClassType typeObject;

  public DexClassHierarchy(DexClassType typeObject) {
    this.classes = new HashMap<DexClassType, ClassEntry>();
    this.typeObject = typeObject;
  }

  public void addClass(DexClassType classType, DexClassType superclassType) {
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

  public boolean isConsistent() {
    // hierarchy is consistent if all classes have their parents in the hierarchy as well
    // (Object's parent is Object)
    for (val entry : classes.entrySet())
      if (!classes.containsKey(entry.getValue().getSuperclassType()))
        return false;
    return true;
  }

  public boolean isAncestor(DexClassType clazz, DexClassType ancestor) {
    // start at clazz and work our way up the hierarchy tree
    // checking equality at each level

    // the loop condition won't allow clazz to be tested against
    // Object; that's why it is tested here
    if (ancestor == typeObject)
      return true;

    do {
      if (clazz == ancestor)
        return true;

      clazz = getSuperclassType(clazz);
    } while (clazz != typeObject);

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
