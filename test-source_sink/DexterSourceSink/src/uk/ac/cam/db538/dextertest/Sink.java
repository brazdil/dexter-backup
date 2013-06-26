package uk.ac.cam.db538.dextertest;

import android.content.Context;

public interface Sink {
  void sendData(String data, Context context);
}
