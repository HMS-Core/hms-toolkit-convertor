package org.xms.f.messaging;

import org.xms.g.utils.XBox;

/**
 * Basic class of HUAWEI Push Kit for receiving downlink messages or updated tokens.<br/>
 * Wrapper class for com.huawei.hms.push.HmsMessageService, but only the HMS API are provided.<br/>
 * com.huawei.hms.push.HmsMessageService: Basic class of HUAWEI Push Kit for receiving downlink messages or updated tokens.<br/>
 */
public class ExtensionMessagingService extends com.huawei.hms.push.HmsMessageService {

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageReceived(com.huawei.hms.push.RemoteMessage) Receives data messages pushed by the app server.<br/>
     * com.huawei.hms.push.HmsMessageService.onMessageReceived(com.huawei.hms.push.RemoteMessage remoteMessage) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116</a><br/>
     *
     * @param remoteMessage Data message
     */
    public void onMessageReceived(com.huawei.hms.push.RemoteMessage remoteMessage) {
        this.onMessageReceived(new org.xms.f.messaging.RemoteMessage(new XBox(null, remoteMessage)));
    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.onMessageReceived(org.xms.f.messaging.RemoteMessage) Receives data messages pushed by the app server.<br/>
     * com.huawei.hms.push.HmsMessageService.onMessageReceived(com.huawei.hms.push.RemoteMessage remoteMessage) : <a href="https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116">https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/hmsmessageservice-0000001050173839-V5#EN-US_TOPIC_0000001050173839__section2394629102116</a><br/>
     *
     * @param remoteMessage Data message
     */
    public void onMessageReceived(org.xms.f.messaging.RemoteMessage remoteMessage) {

    }

    /**
     * org.xms.f.messaging.ExtensionMessagingService.dynamicCast(java.lang.Object) dynamic cast the input object to ExtensionMessagingService.<br/>
     *
     * @param param0 the input Object
     * @return casted ExtensionMessagingService
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
        return ((org.xms.g.utils.XGettable) param0).getHInstance() instanceof com.huawei.hms.push.HmsMessageService;
    }
}
