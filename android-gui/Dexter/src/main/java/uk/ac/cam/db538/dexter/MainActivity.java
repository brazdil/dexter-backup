package uk.ac.cam.db538.dexter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends Activity {

    protected ListView listPackages = null;
    protected PackageAdapter adapterPackages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listPackages = (ListView) this.findViewById(R.id.listPackages);
        adapterPackages = new PackageAdapter(this);
        listPackages.setAdapter(adapterPackages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
