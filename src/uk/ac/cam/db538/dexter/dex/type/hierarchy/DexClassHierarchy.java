package uk.ac.cam.db538.dexter.dex.type.hierarchy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  private final DexClassType rootClass;

  public DexClassHierarchy(DexClassType rootClass) {
    this.classes = new HashMap<DexClassType, ClassEntry>();
    this.rootClass = rootClass;
  }

  public void addClass(DexClassType classType, DexClassType superclassType) {
    Set<DexClassType> emptyInterfaces = Collections.emptySet();
    addMember(classType, superclassType, emptyInterfaces, false);
  }

  public void addClass(DexClassType classType, DexClassType superclassType, Set<DexClassType> interfaces) {
    addMember(classType, superclassType, interfaces, false);
  }

  public void addInterface(DexClassType classType) {
    Set<DexClassType> emptyInterfaces = Collections.emptySet();
    addMember(classType, rootClass, emptyInterfaces, true);
  }

  public void addMember(DexClassType classType, DexClassType superclassType, Set<DexClassType> interfaces, boolean flagInterface) {
    if (classes.containsKey(classType))
      throw new ClassHierarchyException("Class " + classType.getPrettyName() + " defined multiple times");

    if (classType != rootClass && wouldIntroduceLoop(classType, superclassType))
      throw new ClassHierarchyException("Class " + classType.getPrettyName() + " introduces a loop in the class hierarchy");

    if (interfaces == null)
      interfaces = new HashSet<DexClassType>();

    val classEntry = new ClassEntry(classType, superclassType, interfaces, flagInterface);
    classes.put(classType, classEntry);
  }

  public void addAllClassesFromJAR(File file, DexParsingCache cache) throws IOException {
    val jarFile = new JarFile(file);

    for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); jarEntryEnum.hasMoreElements();) {
      val jarEntry = jarEntryEnum.nextElement();

      if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {

        val jarClass = new ClassParser(jarFile.getInputStream(jarEntry), jarEntry.getName()).parse();

        val setInterfaces = new HashSet<DexClassType>();
        for (val i : jarClass.getInterfaceNames())
          setInterfaces.add(cache.getClassType(createDescriptor(i)));

        addMember(
          cache.getClassType(createDescriptor(jarClass.getClassName())),
          cache.getClassType(createDescriptor(jarClass.getSuperclassName())),
          setInterfaces,
          jarClass.isInterface()
        );
      }
    }
  }

  public DexClassType getSuperclassType(DexClassType clazz) {
    return classes.get(clazz).getSuperclassType();
  }

  public Set<DexClassType> getInterfaces(DexClassType clazz) {
    return Collections.unmodifiableSet(classes.get(clazz).getInterfaces());
  }

  public void checkConsistentency() {
    for (val entry : classes.entrySet()) {
      val clazz = entry.getKey();
      val clazzEntry = entry.getValue();
      val superclazz = clazzEntry.getSuperclassType();
      val isInterface = clazzEntry.isFlaggedInterface();
      val clazzInterfaces = clazzEntry.getInterfaces();

      // hierarchy is consistent if all classes have their parents in the hierarchy as well
      if (!classes.containsKey(superclazz))
        throw new ClassHierarchyException("Class hierarchy not consistent (" +
                                          clazz.getPrettyName() + " needs its parent " +
                                          superclazz.getPrettyName() + ")");

      if (isInterface) {
        if (superclazz != rootClass)
          throw new ClassHierarchyException("Class hierarchy not consistent (interface " +
                                            clazz.getPrettyName() + " must extend root class)");
      } else {
        for (val i : clazzInterfaces) {
          if (!classes.containsKey(i))
            throw new ClassHierarchyException("Class hierarchy not consistent (" +
                                              clazz.getPrettyName() + " needs its interface " +
                                              i.getPrettyName() + ")");
          if (!classes.get(i).isFlaggedInterface())
            throw new ClassHierarchyException("Class hierarchy not consistent (class " +
                                              clazz.getPrettyName() + " implements non-interface " +
                                              i.getPrettyName() + ")");
        }
      }
    }

    // root class can't be an interface
    if (classes.get(rootClass).isFlaggedInterface())
      throw new ClassHierarchyException("Root class cannot be an interface");

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

  public boolean implementsInterface(DexClassType clazz, DexClassType intrface) {
    // start at clazz and work our way up the hierarchy tree
    // searching through implemented interfaces at each level

    DexClassType prevClazz = null;
    do {
      if (classes.get(clazz).getInterfaces().contains(intrface))
        return true;

      prevClazz = clazz;
      clazz = getSuperclassType(clazz);
    } while (clazz != prevClazz);

    return false;
  }

  private boolean wouldIntroduceLoop(DexClassType clazz, DexClassType superclazz) {
    DexClassType currClazz = superclazz;

    do {
      if (currClazz == clazz)
        return true;

      val entryClazz = classes.get(currClazz);
      if (entryClazz == null)
        return false;
      else
        currClazz = entryClazz.getSuperclassType();
    } while (currClazz != rootClass);

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
    private final Set<DexClassType> interfaces;
    private final boolean flaggedInterface;
  }
}
