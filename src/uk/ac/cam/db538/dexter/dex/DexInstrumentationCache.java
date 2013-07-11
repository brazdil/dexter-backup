package uk.ac.cam.db538.dexter.dex;

import lombok.Getter;


public class DexInstrumentationCache {

//  private final Dex parentFile;
//
  @Getter private final boolean insertDebugLogging;
  
  public DexInstrumentationCache(boolean debug) {
	  insertDebugLogging = debug;
  }
  
//
//  private final Map<DexField, DexField> fieldInstrumentation;
//  private final Cache<FieldDefinition, DexField>
//  staticExternalFieldInstrumentation = new Cache<FieldDefinition, DexField>() {
//    private boolean fieldExists(String name) {
//      for (DexField field : parentFile.getExternalStaticFieldTaint_Class().getFields())
//        if (field.getName().equals(name))
//          return true;
//      return false;
//    }
//
//    private String generateFieldName() {
//      long suffix = 0L;
//      String fieldName;
//      do {
//        fieldName = "$" + suffix++;
//      } while (fieldExists(fieldName));
//      return fieldName;
//    }
//
//    @Override
//    protected DexField createNewEntry(Triple<DexClassType, DexPrimitiveType, String> key) {
//      DexClass taintClass = parentFile.getExternalStaticFieldTaint_Class();
//      DexMethodWithCode taintClinit = parentFile.getExternalStaticFieldTaint_Clinit();
//
//      DexField newField = new DexField(
//        taintClass,
//        generateFieldName(),
//        DexRegisterType.parse("I", parentFile.getTypeCache()),
//        EnumSet.of(AccessFlags.PUBLIC, AccessFlags.STATIC),
//        null);
//      taintClass.addField(newField);
//
//      // add initialisation to <clinit>
//      DexCode taintClinit_Code = taintClinit.getCode();
//      DexRegister regZero = new DexRegister();
//      taintClinit_Code.insertAfter(
//        new DexCodeElement[] {
//          new DexInstruction_Const(taintClinit_Code, regZero, 0L),
//          new DexInstruction_StaticPut(taintClinit_Code, regZero, newField)
//        },
//        taintClinit_Code.getStartingLabel());
//
//      return newField;
//    }
//  };

//  @Getter private final List<InstrumentationWarning> warnings;
//
//  public DexInstrumentationCache(Dex parentFile) {
//    this(parentFile, false);
//  }
//
//  public DexInstrumentationCache(Dex parentFile, boolean debug) {
//    this.parentFile = parentFile;
//    fieldInstrumentation = new HashMap<DexField, DexField>();
//    warnings = new ArrayList<InstrumentationWarning>();
//    insertDebugLogging = debug;
//  }
//
//  public DexField getTaintField(DexField f) {
//    if (!fieldInstrumentation.containsKey(f))
//      fieldInstrumentation.put(f, f.instrument());
//    return fieldInstrumentation.get(f);
//  }
//
//  public DexField getTaintField_ExternalStatic(DexClassType clazz, DexPrimitiveType fieldType, String fieldName) {
//    return staticExternalFieldInstrumentation.getCachedEntry(
//             new Triple<DexClassType, DexPrimitiveType, String>(clazz, fieldType, fieldName));
//  }
//
//  @AllArgsConstructor
//  @Getter
//  public static class InstrumentationWarning {
//    private final String message;
//  }
}
