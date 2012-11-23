package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class Dex {

  @Getter private final List<DexClass> Classes;
  @Getter private DexClass_ObjectTaint ObjectTaintClass;

  @Getter private final DexParsingCache ParsingCache;

  public Dex() {
    Classes = new NoDuplicatesList<DexClass>();
    ParsingCache = new DexParsingCache();
  }

  public Dex(File filename) throws IOException, UnknownTypeException, InstructionParsingException {
    this();

    val originalFile = new DexFile(filename);
    val dexClsInfos = originalFile.ClassDefsSection.getItems();
    for (val dexClsInfo : dexClsInfos)
      Classes.add(new DexClass(this, dexClsInfo));
  }

  public void instrument() {
    ObjectTaintClass = new DexClass_ObjectTaint(this);

    for (val cls : Classes)
      cls.instrument();

    Classes.add(ObjectTaintClass);
  }

  public void writeToFile(File filename) throws IOException {
    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    val asmCache = new DexAssemblingCache(outFile);
    for (val cls : Classes)
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
