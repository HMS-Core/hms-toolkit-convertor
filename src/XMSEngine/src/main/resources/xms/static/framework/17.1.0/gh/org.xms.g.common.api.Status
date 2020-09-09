package org.xms.g.common.api;

/**
 * Represents the results of work.<br/>
 * Combination of com.huawei.hms.support.api.client.Status and com.google.android.gms.common.api.Status.<br/>
 * com.huawei.hms.support.api.client.Status : Represents the task processing result.<br/>
 * com.google.android.gms.common.api.Status : Represents the results of work.<br/>
 */
public final class Status extends org.xms.g.utils.XObject implements org.xms.g.common.api.Result, android.os.Parcelable {

    /**
     * android.os.Parcelable.Creator.CREATOR a public CREATOR field that generates instances of your Parcelable class from a Parcel.<br/>
     * <p>
     * com.huawei.hms.support.api.client.Status.CREATOR: <a href=""></a><br/>
     * com.google.android.gms.common.api.Status.CREATOR: <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-static-final-creatorstatus-creator">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-static-final-creatorstatus-creator</a><br/>
     */
    public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {

        public org.xms.g.common.api.Status createFromParcel(android.os.Parcel param0) {
            com.google.android.gms.common.api.Status gReturn = null;
            com.huawei.hms.support.api.client.Status hReturn = null;
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                hReturn = com.huawei.hms.support.api.client.Status.CREATOR.createFromParcel(param0);
            } else {
                gReturn = com.google.android.gms.common.api.Status.CREATOR.createFromParcel(param0);
            }
            return new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(gReturn, hReturn));
        }

        public org.xms.g.common.api.Status[] newArray(int param0) {
            return new org.xms.g.common.api.Status[param0];
        }
    };

    /**
     * org.xms.g.common.api.Status.Status(org.xms.g.utils.XBox)  constructor of Status with XBox.<br/>
     *
     * @param param0 the wrapper of xms instance
     */
    public Status(org.xms.g.utils.XBox param0) {
        super(param0);
    }

    /**
     * org.xms.g.common.api.Status.Status(int) Creates a representation of the status resulting from a GoogleApiClient operation.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status(int) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section16416744121012">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section16416744121012</a><br/>
     * com.google.android.gms.common.api.Status(int) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode</a><br/>
     *
     * @param param0 The status code
     */
    public Status(int param0) {
        super((org.xms.g.utils.XBox) null);
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            this.setHInstance(new com.huawei.hms.support.api.client.Status(param0));
        } else {
            this.setGInstance(new com.google.android.gms.common.api.Status(param0));
        }
    }

    /**
     * org.xms.g.common.api.Status.Status(int,java.lang.String) Creates a representation of the status resulting from a GoogleApiClient operation.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.Status(int,java.lang.String) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section16416744121012">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section16416744121012</a><br/>
     * com.google.android.gms.common.api.Status.Status(int,java.lang.String) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode,-string-statusmessage">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode,-string-statusmessage</a><br/>
     *
     * @param param0 The status code
     * @param param1 The message associated with this status, or null
     */
    public Status(int param0, java.lang.String param1) {
        super((org.xms.g.utils.XBox) null);
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            this.setHInstance(new com.huawei.hms.support.api.client.Status(param0, param1));
        } else {
            this.setGInstance(new com.google.android.gms.common.api.Status(param0, param1));
        }
    }

    /**
     * org.xms.g.common.api.Status.Status(int,java.lang.String,android.app.PendingIntent) Creates a representation of the status resulting from a GoogleApiClient operation.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.Status(int,java.lang.String,android.app.PendingIntent) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section9358439115">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section9358439115</a><br/>
     * com.google.android.gms.common.api.Status.Status(int,java.lang.String,android.app.PendingIntent) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode,-string-statusmessage,-pendingintent-pendingintent">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-int-statuscode,-string-statusmessage,-pendingintent-pendingintent</a><br/>
     *
     * @param param0 The status code
     * @param param1 The message associated with this status, or null
     * @param param2 A pending intent that will resolve the issue when started, or null
     */
    public Status(int param0, java.lang.String param1, android.app.PendingIntent param2) {
        super((org.xms.g.utils.XBox) null);
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            this.setHInstance(new com.huawei.hms.support.api.client.Status(param0, param1, param2));
        } else {
            this.setGInstance(new com.google.android.gms.common.api.Status(param0, param1, param2));
        }
    }

    /**
     * org.xms.g.common.api.Status.equals(java.lang.Object) Determines whether two instances are equal.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.equals(java.lang.Object) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section144021234112617">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section144021234112617</a><br/>
     * com.google.android.gms.common.api.Status.equals(java.lang.Object) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-equals-object-obj">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-equals-object-obj</a><br/>
     *
     * @param param0 Objects to be compared
     * @return Comparison result: equal if true, and unequal if false
     */
    public boolean equals(java.lang.Object param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).equals(param0)");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).equals(param0);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).equals(param0)");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).equals(param0);
        }
    }

    /**
     * org.xms.g.common.api.Status.getResolution() Obtains the pending intent to resolve the failure. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.getResolution() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section10607104115214">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section10607104115214</a><br/>
     * com.google.android.gms.common.api.Status.getResolution() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-pendingintent-getresolution">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-pendingintent-getresolution</a><br/>
     *
     * @return a pending intent to resolve the failure
     */
    public final android.app.PendingIntent getResolution() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).getResolution()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).getResolution();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).getResolution()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).getResolution();
        }
    }

    /**
     * org.xms.g.common.api.Status.getStatus() Returns the status of this result.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.support.api.client.Status.getStatus() : <a href=""></a><br/>
     * com.google.android.gms.common.api.Status.getStatus() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-getstatus">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-status-getstatus</a><br/>
     *
     * @return the status of this result
     */
    public final org.xms.g.common.api.Status getStatus() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatus()");
            com.huawei.hms.support.api.client.Status hReturn = ((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatus();
            return ((hReturn) == null ? null : (new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).getStatus()");
            com.google.android.gms.common.api.Status gReturn = ((com.google.android.gms.common.api.Status) this.getGInstance()).getStatus();
            return ((gReturn) == null ? null : (new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.common.api.Status.getStatusCode() Indicates the status of the operation.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.getStatusCode() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section32821622269">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section32821622269</a><br/>
     * com.google.android.gms.common.api.Status.getStatusCode() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-int-getstatuscode">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-int-getstatuscode</a><br/>
     *
     * @return Status code resulting from the operation. The value is one of the constants in CommonStatusCodes or specific to the APIs added to the GoogleApiClient
     */
    public final int getStatusCode() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatusCode()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatusCode();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).getStatusCode()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).getStatusCode();
        }
    }

    /**
     * org.xms.g.common.api.Status.getStatusMessage() Obtains the error description.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.getStatusMessage() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section143141720142010">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section143141720142010</a><br/>
     * com.google.android.gms.common.api.Status.getStatusMessage() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-string-getstatusmessage">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-string-getstatusmessage</a><br/>
     *
     * @return Status description
     */
    public final java.lang.String getStatusMessage() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatusMessage()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).getStatusMessage();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).getStatusMessage()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).getStatusMessage();
        }
    }

    /**
     * org.xms.g.common.api.Status.hasResolution() Returns true if calling startResolutionForResult(Activity, int) will start any intents requiring user interaction.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.hasResolution() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section289212616244">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section289212616244</a><br/>
     * com.google.android.gms.common.api.Status.hasResolution() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-hasresolution">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-hasresolution</a><br/>
     *
     * @return true if there is a resolution that can be started
     */
    public final boolean hasResolution() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).hasResolution()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).hasResolution();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).hasResolution()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).hasResolution();
        }
    }

    /**
     * org.xms.g.common.api.Status.hashCode() hashCode of a compute instance.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.hashCode() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section0102201410491">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section0102201410491</a><br/>
     * com.google.android.gms.common.api.Status.hashCode() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-int-hashcode">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-int-hashcode</a><br/>
     *
     * @return hashCode of a compute instance
     */
    public final int hashCode() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).hashCode()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).hashCode();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).hashCode()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).hashCode();
        }
    }

    /**
     * org.xms.g.common.api.Status.isCanceled() Returns true if the operation was canceled.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.support.api.client.Status.isCanceled() : <a href=""></a><br/>
     * com.google.android.gms.common.api.Status.isCanceled() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-iscanceled">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-iscanceled</a><br/>
     *
     * @return true if the operation was canceled
     */
    public final boolean isCanceled() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).isCanceled()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).isCanceled();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).isCanceled()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).isCanceled();
        }
    }

    /**
     * org.xms.g.common.api.Status.isInterrupted() Returns true if the operation was interrupted.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hms.support.api.client.Status.isInterrupted() : <a href=""></a><br/>
     * com.google.android.gms.common.api.Status.isInterrupted() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-isinterrupted">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-isinterrupted</a><br/>
     *
     * @return true if the operation was interrupted
     */
    public final boolean isInterrupted() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).isInterrupted()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).isInterrupted();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).isInterrupted()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).isInterrupted();
        }
    }

    /**
     * org.xms.g.common.api.Status.isSuccess() Returns true if the operation was successful.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.isSuccess() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section178461430142215">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section178461430142215</a><br/>
     * com.google.android.gms.common.api.Status.isSuccess() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-issuccess">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-boolean-issuccess</a><br/>
     *
     * @return true if the operation was successful, false if there was an error
     */
    public final boolean isSuccess() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).isSuccess()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).isSuccess();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).isSuccess()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).isSuccess();
        }
    }

    /**
     * org.xms.g.common.api.Status.startResolutionForResult(android.app.Activity,int) Resolves an error by starting any intents requiring user interaction. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.startResolutionForResult(android.app.Activity,int) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section04641751152411">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section04641751152411</a><br/>
     * com.google.android.gms.common.api.Status.startResolutionForResult(android.app.Activity,int) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-void-startresolutionforresult-activity-activity,-int-requestcode">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-void-startresolutionforresult-activity-activity,-int-requestcode</a><br/>
     *
     * @param param0 An Activity context to use to resolve the issue. The activity's onActivityResult method will be invoked after the user is done. If the resultCode is RESULT_OK, the application should try to connect again
     * @param param1 The request code to pass to onActivityResult
     * @throws android.content.IntentSender.SendIntentException If the resolution intent has been canceled or is no longer able to execute the request
     */
    public void startResolutionForResult(android.app.Activity param0, int param1) throws android.content.IntentSender.SendIntentException {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).startResolutionForResult(param0, param1)");
            ((com.huawei.hms.support.api.client.Status) this.getHInstance()).startResolutionForResult(param0, param1);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).startResolutionForResult(param0, param1)");
            ((com.google.android.gms.common.api.Status) this.getGInstance()).startResolutionForResult(param0, param1);
        }
    }

    /**
     * org.xms.g.common.api.Status.toString() Constructs and outputs the string of an instance.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.toString() : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section5954172512309">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section5954172512309</a><br/>
     * com.google.android.gms.common.api.Status.toString() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-string-tostring">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-string-tostring</a><br/>
     *
     * @return the string of an instance
     */
    public final java.lang.String toString() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).toString()");
            return ((com.huawei.hms.support.api.client.Status) this.getHInstance()).toString();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).toString()");
            return ((com.google.android.gms.common.api.Status) this.getGInstance()).toString();
        }
    }

    /**
     * org.xms.g.common.api.Status.writeToParcel(android.os.Parcel,int) used in serialization and deserialization.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.support.api.client.Status.writeToParcel(android.os.Parcel,int) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section1270513112283">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/status-0000001050121132#EN-US_TOPIC_0000001050121132__section1270513112283</a><br/>
     * com.google.android.gms.common.api.Status.writeToParcel(android.os.Parcel,int) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-void-writetoparcel-parcel-out,-int-flags">https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#public-void-writetoparcel-parcel-out,-int-flags</a><br/>
     *
     * @param param0 Parcel Object
     * @param param1 Writing mode
     */
    public void writeToParcel(android.os.Parcel param0, int param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.support.api.client.Status) this.getHInstance()).writeToParcel(param0, param1)");
            ((com.huawei.hms.support.api.client.Status) this.getHInstance()).writeToParcel(param0, param1);
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.Status) this.getGInstance()).writeToParcel(param0, param1)");
            ((com.google.android.gms.common.api.Status) this.getGInstance()).writeToParcel(param0, param1);
        }
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public int describeContents() {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * org.xms.g.common.api.Status.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.api.Status.<br/>
     * <p>
     *
     * @param param0 the input object
     * @return casted Status object
     */
    public static org.xms.g.common.api.Status dynamicCast(java.lang.Object param0) {
        if (param0 instanceof org.xms.g.common.api.Status) {
            return ((org.xms.g.common.api.Status) param0);
        }
        if (param0 instanceof org.xms.g.utils.XGettable) {
            com.google.android.gms.common.api.Status gReturn = ((com.google.android.gms.common.api.Status) ((org.xms.g.utils.XGettable) param0).getGInstance());
            com.huawei.hms.support.api.client.Status hReturn = ((com.huawei.hms.support.api.client.Status) ((org.xms.g.utils.XGettable) param0).getHInstance());
            return new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(gReturn, hReturn));
        }
        if (param0 instanceof com.google.android.gms.common.api.Status) {
            return new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(param0, null));
        }
        if (param0 instanceof com.huawei.hms.support.api.client.Status) {
            return new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(null, param0));
        }
        return ((org.xms.g.common.api.Status) param0);
    }

    /**
     * org.xms.g.common.api.Status.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.support.api.client.Status;
        } else {
            return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.api.Status;
        }
    }
}