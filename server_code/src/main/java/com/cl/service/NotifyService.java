package com.cl.service;

import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.YishengyuyueEntity;
import com.cl.entity.TongzhijiluEntity;

import java.util.List;
import java.util.Map;

public interface NotifyService {
    /**
     * 发送就诊通知（预约成功后立即发送所有后续提醒）
     * @param yishengyuyue 医生预约信息
     */
    void sendNotification(YishengyuyueEntity yishengyuyue);
    
    /**
     * 重试发送失败的通知
     * @param notificationId 通知ID
     */
    void retryNotification(Long notificationId);
    
    /**
     * 批量重试发送失败的通知
     * @param ids 通知ID数组
     */
    void batchRetryNotification(Long[] ids);
    
    /**
     * 获取通知发送记录列表（带分页和条件查询）
     * @param params 查询参数
     * @return 分页数据
     */
    Map<String, Object> queryNotifyRecordPage(Map<String, Object> params);
    
    /**
     * 获取发送失败的通知列表
     * @param params 查询参数
     * @return 分页数据
     */
    Map<String, Object> queryFailedNotifyPage(Map<String, Object> params);
    
    /**
     * 获取通知统计信息
     * @return 统计数据
     */
    Map<String, Object> getNotifyStatistics();
    
    /**
     * 手动发送指定通知
     * @param recordId 记录ID
     * @return 是否发送成功
     */
    boolean manualSendNotification(Long recordId);
    
    /**
     * 获取用户的所有通知记录
     * @param zhanghao 用户账号
     * @return 通知记录列表
     */
    List<TongzhijiluEntity> getUserNotifyRecords(String zhanghao);
}
