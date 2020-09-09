package org.xms.adapter.utils;

import android.app.Application;

class ClassloaderHookerFactory {
    static ClassloaderHooker get(Application app, String xRouter) {
        switch (xRouter) {
            case "H":
                return new ClassloaderHooker.PureH(app);
            case "G":
                return new ClassloaderHooker.PureG(app);
            case "HG":
                return new ClassloaderHooker.H1(app);
            default:
                return new ClassloaderHooker.G1(app);
        }
    }
}
