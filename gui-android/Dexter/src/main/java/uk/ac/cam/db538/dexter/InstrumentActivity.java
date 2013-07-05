package uk.ac.cam.db538.dexter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.HierarchyBuilder;
import uk.ac.cam.db538.dexter.hierarchy.HierarchyScanCallback;

public class InstrumentActivity extends Activity {

    private ActivityManager activityManager;
    private PackageManager packageManager;

    private PackageInfo packageInfo;
    private File packageFile;

    private EditText textTerminal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument);

        activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = this.getPackageManager();

        textTerminal = (EditText) findViewById(R.id.terminal);

        if (savedInstanceState == null) {
            // initialize
            String packageName = getIntent().getStringExtra(PACKAGE_NAME);
            try {
                packageInfo = packageManager.getPackageInfo(packageName, 0);
                packageFile = new File(packageInfo.applicationInfo.sourceDir);

                if (!packageFile.exists()) {
                    // TODO: show error message
                    throw new RuntimeException();
                } else if (!packageFile.canRead()) {
                    // TODO: show error message
                    throw new RuntimeException();
                }
            } catch (PackageManager.NameNotFoundException ex) {
                // package of given name not found
                // TODO: show error message
                throw new RuntimeException(ex);
            }
        } else {
            // intent does not contain package name
            // TODO: show error message
            throw new RuntimeException();
        }

        // initiate the instrumentation
        new Thread(workerInstrumentation).start();
    }

    private Runnable workerInstrumentation = new Runnable() {
        @Override
        public void run() {
            DexTypeCache typeCache = new DexTypeCache();
            File frameworkCache = new File(InstrumentActivity.this.getFilesDir(), "framework.cache");
            HierarchyBuilder hierarchyBuilder;
            try {
                if (frameworkCache.exists()) {
                    InstrumentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textTerminal.append("found previous framework dump... ");
                        }
                    });
                    hierarchyBuilder = HierarchyBuilder.deserialize(frameworkCache);
                } else {
                    hierarchyBuilder = new HierarchyBuilder(typeCache);
                    hierarchyBuilder.importDexFolder(new File("/system/framework/"));
                    InstrumentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textTerminal.append("storing framework dump... ");
                        }
                    });
                    hierarchyBuilder.serialize(frameworkCache);
                }

                hierarchyBuilder.importDex(packageFile, true);
            } catch (IOException ex) {
                // TODO: show error message
                throw new RuntimeException(ex);
            }

            InstrumentActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textTerminal.append("DONE !!!");
                }
            });
        }
    };

//    private HierarchyScanCallback callbackStage1 = new HierarchyScanCallback() {
//        @Override
//        public void onFileScanStarted(final File file) {
//            InstrumentActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    textTerminal.append("scanning " + file.getName() + "... ");
//                }
//            });
//        }
//
//        @Override
//        public void onFileScanFinished(File file) {
//            InstrumentActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    textTerminal.append("ok\n");
//                }
//            });
//        }
//
//        @Override
//        public void onFolderScanStarted(File file, int i) {
//
//        }
//
//        @Override
//        public void onFolderScanFinished(File file, int i) {
//
//        }
//    };

    public static final String PACKAGE_NAME = "package_name";
}
