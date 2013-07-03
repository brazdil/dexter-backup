package uk.ac.cam.db538.dexter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jf.dexlib.DexFile;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import uk.ac.cam.db538.dexter.apk.Apk;

/**
 * A fragment representing a single Package detail screen.
 * This fragment is either contained in a {@link PackageListActivity}
 * in two-pane mode (on tablets) or a {@link PackageDetailActivity}
 * on handsets.
 */
public class PackageDetailFragment extends Fragment {

    private PackageManager packageManager;

    private PackageInfo packageInfo;
    private File packageFile;

    private ImageView imgPackageIcon;
    private TextView textPackageName;
    private EditText textPackageVersion;
    private EditText textLastUpdated;
    private EditText textApkPath;
    private EditText textApkSize;
    private Button btnInstrument;

    public PackageDetailFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageManager = this.getActivity().getPackageManager();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            String packageName = getArguments().getString(ARG_ITEM_ID);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_package_detail, container, false);

        imgPackageIcon = (ImageView) rootView.findViewById(R.id.imgPackageIcon);
        textPackageName = (TextView) rootView.findViewById(R.id.textPackageName);
        textPackageVersion = (EditText) rootView.findViewById(R.id.textVersion);
        textLastUpdated = (EditText) rootView.findViewById(R.id.textLastUpdated);
        textApkPath = (EditText) rootView.findViewById(R.id.textApkPath);
        textApkSize = (EditText) rootView.findViewById(R.id.textApkSize);
        btnInstrument = (Button) rootView.findViewById(R.id.buttonInstrument);

        if (packageInfo != null) {
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            String lastUpdated = df.format(new Date(packageInfo.lastUpdateTime));
            String apkSize = Long.toString(packageFile.length() / 1024) + " KB";

            imgPackageIcon.setImageDrawable(icon);
            textPackageName.setText(packageInfo.packageName);
            textPackageVersion.setText(packageInfo.versionName);
            textLastUpdated.setText(lastUpdated);
            textApkPath.setText(packageInfo.applicationInfo.sourceDir);
            textApkSize.setText(apkSize);

            btnInstrument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Apk dexterApk;
                    try {
                        dexterApk = new Apk(PackageDetailFragment.this.packageFile,
                                            new File("/system/framework/"));
                    } catch (IOException ex) {
                        // TODO: show error message
                        throw new RuntimeException(ex);
                    }

                    dexterApk.getDexFile().instrument(false);
                }
            });
        }

        return rootView;
    }

    public static final String ARG_ITEM_ID = "package_name";

}
