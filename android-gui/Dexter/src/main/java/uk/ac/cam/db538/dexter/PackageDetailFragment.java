package uk.ac.cam.db538.dexter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Package detail screen.
 * This fragment is either contained in a {@link PackageListActivity}
 * in two-pane mode (on tablets) or a {@link PackageDetailActivity}
 * on handsets.
 */
public class PackageDetailFragment extends Fragment {

    private PackageInfo packageInfo;

    public PackageDetailFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            String packageName = getArguments().getString(ARG_ITEM_ID);
            try {
                packageInfo = this.getActivity().getPackageManager().getPackageInfo(packageName, 0);
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

        if (packageInfo != null) {
            ((TextView) rootView.findViewById(R.id.package_detail)).setText(packageInfo.packageName);
        }

        return rootView;
    }

    public static final String ARG_ITEM_ID = "package_name";
}
