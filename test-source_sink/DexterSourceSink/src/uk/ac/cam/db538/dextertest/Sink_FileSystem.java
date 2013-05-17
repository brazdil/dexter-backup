package uk.ac.cam.db538.dextertest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.os.Environment;


public class Sink_FileSystem implements Sink {

  @Override
  public void sendData(String data, Context context) {
	  try {
		  File file1 = new File(Environment.getExternalStorageDirectory(), "dexter.test1");
		  FileOutputStream fos1 = new FileOutputStream(file1);
		  fos1.write(data.getBytes());
		  fos1.close();
		  
		  File file2 = new File(Environment.getExternalStorageDirectory(), "dexter.test2");
		  FileOutputStream fos2 = new FileOutputStream(file2);
		  PrintWriter pw = new PrintWriter(fos2);
		  pw.println(data);
		  pw.close();
		  fos2.close();
	  } catch (Exception e) {
		  throw new RuntimeException(e);
	  }
  }

}
