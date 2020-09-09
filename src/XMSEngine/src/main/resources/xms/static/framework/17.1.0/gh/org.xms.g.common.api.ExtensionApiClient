package org.xms.g.common.api;

/**
 * org.xms.g.common.api.ExtensionApiClient: The main entry point for services integration.<br/>
 * Combination of com.huawei.hms.api.HuaweiApiClient and com.google.android.gms.common.api.GoogleApiClient.<br/>
 * com.huawei.hms.api.HuaweiApiClient : <br/>
 * com.google.android.gms.common.api.GoogleApiClient : The main entry point for Google Play services integration.<br/>
 */
public abstract class ExtensionApiClient extends org.xms.g.utils.XObject {
    private boolean wrapper = true;

    /**
     * org.xms.g.common.api.ExtensionApiClient.ExtensionApiClient(org.xms.g.utils.XBox)  constructor of ExtensionApiClient with XBox.<br/>
     *
     * @param param0 the wrapper of xms instance
     */
    public ExtensionApiClient(org.xms.g.utils.XBox param0) {
        super(param0);
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.ExtensionApiClient() constructor of ExtensionApiClient.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.HuaweiApiClient() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.GoogleApiClient() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-googleapiclient">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-googleapiclient</a><br/>
     *
     */
    public ExtensionApiClient() {
        super((org.xms.g.utils.XBox) null);
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            this.setHInstance(new HImpl());
        } else {
            this.setGInstance(new GImpl());
        }
        wrapper = false;
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public static int getSIGN_IN_MODE_OPTIONAL() {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * XMS does not provide this api.<br/>
     */
    public static int getSIGN_IN_MODE_REQUIRED() {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.blockingConnect() Connects the client to Google Play services. Blocks until the connection either succeeds or fails.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.holdUpConnect() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.blockingConnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect</a><br/>
     *
     * @return the result of the connection
     */
    public abstract org.xms.g.common.ConnectionResult blockingConnect();

    /**
     * org.xms.g.common.api.ExtensionApiClient.blockingConnect(long,java.util.concurrent.TimeUnit) Connects the client to Google Play services. Blocks until the connection either succeeds or fails, or the timeout is reached.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.holdUpConnect(long,java.util.concurrent.TimeUnit) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.blockingConnect(long,java.util.concurrent.TimeUnit) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect-long-timeout,-timeunit-unit">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect-long-timeout,-timeunit-unit</a><br/>
     *
     * @param param0 the maximum time to wait
     * @param param1 the time unit of the timeout argument
     * @return the result of the connection
     */
    public abstract org.xms.g.common.ConnectionResult blockingConnect(long param0, java.util.concurrent.TimeUnit param1);

    /**
     * org.xms.g.common.api.ExtensionApiClient.clearDefaultAccountAndReconnect() Clears the account selected by the user and reconnects the client asking the user to pick an account again if useDefaultAccount() was set.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.discardAndReconnect() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.clearDefaultAccountAndReconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-pendingresultstatus-cleardefaultaccountandreconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-pendingresultstatus-cleardefaultaccountandreconnect</a><br/>
     *
     * @return the pending result is fired once the default account has been cleared, but before the client is reconnected - for that ConnectionCallbacks can be used
     */
    public abstract org.xms.g.common.api.PendingResult<org.xms.g.common.api.Status> clearDefaultAccountAndReconnect();

    /**
     * org.xms.g.common.api.ExtensionApiClient.connect(int) Connects the client to Google Play services using the given sign in mode.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.connect(int) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.connect(int) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-void-connect-int-signinmode">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-void-connect-int-signinmode</a><br/>
     *
     * @param param0 signIn Mode
     */
    public void connect(int param0) {
        if (wrapper) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).connect(param0)");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).connect(param0);
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).connect(param0)");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).connect(param0);
            }
        } else {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                ((HImpl) ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance())).connectCallSuper(param0);
            } else {
                ((GImpl) ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance())).connectCallSuper(param0);
            }
        }
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.connect() Connects the client to Google Play services. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.connect(null) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.connect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-connect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-connect</a><br/>
     *
     */
    public abstract void connect();

    /**
     * org.xms.g.common.api.ExtensionApiClient.disconnect() Closes the connection to Google Play services.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.disconnect() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.disconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-disconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-disconnect</a><br/>
     *
     */
    public abstract void disconnect();

    /**
     * org.xms.g.common.api.ExtensionApiClient.dump(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) Prints the GoogleApiClient's state into the given stream.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.print(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.dump(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-dump-string-prefix,-filedescriptor-fd,-printwriter-writer,-string[]-args">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-dump-string-prefix,-filedescriptor-fd,-printwriter-writer,-string[]-args</a><br/>
     *
     * @param param0    Desired prefix to prepend at each line of output
     * @param param1    The raw file descriptor that the dump is being sent to
     * @param param2    The PrintWriter to use for writing the dump
     * @param param3	Additional arguments to the dump request
     */
    public abstract void dump(java.lang.String param0, java.io.FileDescriptor param1, java.io.PrintWriter param2, java.lang.String[] param3);

    /**
     * XMS does not provide this api.<br/>
     */
    public static void dumpAll(java.lang.String param0, java.io.FileDescriptor param1, java.io.PrintWriter param2, java.lang.String[] param3) {
        throw new java.lang.RuntimeException("Not Supported");
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.getConnectionResult(org.xms.g.common.api.Api<?>) Returns the ConnectionResult for the GoogleApiClient's connection to the specified API.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.getConnectionResult(com.huawei.hms.api.Api<?>) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.getConnectionResult(com.google.android.gms.common.api.Api<?>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-getconnectionresult-api-api">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-getconnectionresult-api-api</a><br/>
     *
     * @param param0 The Api to retrieve the ConnectionResult of. Passing an API that was not registered with the GoogleApiClient results in an IllegalArgumentException
     * @return the ConnectionResult
     */
    public abstract org.xms.g.common.ConnectionResult getConnectionResult(org.xms.g.common.api.Api<?> param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.hasConnectedApi(org.xms.g.common.api.Api<?>) Returns whether or not this GoogleApiClient has the specified API in a connected state.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.hasConnectedApi(com.huawei.hms.api.Api<?>) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.hasConnectedApi(com.google.android.gms.common.api.Api<?>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-hasconnectedapi-api-api">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-hasconnectedapi-api-api</a><br/>
     *
     * @param param0 The Api to test the connection of
     * @return true if or not this GoogleApiClient has the specified API in a connected state
     */
    public abstract boolean hasConnectedApi(org.xms.g.common.api.Api<?> param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.isConnected() Checks if the client is currently connected to the service, so that requests to other methods will succeed. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.isConnected() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.isConnected() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnected">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnected</a><br/>
     *
     * @return true if the client is connected to the service
     */
    public abstract boolean isConnected();

    /**
     * org.xms.g.common.api.ExtensionApiClient.isConnecting() Checks if the client is attempting to connect to the service.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.isConnecting() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.isConnecting() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnecting">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnecting</a><br/>
     *
     * @return true if the client is attempting to connect to the service
     */
    public abstract boolean isConnecting();

    /**
     * org.xms.g.common.api.ExtensionApiClient.isConnectionCallbacksRegistered(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Returns true if the specified listener is currently registered to receive connection events.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.hasConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.isConnectionCallbacksRegistered(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectioncallbacksregistered-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectioncallbacksregistered-googleapiclient.connectioncallbacks-listener</a><br/>
     *
     * @param param0 The listener to check for
     * @return true if the specified listener is currently registered to receive connection events
     */
    public abstract boolean isConnectionCallbacksRegistered(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.isConnectionFailedListenerRegistered(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Returns true if the specified listener is currently registered to receive connection failed events.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.hasConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClient.isConnectionFailedListenerRegistered(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectionfailedlistenerregistered-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectionfailedlistenerregistered-googleapiclient.onconnectionfailedlistener-listener</a><br/>
     *
     * @param param0 The listener to check for
     * @return true if the specified listener is currently registered to receive connection failed events
     */
    public abstract boolean isConnectionFailedListenerRegistered(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.reconnect() Closes the current connection to Google Play services and creates a new connection.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.reconnect() : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.reconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-reconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-reconnect</a><br/>
     *
     */
    public abstract void reconnect();

    /**
     * org.xms.g.common.api.ExtensionApiClient.registerConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Registers a listener to receive connection events from this GoogleApiClient.<br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.setConnectionCallbacks(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks): <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.registerConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectioncallbacks-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectioncallbacks-googleapiclient.connectioncallbacks-listener</a><br/>
     *
     * @param param0 the listener where the results of the asynchronous connect() call are delivered
     */
    public abstract void registerConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.registerConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Registers a listener to receive connection failed events from this GoogleApiClient. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.setConnectionFailedListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.registerConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener</a><br/>
     *
     * @param param0 the listener where the results of the asynchronous connect() call are delivered
     */
    public abstract void registerConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.stopAutoManage(androidx.fragment.app.FragmentActivity) Disconnects the client and stops automatic lifecycle management. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.disableLifeCycleManagement(androidx.fragment.app.FragmentActivity) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.stopAutoManage(androidx.fragment.app.FragmentActivity) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-stopautomanage-fragmentactivity-lifecycleactivity">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-stopautomanage-fragmentactivity-lifecycleactivity</a><br/>
     *
     * @param param0 the activity managing the client's lifecycle
     * @throws java.lang.IllegalStateException if called from outside of the main thread
     */
    public abstract void stopAutoManage(androidx.fragment.app.FragmentActivity param0) throws java.lang.IllegalStateException;

    /**
     * org.xms.g.common.api.ExtensionApiClient.unregisterConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Removes a connection listener from this GoogleApiClientbr/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.removeConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.unregisterConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectioncallbacks-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectioncallbacks-googleapiclient.connectioncallbacks-listener</a><br/>
     *
     * @param param0 the listener to unregister
     */
    public abstract void unregisterConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.unregisterConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Removes a connection failed listener from the GoogleApiClient. <br/>
     *
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.api.HuaweiApiClient.removeConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
     * com.google.android.gms.common.api.GoogleApiClien.unregisterConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener</a><br/>
     *
     * @param param0 the listener to unregister
     */
    public abstract void unregisterConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0);

    /**
     * org.xms.g.common.api.ExtensionApiClient.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.api.ExtensionApiClient.<br/>
     * <p>
     *
     * @param param0 the input object
     * @return casted ExtensionApiClient object
     */
    public static org.xms.g.common.api.ExtensionApiClient dynamicCast(java.lang.Object param0) {
        if (param0 instanceof org.xms.g.common.api.ExtensionApiClient) {
            return ((org.xms.g.common.api.ExtensionApiClient) param0);
        }
        if (param0 instanceof org.xms.g.utils.XGettable) {
            com.google.android.gms.common.api.GoogleApiClient gReturn = ((com.google.android.gms.common.api.GoogleApiClient) ((org.xms.g.utils.XGettable) param0).getGInstance());
            com.huawei.hms.api.HuaweiApiClient hReturn = ((com.huawei.hms.api.HuaweiApiClient) ((org.xms.g.utils.XGettable) param0).getHInstance());
            return new org.xms.g.common.api.ExtensionApiClient.XImpl(new org.xms.g.utils.XBox(gReturn, hReturn));
        }
        return ((org.xms.g.common.api.ExtensionApiClient) param0);
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
            return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.api.HuaweiApiClient;
        } else {
            return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.api.GoogleApiClient;
        }
    }

    private class GImpl extends com.google.android.gms.common.api.GoogleApiClient {

        public void connect(int param0) {
            org.xms.g.common.api.ExtensionApiClient.this.connect(param0);
        }

        public void connectCallSuper(int param0) {
            super.connect(param0);
        }

        public com.google.android.gms.common.ConnectionResult blockingConnect() {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.blockingConnect();
            return ((com.google.android.gms.common.ConnectionResult) ((xResult) == null ? null : (xResult.getGInstance())));
        }

        public com.google.android.gms.common.ConnectionResult blockingConnect(long param0, java.util.concurrent.TimeUnit param1) {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.blockingConnect(param0, param1);
            return ((com.google.android.gms.common.ConnectionResult) ((xResult) == null ? null : (xResult.getGInstance())));
        }

        public com.google.android.gms.common.api.PendingResult<com.google.android.gms.common.api.Status> clearDefaultAccountAndReconnect() {
            org.xms.g.common.api.PendingResult xResult = org.xms.g.common.api.ExtensionApiClient.this.clearDefaultAccountAndReconnect();
            return ((com.google.android.gms.common.api.PendingResult) ((xResult) == null ? null : (xResult.getGInstance())));
        }

        public void connect() {
            org.xms.g.common.api.ExtensionApiClient.this.connect();
        }

        public void disconnect() {
            org.xms.g.common.api.ExtensionApiClient.this.disconnect();
        }

        public void dump(java.lang.String param0, java.io.FileDescriptor param1, java.io.PrintWriter param2, java.lang.String[] param3) {
            org.xms.g.common.api.ExtensionApiClient.this.dump(param0, param1, param2, param3);
        }

        public com.google.android.gms.common.ConnectionResult getConnectionResult(com.google.android.gms.common.api.Api<?> param0) {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.getConnectionResult(((param0) == null ? null : (new org.xms.g.common.api.Api(new org.xms.g.utils.XBox(param0, null)))));
            return ((com.google.android.gms.common.ConnectionResult) ((xResult) == null ? null : (xResult.getGInstance())));
        }

        public boolean hasConnectedApi(com.google.android.gms.common.api.Api<?> param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.hasConnectedApi(((param0) == null ? null : (new org.xms.g.common.api.Api(new org.xms.g.utils.XBox(param0, null)))));
        }

        public boolean isConnected() {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnected();
        }

        public boolean isConnecting() {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnecting();
        }

        public boolean isConnectionCallbacksRegistered(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnectionCallbacksRegistered(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public boolean isConnectionFailedListenerRegistered(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnectionFailedListenerRegistered(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public void reconnect() {
            org.xms.g.common.api.ExtensionApiClient.this.reconnect();
        }

        public void registerConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks param0) {
            org.xms.g.common.api.ExtensionApiClient.this.registerConnectionCallbacks(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public void registerConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener param0) {
            org.xms.g.common.api.ExtensionApiClient.this.registerConnectionFailedListener(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public void stopAutoManage(androidx.fragment.app.FragmentActivity param0) throws java.lang.IllegalStateException {
            org.xms.g.common.api.ExtensionApiClient.this.stopAutoManage(param0);
        }

        public void unregisterConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks param0) {
            org.xms.g.common.api.ExtensionApiClient.this.unregisterConnectionCallbacks(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public void unregisterConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener param0) {
            org.xms.g.common.api.ExtensionApiClient.this.unregisterConnectionFailedListener(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(param0, null)))));
        }

        public GImpl() {
            super();
        }
    }

    private class HImpl extends com.huawei.hms.api.HuaweiApiClient {

        public void connect(int param0) {
            org.xms.g.common.api.ExtensionApiClient.this.connect(param0);
        }

        public void connectCallSuper(int param0) {
            super.connect(param0);
        }

        public com.huawei.hms.api.ConnectionResult holdUpConnect() {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.blockingConnect();
            return ((com.huawei.hms.api.ConnectionResult) ((xResult) == null ? null : (xResult.getHInstance())));
        }

        public com.huawei.hms.api.ConnectionResult holdUpConnect(long param0, java.util.concurrent.TimeUnit param1) {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.blockingConnect(param0, param1);
            return ((com.huawei.hms.api.ConnectionResult) ((xResult) == null ? null : (xResult.getHInstance())));
        }

        public com.huawei.hms.support.api.client.PendingResult<com.huawei.hms.support.api.client.Status> discardAndReconnect() {
            org.xms.g.common.api.PendingResult xResult = org.xms.g.common.api.ExtensionApiClient.this.clearDefaultAccountAndReconnect();
            return ((com.huawei.hms.support.api.client.PendingResult) ((xResult) == null ? null : (xResult.getHInstance())));
        }

        public void disconnect() {
            org.xms.g.common.api.ExtensionApiClient.this.disconnect();
        }

        public void print(java.lang.String param0, java.io.FileDescriptor param1, java.io.PrintWriter param2, java.lang.String[] param3) {
            org.xms.g.common.api.ExtensionApiClient.this.dump(param0, param1, param2, param3);
        }

        public com.huawei.hms.api.ConnectionResult getConnectionResult(com.huawei.hms.api.Api<?> param0) {
            org.xms.g.common.ConnectionResult xResult = org.xms.g.common.api.ExtensionApiClient.this.getConnectionResult(((param0) == null ? null : (new org.xms.g.common.api.Api(new org.xms.g.utils.XBox(null, param0)))));
            return ((com.huawei.hms.api.ConnectionResult) ((xResult) == null ? null : (xResult.getHInstance())));
        }

        public boolean hasConnectedApi(com.huawei.hms.api.Api<?> param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.hasConnectedApi(((param0) == null ? null : (new org.xms.g.common.api.Api(new org.xms.g.utils.XBox(null, param0)))));
        }

        public boolean isConnected() {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnected();
        }

        public boolean isConnecting() {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnecting();
        }

        public boolean hasConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnectionCallbacksRegistered(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public boolean hasConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener param0) {
            return org.xms.g.common.api.ExtensionApiClient.this.isConnectionFailedListenerRegistered(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public void reconnect() {
            org.xms.g.common.api.ExtensionApiClient.this.reconnect();
        }

        public void setConnectionCallbacks(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks param0) {
            org.xms.g.common.api.ExtensionApiClient.this.registerConnectionCallbacks(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public void setConnectionFailedListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener param0) {
            org.xms.g.common.api.ExtensionApiClient.this.registerConnectionFailedListener(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public java.util.List<com.huawei.hms.support.api.entity.auth.Scope> getScopes() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.util.List<com.huawei.hms.support.api.entity.auth.PermissionInfo> getPermissionInfos() {
            throw new java.lang.RuntimeException("Stub");
        }

        public void disableLifeCycleManagement(android.app.Activity param0) {
            throw new java.lang.RuntimeException("Stub");
        }

        public void removeConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks param0) {
            org.xms.g.common.api.ExtensionApiClient.this.unregisterConnectionCallbacks(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public void removeConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener param0) {
            org.xms.g.common.api.ExtensionApiClient.this.unregisterConnectionFailedListener(((param0) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(null, param0)))));
        }

        public void connect(android.app.Activity param0) {
            throw new java.lang.RuntimeException("Stub");
        }

        public void connectForeground() {
            throw new java.lang.RuntimeException("Stub");
        }

        public boolean setSubAppInfo(com.huawei.hms.support.api.client.SubAppInfo param0) {
            throw new java.lang.RuntimeException("Stub");
        }

        public void checkUpdate(android.app.Activity param0, com.huawei.hms.api.CheckUpdatelistener param1) {
            throw new java.lang.RuntimeException("Stub");
        }

        public void onResume(android.app.Activity param0) {
            throw new java.lang.RuntimeException("Stub");
        }

        public void onPause(android.app.Activity param0) {
            throw new java.lang.RuntimeException("Stub");
        }

        public android.app.Activity getTopActivity() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.util.Map<com.huawei.hms.api.Api<?>, com.huawei.hms.api.Api.ApiOptions> getApiMap() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.util.List<java.lang.String> getApiNameList() {
            throw new java.lang.RuntimeException("Stub");
        }

        public com.huawei.hms.core.aidl.d getService() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.lang.String getTransportName() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.lang.String getAppID() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.lang.String getCpID() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.lang.String getSessionId() {
            throw new java.lang.RuntimeException("Stub");
        }

        public java.lang.String getPackageName() {
            throw new java.lang.RuntimeException("Stub");
        }

        public com.huawei.hms.support.api.client.SubAppInfo getSubAppInfo() {
            throw new java.lang.RuntimeException("Stub");
        }

        public android.content.Context getContext() {
            throw new java.lang.RuntimeException("Stub");
        }

        public HImpl() {
            super();
        }
    }

    /**
     * org.xms.g.common.api.ExtensionApiClient.XImpl: Wrapper class of ExtensionApiClient which is the main entry point for services integration.<br/>
     * com.huawei.hms.api.HuaweiApiClient : <br/>
     * com.google.android.gms.common.api.GoogleApiClient : The main entry point for Google Play services integration.<br/>
     */
    public static class XImpl extends org.xms.g.common.api.ExtensionApiClient {

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.XImpl(org.xms.g.utils.XBox)  constructor of XImpl with XBox.<br/>
         *
         * @param param0 the wrapper of xms instance
         */
        public XImpl(org.xms.g.utils.XBox param0) {
            super(param0);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.blockingConnect() Connects the client to Google Play services. Blocks until the connection either succeeds or fails.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.holdUpConnect() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.blockingConnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect</a><br/>
         *
         * @return the result of the connection
         */
        public org.xms.g.common.ConnectionResult blockingConnect() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).holdUpConnect()");
                com.huawei.hms.api.ConnectionResult hReturn = ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).holdUpConnect();
                return ((hReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).blockingConnect()");
                com.google.android.gms.common.ConnectionResult gReturn = ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).blockingConnect();
                return ((gReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.blockingConnect(long,java.util.concurrent.TimeUnit) Connects the client to Google Play services. Blocks until the connection either succeeds or fails, or the timeout is reached.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.holdUpConnect(long,java.util.concurrent.TimeUnit) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.blockingConnect(long,java.util.concurrent.TimeUnit) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect-long-timeout,-timeunit-unit">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-blockingconnect-long-timeout,-timeunit-unit</a><br/>
         *
         * @param param0 the maximum time to wait
         * @param param1 the time unit of the timeout argument
         * @return the result of the connection
         */
        public org.xms.g.common.ConnectionResult blockingConnect(long param0, java.util.concurrent.TimeUnit param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).holdUpConnect(param0, param1)");
                com.huawei.hms.api.ConnectionResult hReturn = ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).holdUpConnect(param0, param1);
                return ((hReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).blockingConnect(param0, param1)");
                com.google.android.gms.common.ConnectionResult gReturn = ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).blockingConnect(param0, param1);
                return ((gReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.clearDefaultAccountAndReconnect() Clears the account selected by the user and reconnects the client asking the user to pick an account again if useDefaultAccount() was set.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.discardAndReconnect() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.clearDefaultAccountAndReconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-pendingresultstatus-cleardefaultaccountandreconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-pendingresultstatus-cleardefaultaccountandreconnect</a><br/>
         *
         * @return the pending result is fired once the default account has been cleared, but before the client is reconnected for that ConnectionCallbacks can be used
         */
        public org.xms.g.common.api.PendingResult<org.xms.g.common.api.Status> clearDefaultAccountAndReconnect() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).discardAndReconnect()");
                com.huawei.hms.support.api.client.PendingResult hReturn = ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).discardAndReconnect();
                return ((hReturn) == null ? null : (new org.xms.g.common.api.PendingResult.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).clearDefaultAccountAndReconnect()");
                com.google.android.gms.common.api.PendingResult gReturn = ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).clearDefaultAccountAndReconnect();
                return ((gReturn) == null ? null : (new org.xms.g.common.api.PendingResult.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.connect() Connects the client to Google Play services. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.connect(null) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.connect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-connect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-connect</a><br/>
         *
         */
        public void connect() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).connect(null);
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).connect()");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).connect();
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.disconnect() Closes the connection to Google Play services.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.disconnect() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.disconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-disconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-disconnect</a><br/>
         *
         */
        public void disconnect() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).disconnect()");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).disconnect();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).disconnect()");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).disconnect();
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.dump(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) Prints the GoogleApiClient's state into the given stream.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.print(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.dump(java.lang.String,java.io.FileDescriptor,java.io.PrintWriter,java.lang.String[]) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-dump-string-prefix,-filedescriptor-fd,-printwriter-writer,-string[]-args">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-dump-string-prefix,-filedescriptor-fd,-printwriter-writer,-string[]-args</a><br/>
         *
         * @param param0 Desired prefix to prepend at each line of output
         * @param param1 The raw file descriptor that the dump is being sent to
         * @param param2 The PrintWriter to use for writing the dump
         * @param param3 Additional arguments to the dump request
         */
        public void dump(java.lang.String param0, java.io.FileDescriptor param1, java.io.PrintWriter param2, java.lang.String[] param3) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).print(param0, param1, param2, param3)");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).print(param0, param1, param2, param3);
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).dump(param0, param1, param2, param3)");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).dump(param0, param1, param2, param3);
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.getConnectionResult(org.xms.g.common.api.Api<?>) Returns the ConnectionResult for the GoogleApiClient's connection to the specified API.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.getConnectionResult(com.huawei.hms.api.Api<?>) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.getConnectionResult(com.google.android.gms.common.api.Api<?>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-getconnectionresult-api-api">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-connectionresult-getconnectionresult-api-api</a><br/>
         *
         * @param param0 The Api to retrieve the ConnectionResult of. Passing an API that was not registered with the GoogleApiClient results in an IllegalArgumentException
         * @return the ConnectionResult
         */
        public org.xms.g.common.ConnectionResult getConnectionResult(org.xms.g.common.api.Api<?> param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).getConnectionResult(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))))");
                com.huawei.hms.api.ConnectionResult hReturn = ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).getConnectionResult(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))));
                return ((hReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).getConnectionResult(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))))");
                com.google.android.gms.common.ConnectionResult gReturn = ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).getConnectionResult(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))));
                return ((gReturn) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.hasConnectedApi(org.xms.g.common.api.Api<?>) Returns whether or not this GoogleApiClient has the specified API in a connected state.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.hasConnectedApi(com.huawei.hms.api.Api<?>) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.hasConnectedApi(com.google.android.gms.common.api.Api<?>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-hasconnectedapi-api-api">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-hasconnectedapi-api-api</a><br/>
         *
         * @param param0 The Api to test the connection of
         * @return true if or not this GoogleApiClient has the specified API in a connected state
         */
        public boolean hasConnectedApi(org.xms.g.common.api.Api<?> param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectedApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))))");
                return ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectedApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).hasConnectedApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))))");
                return ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).hasConnectedApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.isConnected() Checks if the client is currently connected to the service, so that requests to other methods will succeed. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.isConnected() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.isConnected() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnected">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnected</a><br/>
         *
         * @return true if the client is connected to the service
         */
        public boolean isConnected() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).isConnected()");
                return ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).isConnected();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnected()");
                return ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnected();
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.isConnecting() Checks if the client is attempting to connect to the service.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.isConnecting() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.isConnecting() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnecting">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnecting</a><br/>
         *
         * @return true if the client is attempting to connect to the service
         */
        public boolean isConnecting() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).isConnecting()");
                return ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).isConnecting();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnecting()");
                return ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnecting();
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.isConnectionCallbacksRegistered(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Returns true if the specified listener is currently registered to receive connection events.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.hasConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.isConnectionCallbacksRegistered(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectioncallbacksregistered-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectioncallbacksregistered-googleapiclient.connectioncallbacks-listener</a><br/>
         *
         * @param param0 The listener to check for
         * @return true if the specified listener is currently registered to receive connection events
         */
        public boolean isConnectionCallbacksRegistered(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectionSuccessListener(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())))");
                return ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectionSuccessListener(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnectionCallbacksRegistered(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())))");
                return ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnectionCallbacksRegistered(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.isConnectionFailedListenerRegistered(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Returns true if the specified listener is currently registered to receive connection failed events.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.hasConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.isConnectionFailedListenerRegistered(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectionfailedlistenerregistered-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-boolean-isconnectionfailedlistenerregistered-googleapiclient.onconnectionfailedlistener-listener</a><br/>
         *
         * @param param0 The listener to check for
         * @return true if the specified listener is currently registered to receive connection failed events
         */
        public boolean isConnectionFailedListenerRegistered(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectionFailureListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())))");
                return ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).hasConnectionFailureListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnectionFailedListenerRegistered(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())))");
                return ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).isConnectionFailedListenerRegistered(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.reconnect() Closes the current connection to Google Play services and creates a new connection.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.reconnect() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.reconnect() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-reconnect">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-reconnect</a><br/>
         *
         */
        public void reconnect() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).reconnect()");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).reconnect();
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).reconnect()");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).reconnect();
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.registerConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Registers a listener to receive connection events from this GoogleApiClient.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.setConnectionCallbacks(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks): <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.registerConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectioncallbacks-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectioncallbacks-googleapiclient.connectioncallbacks-listener</a><br/>
         *
         * @param param0 the listener where the results of the asynchronous connect() call are delivered
         */
        public void registerConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).setConnectionCallbacks(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())))");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).setConnectionCallbacks(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).registerConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())))");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).registerConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.registerConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Registers a listener to receive connection failed events from this GoogleApiClient. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.setConnectionFailedListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.registerConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-registerconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener</a><br/>
         *
         * @param param0 the listener where the results of the asynchronous connect() call are delivered
         */
        public void registerConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).setConnectionFailedListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())))");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).setConnectionFailedListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).registerConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())))");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).registerConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.stopAutoManage(androidx.fragment.app.FragmentActivity) Disconnects the client and stops automatic lifecycle management. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.disableLifeCycleManagement(androidx.fragment.app.FragmentActivity) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.stopAutoManage(androidx.fragment.app.FragmentActivity) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-stopautomanage-fragmentactivity-lifecycleactivity">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-stopautomanage-fragmentactivity-lifecycleactivity</a><br/>
         *
         * @param param0 the activity managing the client's lifecycle
         * @throws java.lang.IllegalStateException if called from outside of the main thread
         */
        public void stopAutoManage(androidx.fragment.app.FragmentActivity param0) throws java.lang.IllegalStateException {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).disableLifeCycleManagement(param0);
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).stopAutoManage(param0)");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).stopAutoManage(param0);
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.unregisterConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Removes a connection listener from this GoogleApiClientbr/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.removeConnectionSuccessListener(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.unregisterConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectioncallbacks-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectioncallbacks-googleapiclient.connectioncallbacks-listener</a><br/>
         *
         * @param param0 the listener to unregister
         */
        public void unregisterConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).removeConnectionSuccessListener(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())))");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).removeConnectionSuccessListener(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).unregisterConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())))");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).unregisterConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.XImpl.unregisterConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Removes a connection failed listener from the GoogleApiClient. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.removeConnectionFailureListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.unregisterConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient#public-abstract-void-unregisterconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener</a><br/>
         *
         * @param param0 the listener to unregister
         */
        public void unregisterConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).removeConnectionFailureListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())))");
                ((com.huawei.hms.api.HuaweiApiClient) this.getHInstance()).removeConnectionFailureListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).unregisterConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())))");
                ((com.google.android.gms.common.api.GoogleApiClient) this.getGInstance()).unregisterConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())));
            }
        }
    }

    /**
     * Wrapper class of Builder to configure a ExtensionApiClient.<br/>
     * Combination of com.huawei.hms.api.HuaweiApiClient.Builder and com.google.android.gms.common.api.GoogleApiClient.Builder.<br/>
     * com.huawei.hms.api.HuaweiApiClient.Builder : <br/>
     * com.google.android.gms.common.api.GoogleApiClient.Builder : Builder to configure a GoogleApiClient.<br/>
     */
    public static final class Builder extends org.xms.g.utils.XObject {

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.Builder(org.xms.g.utils.XBox)  constructor of Builder with XBox.<br/>
         *
         * @param param0 the wrapper of xms instance
         */
        public Builder(org.xms.g.utils.XBox param0) {
            super(param0);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.Builder(android.content.Context) Builder to help construct the GoogleApiClient object.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.Builder(android.content.Context) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClien.Builder.Builder(android.content.Context) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-context-context">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-context-context</a><br/>
         *
         * @param param0 The context to use for the connection
         */
        public Builder(android.content.Context param0) {
            super((org.xms.g.utils.XBox) null);
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                this.setHInstance(new com.huawei.hms.api.HuaweiApiClient.Builder(param0));
            } else {
                this.setGInstance(new com.google.android.gms.common.api.GoogleApiClient.Builder(param0));
            }
        }

        /**
         * XMS does not provide this api.<br/>
         */
        public Builder(android.content.Context param0, org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param1, org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param2) {
            super((org.xms.g.utils.XBox) null);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addApi(org.xms.g.common.api.Api<XO>,XO) Specify which Apis are requested by your app. See Api for more information.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addApi(com.huawei.hms.api.Api<O>,O) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addApi(com.google.android.gms.common.api.Api<O>,O) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapi-apio-api,-o-options">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapi-apio-api,-o-options</a><br/>
         *
         * @param param0 The Api requested by your app
         * @param param1 Any additional parameters required for the specific AP
         * @return the Builder
         */
        public final <XO extends org.xms.g.common.api.Api.ApiOptions.HasOptions> org.xms.g.common.api.ExtensionApiClient.Builder addApi(org.xms.g.common.api.Api<XO> param0, XO param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                com.huawei.hms.api.Api.ApiOptions.HasOptions hObj1 = ((com.huawei.hms.api.Api.ApiOptions.HasOptions) org.xms.g.utils.Utils.getInstanceInInterface(param1, true));
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), hObj1)");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), hObj1);
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                com.google.android.gms.common.api.Api.ApiOptions.HasOptions gObj1 = ((com.google.android.gms.common.api.Api.ApiOptions.HasOptions) org.xms.g.utils.Utils.getInstanceInInterface(param1, false));
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), gObj1)");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), gObj1);
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addApi(org.xms.g.common.api.Api<? extends org.xms.g.common.api.Api.ApiOptions.NotRequiredOptions>) Specify which Apis are requested by your app. See Api for more information. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addApi(com.huawei.hms.api.Api<? extends com.huawei.hms.api.Api.ApiOptions.NotRequiredOptions>) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addApi(com.google.android.gms.common.api.Api<? extends com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions>) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapi-api-extends-api.apioptions.notrequiredoptions-api">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapi-api-extends-api.apioptions.notrequiredoptions-api</a><br/>
         *
         * @param param0 The Api requested by your app
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder addApi(org.xms.g.common.api.Api<? extends org.xms.g.common.api.Api.ApiOptions.NotRequiredOptions> param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApi(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApi(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addApiIfAvailable(org.xms.g.common.api.Api<XO>,XO,org.xms.g.common.api.Scope...) Specify which Apis should attempt to connect, but are not strictly required for your app. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addApiWithScope(com.huawei.hms.api.Api<O>,O,com.huawei.hms.support.api.entity.auth.Scope[]) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addApiIfAvailable(com.google.android.gms.common.api.Api<XO>,XO,com.google.android.gms.common.api.Scope[]) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapiifavailable-apio-api,-o-options,-scope...-scopes">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapiifavailable-apio-api,-o-options,-scope...-scopes</a><br/>
         *
         * @param param0 The Api requested by your app
         * @param param1 XO that extends ApiOptions.HasOptions
         * @param param2 Scopes required by this API
         * @return the Builder
         */
        public final <XO extends org.xms.g.common.api.Api.ApiOptions.HasOptions> org.xms.g.common.api.ExtensionApiClient.Builder addApiIfAvailable(org.xms.g.common.api.Api<XO> param0, XO param1, org.xms.g.common.api.Scope... param2) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                com.huawei.hms.api.Api.ApiOptions.HasOptions hObj1 = ((com.huawei.hms.api.Api.ApiOptions.HasOptions) org.xms.g.utils.Utils.getInstanceInInterface(param1, true));
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApiWithScope(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), hObj1, ((com.huawei.hms.support.api.entity.auth.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param2, com.huawei.hms.support.api.entity.auth.Scope.class, x -> (com.huawei.hms.support.api.entity.auth.Scope)x.getHInstance())))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApiWithScope(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), hObj1, ((com.huawei.hms.support.api.entity.auth.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param2, com.huawei.hms.support.api.entity.auth.Scope.class, x -> (com.huawei.hms.support.api.entity.auth.Scope) x.getHInstance())));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                com.google.android.gms.common.api.Api.ApiOptions.HasOptions gObj1 = ((com.google.android.gms.common.api.Api.ApiOptions.HasOptions) org.xms.g.utils.Utils.getInstanceInInterface(param1, false));
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApiIfAvailable(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), gObj1, ((com.google.android.gms.common.api.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param2, com.google.android.gms.common.api.Scope.class, x -> (com.google.android.gms.common.api.Scope)x.getGInstance())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApiIfAvailable(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), gObj1, ((com.google.android.gms.common.api.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param2, com.google.android.gms.common.api.Scope.class, x -> (com.google.android.gms.common.api.Scope) x.getGInstance())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addApiIfAvailable(org.xms.g.common.api.Api<? extends org.xms.g.common.api.Api.ApiOptions.NotRequiredOptions>,org.xms.g.common.api.Scope[]) Specify which Apis should attempt to connect, but are not strictly required for your app.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addApiWithScope(com.huawei.hms.api.Api<? extends com.huawei.hms.api.Api.ApiOptions.NotRequiredOptions>,com.huawei.hms.support.api.entity.auth.Scope[]) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addApiIfAvailable(com.google.android.gms.common.api.Api<? extends com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions>,com.google.android.gms.common.api.Scope[]) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapiifavailable-api-extends-api.apioptions.notrequiredoptions-api,-scope...-scopes">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addapiifavailable-api-extends-api.apioptions.notrequiredoptions-api,-scope...-scopes</a><br/>
         *
         * @param param0 The Api requested by your app
         * @param param1 Scopes required by this API
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder addApiIfAvailable(org.xms.g.common.api.Api<? extends org.xms.g.common.api.Api.ApiOptions.NotRequiredOptions> param0, org.xms.g.common.api.Scope[] param1) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApiWithScope(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), ((com.huawei.hms.support.api.entity.auth.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param1, com.huawei.hms.support.api.entity.auth.Scope.class, x -> (com.huawei.hms.support.api.entity.auth.Scope)x.getHInstance())))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addApiWithScope(((com.huawei.hms.api.Api) ((param0) == null ? null : (param0.getHInstance()))), ((com.huawei.hms.support.api.entity.auth.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param1, com.huawei.hms.support.api.entity.auth.Scope.class, x -> (com.huawei.hms.support.api.entity.auth.Scope) x.getHInstance())));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApiIfAvailable(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), ((com.google.android.gms.common.api.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param1, com.google.android.gms.common.api.Scope.class, x -> (com.google.android.gms.common.api.Scope)x.getGInstance())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addApiIfAvailable(((com.google.android.gms.common.api.Api) ((param0) == null ? null : (param0.getGInstance()))), ((com.google.android.gms.common.api.Scope[]) org.xms.g.utils.Utils.genericArrayCopy(param1, com.google.android.gms.common.api.Scope.class, x -> (com.google.android.gms.common.api.Scope) x.getGInstance())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) Registers a listener to receive connection events from this GoogleApiClient. <br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addConnectionCallbacks(com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addConnectionCallbacks(com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addconnectioncallbacks-googleapiclient.connectioncallbacks-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addconnectioncallbacks-googleapiclient.connectioncallbacks-listener</a><br/>
         *
         * @param param0 the listener where the results of the asynchronous connect() call are delivered
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder addConnectionCallbacks(org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addConnectionCallbacks(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addConnectionCallbacks(((param0) == null ? null : (param0.getHInstanceConnectionCallbacks())));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addConnectionCallbacks(((param0) == null ? null : (param0.getGInstanceConnectionCallbacks())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addOnConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Adds a listener to register to receive connection failed events from this GoogleApiClient.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addOnConnectionFailedListener(com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addOnConnectionFailedListener(com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addonconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addonconnectionfailedlistener-googleapiclient.onconnectionfailedlistener-listener</a><br/>
         *
         * @param param0 the listener where the results of the asynchronous connect() call are delivered
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder addOnConnectionFailedListener(org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addOnConnectionFailedListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addOnConnectionFailedListener(((param0) == null ? null : (param0.getHInstanceOnConnectionFailedListener())));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addOnConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addOnConnectionFailedListener(((param0) == null ? null : (param0.getGInstanceOnConnectionFailedListener())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.addScope(org.xms.g.common.api.Scope) Specify the OAuth 2.0 scopes requested by your app.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.addScope(com.huawei.hms.support.api.entity.auth.Scope) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.addScope(com.google.android.gms.common.api.Scope) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addscope-scope-scope">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-addscope-scope-scope</a><br/>
         *
         * @param param0 The OAuth 2.0 scopes requested by your app
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder addScope(org.xms.g.common.api.Scope param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addScope(((com.huawei.hms.support.api.entity.auth.Scope) ((param0) == null ? null : (param0.getHInstance()))))");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).addScope(((com.huawei.hms.support.api.entity.auth.Scope) ((param0) == null ? null : (param0.getHInstance()))));
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addScope(((com.google.android.gms.common.api.Scope) ((param0) == null ? null : (param0.getGInstance()))))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).addScope(((com.google.android.gms.common.api.Scope) ((param0) == null ? null : (param0.getGInstance()))));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.build() Builds a new GoogleApiClient object for communicating with the Google APIs.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.build() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.build() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient-build">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient-build</a><br/>
         *
         * @return The ExtensionApiClient object
         */
        public final org.xms.g.common.api.ExtensionApiClient build() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).build()");
                com.huawei.hms.api.HuaweiApiClient hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).build();
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.XImpl(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).build()");
                com.google.android.gms.common.api.GoogleApiClient gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).build();
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.XImpl(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.enableAutoManage(androidx.fragment.app.FragmentActivity,org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Enables automatic lifecycle management in a support library FragmentActivity that connects the client in onStart() and disconnects it in onStop().<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.allowLifeCycleManagement(android.app.Activity,com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.enableAutoManage(androidx.fragment.app.FragmentActivity,com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-enableautomanage-fragmentactivity-fragmentactivity,-googleapiclient.onconnectionfailedlistener-unresolvedconnectionfailedlistener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-enableautomanage-fragmentactivity-fragmentactivity,-googleapiclient.onconnectionfailedlistener-unresolvedconnectionfailedlistener</a><br/>
         *
         * @param param0 The activity that uses the GoogleApiClient. For lifecycle management to work correctly the activity must call its parent's onActivityResult(int, int, android.content.Intent)
         * @param param1 Called if the connection failed and there was no resolution or the user chose not to complete the provided resolution. If this listener is called, the client will no longer be auto-managed, and a new instance must be built. In the event that the user chooses not to complete a resolution, the ConnectionResult will have a status code of CANCELED
         * @return the Builder
         * @throws java.lang.NullPointerException if fragmentActivity is null
         * @throws java.lang.IllegalStateException if another GoogleApiClient is already being auto-managed with the default clientId
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder enableAutoManage(androidx.fragment.app.FragmentActivity param0, org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param1) throws java.lang.NullPointerException, java.lang.IllegalStateException {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = null;
                hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).allowLifeCycleManagement(param0, ((param1) == null ? null : (param1.getHInstanceOnConnectionFailedListener())));
                if (hReturn == null) {
                    return null;
                }
                return new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).enableAutoManage(param0, ((param1) == null ? null : (param1.getGInstanceOnConnectionFailedListener())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).enableAutoManage(param0, ((param1) == null ? null : (param1.getGInstanceOnConnectionFailedListener())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.enableAutoManage(androidx.fragment.app.FragmentActivity,int,org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) Enables automatic lifecycle management in a support library FragmentActivity that connects the client in onStart() and disconnects it in onStop().<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.allowLifeCycleManagement(android.app.Activity,int,com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.enableAutoManage(androidx.fragment.app.FragmentActivity,int,com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-enableautomanage-fragmentactivity-fragmentactivity,-int-clientid,-googleapiclient.onconnectionfailedlistener-unresolvedconnectionfailedlistener">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-enableautomanage-fragmentactivity-fragmentactivity,-int-clientid,-googleapiclient.onconnectionfailedlistener-unresolvedconnectionfailedlistener</a><br/>
         *
         * @param param0 The activity that uses the GoogleApiClient. For lifecycle management to work correctly the activity must call its parent's onActivityResult(int, int, android.content.Intent)
         * @param param1 A non-negative identifier for this client. At any given time, only one auto-managed client is allowed per id. To reuse an id you must first call stopAutoManage(FragmentActivity) on the previous client
         * @param param2 The listener instance
         * @return the Builder
         * @throws java.lang.NullPointerException if fragmentActivity is null
         * @throws java.lang.IllegalArgumentException if clientId is negative
         * @throws java.lang.IllegalStateException if clientId is already being auto-managed
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder enableAutoManage(androidx.fragment.app.FragmentActivity param0, int param1, org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener param2) throws java.lang.NullPointerException, java.lang.IllegalArgumentException, java.lang.IllegalStateException {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = null;
                hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).allowLifeCycleManagement(param0, param1, ((param2) == null ? null : (param2.getHInstanceOnConnectionFailedListener())));
                if (hReturn == null) {
                    return null;
                }
                return new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).enableAutoManage(param0, param1, ((param2) == null ? null : (param2.getGInstanceOnConnectionFailedListener())))");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).enableAutoManage(param0, param1, ((param2) == null ? null : (param2.getGInstanceOnConnectionFailedListener())));
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.setAccountName(java.lang.String) Specify an account name on the device that should be used.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.setAccountName(java.lang.String) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.setAccountName(java.lang.String) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setaccountname-string-accountname">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setaccountname-string-accountname</a><br/>
         *
         * @param param0 The account name on the device that should be used by GoogleApiClient
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder setAccountName(java.lang.String param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setAccountName(param0)");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setAccountName(param0);
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setAccountName(param0)");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setAccountName(param0);
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.setGravityForPopups(int) Specifies the part of the screen at which games service popups (for example, "welcome back" or "achievement unlocked" popups) will be displayed using gravity.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.setPopupsGravity(int) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.setGravityForPopups(int) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setgravityforpopups-int-gravityforpopups">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setgravityforpopups-int-gravityforpopups</a><br/>
         *
         * @param param0 The gravity which controls the placement of games service popups
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder setGravityForPopups(int param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setPopupsGravity(param0)");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setPopupsGravity(param0);
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setGravityForPopups(param0)");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setGravityForPopups(param0);
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.setHandler(android.os.Handler) Sets a Handler to indicate which thread to use when invoking callbacks. Will not be used directly to handle callbacks. If this is not called then the application's main thread will be used.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.setHandler(android.os.Handler) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.setHandler(android.os.Handler) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-sethandler-handler-handler">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-sethandler-handler-handler</a><br/>
         *
         * @param param0 A Handler instance
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder setHandler(android.os.Handler param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setHandler(param0)");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setHandler(param0);
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setHandler(param0)");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setHandler(param0);
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.setViewForPopups(android.view.View) Sets the View to use as a content view for popups.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.setViewForPopups(android.view.View) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.setViewForPopups(android.view.View) : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setviewforpopups-view-viewforpopups">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-setviewforpopups-view-viewforpopups</a><br/>
         *
         * @param param0 The view to use as a content view for popups. View cannot be null
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder setViewForPopups(android.view.View param0) {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setViewForPopups(param0)");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).setViewForPopups(param0);
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setViewForPopups(param0)");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).setViewForPopups(param0);
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.useDefaultAccount() Specify that the default account should be used when connecting to services.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.Builder.applyDefaultAccount() : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.Builder.useDefaultAccount() : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-usedefaultaccount">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder#public-googleapiclient.builder-usedefaultaccount</a><br/>
         *
         * @return the Builder
         */
        public final org.xms.g.common.api.ExtensionApiClient.Builder useDefaultAccount() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).applyDefaultAccount()");
                com.huawei.hms.api.HuaweiApiClient.Builder hReturn = ((com.huawei.hms.api.HuaweiApiClient.Builder) this.getHInstance()).applyDefaultAccount();
                return ((hReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(null, hReturn))));
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).useDefaultAccount()");
                com.google.android.gms.common.api.GoogleApiClient.Builder gReturn = ((com.google.android.gms.common.api.GoogleApiClient.Builder) this.getGInstance()).useDefaultAccount();
                return ((gReturn) == null ? null : (new org.xms.g.common.api.ExtensionApiClient.Builder(new org.xms.g.utils.XBox(gReturn, null))));
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.api.ExtensionApiClient.Builder.<br/>
         * <p>
         *
         * @param param0 the input object
         * @return casted ExtensionApiClient.Builder object
         */
        public static org.xms.g.common.api.ExtensionApiClient.Builder dynamicCast(java.lang.Object param0) {
            return ((org.xms.g.common.api.ExtensionApiClient.Builder) param0);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.Builder.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
         *
         * @param param0 the input object
         * @return true if the Object is XMS instance, otherwise false
         */
        public static boolean isInstance(java.lang.Object param0) {
            if (!(param0 instanceof org.xms.g.utils.XGettable)) {
                return false;
            }
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.api.HuaweiApiClient.Builder;
            } else {
                return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.api.GoogleApiClient.Builder;
            }
        }
    }

    /**
     * Provides callbacks that are called when the client is connected or disconnected from the service. <br/>
     * Combination of com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks and com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.<br/>
     * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks : <br/>
     * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks : Provides callbacks that are called when the client is connected or disconnected from the service.<br/>
     */
    public static interface ConnectionCallbacks extends org.xms.g.utils.XInterface {

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.getCAUSE_NETWORK_LOST() return the value of CAUSE_NETWORK_LOST.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks#public-static-final-int-cause_network_lost">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks#public-static-final-int-cause_network_lost</a><br/>
         *
         * @return A suspension cause informing you that a peer device connection was lost
         */
        public static int getCAUSE_NETWORK_LOST() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST");
                return com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST;
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST");
                return com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST;
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.getCAUSE_SERVICE_DISCONNECTED() return the value of CAUSE_SERVICE_DISCONNECTED.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED : <a href="https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks#public-static-final-int-cause_service_disconnected">https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks#public-static-final-int-cause_service_disconnected</a><br/>
         *
         * @return A suspension cause informing that the service has been killed
         */
        public static int getCAUSE_SERVICE_DISCONNECTED() {
            if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                org.xms.g.utils.XmsLog.d("XMSRouter", "com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED");
                return com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED;
            } else {
                org.xms.g.utils.XmsLog.d("XMSRouter", "com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED");
                return com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED;
            }
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.onConnected(android.os.Bundle) Called when attempt to connect the client to the service.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.onConnected() :<a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.onConnected(android.os.Bundle) : <a href=""></a><br/>
         *
         * @param param0 Bundle instance
         */
        public void onConnected(android.os.Bundle param0);

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.onConnectionSuspended(int) Called when connection with the client is Suspended.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.onConnectionSuspended(int) :<a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.onConnectionSuspended(int) : <a href=""></a><br/>
         *
         * @param param0 the value about connection
         */
        public void onConnectionSuspended(int param0);

        default com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks getGInstanceConnectionCallbacks() {
            if (this instanceof org.xms.g.utils.XGettable) {
                return ((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) ((org.xms.g.utils.XGettable) this).getGInstance());
            }
            return new com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks() {

                public void onConnected(android.os.Bundle param0) {
                    org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.this.onConnected(param0);
                }

                public void onConnectionSuspended(int param0) {
                    org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.this.onConnectionSuspended(param0);
                }
            };
        }

        default com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks getHInstanceConnectionCallbacks() {
            if (this instanceof org.xms.g.utils.XGettable) {
                return ((com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) ((org.xms.g.utils.XGettable) this).getHInstance());
            }
            return new com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks() {

                public void onConnectionSuspended(int param0) {
                    org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.this.onConnectionSuspended(param0);
                }

                public void onConnected() {
                    org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.this.onConnected(null);
                }
            };
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.<br/>
         * <p>
         *
         * @param param0 the input object
         * @return casted ExtensionApiClient.ConnectionCallbacks object
         */
        public static org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks dynamicCast(java.lang.Object param0) {
            if (param0 instanceof org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) {
                return ((org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) param0);
            }
            if (param0 instanceof org.xms.g.utils.XGettable) {
                com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks gReturn = ((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) ((org.xms.g.utils.XGettable) param0).getGInstance());
                com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks hReturn = ((com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) ((org.xms.g.utils.XGettable) param0).getHInstance());
                return new org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl(new org.xms.g.utils.XBox(gReturn, hReturn));
            }
            return ((org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks) param0);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
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
                    return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks;
                } else {
                    return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
                }
            }
            return param0 instanceof org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks;
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl: Wrapper class of ConnectionCallbacks which provides callbacks that are called when the client is connected or disconnected from the service. <br/>
         * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks : <br/>
         * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks : Provides callbacks that are called when the client is connected or disconnected from the service.<br/>
         */
        public static class XImpl extends org.xms.g.utils.XObject implements org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks {

            /**
             * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl.XImpl(org.xms.g.utils.XBox)  constructor of XImpl with XBox.<br/>
             *
             * @param param0 the wrapper of xms instance
             */
            public XImpl(org.xms.g.utils.XBox param0) {
                super(param0);
            }

            /**
             * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl.onConnected(android.os.Bundle) Called when attempt to connect the client to the service.<br/>
             *
             * Support running environments including both HMS and GMS which are chosen by users.<br/>
             * Below are the references of HMS apis and GMS apis respectively:<br/>
             * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.onConnected() :<a href=""></a><br/>
             * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.onConnected(android.os.Bundle) : <a href=""></a><br/>
             *
             * @param param0 Bundle instance
             */
            public void onConnected(android.os.Bundle param0) {
                if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                    ((com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) this.getHInstance()).onConnected();
                } else {
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) this.getGInstance()).onConnected(param0)");
                    ((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) this.getGInstance()).onConnected(param0);
                }
            }

            /**
             * org.xms.g.common.api.ExtensionApiClient.ConnectionCallbacks.XImpl.onConnectionSuspended(int) Called when connection with the client is Suspended.<br/>
             *
             * Support running environments including both HMS and GMS which are chosen by users.<br/>
             * Below are the references of HMS apis and GMS apis respectively:<br/>
             * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks.onConnectionSuspended(int) :<a href=""></a><br/>
             * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.onConnectionSuspended(int) : <a href=""></a><br/>
             *
             * @param param0 the value about connection
             */
            public void onConnectionSuspended(int param0) {
                if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) this.getHInstance()).onConnectionSuspended(param0)");
                    ((com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks) this.getHInstance()).onConnectionSuspended(param0);
                } else {
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) this.getGInstance()).onConnectionSuspended(param0)");
                    ((com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks) this.getGInstance()).onConnectionSuspended(param0);
                }
            }
        }
    }

    /**
     * Provides callbacks for scenarios that result in a failed attempt to connect the client to the service. <br/>
     * Combination of com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks and com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener.<br/>
     * com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks : <br/>
     * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener : Provides callbacks for scenarios that result in a failed attempt to connect the client to the service.<br/>
     */
    public static interface OnConnectionFailedListener extends org.xms.g.utils.XInterface {

        /**
         * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.onConnectionFailed(org.xms.g.common.ConnectionResult) Called when connect with client is failed.<br/>
         *
         * Support running environments including both HMS and GMS which are chosen by users.<br/>
         * Below are the references of HMS apis and GMS apis respectively:<br/>
         * com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener.onConnectionFailed(com.huawei.hms.api.ConnectionResult) : <a href=""></a><br/>
         * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener.onConnectionFailed(com.google.android.gms.common.ConnectionResult) : <a href=""></a><br/>
         *
         * @param param0 ConnectionResult instance
         */
        public void onConnectionFailed(org.xms.g.common.ConnectionResult param0);

        default com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener getGInstanceOnConnectionFailedListener() {
            if (this instanceof org.xms.g.utils.XGettable) {
                return ((com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) ((org.xms.g.utils.XGettable) this).getGInstance());
            }
            return new com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener() {

                public void onConnectionFailed(com.google.android.gms.common.ConnectionResult param0) {
                    org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.this.onConnectionFailed(((param0) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(param0, null)))));
                }
            };
        }

        default com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener getHInstanceOnConnectionFailedListener() {
            if (this instanceof org.xms.g.utils.XGettable) {
                return ((com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) ((org.xms.g.utils.XGettable) this).getHInstance());
            }
            return new com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener() {

                public void onConnectionFailed(com.huawei.hms.api.ConnectionResult param0) {
                    org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.this.onConnectionFailed(((param0) == null ? null : (new org.xms.g.common.ConnectionResult(new org.xms.g.utils.XBox(null, param0)))));
                }
            };
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.<br/>
         * <p>
         *
         * @param param0 the input object
         * @return casted ExtensionApiClient.OnConnectionFailedListener object
         */
        public static org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener dynamicCast(java.lang.Object param0) {
            if (param0 instanceof org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) {
                return ((org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) param0);
            }
            if (param0 instanceof org.xms.g.utils.XGettable) {
                com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener gReturn = ((com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) ((org.xms.g.utils.XGettable) param0).getGInstance());
                com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener hReturn = ((com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) ((org.xms.g.utils.XGettable) param0).getHInstance());
                return new org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl(new org.xms.g.utils.XBox(gReturn, hReturn));
            }
            return ((org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener) param0);
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
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
                    return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener;
                } else {
                    return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
                }
            }
            return param0 instanceof org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener;
        }

        /**
         * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl: Wrapper class of OnConnectionFailedListener which provides callbacks for scenarios that result in a failed attempt to connect the client to the service. <br/>
         * com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener : <br/>
         * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener : Provides callbacks for scenarios that result in a failed attempt to connect the client to the service.<br/>
         */
        public static class XImpl extends org.xms.g.utils.XObject implements org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener {

            /**
             * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl.XImpl(org.xms.g.utils.XBox)  constructor of XImpl with XBox.<br/>
             *
             * @param param0 the wrapper of xms instance
             */
            public XImpl(org.xms.g.utils.XBox param0) {
                super(param0);
            }

            /**
             * org.xms.g.common.api.ExtensionApiClient.OnConnectionFailedListener.XImpl.onConnectionFailed(org.xms.g.common.ConnectionResult) Called when connect with client is failed.<br/>
             *
             * Support running environments including both HMS and GMS which are chosen by users.<br/>
             * Below are the references of HMS apis and GMS apis respectively:<br/>
             * com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener.onConnectionFailed(com.huawei.hms.api.ConnectionResult) : <a href=""></a><br/>
             * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener.onConnectionFailed(com.google.android.gms.common.ConnectionResult) : <a href=""></a><br/>
             *
             * @param param0 ConnectionResult instance
             */
            public void onConnectionFailed(org.xms.g.common.ConnectionResult param0) {
                if (org.xms.g.utils.GlobalEnvSetting.isHms()) {
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) this.getHInstance()).onConnectionFailed(((com.huawei.hms.api.ConnectionResult) ((param0) == null ? null : (param0.getHInstance()))))");
                    ((com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener) this.getHInstance()).onConnectionFailed(((com.huawei.hms.api.ConnectionResult) ((param0) == null ? null : (param0.getHInstance()))));
                } else {
                    org.xms.g.utils.XmsLog.d("XMSRouter", "((com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) this.getGInstance()).onConnectionFailed(((com.google.android.gms.common.ConnectionResult) ((param0) == null ? null : (param0.getGInstance()))))");
                    ((com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) this.getGInstance()).onConnectionFailed(((com.google.android.gms.common.ConnectionResult) ((param0) == null ? null : (param0.getGInstance()))));
                }
            }
        }
    }
}