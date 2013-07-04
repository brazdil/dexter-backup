package uk.ac.cam.db538.dexter.hierarchy;

import java.io.File;

public interface HierarchyScanCallback {
	public void onFileScanStarted(File scannedFile);
	public void onFileScanFinished(File scannedFile);

	public void onFolderScanStarted(File scannedFolder, int fileCount);
	public void onFolderScanFinished(File scannedFolder, int fileCount);
}
