package org.xms.f.messaging;

import org.xms.g.utils.XBox;

/**
 * Base class for receiving messages from Firebase Cloud Messaging.<br/>
 * Wrapper class for com.google.firebase.messaging.FirebaseMessagingService, but only the GMS API are provided.<br/>
 * com.google.firebase.messaging.FirebaseMessagingService: Base class for receiving messages from Firebase Cloud Messaging.<br/>
 */
public class ExtensionMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageReceived(com.google.firebase.messaging.RemoteMessage) Called when a message is received.This is also called when a notification message is received while the app is in the foreground. The notification parameters can be retrieved with getNotification().<br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onMessageReceived(com.google.firebase.messaging.RemoteMessage): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message</a><br/>
     *
     * @param remoteMessage Remote message that has been received
     */
    public void onMessageReceived(com.google.firebase.messaging.RemoteMessage remoteMessage) {
        this.onMessageReceived(new org.xms.f.messaging.RemoteMessage(new XBox(remoteMessage, null)));
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageReceived(org.xms.f.messaging.RemoteMessage) Called when a message is received.This is also called when a notification message is received while the app is in the foreground. The notification parameters can be retrieved with getNotification().<br/>
     * com.google.firebase.messaging.FirebaseMessagingService.onMessageReceived(com.google.firebase.messaging.RemoteMessage): <a href="https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message">https://developers.google.com/android/reference/com/google/firebase/messaging/FirebaseMessagingService#public-void-onmessagereceived-remotemessage-message</a><br/>
     *
     * @param remoteMessage Remote message that has been received
     */
    public void onMessageReceived(org.xms.f.messaging.RemoteMessage remoteMessage) {

    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.dynamicCast(java.lang.Object) dynamic cast the input object to ExtensionMessagingService.<br/>
     *
     * @param param0 the input Object
     * @return casted ExtensionMessagingService object
     */
    public static org.xms.f.messaging.ExtensionMessagingService dynamicCast(java.lang.Object param0) {
        return ((org.xms.f.messaging.ExtensionMessagingService) param0);
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.isInstance(java.lang.Object) judge whether the Object is XMS instance or not.<br/>
     *
     * @param param0 the input object
     * @return if the Object is XMS instance, otherwise false
     */
    public static boolean isInstance(java.lang.Object param0) {
        if (!(param0 instanceof org.xms.g.utils.XGettable)) {
            return false;
        }
        return ((org.xms.g.utils.XGettable) param0).getGInstance() instanceof com.google.firebase.messaging.FirebaseMessagingService;
    }
}
