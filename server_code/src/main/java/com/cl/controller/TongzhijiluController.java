package com.cl.controller;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.cl.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.annotation.IgnoreAuth;
import com.cl.annotation.SysLog;

import com.cl.entity.TongzhijiluEntity;
import com.baomidou.mybatisplus.plugins.Page;

import com.cl.service.TongzhijiluService;
import com.cl.service.NotifyService;
import com.cl.utils.PageUtils;
import com.cl.utils.R;
import com.cl.utils.MPUtil;

/**
 * 通知发送记录
 * 后端接口
 * @author 
 * @email 
 * @date 2025-03-27 15:44:15
 */
@RestController
@RequestMapping("/tongzhijilu")
public class TongzhijiluController {
    @Autowired
    private TongzhijiluService tongzhijiluService;
    
    @Autowired
    private NotifyService notifyService;

    /**
     * 后台列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, TongzhijiluEntity tongzhijilu,
                  HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        
        // 支持按状态筛选
        if (tongzhijilu.getFasongzhuangtai() != null) {
            ew.eq("fasongzhuangtai", tongzhijilu.getFasongzhuangtai());
        }
        
        // 支持按类型筛选
        if (tongzhijilu.getTongzhileixing() != null) {
            ew.eq("tongzhileixing", tongzhijilu.getTongzhileixing());
        }
        
        // 支持按用户账号筛选
        if (StringUtils.isNotBlank(tongzhijilu.getZhanghao())) {
            ew.eq("zhanghao", tongzhijilu.getZhanghao());
        }
        
        // 支持按手机号筛选
        if (StringUtils.isNotBlank(tongzhijilu.getShouji())) {
            ew.eq("shouji", tongzhijilu.getShouji());
        }
        
        // 支持按预约编号筛选
        if (StringUtils.isNotBlank(tongzhijilu.getYuyuebianhao())) {
            ew.eq("yuyuebianhao", tongzhijilu.getYuyuebianhao());
        }
        
        Page<TongzhijiluEntity> page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhijilu), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, TongzhijiluEntity tongzhijilu,
                  HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();

        Page<TongzhijiluEntity> page = tongzhijiluService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhijilu), params), params));
        return R.ok().put("data", new PageUtils(page));
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(TongzhijiluEntity tongzhijilu){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        ew.allEq(MPUtil.allEQMapPre(tongzhijilu, "tongzhijilu")); 
        return R.ok().put("data", tongzhijiluService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(TongzhijiluEntity tongzhijilu){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        ew.allEq(MPUtil.allEQMapPre(tongzhijilu, "tongzhijilu")); 
        TongzhijiluEntity tongzhijiluView = tongzhijiluService.selectView(ew);
        return R.ok("查询通知发送记录成功").put("data", tongzhijiluView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        TongzhijiluEntity tongzhijilu = tongzhijiluService.selectById(id);
        return R.ok().put("data", tongzhijilu);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        TongzhijiluEntity tongzhijilu = tongzhijiluService.selectById(id);
        return R.ok().put("data", tongzhijilu);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    @SysLog("新增通知发送记录")
    public R save(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
        tongzhijiluService.insert(tongzhijilu);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @SysLog("新增通知发送记录")
    @RequestMapping("/add")
    public R add(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
        tongzhijiluService.insert(tongzhijilu);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @Transactional
    @SysLog("修改通知发送记录")
    public R update(@RequestBody TongzhijiluEntity tongzhijilu, HttpServletRequest request){
        tongzhijiluService.updateById(tongzhijilu);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @SysLog("删除通知发送记录")
    public R delete(@RequestBody Long[] ids){
        tongzhijiluService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 获取发送失败的通知列表
     */
    @RequestMapping("/failedList")
    public R failedList(@RequestParam Map<String, Object> params, HttpServletRequest request){
        Map<String, Object> result = notifyService.queryFailedNotifyPage(params);
        return R.ok().put("data", result);
    }
    
    /**
     * 重试发送失败的通知
     */
    @RequestMapping("/retry/{id}")
    @Transactional
    @SysLog("重试发送通知")
    public R retry(@PathVariable("id") Long id, HttpServletRequest request){
        try {
            notifyService.retryNotification(id);
            return R.ok("重试发送成功");
        } catch (Exception e) {
            return R.error("重试发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量重试发送失败的通知
     */
    @RequestMapping("/batchRetry")
    @Transactional
    @SysLog("批量重试发送通知")
    public R batchRetry(@RequestBody Long[] ids, HttpServletRequest request){
        if (ids == null || ids.length == 0) {
            return R.error("请选择要重试的记录");
        }
        
        try {
            notifyService.batchRetryNotification(ids);
            return R.ok("批量重试发送成功");
        } catch (Exception e) {
            return R.error("批量重试发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 手动发送通知
     */
    @RequestMapping("/manualSend/{id}")
    @Transactional
    @SysLog("手动发送通知")
    public R manualSend(@PathVariable("id") Long id, HttpServletRequest request){
        boolean success = notifyService.manualSendNotification(id);
        if (success) {
            return R.ok("手动发送成功");
        } else {
            return R.error("手动发送失败");
        }
    }
    
    /**
     * 获取通知统计信息
     */
    @RequestMapping("/statistics")
    public R statistics(HttpServletRequest request){
        Map<String, Object> statistics = notifyService.getNotifyStatistics();
        return R.ok().put("data", statistics);
    }
    
    /**
     * 获取用户的通知记录
     */
    @RequestMapping("/userRecords/{zhanghao}")
    public R userRecords(@PathVariable("zhanghao") String zhanghao, HttpServletRequest request){
        List<TongzhijiluEntity> records = notifyService.getUserNotifyRecords(zhanghao);
        return R.ok().put("data", records);
    }
    
    /**
     * 总数量
     */
    @RequestMapping("/count")
    public R count(@RequestParam Map<String, Object> params, TongzhijiluEntity tongzhijilu, HttpServletRequest request){
        EntityWrapper<TongzhijiluEntity> ew = new EntityWrapper<TongzhijiluEntity>();
        
        // 支持按状态筛选
        if (tongzhijilu.getFasongzhuangtai() != null) {
            ew.eq("fasongzhuangtai", tongzhijilu.getFasongzhuangtai());
        }
        
        // 支持按类型筛选
        if (tongzhijilu.getTongzhileixing() != null) {
            ew.eq("tongzhileixing", tongzhijilu.getTongzhileixing());
        }
        
        int count = tongzhijiluService.selectCount(MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, tongzhijilu), params), params));
        return R.ok().put("data", count);
    }
}
