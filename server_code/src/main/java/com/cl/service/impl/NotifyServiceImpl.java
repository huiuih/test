package com.cl.service.impl;

import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.YishengyuyueEntity;
import com.cl.service.JiuzhentongzhiService;
import com.cl.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service("notifyService")
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;

    @Override
    @Transactional
    public void sendNotification(YishengyuyueEntity yishengyuyue) {
        // 创建就诊通知
        JiuzhentongzhiEntity notification = new JiuzhentongzhiEntity();
        notification.setTongzhibianhao(System.currentTimeMillis() + "" + new Random().nextInt(1000));
        notification.setYishengzhanghao(yishengyuyue.getYishengzhanghao());
        notification.setDianhua(yishengyuyue.getDianhua());
        notification.setJiuzhenshijian(yishengyuyue.getYuyueshijian());
        notification.setTongzhishijian(new Date());
        notification.setZhanghao(yishengyuyue.getZhanghao());
        notification.setShouji(yishengyuyue.getShouji());
        notification.setTongzhibeizhu("预约成功，您的就诊时间为：" + yishengyuyue.getYuyueshijian());

        // 保存通知
        jiuzhentongzhiService.insert(notification);

        // 模拟发送通知（实际项目中这里应该调用短信、邮件等发送服务）
        boolean sendSuccess = sendNotificationToUser(notification);

        if (!sendSuccess) {
            // 发送失败，记录失败状态到系统日志
            System.err.println("通知发送失败：" + notification.getTongzhibianhao() + "，用户：" + notification.getZhanghao() + "，手机：" + notification.getShouji());
            // 这里可以添加失败记录到数据库，或者使用消息队列进行重试
        }
    }

    @Override
    public void retryNotification(Long notificationId) {
        JiuzhentongzhiEntity notification = jiuzhentongzhiService.selectById(notificationId);
        if (notification != null) {
            boolean sendSuccess = sendNotificationToUser(notification);
            if (sendSuccess) {
                System.out.println("通知重试成功：" + notification.getTongzhibianhao());
            } else {
                System.err.println("通知重试失败：" + notification.getTongzhibianhao());
            }
        }
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
        System.out.println("发送通知到 " + notification.getShouji() + "，结果：" + (success ? "成功" : "失败"));
        return success;
    }
}
