package uk.ac.cam.db538.dexter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import uk.ac.cam.db538.dexter.dex.DexParser;
import uk.ac.cam.db538.dexter.dex.DexParsingException;

import lombok.val;

public class App {
	
    public static void main(String[] args) throws FileNotFoundException, DexParsingException {
		val dex = new FileInputStream("classes.dex");
		val parser = new DexParser(dex);
		parser.parse();
    }
    
}
