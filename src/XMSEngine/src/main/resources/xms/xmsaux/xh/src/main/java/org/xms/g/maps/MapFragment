package org.xms.g.maps;

/**
 * org.xms.g.maps.MapFragment : xms A Map component in an app. This fragment is the simplest way to place a map in an application.<br/>
 */
public class MapFragment extends com.huawei.hms.maps.MapFragment {
    /**
     * org.xms.g.maps.MapFragment.newInstance() Creates a map fragment, using default options.<br/>
     * Devices under hms running environments are supported.<br/>
     *
     * @return the return object is MapFragment
     */
    public static org.xms.g.maps.MapFragment newInstance() {
        return new org.xms.g.maps.MapFragment();
    }

    /**
     * org.xms.g.maps.MapFragment.newInstance(org.xms.g.maps.ExtensionMapOptions) Creates a map fragment with the given options.<br/>
     * Devices under hms running environments are supported.<br/>
     *
     * @param var0 the param should instance of ExtensionMapOptions
     * @return the return object is MapFragment
     */
    public static org.xms.g.maps.MapFragment newInstance(org.xms.g.maps.ExtensionMapOptions extensionMapOptions) {
        org.xms.g.maps.MapFragment mapFragment = new org.xms.g.maps.MapFragment();
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putParcelable("HuaweiMapOptions", (com.huawei.hms.maps.HuaweiMapOptions)(extensionMapOptions.getHInstance()));
        mapFragment.setArguments(bundle);
        return mapFragment;
    }

    /**
     * org.xms.g.maps.MapFragment.MapFragment() constructor of MapFragment <br/>
     */
    public MapFragment() {
    }

    /**
     * org.xms.g.maps.MapFragment.getMapAsync(org.xms.g.maps.OnMapReadyCallback) Sets a callback object which will be triggered when the GoogleMap instance is ready to be used.<br/>
     * Devices under hms running environments are supported.<br/>
     *
     * @param onMapReadyCallback the param should be instanceof OnMapReadyCallback
     */
    public void getMapAsync(org.xms.g.maps.OnMapReadyCallback onMapReadyCallback) {
        super.getMapAsync(onMapReadyCallback.getHInstanceOnMapReadyCallback());
    }

    /**
     * org.xms.g.maps.MapFragment.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     * Devices under hms running environments are supported.<br/>
     *
     * @param  param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.maps.MapFragment;
    }

    /**
     * org.xms.g.maps.MapFragment.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.maps.MapFragment<br/>
     * Devices under hms running environments are supported.<br/>
     *
     * @param  param0 the param should instanceof java.lang.Object
     * @return cast maps.MapFragment object
     */
    public static org.xms.g.maps.MapFragment dynamicCast(java.lang.Object param0) {
        return ((org.xms.g.maps.MapFragment) param0);
    }
}