package org.xms.adapter.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import org.xms.xmsaux.BuildConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class FileUtils {
    private static final String TAG = "FileUtils";

    private static final int BUFFER_SIZE = 1024;

    private static void copyFile(Context context, String src, File dest) {
        Log.i(TAG, "copy from " + src + " to " + dest.getAbsolutePath());
        try (InputStream ins = context.getAssets().open(src);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] bytes = new byte[BUFFER_SIZE];
            int total = 0;
            int i;
            while ((i = ins.read(bytes)) != -1) {
                total += i;
                os.write(bytes, 0, i);
            }
            Log.i(TAG, "" + total + " bytes written");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String checkAndCopyAsset(Context context, String assetPath, String xgHash, String xhHash) throws IOException {
        String xRouter = "";
        String fileNames[] = context.getAssets().list(assetPath);
        if (fileNames.length != 2 && fileNames.length != 1) {
            throw new IllegalStateException("Failed to read Apk files");
        }
        for (String strFile : fileNames) {
            String realPath = assetPath + File.separator + strFile;
            if (!strFile.equals(BuildConfig.XH_BIN_NAME) && !strFile.equals(BuildConfig.XG_BIN_NAME)) {
                throw new IllegalStateException("Failed to read Apk files");
            } else if (strFile.equals(BuildConfig.XH_BIN_NAME)) {
                if (!checkHash(context, realPath, xhHash)) {
                    throw new IllegalStateException("Apk File is corrupted.");
                }
                xRouter += "H";
            } else {
                if (!checkHash(context, realPath, xgHash)) {
                    throw new IllegalStateException("Apk File is corrupted.");
                }
                xRouter += "G";
            }
            if (!FileUtils.copyAsset(context, realPath, strFile)) {
                throw new IllegalStateException("Failed to copy Apk files");
            }
        }
        return xRouter;
    }

    private static boolean checkHash(Context context, String filePath, String hashValue) {
        String realFileHash = getSHA256(context, filePath);
        return realFileHash.equals(hashValue);
    }

    private static boolean copyAsset(Context context, String assetPath, String targetName) {
        File cacheFile = FileUtils.getCacheDir(context);
        String internalPath = cacheFile.getAbsolutePath() + File.separator + targetName;
        File destFile = new File(internalPath);
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileUtils.copyFile(context, assetPath, destFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    static File getCacheDir(Context context) {
        File cache;
        if (hasExternalStorage()) {
            cache = context.getExternalCacheDir();
        } else {
            cache = context.getCacheDir();
        }
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    private static String getSHA256(Context context, final String path) {
        String sha256 = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream ins = context.getAssets().open(path);
            BufferedInputStream bis = new BufferedInputStream(ins);
            byte[] bytes = new byte[bis.available()];
            if (bis.read(bytes) > 0) {
                byte[] sha = digest.digest(bytes);
                StringBuilder sb = new StringBuilder();
                for (byte encde : sha) {
                    String hex = Integer.toHexString(0xff & encde);
                    if (hex.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(hex);
                }
                sha256 = sb.toString();
            }
            bis.close();
            return sha256;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "check File" + path + "error.");
            throw new IllegalStateException("Failed to check Apk files");
        }
    }
}
