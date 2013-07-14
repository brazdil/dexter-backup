package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexCatch extends DexCodeElement {

  @Getter private final int id;
  @Getter private final DexClassType exceptionType;

  public DexCatch(int id, DexClassType exceptionType, RuntimeHierarchy hierarchy) {
    this.id = id;
    this.exceptionType = exceptionType;
    
    // check that it is a Throwable class
    val throwableType = DexClassType.parse("Ljava/lang/Throwable;", hierarchy.getTypeCache());
    val throwableDef = hierarchy.getClassDefinition(throwableType);
    val classDef = hierarchy.getClassDefinition(this.exceptionType);
    if (!classDef.isChildOf(throwableDef))
    	throw new IllegalArgumentException("Given class does not extend Throwable");
  }

  @Override
  public String toString() {
    return "CATCH" + Integer.toString(id);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
