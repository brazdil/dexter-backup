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
import uk.ac.cam.db538.dexter.manifest.BinXmlUtil;

public class Apk {
	private static final String ManifestFile = "AndroidManifest.xml";
	private static final String ClassesDex = "classes.dex";
	private static final String MetaInfo = "META-INF";
	
	/**
	 * Read application class in the APK's manifest file
	 * @param apkFile Input APK file
	 * @return The fully qualified application class name if exists, null otherwise
	 * @throws IOException
	 */
	public static String getApplicationClass(File apkFile)
			throws IOException {
		ZipFile apk = null;
		try {
			apk = new ZipFile(apkFile);
			Enumeration<? extends ZipEntry> entries = apk.entries();

			while (entries.hasMoreElements()) {

				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.getName().equals(ManifestFile)) {
					return BinXmlUtil.getApplicationClass(apk.getInputStream(entry));
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
	
	public static void produceAPK(File originalFile, File destinationFile, String newApplicationClass, byte[] dexData) throws IOException {

		// originalFile ---(replacing content)--->  workingFile --(signing)--> destinationFile 
	    File workingFile = File.createTempFile("dexter-", ".apk");

	    ZipFile originalAPK = null;
	    ZipOutputStream workingAPK = null;
		try {
			byte[] buffer = new byte[16*1024];
			
			originalAPK = new ZipFile(originalFile);
		    workingAPK = new ZipOutputStream(new FileOutputStream(workingFile));

		    // Create intermediate apk with new classes.dex and AndroidManifest.xml, excluding
		    // old signature data
			Enumeration<? extends ZipEntry> entries = originalAPK.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
		        String name = entry.getName();
		        
		        ZipEntry newEntry = new ZipEntry(name);
		        InputStream data = null;
		        if (name.equals(ManifestFile) && newApplicationClass != null) {
		        	data = modifyManifest(originalAPK.getInputStream(entry), newApplicationClass);
		        	
		        } else if (name.equals(ClassesDex) && dexData != null) {
		        	data = new ByteArrayInputStream(dexData);
		        	
		        } else if (name.startsWith(MetaInfo)) {
		        	newEntry = null;
		        	
		        } else {
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
			workingAPK = null;
			originalAPK.close();
			originalAPK = null;
			
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
	
	private static InputStream modifyManifest(InputStream inputStream, String newApplicationClass) throws IOException {
			byte[] modified = BinXmlUtil.setApplicationClass(inputStream, newApplicationClass);
			return new ByteArrayInputStream(modified);
	}
}
