package uk.ac.cam.db538.dexter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class PackageAdapter implements ListAdapter {

    private final Activity activity;
    private final LayoutInflater inflater;
    private final PackageManager packageManager;
    private List<PackageInfo> packages;

    public PackageAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = this.activity.getLayoutInflater();
        this.packageManager = this.activity.getPackageManager();
        this.updateList();
    }

    public void updateList() {
        this.packages = this.packageManager.getInstalledPackages(0);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        return this.packages.size();
    }

    @Override
    public Object getItem(int i) {
        return this.packages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return this.packages.get(i).packageName.hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View rowView, ViewGroup parent) {
        // initialize (if needed) the row view
        if (rowView == null)
            rowView = this.inflater.inflate(R.layout.listitem_packages, parent, false);

        // find widgets
        ImageView imgPackageIcon = (ImageView) rowView.findViewById(R.id.imgPackageIcon);
        TextView textPackageName = (TextView) rowView.findViewById(R.id.textPackageName);
        TextView textApkPath = (TextView) rowView.findViewById(R.id.textApkPath);

        // acquire package info
        PackageInfo pkg = this.packages.get(i);
        final Drawable pkgIcon = pkg.applicationInfo.loadIcon(this.packageManager);
        final String pkgName = pkg.applicationInfo.packageName;
        final String pkgApkPath = pkg.applicationInfo.sourceDir;

        // update the fields
        imgPackageIcon.setImageDrawable(pkgIcon);
        textPackageName.setText(pkgName);
        textApkPath.setText(pkgApkPath);

        return rowView;
    }

    @Override
    public int getItemViewType(int i) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.packages.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) { }

}
