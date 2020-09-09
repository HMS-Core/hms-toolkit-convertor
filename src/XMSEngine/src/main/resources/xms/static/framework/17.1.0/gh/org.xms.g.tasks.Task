package org.xms.g.tasks;

import org.xms.g.utils.Utils;

/**
 * Represents the results of work.<br/>
 * Combination of com.huawei.hmf.tasks.Task and com.google.android.gms.tasks.Task.<br/>
 * org.xms.g.tasks.Task<TResult> : Represents an asynchronous operation.<br/>
 * com.huawei.hmf.tasks.Task<TResult> : A basic class of a task.<br/>
 * com.google.android.gms.tasks.Task<TResult> : Represents an asynchronous operation.<br/>
 */
public abstract class Task<XTResult> extends org.xms.g.utils.XObject {
    /**
     * org.xms.g.tasks.Task.Task(org.xms.g.utils.XBox)  constructor of Task with XBox.<br/>
     *
     * @param param0 the wrapper of xms instance
     */
    public Task(org.xms.g.utils.XBox param0) {
        super(param0);
    }

    /**
     * org.xms.g.tasks.Task.addOnCanceledListener(org.xms.g.tasks.OnCanceledListener) Adds a listener that is called if the Task is canceled.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnCanceledListener(com.huawei.hmf.tasks.OnCanceledListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1725211411284">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1725211411284</a><br/>
     * com.google.android.gms.tasks.Task.addOnCanceledListener(com.google.android.gms.tasks.OnCanceledListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-oncanceledlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-oncanceledlistener-listener</a><br/>
     *
     * @param param0 Listener, which is called back after a task is canceled
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCanceledListener(org.xms.g.tasks.OnCanceledListener param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(((param0) == null ? null : (param0.getHInstanceOnCanceledListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(((param0) == null ? null : (param0.getHInstanceOnCanceledListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(((param0) == null ? null : (param0.getGInstanceOnCanceledListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(((param0) == null ? null : (param0.getGInstanceOnCanceledListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnCanceledListener(java.util.concurrent.Executor,org.xms.g.tasks.OnCanceledListener) Adds a listener that is called if the Task is canceled.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnCanceledListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnCanceledListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1055452883316">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1055452883316</a><br/>
     * com.google.android.gms.tasks.Task.addOnCanceledListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnCanceledListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-executor-executor,-oncanceledlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-executor-executor,-oncanceledlistener-listener</a><br/>
     *
     * @param param0 the executor to use to call the listener
     * @param param1 Listener, which is called back after a task is canceled
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCanceledListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnCanceledListener param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCanceledListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCanceledListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCanceledListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCanceledListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnCanceledListener(android.app.Activity,org.xms.g.tasks.OnCanceledListener) Adds an Activity-scoped listener that is called if the Task is canceled.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hmf.tasks.Task.addOnCanceledListener(android.app.Activity,com.huawei.hmf.tasks.OnCanceledListener) : <a href=""></a><br/>
     * com.google.android.gms.tasks.Task.addOnCanceledListener(android.app.Activity,com.google.android.gms.tasks.OnCanceledListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-activity-activity,-oncanceledlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncanceledlistener-activity-activity,-oncanceledlistener-listener</a><br/>
     *
     * @param param0 the scope of Activity
     * @param param1 Listener, which is called back after a task is canceled
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCanceledListener(android.app.Activity param0, org.xms.g.tasks.OnCanceledListener param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCanceledListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCanceledListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCanceledListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCanceledListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCanceledListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnCompleteListener(org.xms.g.tasks.OnCompleteListener<XTResult>) Adds a listener that is called when the Task completes.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnCompleteListener(com.huawei.hmf.tasks.OnCompleteListener<TResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section12529142112816">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section12529142112816</a><br/>
     * com.google.android.gms.tasks.Task.addOnCompleteListener(com.google.android.gms.tasks.OnCompleteListener<TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-oncompletelistenertresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-oncompletelistenertresult-listener</a><br/>
     *
     * @param param0 Listener, which is called back after task completion
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCompleteListener(org.xms.g.tasks.OnCompleteListener<XTResult> param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(((param0) == null ? null : (param0.getHInstanceOnCompleteListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(((param0) == null ? null : (param0.getHInstanceOnCompleteListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(((param0) == null ? null : (param0.getGInstanceOnCompleteListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(((param0) == null ? null : (param0.getGInstanceOnCompleteListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnCompleteListener(android.app.Activity,org.xms.g.tasks.OnCompleteListener<XTResult>) Adds an Activity-scoped listener that is called when the Task completes.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hmf.tasks.Task.addOnCompleteListener(android.app.Activity,com.huawei.hmf.tasks.OnCompleteListener<TResult>) : <a href=""></a><br/>
     * com.google.android.gms.tasks.Task.addOnCompleteListener(android.app.Activity,com.google.android.gms.tasks.OnCompleteListener<TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-activity-activity,-oncompletelistenertresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-activity-activity,-oncompletelistenertresult-listener</a><br/>
     *
     * @param param0 the scope of Activity
     * @param param1 Listener, which is called back after task completion
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCompleteListener(android.app.Activity param0, org.xms.g.tasks.OnCompleteListener<XTResult> param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCompleteListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCompleteListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCompleteListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCompleteListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnCompleteListener(java.util.concurrent.Executor,org.xms.g.tasks.OnCompleteListener<XTResult>) Adds a listener that is called when the Task completes.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hmf.tasks.Task.addOnCompleteListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnCompleteListener<TResult>) : <a href=""></a><br/>
     * com.google.android.gms.tasks.Task.addOnCompleteListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnCompleteListener<TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-executor-executor,-oncompletelistenertresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktresult-addoncompletelistener-executor-executor,-oncompletelistenertresult-listener</a><br/>
     *
     * @param param0 the executor to use to call the listener
     * @param param1 Listener, which is called back after task completion
     * @return this Task
     */
    public org.xms.g.tasks.Task<XTResult> addOnCompleteListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnCompleteListener<XTResult> param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCompleteListener())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getHInstanceOnCompleteListener())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCompleteListener())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnCompleteListener(param0, ((param1) == null ? null : (param1.getGInstanceOnCompleteListener())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.addOnFailureListener(android.app.Activity,org.xms.g.tasks.OnFailureListener) Adds an Activity-scoped listener that is called if the Task fails. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hmf.tasks.Task.addOnFailureListener(android.app.Activity, com.huawei.hmf.tasks.OnFailureListener) : <a href=""></a><br/>
     * com.google.android.gms.tasks.Task.addOnFailureListener(android.app.Activity,com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-activity-activity,-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-activity-activity,-onfailurelistener-listener</a><br/>
     *
     * @param param0 the scope of Activity
     * @param param1 Listener, which is called back after a task fails
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnFailureListener(android.app.Activity param0, org.xms.g.tasks.OnFailureListener param1);

    /**
     * org.xms.g.tasks.Task.addOnFailureListener(org.xms.g.tasks.OnFailureListener) Adds a listener that is called if the Task fails.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnFailureListener(com.huawei.hmf.tasks.OnFailureListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section321517595278">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section321517595278</a><br/>
     * com.google.android.gms.tasks.Task.addOnFailureListener(com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-onfailurelistener-listener</a><br/>
     *
     * @param param0 Listener, which is called back after a task fails
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnFailureListener(org.xms.g.tasks.OnFailureListener param0);

    /**
     * org.xms.g.tasks.Task.addOnFailureListener(java.util.concurrent.Executor,org.xms.g.tasks.OnFailureListener) Adds a listener that is called if the Task fails.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnFailureListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnFailureListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5793192813">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5793192813</a><br/>
     * com.google.android.gms.tasks.Task.addOnFailureListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-executor-executor,-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-executor-executor,-onfailurelistener-listener</a><br/>
     *
     * @param param0 the executor to use to call the listener
     * @param param1 Listener, which is called back after a task fails
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnFailureListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnFailureListener param1);

    /**
     * org.xms.g.tasks.Task.addOnSuccessListener(java.util.concurrent.Executor,org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds a listener that is called if the Task completes successfully.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnSuccessListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnSuccessListener<TResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section8249164516276">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section8249164516276</a><br/>
     * com.google.android.gms.tasks.Task.addOnSuccessListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-executor-executor,-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-executor-executor,-onsuccesslistener-super-tresult-listener</a><br/>
     *
     * @param param0 the executor to use to call the listener
     * @param param1 Listener, which is called back after a task is successfully completed
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnSuccessListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnSuccessListener<? super XTResult> param1);

    /**
     * org.xms.g.tasks.Task.addOnSuccessListener(org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds a listener that is called if the Task completes successfully.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.addOnSuccessListener(com.huawei.hmf.tasks.OnSuccessListener<TResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section947111462618">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section947111462618</a><br/>
     * com.google.android.gms.tasks.Task.addOnSuccessListener(com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-onsuccesslistener-super-tresult-listener</a><br/>
     *
     * @param param0 Listener, which is called back after a task is successfully completed
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnSuccessListener(org.xms.g.tasks.OnSuccessListener<? super XTResult> param0);

    /**
     * org.xms.g.tasks.Task.addOnSuccessListener(android.app.Activity,org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds an Activity-scoped listener that is called if the Task completes successfully.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below is the reference of GMS apis:<br/>
     * com.huawei.hmf.tasks.Task.addOnSuccessListener(android.app.Activity,com.huawei.hmf.tasks.OnSuccessListener<? super TResult>) : <a href=""></a><br/>
     * com.google.android.gms.tasks.Task.addOnSuccessListener(android.app.Activity,com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-activity-activity,-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-activity-activity,-onsuccesslistener-super-tresult-listener</a><br/>
     *
     * @param param0 the scope of Activity
     * @param param1 Listener, which is called back after a task is successfully completed
     * @return this Task
     */
    public abstract org.xms.g.tasks.Task<XTResult> addOnSuccessListener(android.app.Activity param0, org.xms.g.tasks.OnSuccessListener<? super XTResult> param1);

    /**
     * org.xms.g.tasks.Task.continueWith(org.xms.g.tasks.Continuation<XTResult, XTContinuationResult>) Returns a new Task that will be completed with the result of applying the specified Continuation to this Task.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.continueWith(com.huawei.hmf.tasks.Continuation<TResult, TContinuationResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section161011626193812">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section161011626193812</a><br/>
     * com.google.android.gms.tasks.Task.continueWith(com.google.android.gms.tasks.Continuation<TResult, TContinuationResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewith-continuationtresult,-tcontinuationresult-continuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewith-continuationtresult,-tcontinuationresult-continuation</a><br/>
     *
     * @param param0 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this Task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> continueWith(org.xms.g.tasks.Continuation<XTResult, XTContinuationResult> param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWith(((param0) == null ? null : (param0.getHInstanceContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWith(((param0) == null ? null : (param0.getHInstanceContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).continueWith(((param0) == null ? null : (param0.getGInstanceContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).continueWith(((param0) == null ? null : (param0.getGInstanceContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.continueWith(java.util.concurrent.Executor,org.xms.g.tasks.Continuation<XTResult, XTContinuationResult>) Returns a new Task that will be completed with the result of applying the specified Continuation to this Task.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.continueWith(java.util.concurrent.Executor,com.huawei.hmf.tasks.Continuation<TResult, TContinuationResult): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1257792715383">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1257792715383</a><br/>
     * com.google.android.gms.tasks.Task.continueWith(java.util.concurrent.Executor,com.google.android.gms.tasks.Continuation<TResult, TContinuationResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewith-executor-executor,-continuationtresult,-tcontinuationresult-continuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewith-executor-executor,-continuationtresult,-tcontinuationresult-continuation</a><br/>
     *
     * @param param0 the executor to use to call the Continuation
     * @param param1 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this Task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> continueWith(java.util.concurrent.Executor param0, org.xms.g.tasks.Continuation<XTResult, XTContinuationResult> param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWith(param0, ((param1) == null ? null : (param1.getHInstanceContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWith(param0, ((param1) == null ? null : (param1.getHInstanceContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).continueWith(param0, ((param1) == null ? null : (param1.getGInstanceContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).continueWith(param0, ((param1) == null ? null : (param1.getGInstanceContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.continueWithTask(org.xms.g.tasks.Continuation<XTResult, org.xms.g.tasks.Task<XTContinuationResult>>) Returns a new Task that will be completed with the result of applying the specified Continuation to this Task.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.continueWithTask(com.huawei.hmf.tasks.Continuation<TResult, com.huawei.hmf.tasks.Task<TContinuationResult>>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section14954352183613">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section14954352183613</a><br/>
     * com.google.android.gms.tasks.Task.continueWithTask(com.google.android.gms.tasks.Continuation<TResult, com.google.android.gms.tasks.Task<TContinuationResult>>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewithtask-continuationtresult,-tasktcontinuationresult-continuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewithtask-continuationtresult,-tasktcontinuationresult-continuation</a><br/>
     *
     * @param param0 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this Task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> continueWithTask(org.xms.g.tasks.Continuation<XTResult, org.xms.g.tasks.Task<XTContinuationResult>> param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWithTask(((param0) == null ? null : (param0.getHInstanceContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWithTask(((param0) == null ? null : (param0.getHInstanceContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).continueWithTask(((param0) == null ? null : (param0.getGInstanceContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).continueWithTask(((param0) == null ? null : (param0.getGInstanceContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.continueWithTask(java.util.concurrent.Executor,org.xms.g.tasks.Continuation<XTResult, org.xms.g.tasks.Task<XTContinuationResult>>) Returns a new Task that will be completed with the result of applying the specified Continuation to this Task.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.continueWithTask(java.util.concurrent.Executor,com.huawei.hmf.tasks.Continuation<TResult, com.huawei.hmf.tasks.Task<TContinuationResult>>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section161011626193812">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section161011626193812</a><br/>
     * com.google.android.gms.tasks.Task.continueWithTask(java.util.concurrent.Executor,com.google.android.gms.tasks.Continuation<TResult, com.google.android.gms.tasks.Task<TContinuationResult>>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewithtask-executor-executor,-continuationtresult,-tasktcontinuationresult-continuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-continuewithtask-executor-executor,-continuationtresult,-tasktcontinuationresult-continuation</a><br/>
     *
     * @param param0 the executor to use to call the Continuation
     * @param param1 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this Task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> continueWithTask(java.util.concurrent.Executor param0, org.xms.g.tasks.Continuation<XTResult, org.xms.g.tasks.Task<XTContinuationResult>> param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWithTask(param0, ((param1) == null ? null : (param1.getHInstanceContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).continueWithTask(param0, ((param1) == null ? null : (param1.getHInstanceContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).continueWithTask(param0, ((param1) == null ? null : (param1.getGInstanceContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).continueWithTask(param0, ((param1) == null ? null : (param1.getGInstanceContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.getException() Returns the exception that caused the Task to fail. Returns null if the Task is not yet complete, or completed successfully.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.getException(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section3718101619252">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section3718101619252</a><br/>
     * com.google.android.gms.tasks.Task.getException() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-exception-getexception">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-exception-getexception</a><br/>
     *
     * @return the exception that caused the Task to fail
     */
    public abstract java.lang.Exception getException();

    /**
     * org.xms.g.tasks.Task.getResult() Gets the result of the Task, if it has already completed.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.getResult(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1531714283207">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1531714283207</a><br/>
     * com.google.android.gms.tasks.Task.getResult() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult</a><br/>
     *
     * @return the result of the Task
     */
    public abstract XTResult getResult();

    /**
     * org.xms.g.tasks.Task.getResult(java.lang.Class<XX>) Gets the result of the Task, if it has already completed.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.getResult(java.lang.Class<X>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5883248192216">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5883248192216</a><br/>
     * com.google.android.gms.tasks.Task.getResult(java.lang.Class<X>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult-classx-exceptiontype">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult-classx-exceptiontype</a><br/>
     *
     * @param param0 Specified exception class
     * @return Result of the task
     * @throws XX the specified exception class
     */
    public abstract <XX extends java.lang.Throwable> XTResult getResult(java.lang.Class<XX> param0) throws XX;

    /**
     * org.xms.g.tasks.Task.isCanceled() Returns true if the Task is canceled; false otherwise.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.isCanceled(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1396563241912">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1396563241912</a><br/>
     * com.google.android.gms.tasks.Task.isCanceled() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscanceled">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscanceled</a><br/>
     *
     * @return true if the Task is canceled; false otherwise
     */
    public abstract boolean isCanceled();

    /**
     * org.xms.g.tasks.Task.isComplete() Returns true if the Task is complete; false otherwise.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.isComplete(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section32821622269">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section32821622269</a><br/>
     * com.google.android.gms.tasks.Task.isComplete() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscomplete">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscomplete</a><br/>
     *
     * @return true if the Task is complete; false otherwise
     */
    public abstract boolean isComplete();

    /**
     * org.xms.g.tasks.Task.isSuccessful() Returns true if the Task has completed successfully; false otherwise.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.isSuccessful(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1548820091913">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1548820091913</a><br/>
     * com.google.android.gms.tasks.Task.isSuccessful() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-issuccessful">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-issuccessful</a><br/>
     *
     * @return true if the Task has completed successfully; false otherwise
     */
    public abstract boolean isSuccessful();

    /**
     * org.xms.g.tasks.Task.onSuccessTask(java.util.concurrent.Executor,org.xms.g.tasks.SuccessContinuation<XTResult,XTContinuationResult>) Returns a new Task that will be completed with the result of applying the specified SuccessContinuation to this Task when this Task completes successfully. If the previous Task fails, the onSuccessTask completion will be skipped and failure listeners will be invoked.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.onSuccessTask(java.util.concurrent.Executor,com.huawei.hmf.tasks.SuccessContinuation<TResult,TContinuationResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section7451231163315">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section7451231163315</a><br/>
     * com.google.android.gms.tasks.Task.onSuccessTask(java.util.concurrent.Executor,com.google.android.gms.tasks.SuccessContinuation<TResult, TContinuationResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-onsuccesstask-executor-executor,-successcontinuationtresult,-tcontinuationresult-successcontinuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-onsuccesstask-executor-executor,-successcontinuationtresult,-tcontinuationresult-successcontinuation</a><br/>
     *
     * @param param0 the executor to use to call the SuccessContinuation
     * @param param1 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> onSuccessTask(java.util.concurrent.Executor param0, org.xms.g.tasks.SuccessContinuation<XTResult, XTContinuationResult> param1) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).onSuccessTask(param0, ((param1) == null ? null : (param1.getHInstanceSuccessContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).onSuccessTask(param0, ((param1) == null ? null : (param1.getHInstanceSuccessContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).onSuccessTask(param0, ((param1) == null ? null : (param1.getGInstanceSuccessContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).onSuccessTask(param0, ((param1) == null ? null : (param1.getGInstanceSuccessContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.onSuccessTask(org.xms.g.tasks.SuccessContinuation<XTResult,XTContinuationResult>) Returns a new Task that will be completed with the result of applying the specified SuccessContinuation to this Task when this Task completes successfully. If the previous Task fails, the onSuccessTask completion will be skipped and failure listeners will be invoked.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hmf.tasks.Task.onSuccessTask(com.huawei.hmf.tasks.SuccessContinuation<TResult,TContinuationResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1714010306338">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1714010306338</a><br/>
     * com.google.android.gms.tasks.Task.onSuccessTask(com.google.android.gms.tasks.SuccessContinuation<TResult,TContinuationResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-onsuccesstask-successcontinuationtresult,-tcontinuationresult-successcontinuation">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-tasktcontinuationresult-onsuccesstask-successcontinuationtresult,-tcontinuationresult-successcontinuation</a><br/>
     *
     * @param param0 API implementation instance, which implements the then API and is used to call the subsequent task after the current task is successfully completed
     * @return this task
     */
    public <XTContinuationResult> org.xms.g.tasks.Task<XTContinuationResult> onSuccessTask(org.xms.g.tasks.SuccessContinuation<XTResult, XTContinuationResult> param0) {
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).onSuccessTask(((param0) == null ? null : (param0.getHInstanceSuccessContinuation())))");
            com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).onSuccessTask(((param0) == null ? null : (param0.getHInstanceSuccessContinuation())));
            return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
        } else {
            org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).onSuccessTask(((param0) == null ? null : (param0.getGInstanceSuccessContinuation())))");
            com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).onSuccessTask(((param0) == null ? null : (param0.getGInstanceSuccessContinuation())));
            return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
        }
    }

    /**
     * org.xms.g.tasks.Task.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.tasks.Task.<br/>
     *
     * @param param0 the input object
     * @return casted Task object
     */
    public static org.xms.g.tasks.Task dynamicCast(java.lang.Object param0) {
        return ((org.xms.g.tasks.Task) param0);
    }

    /**
     * org.xms.g.tasks.Task.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hmf.tasks.Task;
        } else {
            return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.tasks.Task;
        }
    }

    /**
     * org.xms.g.tasks.Task.XImpl<TResult> : Represents an asynchronous operation.<br/>
     * com.huawei.hmf.tasks.Task<TResult> : A basic class of a task.<br/>
     * com.google.android.gms.tasks.Task<TResult> : Represents an asynchronous operation.<br/>
     */
    public static class XImpl<XTResult> extends org.xms.g.tasks.Task<XTResult> {
        /**
         * org.xms.g.tasks.Task.XImpl.XImpl(org.xms.g.utils.XBox)  constructor of XImpl with XBox.<br/>
         *
         * @param param0 the wrapper of xms instance
         */
        public XImpl(org.xms.g.utils.XBox param0) {
            super(param0);
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnFailureListener(android.app.Activity,org.xms.g.tasks.OnFailureListener) Adds an Activity-scoped listener that is called if the Task fails. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below is the reference of GMS apis:<br/>
         * com.huawei.hmf.tasks.Task.addOnFailureListener(android.app.Activity, com.huawei.hmf.tasks.OnFailureListener) : <a href=""></a><br/>
         * com.google.android.gms.tasks.Task.addOnFailureListener(android.app.Activity,com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-activity-activity,-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-activity-activity,-onfailurelistener-listener</a><br/>
         *
         * @param param0 the scope of Activity
         * @param param1 Listener, which is called back after a task fails
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnFailureListener(android.app.Activity param0, org.xms.g.tasks.OnFailureListener param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getHInstanceOnFailureListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getHInstanceOnFailureListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getGInstanceOnFailureListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getGInstanceOnFailureListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnFailureListener(org.xms.g.tasks.OnFailureListener) Adds a listener that is called if the Task fails.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.addOnFailureListener(com.huawei.hmf.tasks.OnFailureListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section321517595278">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section321517595278</a><br/>
         * com.google.android.gms.tasks.Task.addOnFailureListener(com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-onfailurelistener-listener</a><br/>
         *
         * @param param0 Listener, which is called back after a task fails
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnFailureListener(org.xms.g.tasks.OnFailureListener param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(((param0) == null ? null : (param0.getHInstanceOnFailureListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(((param0) == null ? null : (param0.getHInstanceOnFailureListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(((param0) == null ? null : (param0.getGInstanceOnFailureListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(((param0) == null ? null : (param0.getGInstanceOnFailureListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnFailureListener(java.util.concurrent.Executor,org.xms.g.tasks.OnFailureListener) Adds a listener that is called if the Task fails.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.addOnFailureListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnFailureListener): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5793192813">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5793192813</a><br/>
         * com.google.android.gms.tasks.Task.addOnFailureListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnFailureListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-executor-executor,-onfailurelistener-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonfailurelistener-executor-executor,-onfailurelistener-listener</a><br/>
         *
         * @param param0 the executor to use to call the listener
         * @param param1 Listener, which is called back after a task fails
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnFailureListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnFailureListener param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getHInstanceOnFailureListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getHInstanceOnFailureListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getGInstanceOnFailureListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnFailureListener(param0, ((param1) == null ? null : (param1.getGInstanceOnFailureListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnSuccessListener(java.util.concurrent.Executor,org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds a listener that is called if the Task completes successfully.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.addOnSuccessListener(java.util.concurrent.Executor,com.huawei.hmf.tasks.OnSuccessListener<TResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section8249164516276">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section8249164516276</a><br/>
         * com.google.android.gms.tasks.Task.addOnSuccessListener(java.util.concurrent.Executor,com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-executor-executor,-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-executor-executor,-onsuccesslistener-super-tresult-listener</a><br/>
         *
         * @param param0 the executor to use to call the listener
         * @param param1 Listener, which is called back after a task is successfully completed
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnSuccessListener(java.util.concurrent.Executor param0, org.xms.g.tasks.OnSuccessListener<? super XTResult> param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getHInstanceOnSuccessListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getHInstanceOnSuccessListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getGInstanceOnSuccessListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getGInstanceOnSuccessListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnSuccessListener(org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds a listener that is called if the Task completes successfully.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.addOnSuccessListener(com.huawei.hmf.tasks.OnSuccessListener<TResult>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section947111462618">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section947111462618</a><br/>
         * com.google.android.gms.tasks.Task.addOnSuccessListener(com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-onsuccesslistener-super-tresult-listener</a><br/>
         *
         * @param param0 Listener, which is called back after a task is successfully completed
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnSuccessListener(org.xms.g.tasks.OnSuccessListener<? super XTResult> param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(((param0) == null ? null : (param0.getHInstanceOnSuccessListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(((param0) == null ? null : (param0.getHInstanceOnSuccessListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(((param0) == null ? null : (param0.getGInstanceOnSuccessListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(((param0) == null ? null : (param0.getGInstanceOnSuccessListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.addOnSuccessListener(android.app.Activity,org.xms.g.tasks.OnSuccessListener<? super XTResult>) Adds an Activity-scoped listener that is called if the Task completes successfully.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below is the reference of GMS apis:<br/>
         * com.huawei.hmf.tasks.Task.addOnSuccessListener(android.app.Activity,com.huawei.hmf.tasks.OnSuccessListener<? super TResult>) : <a href=""></a><br/>
         * com.google.android.gms.tasks.Task.addOnSuccessListener(android.app.Activity,com.google.android.gms.tasks.OnSuccessListener<? super TResult>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-activity-activity,-onsuccesslistener-super-tresult-listener">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tasktresult-addonsuccesslistener-activity-activity,-onsuccesslistener-super-tresult-listener</a><br/>
         *
         * @param param0 the scope of Activity
         * @param param1 Listener, which is called back after a task is successfully completed
         * @return this Task
         */
        public org.xms.g.tasks.Task<XTResult> addOnSuccessListener(android.app.Activity param0, org.xms.g.tasks.OnSuccessListener<? super XTResult> param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getHInstanceOnSuccessListener())))");
                com.huawei.hmf.tasks.Task hReturn = ((com.huawei.hmf.tasks.Task) this.getHInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getHInstanceOnSuccessListener())));
                return ((hReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getGInstanceOnSuccessListener())))");
                com.google.android.gms.tasks.Task gReturn = ((com.google.android.gms.tasks.Task) this.getGInstance()).addOnSuccessListener(param0, ((param1) == null ? null : (param1.getGInstanceOnSuccessListener())));
                return ((gReturn) == null ? null : (new org.xms.g.tasks.Task.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.getException() Returns the exception that caused the Task to fail. Returns null if the Task is not yet complete, or completed successfully.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.getException(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section3718101619252">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section3718101619252</a><br/>
         * com.google.android.gms.tasks.Task.getException() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-exception-getexception">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-exception-getexception</a><br/>
         *
         * @return the exception that caused the Task to fail
         */
        public java.lang.Exception getException() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).getException()");
                return (java.lang.Exception) Utils.getXmsObjectWithHmsObject(((com.huawei.hmf.tasks.Task) this.getHInstance()).getException());
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).getException()");
                return (java.lang.Exception) Utils.getXmsObjectWithGmsObject(((com.google.android.gms.tasks.Task) this.getGInstance()).getException());
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.getResult() Gets the result of the Task, if it has already completed.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.getResult(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1531714283207">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1531714283207</a><br/>
         * com.google.android.gms.tasks.Task.getResult() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult</a><br/>
         *
         * @return the result of the Task
         */
        public XTResult getResult() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).getResult()");
                java.lang.Object hmsObj = ((com.huawei.hmf.tasks.Task) this.getHInstance()).getResult();
                return ((XTResult) org.xms.g.utils.Utils.getXmsObjectWithHmsObject(hmsObj));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).getResult()");
                java.lang.Object gmsObj = ((com.google.android.gms.tasks.Task) this.getGInstance()).getResult();
                return ((XTResult) org.xms.g.utils.Utils.getXmsObjectWithGmsObject(gmsObj));
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.getResult(java.lang.Class<XX>) Gets the result of the Task, if it has already completed.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.getResult(java.lang.Class<X>): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5883248192216">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section5883248192216</a><br/>
         * com.google.android.gms.tasks.Task.getResult(java.lang.Class<X>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult-classx-exceptiontype">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-tresult-getresult-classx-exceptiontype</a><br/>
         *
         * @param param0 Specified exception class
         * @return Result of the task
         * @throws XX the specified exception class
         */
        public <XX extends java.lang.Throwable> XTResult getResult(java.lang.Class<XX> param0) throws XX {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                try {
                    java.lang.Class hObj0 = ((java.lang.Class) org.xms.g.utils.Utils.getHmsClassWithXmsClass(param0));
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).getResultThrowException(hObj0)");
                    java.lang.Object hmsObj = ((com.huawei.hmf.tasks.Task) this.getHInstance()).getResultThrowException(hObj0);
                    return ((XTResult) org.xms.g.utils.Utils.getXmsObjectWithHmsObject(hmsObj));
                }
                catch (java.lang.Throwable e) {
                    throw ((XX) org.xms.g.utils.Utils.getXmsObjectWithHmsObject(e));
                }
            } else {
                try {
                    java.lang.Class gObj0 = ((java.lang.Class) org.xms.g.utils.Utils.getGmsClassWithXmsClass(param0));
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).getResult(gObj0)");
                    java.lang.Object gmsObj = ((com.google.android.gms.tasks.Task) this.getGInstance()).getResult(gObj0);
                    return ((XTResult) org.xms.g.utils.Utils.getXmsObjectWithGmsObject(gmsObj));
                }
                catch (java.lang.Throwable e) {
                    throw ((XX) org.xms.g.utils.Utils.getXmsObjectWithGmsObject(e));
                }
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.isCanceled() Returns true if the Task is canceled; false otherwise.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.isCanceled(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1396563241912">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1396563241912</a><br/>
         * com.google.android.gms.tasks.Task.isCanceled() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscanceled">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscanceled</a><br/>
         *
         * @return true if the Task is canceled; false otherwise
         */
        public boolean isCanceled() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).isCanceled()");
                return ((com.huawei.hmf.tasks.Task) this.getHInstance()).isCanceled();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).isCanceled()");
                return ((com.google.android.gms.tasks.Task) this.getGInstance()).isCanceled();
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.isComplete() Returns true if the Task is complete; false otherwise.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.isComplete(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section32821622269">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section32821622269</a><br/>
         * com.google.android.gms.tasks.Task.isComplete() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscomplete">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-iscomplete</a><br/>
         *
         * @return true if the Task is complete; false otherwise
         */
        public boolean isComplete() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).isComplete()");
                return ((com.huawei.hmf.tasks.Task) this.getHInstance()).isComplete();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).isComplete()");
                return ((com.google.android.gms.tasks.Task) this.getGInstance()).isComplete();
            }
        }

        /**
         * org.xms.g.tasks.Task.XImpl.isSuccessful() Returns true if the Task has completed successfully; false otherwise.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hmf.tasks.Task.isSuccessful(): <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1548820091913">https://developer.huawei.com/consumer/en/doc/HMSCore-References-V5/task_tresult-0000001050121148-V5#EN-US_TOPIC_0000001050121148__section1548820091913</a><br/>
         * com.google.android.gms.tasks.Task.isSuccessful() : <a href="https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-issuccessful">https://developers.google.com/android/reference/com/google/android/gms/tasks/Task#public-abstract-boolean-issuccessful</a><br/>
         *
         * @return true if the Task has completed successfully; false otherwise
         */
        public boolean isSuccessful() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hmf.tasks.Task) this.getHInstance()).isSuccessful()");
                return ((com.huawei.hmf.tasks.Task) this.getHInstance()).isSuccessful();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.tasks.Task) this.getGInstance()).isSuccessful()");
                return ((com.google.android.gms.tasks.Task) this.getGInstance()).isSuccessful();
            }
        }
    }
}