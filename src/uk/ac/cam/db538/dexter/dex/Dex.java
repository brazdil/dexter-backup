package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.dex.code.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class Dex {

  @Getter private DexFile OriginalFile;

  @Getter private List<DexClass> Classes;
  @Getter private DexParsingCache ParsingCache;

  public Dex(File filename) throws IOException, UnknownTypeException, DexInstructionParsingException {
    OriginalFile = new DexFile(filename);

    ParsingCache = new DexParsingCache();

    Classes = new LinkedList<DexClass>();
    val dexClsInfos = OriginalFile.ClassDefsSection.getItems();
    for (val dexClsInfo : dexClsInfos)
      Classes.add(new DexClass(this, dexClsInfo));
  }

}
