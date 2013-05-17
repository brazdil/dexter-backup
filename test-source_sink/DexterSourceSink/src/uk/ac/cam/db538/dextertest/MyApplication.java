package uk.ac.cam.db538.dextertest;

import android.app.Application;

public class MyApplication extends Application {
	public static long Timer_AppInit;
	public static long Timer_ButtonClick;

	static {
		Timer_AppInit = System.nanoTime(); //SystemClock.elapsedRealtime();
	}
}
