package org.jf.dexlib;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jf.dexlib.Util.ByteArrayInput;
import org.jf.dexlib.Util.Input;

public class DexFromMemory extends DexFile {

	/*
	 * Constructor for Smali's DexFile that can load DEX from a byte array
	 * Can only load DEX files, not ODEX
	 */
	public DexFromMemory(byte[] data) {
		Input in = new ByteArrayInput(data);
		  
		/*
		 * !!! Following code taken from Smali source (DexFile.java) !!! 
		 */
		
        ReadContext readContext = new ReadContext();

        HeaderItem.readFrom(in, 0, readContext);

        //the map offset was set while reading in the header item
        int mapOffset = readContext.getSectionOffset(ItemType.TYPE_MAP_LIST);

        in.setCursor(mapOffset);
        MapItem.readFrom(in, 0, readContext);

        //the sections are ordered in such a way that the item types
        Section<?> sections[] = new Section[] {
                StringDataSection,
                StringIdsSection,
                TypeIdsSection,
                TypeListsSection,
                ProtoIdsSection,
                FieldIdsSection,
                MethodIdsSection,
                AnnotationsSection,
                AnnotationSetsSection,
                AnnotationSetRefListsSection,
                AnnotationDirectoriesSection,
                DebugInfoItemsSection,
                CodeItemsSection,
                ClassDataSection,
                EncodedArraysSection,
                ClassDefsSection
        };

        for (Section<?> section: sections) {
            int sectionOffset = readContext.getSectionOffset(section.ItemType);
            if (sectionOffset > 0) {
                int sectionSize = readContext.getSectionSize(section.ItemType);
                in.setCursor(sectionOffset);
                section.readFrom(sectionSize, in, readContext);
            }
        }
	}
	
	public DexFromMemory(InputStream in) throws IOException {
		this(IOUtils.toByteArray(in));
	}
}
