package org.xms.g.tasks;

import org.xms.g.utils.Utils;

/**
 * Represents the results of work.<br/>
 * Combination of com.huawei.hmf.tasks.OnFailureListener and com.google.android.gms.tasks.OnFailureListener.<br/>
 * org.xms.g.tasks.OnFailureListener : Listener called when a Task fails with an exception.<br/>
 * com.huawei.hmf.tasks.OnFailureListener : Called when a task fails.<br/>
 * com.google.android.gms.tasks.OnFailureListener : Listener called when a Task fails with an exception.<br/>
 */
public interface OnFailureListener extends org.xms.g.utils.XInterface {
    /**
     * org.xms.g.tasks.OnFailureListener.onFailure(java.lang.Exception) Listener called when a Task fails with an exception.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.OnFailureListener.onFailure(java.lang.Exception) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/onfailurelistener-0000001050123093#EN-US_TOPIC_0000001050123093__section32821622269">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/onfailurelistener-0000001050123093#EN-US_TOPIC_0000001050123093__section32821622269</a><br/>
     * com.google.android.gms.tasks.OnFailureListener.onFailure(java.lang.Exception) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/OnFailureListener#public-abstract-void-onfailure-exception-e">https://developers.google.com/android/reference/com/google/android/gms/tasks/OnFailureListener#public-abstract-void-onfailure-exception-e</a><br/>
     *
     * @param param0 the exception that caused the Task to fail. Never null
     */
    public void onFailure(java.lang.Exception param0);

    default com.google.android.gms.tasks.OnFailureListener getGInstanceOnFailureListener() {
        if (this instanceof org.xms.g.utils.XGettable) {
            return ((com.google.android.gms.tasks.OnFailureListener) ((org.xms.g.utils.XGettable) this).getGInstance());
        }
        return new com.google.android.gms.tasks.OnFailureListener() {

            public void onFailure(java.lang.Exception param0) {
                if (Utils.isGmsType(param0)) {
                    Object gobj = Utils.getXmsObjectWithGmsObject(param0);
                    org.xms.g.tasks.OnFailureListener.this.onFailure((Exception) gobj);
                } else {
                    org.xms.g.tasks.OnFailureListener.this.onFailure(param0);
                }
            }
        };
    }

    default com.huawei.hmf.tasks.OnFailureListener getHInstanceOnFailureListener() {
        if (this instanceof org.xms.g.utils.XGettable) {
            return ((com.huawei.hmf.tasks.OnFailureListener) ((org.xms.g.utils.XGettable) this).getHInstance());
        }
        return new com.huawei.hmf.tasks.OnFailureListener() {

            public void onFailure(java.lang.Exception param0) {
                if (Utils.isHmsType(param0)) {
                    Object hobj = Utils.getXmsObjectWithHmsObject(param0);
                    org.xms.g.tasks.OnFailureListener.this.onFailure((Exception) hobj);
                } else {
                    org.xms.g.tasks.OnFailureListener.this.onFailure(param0);
                }
            }
        };
    }

    /**
     * org.xms.g.tasks.OnFailureListener.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.tasks.OnFailureListener.<br/>
     *
     * @param param0 the input object
     * @return casted OnFailureListener object
     */
    public static org.xms.g.tasks.OnFailureListener dynamicCast(java.lang.Object param0) {
        if (param0 instanceof org.xms.g.tasks.OnFailureListener) {
            return ((org.xms.g.tasks.OnFailureListener) param0);
        }
        if (param0 instanceof org.xms.g.utils.XGettable) {
            com.google.android.gms.tasks.OnFailureListener gReturn = ((com.google.android.gms.tasks.OnFailureListener) ((org.xms.g.utils.XGettable) param0).getGInstance());
            com.huawei.hmf.tasks.OnFailureListener hReturn = ((com.huawei.hmf.tasks.OnFailureListener) ((org.xms.g.utils.XGettable) param0).getHInstance());
            return new org.xms.g.tasks.OnFailureListener.XImpl(new org.xms.g.utils.XBox(gReturn, hReturn));
        }
        return ((org.xms.g.tasks.OnFailureListener) param0);
    }

    /**
     * org.xms.g.tasks.OnFailureListener.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
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
                return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hmf.tasks.OnFailureListener;
            } else {
                return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.tasks.OnFailureListener;
            }
        }
        return param0 instanceof org.xms.g.tasks.OnFailureListener;
    }

    /**
     * org.xms.g.tasks.OnFailureListener.XImpl : Listener called when a Task fails with an exception.<br/>
     * com.huawei.hmf.tasks.OnFailureListener : Called when a task fails.<br/>
     * com.google.android.gms.tasks.OnFailureListener : Listener called when a Task fails with an exception.<br/>
     */
    public static class XImpl extends org.xms.g.utils.XObject implements org.xms.g.tasks.OnFailureListener {
        /**
         * org.xms.g.tasks.OnFailureListener.XImpl.XImpl(org.xms.g.utils.XBox)  constructor of XImpl with XBox.<br/>
         *
         * @param xBox the wrapper of xms instance
         */
        public XImpl(org.xms.g.utils.XBox xBox) {
            super(xBox);
        }

        /**
         * org.xms.g.tasks.OnFailureListener.XImpl.onFailure(java.lang.Exception) Listener called when a Task fails with an exception.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.OnFailureListener.onFailure(java.lang.Exception) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/onfailurelistener-0000001050123093#EN-US_TOPIC_0000001050123093__section32821622269">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/onfailurelistener-0000001050123093#EN-US_TOPIC_0000001050123093__section32821622269</a><br/>
         * com.google.android.gms.tasks.OnFailureListener.onFailure(java.lang.Exception) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/OnFailureListener#public-abstract-void-onfailure-exception-e">https://developers.google.com/android/reference/com/google/android/gms/tasks/OnFailureListener#public-abstract-void-onfailure-exception-e</a><br/>
         *
         * @param param0 the exception that caused the Task to fail. Never null
         */
        public void onFailure(java.lang.Exception param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.OnFailureListener) this.getHInstance()).onFailure(param0)");
                ((com.huawei.hmf.tasks.OnFailureListener) this.getHInstance()).onFailure(param0);
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.OnFailureListener) this.getGInstance()).onFailure(param0)");
                ((com.google.android.gms.tasks.OnFailureListener) this.getGInstance()).onFailure(param0);
            }
        }
    }
}