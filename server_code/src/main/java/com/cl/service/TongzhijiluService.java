package com.cl.service;

import com.baomidou.mybatisplus.service.IService;
import com.cl.entity.TongzhijiluEntity;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.mapper.Wrapper;
import java.util.Map;
import java.util.List;

/**
 * 通知发送记录 服务类
 */
public interface TongzhijiluService extends IService<TongzhijiluEntity> {
    
    /**
     * 分页查询
     */
    Page<TongzhijiluEntity> queryPage(Map<String, Object> params);
    
    /**
     * 分页查询（带条件）
     */
    Page<TongzhijiluEntity> queryPage(Map<String, Object> params, Wrapper<TongzhijiluEntity> wrapper);
    
    /**
     * 根据条件查询列表
     */
    List<TongzhijiluEntity> selectListView(Wrapper<TongzhijiluEntity> wrapper);
    
    /**
     * 根据条件查询单个对象
     */
    TongzhijiluEntity selectView(Wrapper<TongzhijiluEntity> wrapper);
}
