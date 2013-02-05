package com.example.dextertest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class Source_Contacts implements Source {

  @Override
  public String getData(Context context) {
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

    return contactList.toString();
  }
}
