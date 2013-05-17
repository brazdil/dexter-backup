package uk.ac.cam.db538.dextertest;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Source_DeviceID implements Source {

  @Override
  public String getData(Context context) {
	  TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	  return tel.getDeviceId();
  }
}
