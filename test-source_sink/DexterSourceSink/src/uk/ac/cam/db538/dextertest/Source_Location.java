package uk.ac.cam.db538.dextertest;

import android.content.Context;
import android.location.LocationManager;

public class Source_Location implements Source {

  @Override
  public String getData(Context context) {
	  LocationManager loc = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	  StringBuilder str = new StringBuilder();
	  for (String provider : loc.getAllProviders())
		  str.append(provider);
	  return str.toString();
  }
}
