package com.cl.controller;

import com.cl.service.NotifyService;
import com.cl.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知管理
 * 后端接口
 * @author 
 * @email 
 * @date 2025-03-27 15:44:15
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {
    @Autowired
    private NotifyService notifyService;

    /**
     * 重试发送通知
     */
    @RequestMapping("/retry")
    public R retry(@RequestParam Long id){
        notifyService.retryNotification(id);
        return R.ok();
    }
}
