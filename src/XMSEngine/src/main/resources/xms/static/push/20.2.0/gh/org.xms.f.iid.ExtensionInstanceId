package org.xms.f.iid;

/**
 * org.xms.f.iid.ExtensionInstanceId : Instance ID provides a unique identifier for each app instance and a mechanism to authenticate and authorize actions (example: sending FCM messages).<br/>
 * Combination of com.huawei.hms.aaid.HmsInstanceId and com.google.firebase.iid.FirebaseInstanceId.<br/>
 * com.huawei.hms.aaid.HmsInstanceId : Provides methods for obtaining the AAID of an app and obtaining tokens required for accessing HUAWEI Push Kit.<br/>
 * com.google.firebase.iid.FirebaseInstanceId : Firebase Instance ID provides a unique identifier for each app instance and a mechanism to authenticate and authorize actions (example: sending FCM messages).<br/>
 */
public class ExtensionInstanceId extends org.xms.g.utils.XObject {

    private String getAppId() {
        android.content.Context context = null;
        try {
            java.lang.reflect.Field field = null;
            java.lang.reflect.Field[] fields = com.huawei.hms.aaid.HmsInstanceId.class.getDeclaredFields();
            for (java.lang.reflect.Field f : fields) {
                if (f.getType().getName().equals("android.content.Context")) {
                    field = f;
                    break;
                }
            }
            field.setAccessible(true);
            context = (android.content.Context) field.get(this.getHInstance());
        } catch (java.lang.IllegalAccessException e) {
            org.xms.g.utils.XmsLog.d("XMSRouter", e.toString());
        }
        if (context != null) {
            return com.huawei.hms.utils.Util.getAppId(context);
        } else {
            throw new java.lang.RuntimeException("context cannot be null");
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getInstance(android.content.Context) Returns an instance of this class.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getInstance(android.content.Context) : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section393712230274">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section393712230274</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getInstance() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-static-firebaseinstanceid-getinstance">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-static-firebaseinstanceid-getinstance</a><br/>
     *
     * @param context Context instance
     * @return ExtensionInstanceId instance
     */
    public static org.xms.f.iid.ExtensionInstanceId getInstance(android.content.Context context) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getInstance(context)");
            com.huawei.hms.aaid.HmsInstanceId hReturn = com.huawei.hms.aaid.HmsInstanceId.getInstance(context);
            return ((hReturn) == null ? null : (new org.xms.f.iid.ExtensionInstanceId(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getInstance()");
            com.google.firebase.iid.FirebaseInstanceId gReturn = com.google.firebase.iid.FirebaseInstanceId.getInstance();
            return ((gReturn) == null ? null : (new org.xms.f.iid.ExtensionInstanceId(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.ExtensionInstanceId(org.xms.g.utils.XBox) constructor of ExtensionInstanceId with XBox.<br/>
     *
     * @param param0 the wrapper of xms instance
     */
    public ExtensionInstanceId(org.xms.g.utils.XBox param0) {
        super(param0);
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public static org.xms.f.iid.ExtensionInstanceId getInstance() {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public static org.xms.f.iid.ExtensionInstanceId getInstance(org.xms.f.ExtensionApp param0) {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getInstanceId() Returns the ID and automatically generated token for this Firebase project.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getAAID() : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section14116320143111">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section14116320143111</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getInstanceId() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-taskinstanceidresult-getinstanceid">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-taskinstanceidresult-getinstanceid</a><br/>
     *
     * @return Task which you can use to see the result via the InstanceIdResult which holds the ID and token
     */
    public org.xms.g.tasks.Task<org.xms.f.iid.InstanceIdResult> getInstanceId() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getAAID()");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getAAID();
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getInstanceId()");
            com.google.android.gms.tasks.Task gReturn = ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getInstanceId();
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getId() Returns a stable identifier that uniquely identifies the app instance.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getId() : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section14850101211311">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section14850101211311</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getId() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-getid">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-getid</a><br/>
     *
     * @return The identifier for the application instance
     */
    public java.lang.String getId() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getId()");
            return ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getId();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getId()");
            return ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getId();
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getCreationTime() Returns time when instance ID was created.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getCreationTime() : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section333815358311">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section333815358311</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getCreationTime() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-long-getcreationtime">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-long-getcreationtime</a><br/>
     *
     * @return Time when instance ID was created(milliseconds since Epoch)
     */
    public long getCreationTime() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getCreationTime()");
            return ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getCreationTime();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getCreationTime()");
            return ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getCreationTime();
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.deleteInstanceId() Delete the Instance ID and the data associated with it.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.deleteAAID() : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section8856440133116">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section8856440133116</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.deleteInstanceId() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-void-deleteinstanceid">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-void-deleteinstanceid</a><br/>
     *
     * @throws java.io.IOException if the request fails
     */
    public void deleteInstanceId() throws java.io.IOException {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).deleteAAID()");
            try {
                ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).deleteAAID();
            } catch (com.huawei.hms.common.ApiException e) {
                throw new java.io.IOException(e);
            }
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).deleteInstanceId()");
            ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).deleteInstanceId();
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getToken() Returns the automatically generated token for the default Firebase project.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getToken() : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section167326341163">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section167326341163</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getToken() : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-gettoken">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-gettoken</a><br/>
     *
     * @return the master token or null if the token is not yet available
     */
    public java.lang.String getToken() {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getToken()");
            return ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getToken();
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getToken()");
            return ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getToken();
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.getToken(java.lang.String,java.lang.String) Returns a token that authorizes a sender ID to perform an action on behalf of the application identified by Instance ID.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.getToken(java.lang.String,java.lang.String) : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section1796315281618">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section1796315281618</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.getToken(java.lang.String,java.lang.String) : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-gettoken-string-senderid,-string-scope">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-string-gettoken-string-senderid,-string-scope</a><br/>
     *
     * @param param0 ID of the sender that is authorized by the token in GMS.AppId in HMS
     * @param param1 Action authorized for senderId Set the scope to FCM to get authorization to send messages via FirebaseMessaging
     * @return a token that can identify and authorize the instance of the application on the device
     * @throws java.io.IOException if the request fails
     */
    public java.lang.String getToken(java.lang.String param0, java.lang.String param1) throws java.io.IOException {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getToken(getAppId(), param1)");
            try {
                return ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).getToken(getAppId(), param1);
            } catch (com.huawei.hms.common.ApiException e) {
                throw new java.io.IOException(e);
            }
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getToken(param0, param1)");
            return ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).getToken(param0, param1);
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.deleteToken(java.lang.String,java.lang.String) Revokes access to a scope (action) for a sender ID previously authorized by getToken().<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.aaid.HmsInstanceId.deleteToken(java.lang.String,java.lang.String) : <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section1142844881810">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/hms-instanceid-0000001050255634-V5#EN-US_TOPIC_0000001050255634__section1142844881810</a><br/>
     * com.google.firebase.iid.FirebaseInstanceId.deleteToken(java.lang.String,java.lang.String) : <a href="https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-void-deletetoken-string-senderid,-string-scope">https://developers.google.com/android/reference/com/google/firebase/iid/FirebaseInstanceId#public-void-deletetoken-string-senderid,-string-scope</a><br/>
     *
     * @param param0 ID of the sender that is authorized by the token in GMS.AppId in HMS
     * @param param1 Action authorized for senderId Set the scope to FCM to get authorization to send messages via FirebaseMessaging
     * @throws java.io.IOException if the request fails
     */
    public void deleteToken(java.lang.String param0, java.lang.String param1) throws java.io.IOException {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).deleteToken(getAppId(), param1)");
            try {
                ((com.huawei.hms.aaid.HmsInstanceId) this.getHInstance()).deleteToken(getAppId(), param1);
            } catch (com.huawei.hms.common.ApiException e) {
                throw new java.io.IOException(e);
            }
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).deleteToken(param0, param1)");
            ((com.google.firebase.iid.FirebaseInstanceId) this.getGInstance()).deleteToken(param0, param1);
        }
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.f.iid.ExtensionInstanceId.<br/>
     *
     * @param param0 the input object
     * @return casted ExtensionInstanceId object
     */
    public static org.xms.f.iid.ExtensionInstanceId dynamicCast(java.lang.Object param0) {
        return ((org.xms.f.iid.ExtensionInstanceId) param0);
    }

    /**
     * org.xms.f.iid.ExtensionInstanceId.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.aaid.HmsInstanceId;
        } else {
            return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.firebase.iid.FirebaseInstanceId;
        }
    }
}