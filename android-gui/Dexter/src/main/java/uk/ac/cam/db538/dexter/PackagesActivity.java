package uk.ac.cam.db538.dexter;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;

public class PackagesActivity extends Activity {

    protected ListView listPackages = null;
    protected PackageAdapter adapterPackages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);

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
