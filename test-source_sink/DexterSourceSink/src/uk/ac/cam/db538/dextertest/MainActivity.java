package uk.ac.cam.db538.dextertest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {

	  private Button buttonTest;
	  private Spinner spinnerSource;
	  private Spinner spinnerSink;

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    buttonTest = (Button) findViewById(R.id.buttonTest);
	    buttonTest.setOnClickListener(buttonTest_onClick);

	    spinnerSource = (Spinner) findViewById(R.id.spinnerSource);
	    spinnerSink = (Spinner) findViewById(R.id.spinnerSink);
	  }

	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.activity_main, menu);
	    return true;
	  }

	  private final View.OnClickListener buttonTest_onClick = new View.OnClickListener() {
		@Override
	    public void onClick(View v) {
	    	final Context context = v.getContext();
	    	final ProgressDialog dlgProgress = ProgressDialog.show(context, "Working..", "Leaking data", true, false);

	    	final Handler threadHandler =  new Handler() {
	                @Override
	                public void handleMessage(Message msg) {
	                        dlgProgress.dismiss();
	                        
	  				      AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
					      dlgBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					          dialog.dismiss();
					        }
					      });
					      
					      if (msg.what == 0) {
					    	  dlgBuilder.setMessage(R.string.dataLeaked_long);
						      dlgBuilder.setTitle(R.string.dataLeaked);					    	  
					      } else if (msg.what != 0) {
					    	  dlgBuilder.setMessage("An error occured. Check logcat.");
						      dlgBuilder.setTitle("Error");					    	  
					      }

					      AlertDialog dlgLeakage = dlgBuilder.create();
					      dlgLeakage.show();
					      
							long timeExecution = System.nanoTime() - MyApplication.Timer_ButtonClick;
							Log.d("TIMER_EXECUTION", Long.toString(timeExecution));
	                }
	        };
	        
	    	Thread thread = new Thread() {
				@Override
				public void run() {
				      Source source = null;
				      Sink sink = null;
				      
				      System.out.println("picking source");

				      switch ((int) spinnerSource.getSelectedItemId()) {
				      case 0: // Contacts
				        source = new Source_Contacts();
				        break;
				      case 1: // Text messages
					        source = new Source_TextMessages();
					        break;
				      case 2: // Call log
					        source = new Source_CallLog();
					        break;
				      case 3: // Browser history
				    	  source = new Source_Browser();
				    	  break;
				      case 4: // Location
				    	  source = new Source_Location();
				    	  break;
				      case 5: // Device ID
				    	  source = new Source_DeviceID();
				    	  break;
				      }

				      System.out.println("picking sink");
				      
				      switch ((int) spinnerSink.getSelectedItemId()) {
				      case 0: // Apache HTTPClient
				        sink = new Sink_ApacheHTTPClient();
				        break;
				      case 1: // Socket
				    	  sink = new Sink_Socket();
				    	  break;
				      case 2: // Logging
				    	  sink = new Sink_Log();
				    	  break;
				      case 3: // File system
				    	  sink = new Sink_FileSystem();
				    	  break;
				      case 4: // IPC
				    	  sink = new Sink_IPC();
				    	  break;
				      }

				      try {
				    	  System.out.println("getting data");
				    	  String data = source.getData(context);
				    	  System.out.println("sending data");
				    	  sink.sendData(data, context);
				    	  System.out.println("finishing");
				    	  threadHandler.sendEmptyMessage(0);
				      } catch (RuntimeException e) {
				    	  e.printStackTrace();
				    	  threadHandler.sendEmptyMessage(1);
				      }
				}
	    	};
	    	thread.start();	    	
	    }
	  };

	@Override
	protected void onStart() {
		super.onStart();
		long timeAppLaunch = System.nanoTime() - MyApplication.Timer_AppInit;
		Log.d("TIMER_APPINIT", Long.toString(timeAppLaunch));
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.Timer_ButtonClick = System.nanoTime();
		spinnerSource.setSelection(3);
		spinnerSink.setSelection(2);
		buttonTest.performClick();
	}
}
