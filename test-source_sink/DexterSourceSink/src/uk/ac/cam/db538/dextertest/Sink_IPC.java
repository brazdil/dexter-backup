package uk.ac.cam.db538.dextertest;

import android.content.Context;
import android.content.Intent;

public class Sink_IPC implements Sink {
  @Override
  public void sendData(String data, Context context) {
	  Intent sendIntent = new Intent();
	  sendIntent.setAction(Intent.ACTION_SEND);
	  sendIntent.putExtra(Intent.EXTRA_TEXT, data);
	  sendIntent.setType("text/plain");
	  context.startActivity(sendIntent);  
  }
}
