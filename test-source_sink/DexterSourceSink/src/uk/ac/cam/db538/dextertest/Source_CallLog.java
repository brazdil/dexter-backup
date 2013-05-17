package uk.ac.cam.db538.dextertest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

public class Source_CallLog implements Source {

  @Override
  public String getData(Context context) {
    ContentResolver cr = context.getContentResolver();
    Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null);

    StringBuilder callList = new StringBuilder();
    while (cursor.moveToNext()) {
      String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
      callList.append(number);
      callList.append("\n");
    }

    return callList.toString();
  }
}
