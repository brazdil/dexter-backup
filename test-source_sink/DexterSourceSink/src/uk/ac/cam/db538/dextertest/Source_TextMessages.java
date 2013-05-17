package uk.ac.cam.db538.dextertest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Source_TextMessages implements Source {

  @Override
  public String getData(Context context) {
    ContentResolver cr = context.getContentResolver();
    Cursor cursor = cr.query(Uri.parse("content://sms/inbox"), null, null, null, null);

    StringBuilder messageList = new StringBuilder();
    while (cursor.moveToNext()) {
      String body = cursor.getString(cursor.getColumnIndex("body"));
      messageList.append(body);
      messageList.append("\n");
    }

    return messageList.toString();
  }
}
