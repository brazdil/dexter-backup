package uk.ac.cam.db538.dexter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class InstrumentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument);

        if (savedInstanceState != null) {
            // reload state
        }
    }

    public static final String PACKAGE_NAME = "package_name";
}
