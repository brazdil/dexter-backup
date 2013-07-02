package uk.ac.cam.db538.dexter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class PackageAdapter implements ListAdapter {

    Context context;
    PackageManager packageManager;
    List<PackageInfo> packages;

    public PackageAdapter(Context context) {
        this.context = context;
        this.packageManager = this.context.getPackageManager();
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
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.listitem_packages, parent, false);
        }
        TextView textPackageName = (TextView) rowView.findViewById(R.id.textPackageName);
        ImageView imgPackageIcon = (ImageView) rowView.findViewById(R.id.imgPackageIcon);

        // update the info
        PackageInfo pkg = this.packages.get(i);
        textPackageName.setText(pkg.packageName);
        imgPackageIcon.setImageDrawable(pkg.applicationInfo.loadIcon(this.packageManager));

        // set package icon size

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
