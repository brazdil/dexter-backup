package uk.ac.cam.db538.dexter.apk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class Apk {
	private static final String ManifestFile = "AndroidManifest.xml";
	private static final String ClassesDex = "classes.dex";
	
	public static InputStream readManifestFromAPK(File apkFile)
			throws IOException {
		ZipFile apk = null;
		try {
			apk = new ZipFile(apkFile);
			Enumeration<? extends ZipEntry> entries = apk.entries();

			while (entries.hasMoreElements()) {

				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.getName().equals(ManifestFile)) {
					return apk.getInputStream(entry);
				}
			}
		} catch (ZipException e) {
			throw new IOException(e);
		} finally {
			if (apk != null)
				apk.close();
		}

		return null;
	}
	
	public static void produceAPK(File originalFile, File destinationFile, byte[] manifestData, byte[] dexData) {
		
	}

  public void writeToFile(File filename) throws IOException {
    // prepare the new dex file
    System.out.println("Generating new DEX");
    final byte[] newDex = dexFile.writeToFile();

    val fileCopy = File.createTempFile("dexter-", ".apk");
    copyFile(this.temporaryFilename, fileCopy);
    ZipFile originalFile;
    try {
      originalFile = new ZipFile(fileCopy);
    }
    catch (ZipException e) {
      throw new IOException(e);
    }

    System.out.println("Replacing DEX");
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

    System.out.println("Generating a signature");
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

    System.out.println("Signing the APK");
    // sign the file
    val jarsignerPB = new ProcessBuilder("jarsigner",
                                         "-keystore", keyFile.getAbsolutePath(),
                                         "-storepass", "dexter",
                                         "-keypass", "dexter",
                                         "-sigalg", "MD5withRSA",
                                         "-digestalg", "SHA1",
                                         fileCopy.getAbsolutePath(),
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

    System.out.println("Saving APK to given file");
    // copy the temp file to the given location
    System.out.println("fileCopy = " + fileCopy.getAbsolutePath());
    System.out.println("filename = " + filename.getAbsolutePath());
    copyFile(fileCopy, filename);
  }
  
  private static void copyFile(File src, File dest) throws IOException {
	  InputStream in = new FileInputStream(src);
	  OutputStream out = new FileOutputStream(dest);
	  byte[] buf = new byte[1024];
	  int len;
	  while ((len = in.read(buf)) > 0) {
	     out.write(buf, 0, len);
	  }
	  in.close();
	  out.close(); 	  
  }
}
