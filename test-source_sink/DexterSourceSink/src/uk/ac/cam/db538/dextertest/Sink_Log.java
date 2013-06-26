package uk.ac.cam.db538.dextertest;

import android.content.Context;
import android.util.Log;

public class Sink_Log implements Sink {
  @Override
  public void sendData(String data, Context context) {
	  long timer = System.nanoTime();
	  Log.d("Dexter", data);
	  Log.d("TIMER_LOG", Long.toString(System.nanoTime() - timer));
  }
}
