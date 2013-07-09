package uk.ac.cam.db538.dexter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;

import org.jf.dexlib.DexFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.HierarchyBuilder;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

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
            try {
                DexterApplication thisApp = (DexterApplication) getApplication();

                terminalMessage("Analyzing operating system");
                thisApp.waitForHierarchy();
                terminalDone();

                terminalMessage("Loading application");
                DexFile apk = new DexFile(packageFile);
                terminalDone();

                terminalMessage("Building runtime hierarchy");
                RuntimeHierarchy hierarchy = thisApp.getRuntimeHierarchy(apk);
                terminalDone();

                InputStream auxiliaryDex = InstrumentActivity.this.getResources().openRawResource(R.raw.aux);

                terminalMessage("Parsing application");
                Dex dex = new Dex(apk, hierarchy, auxiliaryDex);
                terminalDone();

                terminalMessage("Instrumenting application");
                dex.instrument(false);
                terminalDone();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void appendToTerminal(final String text) {
            InstrumentActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textTerminal.append(text);
                }
            });
        }

        private void terminalMessage(String msg) {
            appendToTerminal(msg + "...");
        }

        private void terminalDone() {
            appendToTerminal(" DONE\n");
        }
    };

    public static final String PACKAGE_NAME = "package_name";
}
