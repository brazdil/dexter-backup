package uk.ac.cam.db538.dextertest;

import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.util.Log;

public class Source_Browser implements Source {

  @Override
  public String getData(Context context) {
	  long timer = System.nanoTime();
	  StringBuilder str = new StringBuilder();
	  
	  boolean b = Browser.canClearHistory(context.getContentResolver());
	  str.append(b);
	  str.append("\n");
	  
	  Cursor c = Browser.getAllVisitedUrls(context.getContentResolver());
	  while (c.moveToNext()) {
		  String url = c.getString(c.getColumnIndex("URL"));
		  str.append(url);
		  str.append("\n");
	  }
	  
	  String result = str.toString();
	  Log.d("TIMER_BROWSER", Long.toString(System.nanoTime() - timer));
	  return result;
  }
}
