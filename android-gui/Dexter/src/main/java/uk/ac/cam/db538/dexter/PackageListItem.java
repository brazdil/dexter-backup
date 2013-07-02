package uk.ac.cam.db538.dexter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PackageListItem extends LinearLayout {

    private PackageInfo packageInfo;

    private final ImageView imgPackageIcon;
    private final TextView textPackageName;
    private final TextView textApkPath;

    public PackageListItem(Context context, ViewGroup parent) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        this.addView(inflater.inflate(R.layout.listitem_packages, parent, false));

        this.imgPackageIcon = (ImageView) this.findViewById(R.id.imgPackageIcon);
        this.textPackageName = (TextView) this.findViewById(R.id.textPackageName);
        this.textApkPath = (TextView) this.findViewById(R.id.textApkPath);
    }

    public ImageView getImgPackageIcon() {
        return imgPackageIcon;
    }

    public TextView getTextPackageName() {
        return textPackageName;
    }

    public TextView getTextApkPath() {
        return textApkPath;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;

        Drawable pkgIcon = this.packageInfo.applicationInfo.loadIcon(
            this.getContext().getPackageManager());
        String pkgName = this.packageInfo.applicationInfo.packageName;
        String pkgApkPath = this.packageInfo.applicationInfo.sourceDir;

        this.imgPackageIcon.setImageDrawable(pkgIcon);
        this.textPackageName.setText(pkgName);
        this.textApkPath.setText(pkgApkPath);

    }
}
