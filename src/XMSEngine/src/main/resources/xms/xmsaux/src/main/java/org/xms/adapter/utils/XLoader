package org.xms.adapter.utils;

import java.io.IOException;

import android.app.Application;
import org.xms.xmsaux.BuildConfig;
import com.config.xmsaux.XmsConfig;

public class XLoader {
    private Boolean initialized = false;

    private static final XLoader instance = new XLoader();
    public static void init(Application app) {
        synchronized (instance) {
            if (instance.initialized) {
                throw new IllegalStateException("XLoader has already been initialized");
            }
            instance.initialized = true;
            // read the implementation list from directory
            String assetPath = BuildConfig.XMS_ASSET_PATH;
            String xgHash = XmsConfig.XG_HASH;
            String xhHash = XmsConfig.XH_HASH;
            String xRouter = "";
            try {
                xRouter = FileUtils.checkAndCopyAsset(app, assetPath, xgHash, xhHash);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!instance.hookClassloader(app, xRouter)) {
                throw new IllegalStateException("Failed to hook class loader");
            }
        }
    }

    private boolean hookClassloader(Application app, String xRouter) {
        return ClassloaderHookerFactory.get(app, xRouter).run();
    }
}
