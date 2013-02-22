package uk.ac.cam.db538.dexter;

import uk.ac.cam.db538.dexter.apk.Apk;

import java.io.File;
import java.io.IOException;
import lombok.val;

public class MainConsole {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("usage: dexter <framework-dir> <apk-file>");
            System.exit(1);
        }

        val apkFile = new File(args[1]);
        if (!apkFile.isFile()) {
            System.err.println("<apk-file> is not a file");
            System.exit(1);
        }
        val apkFile_new = new File(args[1] + ".dexter");

        val frameworkDir = new File(args[0]);
        if (!frameworkDir.isDirectory()) {
            System.err.println("<framework-dir> is not a directory");
            System.exit(1);
        }
            
        val APK = new Apk(apkFile, frameworkDir);
        val warnings = APK.getDexFile().instrument();
        for (val warning : warnings)
            System.err.println("warning: " + warning.getMessage());
        APK.writeToFile(apkFile_new);
    }
}
