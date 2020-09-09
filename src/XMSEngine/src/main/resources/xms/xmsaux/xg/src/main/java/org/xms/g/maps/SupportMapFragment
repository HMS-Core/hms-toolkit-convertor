package org.xms.g.maps;

/**
 * org.xms.g.maps.SupportMapFragment : xms A Map component in an app. This fragment is the simplest way to place a map in an application.<br/>
 */
public class SupportMapFragment extends com.google.android.gms.maps.SupportMapFragment {
    /**
     * org.xms.g.maps.SupportMapFragment.newInstance() Creates a map fragment, using default options.<br/>
     * Devices under gms running environments are supported.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.google.android.gms.maps.SupportMapFragment.newInstance():<a href="https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#newInstance()">https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#newInstance()</a> <br/>
     *
     * @return the return object is SupportMapFragment
     */
    public static org.xms.g.maps.SupportMapFragment newInstance() {
        return new org.xms.g.maps.SupportMapFragment();
    }

    /**
     * org.xms.g.maps.SupportMapFragment.newInstance(org.xms.g.maps.ExtensionMapOptions) Creates a streetview panorama fragment with the given options.<br/>
     * Devices under gms running environments are supported.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.google.android.gms.maps.SupportMapFragment.newInstance(com.google.android.gms.maps.GoogleMapOptions):<a href="https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#public-static-SupportMapFragment-newinstance-streetviewpanoramaoptions-options">https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#public-static-SupportMapFragment-newinstance-streetviewpanoramaoptions-options</a> <br/>
     *
     * @param var0 the param should instanceof ExtensionMapOptions
     * @return the return object is SupportMapFragment
     */
    public static org.xms.g.maps.SupportMapFragment newInstance(org.xms.g.maps.ExtensionMapOptions extensionMapOptions) {
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putParcelable("MapOptions", (com.google.android.gms.maps.GoogleMapOptions)(extensionMapOptions.getGInstance()));
        org.xms.g.maps.SupportMapFragment supportMapFragment = new org.xms.g.maps.SupportMapFragment();
        supportMapFragment.setArguments(bundle);
        return supportMapFragment;
    }

    /**
     * org.xms.g.maps.SupportMapFragment.SupportMapFragment() constructor of SupportMapFragment <br/>
     */
    public SupportMapFragment() {
    }

    /**
     * org.xms.g.maps.SupportMapFragment.getMapAsync(org.xms.g.maps.OnMapReadyCallback) Sets a callback object which will be triggered when the GoogleMap instance is ready to be used.<br/>
     * Devices under gms running environments are supported.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.google.android.gms.maps.SupportMapFragment.getMapAsync(com.google.android.gms.maps.OnMapReadyCallback):<a href="https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#public-void-getmapasync-onmapreadycallback-callback">https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment#public-void-getmapasync-onmapreadycallback-callback</a> <br/>
     *
     * @param onMapReadyCallback the param should be instanceof OnMapReadyCallback
     */
    public void getMapAsync(org.xms.g.maps.OnMapReadyCallback onMapReadyCallback) {
        super.getMapAsync(onMapReadyCallback.getGInstanceOnMapReadyCallback());
    }

    /**
     * org.xms.g.maps.SupportMapFragment.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     * Devices under gms running environments are supported.<br/>
     *
     * @param  param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.maps.SupportMapFragment;
    }

    /**
     * org.xms.g.maps.SupportMapFragment.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.maps.SupportMapFragment<br/>
     * Devices under gms running environments are supported.<br/>
     *
     * @param  param0 the param should instanceof java.lang.Object
     * @return cast maps.SupportMapFragment object
     */
    public static org.xms.g.maps.SupportMapFragment dynamicCast(java.lang.Object param0) {
        return ((org.xms.g.maps.SupportMapFragment) param0);
    }
}