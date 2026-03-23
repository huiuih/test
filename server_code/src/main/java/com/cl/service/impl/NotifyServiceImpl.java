package com.cl.service.impl;

import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.YishengyuyueEntity;
import com.cl.entity.TongzhijiluEntity;
import com.cl.service.JiuzhentongzhiService;
import com.cl.service.NotifyService;
import com.cl.service.TongzhijiluService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.cl.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service("notifyService")
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;

    @Autowired
    private TongzhijiluService tongzhijiluService;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    // 通知类型常量
    private static final int NOTIFY_TYPE_SUCCESS = 1; // 预约成功通知
    private static final int NOTIFY_TYPE_ONE_DAY_BEFORE = 2; // 就诊前一天提醒
    private static final int NOTIFY_TYPE_TODAY = 3; // 就诊当天提醒

    // 发送状态常量
    private static final int SEND_STATUS_PENDING = 0; // 待发送
    private static final int SEND_STATUS_SUCCESS = 1; // 发送成功
    private static final int SEND_STATUS_FAILED = 2; // 发送失败

    @Override
    @Transactional
    public void sendNotification(YishengyuyueEntity yishengyuyue) {
        // 预约成功后立即创建并发送所有后续提醒
        Date jiuzhenTime = yishengyuyue.getYuyueshijian();
        String yuyuebianhao = yishengyuyue.getYuyuebianhao();

        // 1. 创建并发送预约成功通知
        sendSingleNotification(yishengyuyue, NOTIFY_TYPE_SUCCESS,
                "预约成功，您的就诊时间为：" + formatDate(jiuzhenTime), null);

        // 2. 创建就诊前一天提醒（预约时立即创建记录，但不立即发送）
        Calendar oneDayBefore = Calendar.getInstance();
        oneDayBefore.setTime(jiuzhenTime);
        oneDayBefore.add(Calendar.DAY_OF_MONTH, -1);

        // 如果就诊时间减去一天仍然在当前时间之后，则创建提醒记录
        if (oneDayBefore.getTime().after(new Date())) {
            createNotificationRecord(yishengyuyue, NOTIFY_TYPE_ONE_DAY_BEFORE,
                    "就诊提醒：您预约的就诊时间是明天 " + formatDate(jiuzhenTime) + "，请做好准备。",
                    oneDayBefore.getTime());
        }

        // 3. 创建就诊当天提醒（预约时立即创建记录，但不立即发送）
        Calendar today = Calendar.getInstance();
        today.setTime(jiuzhenTime);
        today.set(Calendar.HOUR_OF_DAY, 8); // 早上8点提醒
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        // 如果就诊当天提醒时间在当前时间之后，则创建提醒记录
        if (today.getTime().after(new Date())) {
            createNotificationRecord(yishengyuyue, NOTIFY_TYPE_TODAY,
                    "就诊提醒：您今天有就诊安排，就诊时间为 " + formatDate(jiuzhenTime) + "，请准时到达。",
                    today.getTime());
        }
    }

    /**
     * 发送单条通知并记录
     */
    private void sendSingleNotification(YishengyuyueEntity yishengyuyue, int notifyType, 
            String content, Date scheduledTime) {
        String tongzhibianhao = generateNotifyCode();
        
        // 创建就诊通知记录
        JiuzhentongzhiEntity notification = new JiuzhentongzhiEntity();
        notification.setTongzhibianhao(tongzhibianhao);
        notification.setYishengzhanghao(yishengyuyue.getYishengzhanghao());
        notification.setDianhua(yishengyuyue.getDianhua());
        notification.setJiuzhenshijian(yishengyuyue.getYuyueshijian());
        notification.setTongzhishijian(new Date());
        notification.setZhanghao(yishengyuyue.getZhanghao());
        notification.setShouji(yishengyuyue.getShouji());
        notification.setTongzhibeizhu(content);
        notification.setTongzhileixing(notifyType);
        notification.setChongshicishu(0);
        notification.setFasongzhuangtai(SEND_STATUS_PENDING);
        
        // 保存通知
        jiuzhentongzhiService.insert(notification);
        
        // 创建发送记录
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setTongzhibianhao(tongzhibianhao);
        record.setYuyuebianhao(yishengyuyue.getYuyuebianhao());
        record.setYishengzhanghao(yishengyuyue.getYishengzhanghao());
        record.setZhanghao(yishengyuyue.getZhanghao());
        record.setShouji(yishengyuyue.getShouji());
        record.setTongzhileixing(notifyType);
        record.setTongzhineirong(content);
        record.setJiuzhenshijian(yishengyuyue.getYuyueshijian());
        record.setChongshicishu(0);
        record.setFasongzhuangtai(SEND_STATUS_PENDING);
        
        // 立即发送通知
        boolean sendSuccess = sendNotificationToUser(notification);
        
        if (sendSuccess) {
            // 发送成功
            notification.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            record.setFasongshijian(new Date());
            System.out.println("通知发送成功：" + tongzhibianhao + "，类型：" + getNotifyTypeName(notifyType));
        } else {
            // 发送失败
            notification.setFasongzhuangtai(SEND_STATUS_FAILED);
            notification.setShibaiyuanyin("首次发送失败");
            record.setFasongzhuangtai(SEND_STATUS_FAILED);
            record.setShibaiyuanyin("首次发送失败");
            System.err.println("通知发送失败：" + tongzhibianhao + "，类型：" + getNotifyTypeName(notifyType));
        }
        
        // 更新通知状态
        jiuzhentongzhiService.updateById(notification);
        
        // 保存发送记录
        tongzhijiluService.insert(record);
    }

/**
     * 创建通知记录（用于定时发送的提醒）
     */
    private void createNotificationRecord(YishengyuyueEntity yishengyuyue, int notifyType, 
            String content, Date scheduledTime) {
        String tongzhibianhao = generateNotifyCode();
        
        // 创建就诊通知记录
        JiuzhentongzhiEntity notification = new JiuzhentongzhiEntity();
        notification.setTongzhibianhao(tongzhibianhao);
        notification.setYishengzhanghao(yishengyuyue.getYishengzhanghao());
        notification.setDianhua(yishengyuyue.getDianhua());
        notification.setJiuzhenshijian(yishengyuyue.getYuyueshijian());
        notification.setTongzhishijian(scheduledTime);
        notification.setZhanghao(yishengyuyue.getZhanghao());
        notification.setShouji(yishengyuyue.getShouji());
        notification.setTongzhibeizhu(content);
        notification.setTongzhileixing(notifyType);
        notification.setChongshicishu(0);
        notification.setFasongzhuangtai(SEND_STATUS_PENDING);
        
        jiuzhentongzhiService.insert(notification);
        
        // 创建发送记录
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setTongzhibianhao(tongzhibianhao);
        record.setYuyuebianhao(yishengyuyue.getYuyuebianhao());
        record.setYishengzhanghao(yishengyuyue.getYishengzhanghao());
        record.setZhanghao(yishengyuyue.getZhanghao());
        record.setShouji(yishengyuyue.getShouji());
        record.setTongzhileixing(notifyType);
        record.setTongzhineirong(content);
        record.setJiuzhenshijian(yishengyuyue.getYuyueshijian());
        record.setChongshicishu(0);
        record.setFasongzhuangtai(SEND_STATUS_PENDING);
        
        tongzhijiluService.insert(record);
        
        System.out.println("创建定时通知记录：" + tongzhibianhao + "，类型：" + getNotifyTypeName(notifyType) + 
                          "，计划发送时间：" + formatDate(scheduledTime));
    }

    @Override
    @Transactional
    public void retryNotification(Long notificationId) {
        JiuzhentongzhiEntity notification = jiuzhentongzhiService.selectById(notificationId);
        if (notification == null) {
            System.err.println("通知不存在：" + notificationId);
            return;
        }
        
        // 检查重试次数
        Integer retryCount = notification.getChongshicishu();
        if (retryCount == null) {
            retryCount = 0;
        }
        
        if (retryCount >= MAX_RETRY_COUNT) {
            System.err.println("通知重试次数已达上限：" + notification.getTongzhibianhao());
            notification.setShibaiyuanyin("重试次数已达上限（" + MAX_RETRY_COUNT + "次）");
            jiuzhentongzhiService.updateById(notification);
            return;
        }
        
        // 执行重试
        boolean sendSuccess = sendNotificationToUser(notification);
        
        // 更新重试次数
        retryCount++;
        notification.setChongshicishu(retryCount);
        
        // 更新发送记录
        EntityWrapper<TongzhijiluEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("tongzhibianhao", notification.getTongzhibianhao());
        TongzhijiluEntity record = tongzhijiluService.selectOne(wrapper);
        
        if (sendSuccess) {
            notification.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            notification.setShibaiyuanyin(null);
            System.out.println("通知重试成功：" + notification.getTongzhibianhao());
            
            if (record != null) {
                record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
                record.setFasongshijian(new Date());
                record.setChongshicishu(retryCount);
                record.setShibaiyuanyin(null);
                tongzhijiluService.updateById(record);
            }
        } else {
            notification.setFasongzhuangtai(SEND_STATUS_FAILED);
            notification.setShibaiyuanyin("第" + retryCount + "次重试失败");
            System.err.println("通知重试失败：" + notification.getTongzhibianhao() + "，已重试" + retryCount + "次");
            
            if (record != null) {
                record.setFasongzhuangtai(SEND_STATUS_FAILED);
                record.setChongshicishu(retryCount);
                record.setShibaiyuanyin("第" + retryCount + "次重试失败");
                tongzhijiluService.updateById(record);
            }
        }
        
        jiuzhentongzhiService.updateById(notification);
    }
    
    @Override
    @Transactional
    public void batchRetryNotification(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        
        for (Long id : ids) {
            try {
                retryNotification(id);
            } catch (Exception e) {
                System.err.println("批量重试通知失败，ID：" + id + "，错误：" + e.getMessage());
            }
        }
    }
    
    @Override
    public Map<String, Object> queryNotifyRecordPage(Map<String, Object> params) {
        Page<TongzhijiluEntity> page = new Query<TongzhijiluEntity>(params).getPage();
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<>();
        
        // 支持按状态筛选
        if (params.get("fasongzhuangtai") != null) {
            ew.eq("fasongzhuangtai", params.get("fasongzhuangtai"));
        }
        
        // 支持按类型筛选
        if (params.get("tongzhileixing") != null) {
            ew.eq("tongzhileixing", params.get("tongzhileixing"));
        }
        
        // 支持按用户账号筛选
        if (params.get("zhanghao") != null) {
            ew.eq("zhanghao", params.get("zhanghao"));
        }
        
        // 支持按手机号筛选
        if (params.get("shouji") != null) {
            ew.eq("shouji", params.get("shouji"));
        }
        
        ew.orderBy("addtime", false);
        
        page = tongzhijiluService.selectPage(page, ew);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("totalCount", page.getTotal());
        result.put("pageSize", page.getSize());
        result.put("currPage", page.getCurrent());
        result.put("totalPage", page.getPages());
        return result;
    }
    
    @Override
    public Map<String, Object> queryFailedNotifyPage(Map<String, Object> params) {
        Page<TongzhijiluEntity> page = new Query<TongzhijiluEntity>(params).getPage();
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<>();
        ew.eq("fasongzhuangtai", SEND_STATUS_FAILED);
        
        // 支持按用户账号筛选
        if (params.get("zhanghao") != null) {
            ew.eq("zhanghao", params.get("zhanghao"));
        }
        
        // 支持按手机号筛选
        if (params.get("shouji") != null) {
            ew.eq("shouji", params.get("shouji"));
        }
        
        ew.orderBy("addtime", false);
        
        page = tongzhijiluService.selectPage(page, ew);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("totalCount", page.getTotal());
        result.put("pageSize", page.getSize());
        result.put("currPage", page.getCurrent());
        result.put("totalPage", page.getPages());
        return result;
    }
    
    @Override
    public Map<String, Object> getNotifyStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总通知数
        int totalCount = tongzhijiluService.selectCount(null);
        statistics.put("totalCount", totalCount);
        
        // 发送成功数
        EntityWrapper<TongzhijiluEntity> successWrapper = new EntityWrapper<>();
        successWrapper.eq("fasongzhuangtai", SEND_STATUS_SUCCESS);
        int successCount = tongzhijiluService.selectCount(successWrapper);
        statistics.put("successCount", successCount);
        
        // 发送失败数
        EntityWrapper<TongzhijiluEntity> failedWrapper = new EntityWrapper<>();
        failedWrapper.eq("fasongzhuangtai", SEND_STATUS_FAILED);
        int failedCount = tongzhijiluService.selectCount(failedWrapper);
        statistics.put("failedCount", failedCount);
        
        // 待发送数
        EntityWrapper<TongzhijiluEntity> pendingWrapper = new EntityWrapper<>();
        pendingWrapper.eq("fasongzhuangtai", SEND_STATUS_PENDING);
        int pendingCount = tongzhijiluService.selectCount(pendingWrapper);
        statistics.put("pendingCount", pendingCount);
        
        // 成功率
        double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        statistics.put("successRate", String.format("%.2f%%", successRate));
        
        return statistics;
    }
    
    @Override
    @Transactional
    public boolean manualSendNotification(Long recordId) {
        TongzhijiluEntity record = tongzhijiluService.selectById(recordId);
        if (record == null) {
            return false;
        }
        
        // 查找对应的就诊通知
        EntityWrapper<JiuzhentongzhiEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("tongzhibianhao", record.getTongzhibianhao());
        JiuzhentongzhiEntity notification = jiuzhentongzhiService.selectOne(wrapper);
        
        if (notification == null) {
            return false;
        }
        
        // 执行发送
        boolean sendSuccess = sendNotificationToUser(notification);
        
        // 更新重试次数
        Integer retryCount = record.getChongshicishu();
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
        
        record.setChongshicishu(retryCount);
        notification.setChongshicishu(retryCount);
        
        if (sendSuccess) {
            record.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            record.setFasongshijian(new Date());
            record.setShibaiyuanyin(null);
            
            notification.setFasongzhuangtai(SEND_STATUS_SUCCESS);
            notification.setShibaiyuanyin(null);
        } else {
            record.setFasongzhuangtai(SEND_STATUS_FAILED);
            record.setShibaiyuanyin("手动重试失败");
            
            notification.setFasongzhuangtai(SEND_STATUS_FAILED);
            notification.setShibaiyuanyin("手动重试失败");
        }
        
        tongzhijiluService.updateById(record);
        jiuzhentongzhiService.updateById(notification);
        
        return sendSuccess;
    }
    
    @Override
    public List<TongzhijiluEntity> getUserNotifyRecords(String zhanghao) {
        EntityWrapper<TongzhijiluEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("zhanghao", zhanghao);
        wrapper.orderBy("addtime", false);
        return tongzhijiluService.selectList(wrapper);
    }

    /**
     * 模拟发送通知给用户
     * @param notification 通知信息
     * @return 是否发送成功
     */
    private boolean sendNotificationToUser(JiuzhentongzhiEntity notification) {
        // 实际项目中这里应该调用短信服务、邮件服务等
        // 这里我们模拟发送，90%的概率发送成功
        Random random = new Random();
        boolean success = random.nextDouble() < 0.9;
        System.out.println("发送通知到 " + notification.getShouji() + "，内容：" + 
                          notification.getTongzhibeizhu().substring(0, Math.min(20, notification.getTongzhibeizhu().length())) + 
                          "...，结果：" + (success ? "成功" : "失败"));
        return success;
    }
    
    /**
     * 生成通知编号
     */
    private String generateNotifyCode() {
        return "TZ" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
    }
    
    /**
     * 格式化日期
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
    
    /**
     * 获取通知类型名称
     */
    private String getNotifyTypeName(int type) {
        switch (type) {
            case NOTIFY_TYPE_SUCCESS:
                return "预约成功通知";
            case NOTIFY_TYPE_ONE_DAY_BEFORE:
                return "就诊前一天提醒";
            case NOTIFY_TYPE_TODAY:
                return "就诊当天提醒";
            default:
                return "未知类型";
        }
    }
}
