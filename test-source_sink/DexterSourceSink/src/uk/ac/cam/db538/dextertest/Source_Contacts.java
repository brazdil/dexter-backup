package uk.ac.cam.db538.dextertest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class Source_Contacts implements Source {

  @Override
  public String getData(Context context) {
	long timer = System.nanoTime();
	
    ContentResolver cr = context.getContentResolver();
    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

    StringBuilder contactList = new StringBuilder();
    while (cursor.moveToNext()) {
      String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
      String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

      contactList.append(id);
      contactList.append(", ");
      contactList.append(name);
      contactList.append("\n");
    }
    
    Log.d("TIMER_CONTACTS", Long.toString(System.nanoTime() - timer));

    return contactList.toString();
  }
}
