package org.xms.g.common;

/**
 * Represents the results of work.<br/>
 * Combination of com.huawei.hms.common.ErrorDialogFragment and com.google.android.gms.common.ErrorDialogFragment.<br/>
 * org.xms.g.common.ErrorDialogFragment : Wraps the Dialog returned by getErrorDialog(Activity, int, int) by using DialogFragment so that it can be properly managed by the Activity.<br/>
 * com.huawei.hms.common.ErrorDialogFragment : the ErrorDialogFragment.<br/>
 * com.google.android.gms.common.ErrorDialogFragment : Wraps the Dialog returned by getErrorDialog(Activity, int, int) by using DialogFragment so that it can be properly managed by the Activity.<br/>
 */
public class ErrorDialogFragment implements org.xms.g.utils.XGettable {
    private boolean wrapper = true;
    public com.google.android.gms.common.ErrorDialogFragment gInstance;
    public com.huawei.hms.common.ErrorDialogFragment hInstance;

    /**
     * org.xms.g.common.ErrorDialogFragment.ErrorDialogFragment(ErrorDialogFragment,ErrorDialogFragment) Constructor of the ErrorDialogFragment.<br/>
     *
     * @param param0 the gInstance
     * @param param1 the hInstance
     */
    public ErrorDialogFragment(com.google.android.gms.common.ErrorDialogFragment param0, com.huawei.hms.common.ErrorDialogFragment param1) {
        gInstance = param0;
        hInstance = param1;
        wrapper = true;
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.newInstance(android.app.Dialog,android.content.DialogInterface.OnCancelListener)Create a DialogFragment for displaying the getErrorDialog(Activity, int, int) with an OnCancelListener. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.common.ErrorDialogFragment.newInstance(android.app.Dialog,android.content.DialogInterface.OnCancelListener) : <a href=""></a><br/>
     * com.google.android.gms.common.ErrorDialogFragment.newInstance(android.app.Dialog,android.content.DialogInterface.OnCancelListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-static-errordialogfragment-newinstance-dialog-dialog,-dialoginterface.oncancellistener-cancellistener">https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-static-errordialogfragment-newinstance-dialog-dialog,-dialoginterface.oncancellistener-cancellistener</a><br/>
     *
     * @param param0 The Dialog created by getErrorDialog(Activity, int, int)
     * @param param1 A DialogInterface.OnCancelListener for when a user cancels the DialogFragment
     * @return The ErrorDialogFragment
     */
    public static org.xms.g.common.ErrorDialogFragment newInstance(android.app.Dialog param0, android.content.DialogInterface.OnCancelListener param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "com.huawei.hms.common.ErrorDialogFragment.newInstance(param0, param1)");
            com.huawei.hms.common.ErrorDialogFragment hReturn = com.huawei.hms.common.ErrorDialogFragment.newInstance(param0, param1);
            return ((hReturn) == null ? null : (new org.xms.g.common.ErrorDialogFragment(null, hReturn)));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "com.google.android.gms.common.ErrorDialogFragment.newInstance(param0, param1)");
            com.google.android.gms.common.ErrorDialogFragment gReturn = com.google.android.gms.common.ErrorDialogFragment.newInstance(param0, param1);
            return ((gReturn) == null ? null : (new org.xms.g.common.ErrorDialogFragment(gReturn, null)));
        }
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.newInstance(android.app.Dialog) Create a DialogFragment for displaying the getErrorDialog(Activity, int, int).<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.common.ErrorDialogFragment.newInstance(android.app.Dialog) : <a href=""></a><br/>
     * com.google.android.gms.common.ErrorDialogFragment.newInstance(android.app.Dialog) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-static-errordialogfragment-newinstance-dialog-dialog">https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-static-errordialogfragment-newinstance-dialog-dialog</a><br/>
     *
     * @param param0 The Dialog created by getErrorDialog(Activity, int, int)
     * @return The ErrorDialogFragment
     */
    public static org.xms.g.common.ErrorDialogFragment newInstance(android.app.Dialog param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "com.huawei.hms.common.ErrorDialogFragment.newInstance(param0)");
            com.huawei.hms.common.ErrorDialogFragment hReturn = com.huawei.hms.common.ErrorDialogFragment.newInstance(param0);
            return ((hReturn) == null ? null : (new org.xms.g.common.ErrorDialogFragment(null, hReturn)));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "com.google.android.gms.common.ErrorDialogFragment.newInstance(param0)");
            com.google.android.gms.common.ErrorDialogFragment gReturn = com.google.android.gms.common.ErrorDialogFragment.newInstance(param0);
            return ((gReturn) == null ? null : (new org.xms.g.common.ErrorDialogFragment(gReturn, null)));
        }
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.onCancel(android.content.DialogInterface) the onCancel.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.common.ErrorDialogFragment.onCancel(android.content.DialogInterface) : <a href=""></a><br/>
     * com.google.android.gms.common.ErrorDialogFragment.onCancel(android.content.DialogInterface) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-void-oncancel-dialoginterface-dialog">https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-void-oncancel-dialoginterface-dialog</a><br/>
     *
     * @param param0 the DialogInterface
     */
    public void onCancel(android.content.DialogInterface param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).onCancel(param0)");
            ((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).onCancel(param0);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).onCancel(param0)");
            ((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).onCancel(param0);
        }
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.onCreateDialog(android.os.Bundle) Returns a Dialog created by getErrorDialog(Activity, int, int) with the provided errorCode, activity, request code, and cancel listener.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.common.ErrorDialogFragment.onCreateDialog(android.os.Bundle) : <a href=""></a><br/>
     * com.google.android.gms.common.ErrorDialogFragment.onCreateDialog(android.os.Bundle) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-dialog-oncreatedialog-bundle-savedinstancestate">https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-dialog-oncreatedialog-bundle-savedinstancestate</a><br/>
     *
     * @param param0 Not used
     * @return the Dialog
     */
    public android.app.Dialog onCreateDialog(android.os.Bundle param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).onCreateDialog(param0)");
            return ((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).onCreateDialog(param0);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).onCreateDialog(param0)");
            return ((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).onCreateDialog(param0);
        }
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.show(android.app.FragmentManager,java.lang.String) the show.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.common.ErrorDialogFragment.show(android.app.FragmentManager,java.lang.String) : <a href=""></a><br/>
     * com.google.android.gms.common.ErrorDialogFragment.show(android.app.FragmentManager,java.lang.String) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-void-show-fragmentmanager-manager,-string-tag">https://developers.google.com/android/reference/com/google/android/gms/common/ErrorDialogFragment#public-void-show-fragmentmanager-manager,-string-tag</a><br/>
     *
     * @param param0 the manager
     * @param param1 the tag
     */
    public void show(android.app.FragmentManager param0, java.lang.String param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).show(param0, param1)");
            ((com.huawei.hms.common.ErrorDialogFragment) this.getHInstance()).show(param0, param1);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).show(param0, param1)");
            ((com.google.android.gms.common.ErrorDialogFragment) this.getGInstance()).show(param0, param1);
        }
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.setGInstance(java.lang.Object) set the gms instance for the corresponding xms instance.<br/>
     *
     * @param param0 instance of gms
     */
    public void setGInstance(com.google.android.gms.common.ErrorDialogFragment param0) {
        this.gInstance = param0;
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.setHInstance(java.lang.Object) set the hms instance for the corresponding xms instance.<br/>
     *
     * @param param0 instance of hms
     */
    public void setHInstance(com.huawei.hms.common.ErrorDialogFragment param0) {
        this.hInstance = param0;
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.getGInstance() get the gms instance from the corresponding xms instance.<br/>
     *
     * @return instance of gms
     */
    public java.lang.Object getGInstance() {
        return this.gInstance;
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.getHInstance() get the hms instance from the corresponding xms instance.<br/>
     *
     * @return instance of hms
     */
    public java.lang.Object getHInstance() {
        return this.hInstance;
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.ErrorDialogFragment.<br/>
     *
     * @param param0 the input object
     * @return casted ErrorDialogFragment object
     */
    public static org.xms.g.common.ErrorDialogFragment dynamicCast(java.lang.Object param0) {
        return ((org.xms.g.common.ErrorDialogFragment) param0);
    }

    /**
     * org.xms.g.common.ErrorDialogFragment.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.common.ErrorDialogFragment;
        } else {
            return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.ErrorDialogFragment;
        }
    }
}