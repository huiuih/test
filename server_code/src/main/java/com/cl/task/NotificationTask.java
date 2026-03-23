package com.cl.task;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.TongzhijiluEntity;
import com.cl.service.JiuzhentongzhiService;
import com.cl.service.TongzhijiluService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 通知定时任务
 * 用于定时发送待发送的通知和重试失败的通知
 */
@Component
public class NotificationTask {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;
    
    @Autowired
    private TongzhijiluService tongzhijiluService;
    
    // 发送状态常量
    private static final int SEND_STATUS_PENDING = 0;      // 待发送
    private static final int SEND_STATUS_SUCCESS = 1;      // 发送成功
    private static final int SEND_STATUS_FAILED = 2;       // 发送失败
    
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 每分钟检查一次待发送的通知
     * 发送到达发送时间的通知
     */
    @Scheduled(cron = "0 * * * * ?")
    public void sendPendingNotifications() {
        System.out.println("【定时任务】开始检查待发送的通知...");
        
        EntityWrapper<JiuzhentongzhiEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("fasongzhuangtai", SEND_STATUS_PENDING);
        wrapper.le("tongzhishijian", new Date());  // 发送时间已到
        wrapper.orderBy("tongzhishijian", true);
        
        List<JiuzhentongzhiEntity> pendingList = jiuzhentongzhiService.selectList(wrapper);
        
        if (pendingList == null || pendingList.isEmpty()) {
            System.out.println("【定时任务】没有待发送的通知");
            return;
        }
        
        System.out.println("【定时任务】发现 " + pendingList.size() + " 条待发送通知");
        
        for (JiuzhentongzhiEntity notification : pendingList) {
            try {
                sendNotification(notification);
            } catch (Exception e) {
                System.err.println("【定时任务】发送通知失败，ID：" + notification.getId() + "，错误：" + e.getMessage());
            }
        }
        
        System.out.println("【定时任务】待发送通知处理完成");
    }
    
    /**
     * 每5分钟检查一次发送失败的通知进行重试
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void retryFailedNotifications() {
        System.out.println("【定时任务】开始重试发送失败的通知...");
        
        EntityWrapper<JiuzhentongzhiEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("fasongzhuangtai", SEND_STATUS_FAILED);
        wrapper.lt("chongshicishu", MAX_RETRY_COUNT);  // 重试次数未达上限
        wrapper.orderBy("addtime", true);
        
        List<JiuzhentongzhiEntity> failedList = jiuzhentongzhiService.selectList(wrapper);
        
        if (failedList == null || failedList.isEmpty()) {
            System.out.println("【定时任务】没有需要重试的失败通知");
            return;
        }
        
        System.out.println("【定时任务】发现 " + failedList.size() + " 条失败通知需要重试");
        
        for (JiuzhentongzhiEntity notification : failedList) {
            try {
                retryNotification(notification);
            } catch (Exception e) {
                System.err.println("【定时任务】重试通知失败，ID：" + notification.getId() + "，错误：" + e.getMessage());
            }
        }
        
        System.out.println("【定时任务】失败通知重试完成");
    }
    
    /**
     * 每天凌晨2点清理超过30天的已发送成功通知记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldNotifications() {
        System.out.println("【定时任务】开始清理过期通知记录...");
        
        // 计算30天前的日期
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
        
        EntityWrapper<TongzhijiluEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("fasongzhuangtai", SEND_STATUS_SUCCESS);
        wrapper.lt("fasongshijian", thirtyDaysAgo);
        
        // 这里可以根据实际需求决定是否物理删除或标记删除
        // 目前仅记录日志
        int count = tongzhijiluService.selectCount(wrapper);
        System.out.println("【定时任务】发现 " + count + " 条超过30天的已发送成功通知记录");
        
        // 如果需要清理，可以取消下面的注释
        // tongzhijiluService.delete(wrapper);
        
        System.out.println("【定时任务】过期通知记录清理完成");
    }
    
    /**
     * 发送单条通知
     */
    private void sendNotification(JiuzhentongzhiEntity notification) {
        // 模拟发送通知
        boolean sendSuccess = sendNotificationToUser(notification);
        
        // 更新通知状态
        if (sendSuccess) {
            notification.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            System.out.println("【定时任务】通知发送成功：" + notification.getTongzhibianhao());
        } else {
            notification.setFasongzhuangtai(SEND_STATUS_FAILED);
            notification.setShibaiyuanyin("定时任务发送失败");
            System.err.println("【定时任务】通知发送失败：" + notification.getTongzhibianhao());
        }
        
        jiuzhentongzhiService.updateById(notification);
        
        // 更新发送记录
        updateNotifyRecord(notification, sendSuccess);
    }
    
    /**
     * 重试发送通知
     */
    private void retryNotification(JiuzhentongzhiEntity notification) {
        Integer retryCount = notification.getChongshicishu();
        if (retryCount == null) {
            retryCount = 0;
        }
        
        // 模拟发送通知
        boolean sendSuccess = sendNotificationToUser(notification);
        
        // 更新重试次数
        retryCount++;
        notification.setChongshicishu(retryCount);
        
        if (sendSuccess) {
            notification.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            notification.setShibaiyuanyin(null);
            System.out.println("【定时任务】通知重试成功：" + notification.getTongzhibianhao());
        } else {
            notification.setFasongzhuangtai(SEND_STATUS_FAILED);
            notification.setShibaiyuanyin("第" + retryCount + "次重试失败");
            System.err.println("【定时任务】通知重试失败：" + notification.getTongzhibianhao() + "，已重试" + retryCount + "次");
        }
        
        jiuzhentongzhiService.updateById(notification);
        
        // 更新发送记录
        updateNotifyRecord(notification, sendSuccess);
    }
    
    /**
     * 更新通知发送记录
     */
    private void updateNotifyRecord(JiuzhentongzhiEntity notification, boolean sendSuccess) {
        EntityWrapper<TongzhijiluEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("tongzhibianhao", notification.getTongzhibianhao());
        TongzhijiluEntity record = tongzhijiluService.selectOne(wrapper);
        
        if (record != null) {
            record.setChongshicishu(notification.getChongshicishu());
            
            if (sendSuccess) {
                record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
                record.setFasongshijian(new Date());
                record.setShibaiyuanyin(null);
            } else {
                record.setFasongzhuangtai(SEND_STATUS_FAILED);
                record.setShibaiyuanyin(notification.getShibaiyuanyin());
            }
            
            tongzhijiluService.updateById(record);
        }
    }
    
    /**
     * 模拟发送通知给用户
     */
    private boolean sendNotificationToUser(JiuzhentongzhiEntity notification) {
        // 实际项目中这里应该调用短信服务、邮件服务等
        // 这里我们模拟发送，90%的概率发送成功
        Random random = new Random();
        boolean success = random.nextDouble() < 0.9;
        System.out.println("【定时任务】发送通知到 " + notification.getShouji() + "，结果：" + (success ? "成功" : "失败"));
        return success;
    }
}
