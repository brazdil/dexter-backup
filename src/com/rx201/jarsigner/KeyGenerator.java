package com.rx201.jarsigner;

import java.io.IOException;
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
import java.util.Date;

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

public class KeyGenerator {
	
	private X509Certificate[] certChain;
	private PrivateKey privateKey;
	
	public X509Certificate[] getCertificateChain() {
		if (certChain == null)
			generateSigningKey();
		
		return certChain;
	}
	
	public PrivateKey getPrivateKey() {
		if (certChain == null)
			generateSigningKey();
		
		return privateKey;
	}
	
	private void generateSigningKey() {
		if (certChain != null) 
			return;
		
		try {
			// Not sure if this works in Android or not
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
		    KeyPair keyPair = keyGen.genKeyPair();
		    privateKey = keyPair.getPrivate();
		    
		    // From http://stackoverflow.com/questions/1615871/creating-an-x509-certificate-in-java-without-bouncycastle
		    X509CertInfo info = new X509CertInfo();
		    Date from = new Date();
		    Date to = new Date(from.getTime() + 3650 * 86400000l);
		    CertificateValidity interval = new CertificateValidity(from, to);
		    BigInteger sn = new BigInteger(64, new SecureRandom());
		    X500Name owner = new X500Name("CN=Dexter, L=Cambridge, C=GB");
		 
		    info.set(X509CertInfo.VALIDITY, interval);
		    info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		    info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		    info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		    info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
		    info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
		    AlgorithmId algo = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid);
		    info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
		 
		    // Sign the cert to identify the algorithm that's used.
	    	X509CertImpl cert = new X509CertImpl(info);
	    	cert.sign(privateKey, "SHA1withRSA");
		 
	    	certChain = new X509Certificate[] {cert};
		  
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
	}

}
