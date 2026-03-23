package com.cl.service;

import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.YishengyuyueEntity;

public interface NotifyService {
    /**
     * 发送就诊通知
     * @param yishengyuyue 医生预约信息
     */
    void sendNotification(YishengyuyueEntity yishengyuyue);
    
    /**
     * 重试发送失败的通知
     * @param notificationId 通知ID
     */
    void retryNotification(Long notificationId);
}
