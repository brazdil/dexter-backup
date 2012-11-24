package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class Dex {

  private final List<DexClass> classes;
  @Getter private DexClass_ObjectTaint class_ObjectTaint;

  @Getter private final DexParsingCache parsingCache;

  public Dex() {
    classes = new NoDuplicatesList<DexClass>();
    parsingCache = new DexParsingCache();
  }

  public Dex(File filename) throws IOException, UnknownTypeException, InstructionParsingException {
    this();

    val originalFile = new DexFile(filename);
    val dexClsInfos = originalFile.ClassDefsSection.getItems();
    for (val dexClsInfo : dexClsInfos)
      classes.add(new DexClass(this, dexClsInfo));
  }

  public List<DexClass> getClasses() {
    return Collections.unmodifiableList(classes);
  }

  public void instrument() {
    class_ObjectTaint = new DexClass_ObjectTaint(this);

    for (val cls : classes)
      cls.instrument();

    classes.add(class_ObjectTaint);
  }

  public void writeToFile(File filename) throws IOException {
    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    val asmCache = new DexAssemblingCache(outFile);
    for (val cls : classes)
      cls.writeToFile(outFile, asmCache);

    outFile.place();
    outFile.writeTo(out);

    val bytes = out.toByteArray();

    DexFile.calcSignature(bytes);
    DexFile.calcChecksum(bytes);

    val fos = new FileOutputStream(filename);
    fos.write(bytes);
    fos.close();
  }
}
