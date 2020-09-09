package org.xms.g.auth.api.credentials;
/**
 * org.xms.g.auth.api.credentials.CredentialRequestResult: Result returned from a request for a credential.<br/>
 * com.google.android.gms.auth.api.credentials.CredentialRequestResult: Result returned from a request for a credential.<br/>
 */
public interface CredentialRequestResult extends org.xms.g.utils.XInterface, org.xms.g.common.api.Result {
    /**
     * org.xms.g.auth.api.credentials.CredentialRequestResult.getCredential() Gets a Credential that can be used to authenticate the user.<br/>
     * Devices under gms running environments are supported.<br/>
     * Below is the reference of GMS apis: <br/>
     * com.google.android.gms.auth.api.credentials.CredentialRequestResult.getCredential(): <a href="https://developers.google.com/android/reference/com/google/android/gms/auth/api/credentials/CredentialRequestResult#public-abstract-credential-getcredential">https://developers.google.com/android/reference/com/google/android/gms/auth/api/credentials/CredentialRequestResult#public-abstract-credential-getcredential</a><br/>
     *
     * @return Credential object
     */
    public org.xms.g.auth.api.credentials.Credential getCredential();

    default com.google.android.gms.auth.api.credentials.CredentialRequestResult getGInstanceCredentialRequestResult() {
        if (this instanceof org.xms.g.utils.XGettable) {
            return ((com.google.android.gms.auth.api.credentials.CredentialRequestResult) ((org.xms.g.utils.XGettable) this).getGInstance());
        }
        return new com.google.android.gms.auth.api.credentials.CredentialRequestResult() {

            public com.google.android.gms.auth.api.credentials.Credential getCredential() {
                org.xms.g.auth.api.credentials.Credential xResult = org.xms.g.auth.api.credentials.CredentialRequestResult.this.getCredential();
                return ((com.google.android.gms.auth.api.credentials.Credential) ((xResult) == null ? null : (xResult.getGInstance())));
            }

            public com.google.android.gms.common.api.Status getStatus() {
                org.xms.g.common.api.Status xResult = org.xms.g.auth.api.credentials.CredentialRequestResult.this.getStatus();
                return ((com.google.android.gms.common.api.Status) ((xResult) == null ? null : (xResult.getGInstance())));
            }
        };
    }

    default java.lang.Object getHInstanceCredentialRequestResult() {
        if (this instanceof org.xms.g.utils.XGettable) {
            return ((java.lang.Object) ((org.xms.g.utils.XGettable) this).getHInstance());
        }
        return new java.lang.Object();
    }
    /**
     * org.xms.g.auth.api.credentials.CredentialRequestResult.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.auth.api.credentials.CredentialRequestResult.<br/>
     * <p>
     *
     * @param param0 the input object
     * @return casted CredentialRequestResult object
     */
    public static org.xms.g.auth.api.credentials.CredentialRequestResult dynamicCast(java.lang.Object param0) {
        if (param0 instanceof org.xms.g.auth.api.credentials.CredentialRequestResult) {
            return ((org.xms.g.auth.api.credentials.CredentialRequestResult) param0);
        }
        if (param0 instanceof org.xms.g.utils.XGettable) {
            com.google.android.gms.auth.api.credentials.CredentialRequestResult gReturn = ((com.google.android.gms.auth.api.credentials.CredentialRequestResult) ((org.xms.g.utils.XGettable) param0).getGInstance());
            org.xms.g.utils.XmsLog.d("XMSRouter", "org.xms.g.auth.api.credentials.CredentialRequestResult.dynamicCast(java.lang.Object)");
            java.lang.Object hReturn = ((org.xms.g.utils.XGettable) param0).getHInstance();
            return new org.xms.g.auth.api.credentials.CredentialRequestResult.XImpl(new org.xms.g.utils.XBox(gReturn, hReturn));
        }
        return ((org.xms.g.auth.api.credentials.CredentialRequestResult) param0);
    }
    /**
     * org.xms.g.auth.api.credentials.CredentialRequestResult.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XInterface)) {
            return false;
        }
        if (param0 instanceof org.xms.g.utils.XGettable) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "org.xms.g.auth.api.credentials.CredentialRequestResult.isInstance(java.lang.Object)");
                return false;
            } else {
                return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.auth.api.credentials.CredentialRequestResult;
            }
        }
        return param0 instanceof org.xms.g.auth.api.credentials.CredentialRequestResult;
    }

    public static class XImpl extends org.xms.g.utils.XObject implements org.xms.g.auth.api.credentials.CredentialRequestResult {
        /**
         * org.xms.g.auth.api.credentials.CredentialRequestResult.XImpl(org.xms.g.utils.XBox)  constructor of XObject with XBox<br/>
         *
         * @param param0 the wrapper of xms instance
         */
        public XImpl(org.xms.g.utils.XBox param0) {
            super(param0);
        }

        /**
         * org.xms.g.auth.api.credentials.CredentialRequestResult.XImpl.getCredential()  Returns the Credential<br/>
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * the related HMS api is not supported, Below is references of GMS apis:<br/>
         * com.google.android.gms.auth.api.credentials.CredentialRequestResult.getCredential(): <a href="https://developers.google.com/android/reference/com/google/android/gms/auth/api/credentials/CredentialRequestResult#public-abstract-credential-getcredential">https://developers.google.com/android/reference/com/google/android/gms/auth/api/credentials/CredentialRequestResult#public-abstract-credential-getcredential</a><br/>
         *
         * @return the return object is Credential
         */
        public org.xms.g.auth.api.credentials.Credential getCredential() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "org.xms.g.auth.api.credentials.CredentialRequestResult.XImpl.getCredential()");
                java.lang.Object hReturn = new java.lang.Object();
                return new org.xms.g.auth.api.credentials.Credential(new org.xms.g.utils.XBox(null, hReturn));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.auth.api.credentials.CredentialRequestResult) this.getGInstance()).getCredential()");
                com.google.android.gms.auth.api.credentials.Credential gReturn = ((com.google.android.gms.auth.api.credentials.CredentialRequestResult) this.getGInstance()).getCredential();
                return ((gReturn) == null ? null : (new org.xms.g.auth.api.credentials.Credential(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.auth.api.credentials.CredentialRequestResult.XImpl.getStatus()  Returns the status of this result.<br/>
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.google.android.gms.auth.api.credentials.CredentialRequestResult.getStatus(): <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/Result#public-abstract-status-getstatus">https://developers.google.com/android/reference/com/google/android/gms/common/api/Result#public-abstract-status-getstatus</a><br/>
         *
         * @return the return Status
         */
        public org.xms.g.common.api.Status getStatus() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                throw new java.lang.RuntimeException("Not Supported");
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.auth.api.credentials.CredentialRequestResult) this.getGInstance()).getStatus()");
                com.google.android.gms.common.api.Status gReturn = ((com.google.android.gms.auth.api.credentials.CredentialRequestResult) this.getGInstance()).getStatus();
                return ((gReturn) == null ? null : (new org.xms.g.common.api.Status(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }
    }
}