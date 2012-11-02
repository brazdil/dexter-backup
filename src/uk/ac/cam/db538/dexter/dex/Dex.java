package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class Dex {

  @Getter private final DexFile OriginalFile;
  @Getter private final File Filename;

  @Getter private final List<DexClass> Classes;
  @Getter private final DexParsingCache ParsingCache;

  public Dex(File filename) throws IOException, UnknownTypeException, DexInstructionParsingException {
    OriginalFile = new DexFile(filename);
    Filename = filename;

    ParsingCache = new DexParsingCache();

    Classes = new LinkedList<DexClass>();
    val dexClsInfos = OriginalFile.ClassDefsSection.getItems();
    for (val dexClsInfo : dexClsInfos)
      Classes.add(new DexClass(this, dexClsInfo));
  }

  public void instrument() {
    for (val cls : Classes)
      cls.instrument();
  }

  public void writeToFile(File filename) throws IOException {
    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    for (val cls : Classes)
      cls.writeToFile(outFile);

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
