package org.xms.adapter.utils;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.xms.g.utils.GlobalEnvSetting;
import org.xms.xmsaux.BuildConfig;

public abstract class ClassloaderHooker {
    public static final String TAG = ClassloaderHooker.class.getSimpleName();

    private Application app;

    private ClassLoader baseLoader;

    private ClassLoader appLoader;

    private ClassloaderHooker(Application app) {
        this.app = app;
        Context base = app.getBaseContext();
        Object packageInfo = ReflectionUtils.readField(base, "mPackageInfo");
        if (packageInfo == null) {
            return;
        }
        appLoader = ReflectionUtils.readField(ClassLoader.class, packageInfo, "mClassLoader");
        if (appLoader == null) {
            return;
        }
        baseLoader = ReflectionUtils.readField(ClassLoader.class, appLoader, "parent");
    }

    ClassLoader getBaseLoader() {
        return baseLoader;
    }

    ClassLoader getAppLoader() {
        return appLoader;
    }

    Context getContext() {
        return app;
    }

    boolean isFullyInitialized() {
        return baseLoader != null;
    }

    void setAppLoaderParent(ClassLoader loader) {
        ReflectionUtils.writeField(getAppLoader(), "parent", loader);
    }

    public abstract boolean run();

    XClassLoader createClassLoader(String dexFileName, ClassLoader parent) {
        return new XClassLoader(FileUtils.getCacheDir(app).getAbsolutePath() + File.separator + dexFileName,
                FileUtils.getCacheDir(app).getAbsolutePath(), null, parent, appLoader);
    }

    /**
     * AppLoader => GLoader => BaseLoader
     */
    static class PureG extends ClassloaderHooker {
        PureG(Application app) {
            super(app);
        }

        @Override
        public boolean run() {
            if (!isFullyInitialized()) {
                return false;
            }
            Log.i(TAG, "load PureG class");
            ClassLoader loader = createClassLoader(BuildConfig.XG_BIN_NAME, getBaseLoader());
            setAppLoaderParent(loader);
            return true;
        }
    }

    /**
     * AppLoader => HLoader => BaseLoader
     */
    static class PureH extends ClassloaderHooker {
        PureH(Application app) {
            super(app);
        }

        @Override
        public boolean run() {
            if (!isFullyInitialized()) {
                return false;
            }
            Log.i(TAG, "load PureH class");
            ClassLoader loader = createClassLoader(BuildConfig.XH_BIN_NAME, getBaseLoader());
            setAppLoaderParent(loader);
            return true;
        }
    }

    /**
     * AppLoader => GLoader => HLoader[disabled] => BaseLoader
     */
    static class G1 extends ClassloaderHooker {
        G1(Application app) {
            super(app);
        }

        @Override
        public boolean run() {
            if (!isFullyInitialized()) {
                return false;
            }
            XClassLoader hLoader = createClassLoader(BuildConfig.XH_BIN_NAME, getBaseLoader());
            XClassLoader gLoader = createClassLoader(BuildConfig.XG_BIN_NAME, hLoader);
            setAppLoaderParent(gLoader);
            if (GlobalEnvSetting.isHms()) {
                Log.i(TAG, "load G1 class, hms loader enable");
                gLoader.disable();
                hLoader.enable();
            } else {
                Log.i(TAG, "load G1 class, gms loader enable");
                gLoader.enable();
                hLoader.disable();
            }
            return true;
        }
    }

    /**
     * AppLoader => HLoader => GLoader[disabled] => BaseLoader
     */
    static class H1 extends ClassloaderHooker {
        H1(Application app) {
            super(app);
        }

        @Override
        public boolean run() {
            if (!isFullyInitialized()) {
                return false;
            }
            XClassLoader gLoader = createClassLoader(BuildConfig.XG_BIN_NAME, getBaseLoader());
            XClassLoader hLoader = createClassLoader(BuildConfig.XH_BIN_NAME, gLoader);
            setAppLoaderParent(hLoader);
            if (GlobalEnvSetting.isHms()) {
                Log.i(TAG, "load H1 class, hms loader enable");
                hLoader.enable();
                gLoader.disable();
            } else {
                Log.i(TAG, "load H1 class, hms loader disable");
                hLoader.disable();
                gLoader.enable();
            }
            return true;
        }
    }
}
