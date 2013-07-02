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

    private final Context context;
    private final PackageManager packageManager;
    private List<PackageInfo> packages;

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
    public View getView(int i, View view, ViewGroup parent) {
        // initialize (if needed) the row view
        PackageListItem itemView;
        if (view == null || !(view instanceof PackageListItem))
            itemView = new PackageListItem(this.context, parent);
        else
            itemView = (PackageListItem) view;

        System.out.println(i + "/" + packages.size());

        // update the fields
        itemView.setPackageInfo(this.packages.get(i));

        return itemView;
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
