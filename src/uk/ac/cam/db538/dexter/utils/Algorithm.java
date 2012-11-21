package uk.ac.cam.db538.dexter.utils;

import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class Algorithm {

  public static int countParamWords(List<DexRegisterType> paramTypes, boolean isStatic) {
    int totalWords = 0;
    if (!isStatic)
      totalWords += DexClassType.TypeSize.getRegisterCount();
    for (val param : paramTypes)
      totalWords += param.getRegisters();
    return totalWords;
  }

}
