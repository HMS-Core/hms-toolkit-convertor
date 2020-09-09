package org.xms.adapter.utils;

import android.util.Log;
import dalvik.system.DexClassLoader;

public class XClassLoader extends DexClassLoader {
    private static class FlagFlipper {
        private boolean value;

        public boolean getValue() {
            return value;
        }

        public void flip() {
            value = !value;
        }
    }

    private static final String TAG = "XClassLoader";

    private boolean enabled = true;

    // The classloader used to load app classes (by default is PathClassLoader)
    private ClassLoader appClassLoader;

    private FlagFlipper flagFlipper;

    private boolean flag;

    public XClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent, ClassLoader appLoader) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.appClassLoader = appLoader;
        flagFlipper = new FlagFlipper();
        flag = flagFlipper.getValue();
    }

    public void setEnabled(boolean f) {
        enabled = f;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                if (getParent() != null) {
                    c = getParent().loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        if (c != null) {
            return c;
        }
        if (enabled) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // ignore
            }

            // class may be a dependent class which is not able to be loaded by this loader.
            // Try to load it with appClassLoader but avoid loop nesting as appClassLoader may delegate
            // it back. We use a flagFlipper to avoid duplicated delegation.
            if (c == null && appClassLoader != null) {
                synchronized (appClassLoader) {
                    if (flag == flagFlipper.getValue()) {
                        try {
                            flagFlipper.flip();
                            c = appClassLoader.loadClass(name);
                        } finally {
                            // Reset flag after appClassLoader's work
                            flag = flagFlipper.getValue();
                        }
                    } else {
                        flag = flagFlipper.getValue();
                    }
                }
            }
        }
        return c;
    }
}
