package org.xms.f.messaging;

/**
 * Base class for receiving messages from app server.<br/>
 * Combination of com.huawei.hms.push.HmsMessageService and com.google.firebase.messaging.FirebaseMessagingService.<br/>
 * com.huawei.hms.push.HmsMessageService: Basic class of HUAWEI Push Kit for receiving downlink messages or updated tokens.<br/>
 * com.google.firebase.messaging.FirebaseMessagingService: Base class for receiving messages from Firebase Cloud Messaging.<br/>
 */
public class ExtensionMessagingService extends android.app.Service {

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageReceived(org.xms.f.messaging.RemoteMessage) Called when a message is received.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onMessageReceived(com.huawei.hms.push.RemoteMessage): <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116</a><br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onMessageReceived(com.google.firebase.messaging.RemoteMessage): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message</a><br/>
     *
     * @param remoteMessage Remote message that has been received
     */
    public void onMessageReceived(org.xms.f.messaging.RemoteMessage remoteMessage) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.ExtensionMessagingService() Constructor of ExtensionMessagingService.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.HmsMessageService()
     * com.google.firebase.messaging.FirebaseMessagingService.FirebaseMessagingService(): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-firebasemessagingservice">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-firebasemessagingservice</a><br/>
     *
     */
    public ExtensionMessagingService() {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onDeletedMessages() Called when the Cloud Messaging server deletes pending messages.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onDeletedMessages()
     * com.google.firebase.messaging.FirebaseMessagingService.onDeletedMessages(): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-ondeletedmessages">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-ondeletedmessages</a><br/>
     *
     */
    public void onDeletedMessages() {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageSent(java.lang.String) Called after an upstream message is successfully sent.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onMessageSent(java.lang.String): <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section172125618218">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section172125618218</a><br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onMessageSent(java.lang.String): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagesent-string-msgid">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagesent-string-msgid</a><br/>
     *
     * @param var1 Message ID
     */
    public void onMessageSent(String var1) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onSendError(java.lang.String,java.lang.Exception) Called when there was an error sending an upstream message.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onSendError(java.lang.String,java.lang.Exception): <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section63844313230">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section63844313230</a><br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onSendError(java.lang.String,java.lang.Exception): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onsenderror-string-msgid,-exception-exception">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onsenderror-string-msgid,-exception-exception</a><br/>
     *
     * @param var1 Message ID
     * @param var2 description of the error, typically a SendException
     */
    public void onSendError(String var1, Exception var2) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onNewToken(java.lang.String) Called when a new token for the default Firebase project is generated.This is invoked after app install when a token is first generated, and again if the token changes.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onNewToken(java.lang.String): <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section814414561477">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section814414561477</a><br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onNewToken(java.lang.String): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onnewtoken-string-token">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onnewtoken-string-token</a><br/>
     *
     * @param var1 The token used for sending messages to this application instance
     */
    public void onNewToken(String var1) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onBind(android.content.Intent intent) Return the communication channel to the service.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     * Below are the references of HMS apis and GMS apis respectively:<br/>
     * com.huawei.hms.push.HmsMessageService.onBind(android.content.Intent)
     * com.google.firebase.messaging.FirebaseMessagingService.onBind(android.content.Intent)
     *
     * @param intent The Intent that was used to bind to this service, as given to Context.bindService
     * @return an IBinder through which clients can call on to the service
     */
    @Override
    public android.os.IBinder onBind(android.content.Intent intent) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.dynamicCast(java.lang.Object) dynamic cast the input object to org.xms.f.messaging.ExtensionMessagingService.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     *
     * @param param0 the input object
     * @return casted ExtensionMessagingService object
     */
    public static org.xms.f.messaging.ExtensionMessagingService dynamicCast(java.lang.Object param0) {
        throw new java.lang.RuntimeException("Stub");
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     * Support running environments including both HMS and GMS which are chosen by users.<br/>
     *
     * @param param0 the input object
     * @return true if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        throw new java.lang.RuntimeException("Stub");
    }
}
