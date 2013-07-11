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
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.rx201.jarsigner.JarSigner;
import com.rx201.jarsigner.KeyGenerator;

import lombok.Getter;
import lombok.val;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class Apk {
	private static final String ManifestFile = "AndroidManifest.xml";
	private static final String ClassesDex = "classes.dex";
	private static final String MetaInfo = "META-INF";
	
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
	
	private static KeyGenerator keyGenerator = new KeyGenerator();
	
	public static void produceAPK(File originalFile, File destinationFile, byte[] manifestData, byte[] dexData) throws IOException {

		// originalFile ---(replacing content)--->  workingFile --(signing)--> destinationFile 
	    File workingFile = File.createTempFile("dexter-", ".apk");

	    ZipFile originalAPK = null;
	    ZipOutputStream workingAPK = null;
		try {
			byte[] buffer = new byte[16*1024];
			
			originalAPK = new ZipFile(originalFile);
		    workingAPK = new ZipOutputStream(new FileOutputStream(workingFile));

		    // Create intermediate apk with new classes.dex and AndroidManifest.xml, excluding
		    // old signature data/
			Enumeration<? extends ZipEntry> entries = originalAPK.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
		        String name = entry.getName();
		        
		        ZipEntry newEntry;
		        InputStream data = null;
		        if (name.equals(ManifestFile) && manifestData != null) {
		        	newEntry = new ZipEntry(ManifestFile);
		        	data = new ByteArrayInputStream(manifestData);
		        } else if (name.equals(ClassesDex) && dexData != null) {
		        	newEntry = new ZipEntry(ClassesDex);
		        	data = new ByteArrayInputStream(dexData);
		        } else if (name.startsWith(MetaInfo)) {
		        	newEntry = null;
		        } else {
		        	newEntry = entry;
		        	data = originalAPK.getInputStream(entry);
		        }
		        
		        if (newEntry != null) {
		        	workingAPK.putNextEntry(newEntry);
		            int len;
		            while ((len = data.read(buffer)) > 0) {
		            	workingAPK.write(buffer, 0, len);
		            }
		            workingAPK.closeEntry();
		        }
			}
			workingAPK.close();
			
			X509Certificate[] certChain = keyGenerator.getCertificateChain();
			PrivateKey privateKey = keyGenerator.getPrivateKey();
			
			JarSigner.sign(workingFile, destinationFile, "DEXTER", certChain, privateKey);
		} catch (ZipException e) {
			throw new IOException(e);
		} finally {
			if (originalAPK != null)
				originalAPK.close();
			if (workingAPK != null)
				workingAPK.close();
		}

	}
/*
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
*/
}
