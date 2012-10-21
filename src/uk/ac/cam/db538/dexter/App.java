package uk.ac.cam.db538.dexter;

import java.io.File;
import java.io.IOException;

import uk.ac.cam.db538.dexter.dex.Dex;

import lombok.val;

public class App {
	
    public static void main(String[] args) throws IOException {
		val dex = new Dex(new File("classes.dex"));
    }
    
}
