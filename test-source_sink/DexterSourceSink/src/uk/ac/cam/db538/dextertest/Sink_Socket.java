package uk.ac.cam.db538.dextertest;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import android.content.Context;


public class Sink_Socket implements Sink {

  @Override
  public void sendData(String data, Context context) {
	  try {
		  Socket s1 = new Socket("www.google.com", 80);
		  OutputStream sos1 = s1.getOutputStream();
		  sos1.write(data.getBytes());
		  sos1.close();
		  s1.close();
		  
		  Socket s2 = new Socket("www.google.com", 80);
		  OutputStream sos2 = s2.getOutputStream();
		  PrintWriter pw = new PrintWriter(sos2);
		  pw.println(data);
		  pw.close();
		  sos2.close();
		  s2.close();
	  } catch (Exception e) {
		  throw new RuntimeException(e);
	  }
  }

}
