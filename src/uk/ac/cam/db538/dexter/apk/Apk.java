package uk.ac.cam.db538.dexter.apk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import lombok.Getter;
import lombok.val;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import uk.ac.cam.db538.dexter.dex.Dex;

import com.alee.utils.FileUtils;

public class Apk {

  @Getter private final Dex dexFile;
  @Getter private final ZipFile originalFile;
  @Getter private final File temporaryFilename;

  public Apk(File filename, File androidJar) throws IOException {
    this.dexFile = new Dex(filename, androidJar);
    this.temporaryFilename = File.createTempFile("dexter-", ".apk");
    FileUtils.copyFile(filename, this.temporaryFilename);
    try {
      this.originalFile = new ZipFile(this.temporaryFilename);
    }
    catch (ZipException e) {
      throw new IOException(e);
    }
  }

  public void writeToFile(File filename) throws IOException {
    // prepare the new dex file
    final byte[] newDex = dexFile.writeToFile();

    try {
      // replace classes.dex in the original file
      ZipParameters parameters = new ZipParameters();
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      parameters.setFileNameInZip("classes.dex");
      parameters.setSourceExternalStream(true);

      originalFile.removeFile("classes.dex");
      originalFile.addStream(new ByteArrayInputStream(newDex), parameters);

      // remove META-INF/*
      val metaInfFiles = new ArrayList<String>(4);
      for (val entry : originalFile.getFileHeaders()) {
        val fileName = ((FileHeader) entry).getFileName();
        if (fileName.startsWith("META-INF/"))
          metaInfFiles.add(fileName);
      }
      for (val metaInfFile : metaInfFiles)
        originalFile.removeFile(metaInfFile);
    } catch (ZipException e) {
      throw new IOException(e);
    }

    // generate a key
    val keyFile = File.createTempFile("dexter-", ".keystore");
    keyFile.delete();
    val keyToolPB = new ProcessBuilder("keytool", 
    		"-genkey", 
    		"-keystore", keyFile.getAbsolutePath(),
    	    "-storepass", "dexter", 
    	    "-alias", "DexterKey", 
    	    "-keypass", "dexter", 
    	    "-keyalg", "RSA", 
    	    "-keysize", "2048",
    	    "-dname", "CN=Android Debug,O=Android,C=US", 
    	    "-validity", "10000");
    keyToolPB.redirectErrorStream(true);
    val keyToolProcess = keyToolPB.start();
    
	val keyToolExecOutputReader = new BufferedReader(new InputStreamReader(keyToolProcess.getInputStream()));
	String keyToolExecOutputLine;
	while ((keyToolExecOutputLine = keyToolExecOutputReader.readLine()) != null)
		System.err.println(keyToolExecOutputLine);
	
    int keyToolExecResult;
	try {
		keyToolExecResult = keyToolProcess.waitFor();
	} catch (InterruptedException e) {
		keyToolExecResult = -99;
	}
    if (keyToolExecResult != 0) {
    	throw new IOException("Keytool execution failed (code " + keyToolExecResult + ")");
    }

    // sign the file
    val jarsignerPB = new ProcessBuilder("jarsigner",
    		"-keystore", keyFile.getAbsolutePath(),
    	    "-storepass", "dexter", 
    	    "-keypass", "dexter", 
    	    "-sigalg", "MD5withRSA", 
    	    "-digestalg", "SHA1",
    	    temporaryFilename.getAbsolutePath(), 
    	    "DexterKey"); 
      jarsignerPB.redirectErrorStream(true);
    val jarsignerProcess = jarsignerPB.start();
    
	val jarsignerExecOutputReader = new BufferedReader(new InputStreamReader(jarsignerProcess.getInputStream()));
	String jarsignerExecOutputLine;
	while ((jarsignerExecOutputLine = jarsignerExecOutputReader.readLine()) != null)
		System.err.println(jarsignerExecOutputLine);
	
    int jarsignerExecResult;
	try {
		jarsignerExecResult = jarsignerProcess.waitFor();
	} catch (InterruptedException e) {
		jarsignerExecResult = -99;
	}
    if (jarsignerExecResult != 0) {
    	throw new IOException("Jarsigner execution failed (code " + jarsignerExecResult + ")");
    }
    
    // copy the temp file to the given location
    FileUtils.copyFile(temporaryFilename, filename);
  }
}
