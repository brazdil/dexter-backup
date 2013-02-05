package com.example.dextertest;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class SourceSinkActivity extends Activity {

  private Button buttonTest;
  private Spinner spinnerSource;
  private Spinner spinnerSink;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_source_sink);

    buttonTest = (Button) findViewById(R.id.buttonTest);
    buttonTest.setOnClickListener(buttonTest_onClick);

    spinnerSource = (Spinner) findViewById(R.id.spinnerSource);
    spinnerSink = (Spinner) findViewById(R.id.spinnerSink);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_source_sink, menu);
    return true;
  }

  private final View.OnClickListener buttonTest_onClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Source source = null;
      Sink sink = null;

      switch ((int) spinnerSource.getSelectedItemId()) {
      case 0:
        // Contacts
        source = new Source_Contacts();
        break;
      }

      switch ((int) spinnerSink.getSelectedItemId()) {
      case 0:
        // Apache HTTPClient
        sink = new Sink_ApacheHTTPClient();
        break;
      }

      sink.sendData(source.getData(v.getContext()));

      AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(v.getContext());
      dlgBuilder.setMessage(R.string.dataLeaked_long)
      .setTitle(R.string.dataLeaked)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });

      AlertDialog dlg = dlgBuilder.create();
      dlg.show();
    }
  };
}
