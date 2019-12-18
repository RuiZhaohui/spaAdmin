package com.swiftcode.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.jiguang.common.connection.IHttpClient.RESPONSE_OK;

/**
 * @author chen
 **/
@Slf4j
@Service
public class JPushService {
    @Value("${jpush.apnsProduction}")
    private boolean apnsProduction;

    /**
     * 推送到alias列表
     *
     * @param alias             别名或别名组
     * @param notificationTitle 通知内容标题
     * @param msgTitle          消息内容标题
     * @param msgContent        消息内容
     * @param extras            扩展字段
     */
    public PushResult sendToAliasList(List<String> alias, String notificationTitle, String msgTitle, String msgContent, String extras) {
        PushPayload pushPayload = buildPushObject_all_aliasList_alertWithTitle(alias, notificationTitle, msgTitle, msgContent, extras);
        return this.sendPush(pushPayload);
    }

    /**
     * 推送到tag列表
     *
     * @param tagsList          Tag或Tag组
     * @param notificationTitle 通知内容标题
     * @param msgTitle          消息内容标题
     * @param msgContent        消息内容
     * @param extras            扩展字段
     */
    public PushResult sendToTagsList(List<String> tagsList, String notificationTitle, String msgTitle, String msgContent, String extras) {
        PushPayload pushPayload = buildPushObject_all_tagList_alertWithTitle(tagsList, notificationTitle, msgTitle, msgContent, extras);
        return this.sendPush(pushPayload);
    }

    /**
     * 发送给所有安卓用户
     *
     * @param notificationTitle 通知内容标题
     * @param msgTitle          消息内容标题
     * @param msgContent        消息内容
     * @param extras            扩展字段
     */
    public PushResult sendToAllAndroid(String notificationTitle, String msgTitle, String msgContent, String extras) {
        PushPayload pushPayload = buildPushObject_android_all_alertWithTitle(notificationTitle, msgTitle, msgContent, extras);
        return this.sendPush(pushPayload);
    }

    /**
     * 发送给所有IOS用户
     *
     * @param notificationTitle 通知内容标题
     * @param msgTitle          消息内容标题
     * @param msgContent        消息内容
     * @param extras            扩展字段
     */
    public PushResult sendToAllIOS(String notificationTitle, String msgTitle, String msgContent, String extras) {
        PushPayload pushPayload = buildPushObject_ios_all_alertWithTitle(notificationTitle, msgTitle, msgContent, extras);
        return this.sendPush(pushPayload);
    }

    /**
     * 发送给所有用户
     *
     * @param notificationTitle 通知内容标题
     * @param msgTitle          消息内容标题
     * @param msgContent        消息内容
     * @param extras            扩展字段
     */
    public PushResult sendToAll(String notificationTitle, String msgTitle, String msgContent, String extras) {
        PushPayload pushPayload = buildPushObject_android_and_ios(notificationTitle, msgTitle, msgContent, extras);
        return this.sendPush(pushPayload);
    }

    private PushResult sendPush(PushPayload pushPayload) {
        log.info("pushPayload={}", pushPayload);
        PushResult pushResult = null;
        try {
            pushResult = JPushFactory.getInstance().sendPush(pushPayload);
            log.info("" + pushResult);
            if (pushResult.getResponseCode() == RESPONSE_OK) {
                log.info("push successful, pushPayload={}", pushPayload);
            }
        } catch (APIConnectionException | APIRequestException e) {
            log.error("push failed: pushPayload={}, exception={}", pushPayload, e);
        }

        return pushResult;
    }


    /**
     * 向所有平台所有用户推送消息
     */
    public PushPayload buildPushObject_android_and_ios(String notificationTitle, String msgTitle, String msgContent, String extras) {
        return PushPayload.newBuilder()
            .setPlatform(Platform.android_ios())
            .setAudience(Audience.all())
            .setNotification(Notification.newBuilder()
                .setAlert(notificationTitle)
                .addPlatformNotification(AndroidNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .setTitle(notificationTitle)
                    .addExtra("androidNotification extras key", extras)
                    .build()
                )
                .addPlatformNotification(IosNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .incrBadge(1)
                    .setSound("default")
                    .addExtra("iosNotification extras key", extras)
                    .build()
                )
                .build()
            )
            .setMessage(Message.newBuilder()
                .setMsgContent(msgContent)
                .setTitle(msgTitle)
                .addExtra("message extras key", extras)
                .build())
            .setOptions(Options.newBuilder()
                .setApnsProduction(apnsProduction)
                .setSendno(1)
                .setTimeToLive(86400)
                .build())
            .build();
    }


    /**
     * 向所有平台单个或多个指定别名用户推送消息
     */
    private PushPayload buildPushObject_all_aliasList_alertWithTitle(List<String> aliasList, String notificationTitle, String msgTitle, String msgContent, String extras) {
        return PushPayload.newBuilder()
            .setPlatform(Platform.all())
            .setAudience(Audience.alias(aliasList))
            .setNotification(Notification.newBuilder()
                .addPlatformNotification(AndroidNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .setTitle(notificationTitle)
                    .addExtra("androidNotification extras key", extras)
                    .build())
                .addPlatformNotification(IosNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .incrBadge(1)
                    .setSound("default")
                    .addExtra("iosNotification extras key", extras)
                    .build())
                .build())
            .setMessage(Message.newBuilder()
                .setMsgContent(msgContent)
                .setTitle(msgTitle)
                .addExtra("message extras key", extras)
                .build())
            .setOptions(Options.newBuilder()
                .setApnsProduction(apnsProduction)
                .setSendno(1)
                .setTimeToLive(86400)
                .build())
            .build();
    }

    /**
     * 向所有平台单个或多个指定Tag用户推送消息
     */
    private PushPayload buildPushObject_all_tagList_alertWithTitle(List<String> tagsList, String notificationTitle, String msgTitle, String msgContent, String extras) {
        return PushPayload.newBuilder()
            .setPlatform(Platform.all())
            .setAudience(Audience.tag(tagsList))
            .setNotification(Notification.newBuilder()
                .addPlatformNotification(AndroidNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .setTitle(notificationTitle)
                    .addExtra("androidNotification extras key", extras)
                    .build())
                .addPlatformNotification(IosNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .incrBadge(1)
                    .setSound("default")
                    .addExtra("iosNotification extras key", extras)
                    .build())
                .build())
            .setMessage(Message.newBuilder()
                .setMsgContent(msgContent)
                .setTitle(msgTitle)
                .addExtra("message extras key", extras)
                .build())
            .setOptions(Options.newBuilder()
                .setApnsProduction(apnsProduction)
                .setSendno(1)
                .setTimeToLive(86400)
                .build())
            .build();

    }


    /**
     * 向android平台所有用户推送消息
     */
    private PushPayload buildPushObject_android_all_alertWithTitle(String notificationTitle, String msgTitle, String msgContent, String extras) {
        return PushPayload.newBuilder()
            .setPlatform(Platform.android())
            .setAudience(Audience.all())
            .setNotification(Notification.newBuilder()
                .addPlatformNotification(AndroidNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .setTitle(notificationTitle)
                    .addExtra("androidNotification extras key", extras)
                    .build())
                .build()
            )
            .setMessage(Message.newBuilder()
                .setMsgContent(msgContent)
                .setTitle(msgTitle)
                .addExtra("message extras key", extras)
                .build())

            .setOptions(Options.newBuilder()
                .setApnsProduction(apnsProduction)
                .setSendno(1)
                .setTimeToLive(86400)
                .build())
            .build();
    }


    /**
     * 向ios平台所有用户推送消息
     */
    private PushPayload buildPushObject_ios_all_alertWithTitle(String notificationTitle, String msgTitle, String msgContent, String extras) {
        return PushPayload.newBuilder()
            .setPlatform(Platform.ios())
            .setAudience(Audience.all())
            .setNotification(Notification.newBuilder()
                .addPlatformNotification(IosNotification.newBuilder()
                    .setAlert(notificationTitle)
                    .incrBadge(1)
                    .setSound("default")
                    .addExtra("iosNotification extras key", extras)
                    .build())
                .build()
            )
            .setMessage(Message.newBuilder()
                .setMsgContent(msgContent)
                .setTitle(msgTitle)
                .addExtra("message extras key", extras)
                .build())
            .setOptions(Options.newBuilder()
                .setApnsProduction(apnsProduction)
                .setSendno(1)
                .setTimeToLive(86400)
                .build())
            .build();
    }
}
