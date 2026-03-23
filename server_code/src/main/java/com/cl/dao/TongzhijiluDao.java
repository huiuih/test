package com.cl.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.cl.entity.TongzhijiluEntity;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 通知发送记录 DAO接口
 */
public interface TongzhijiluDao extends BaseMapper<TongzhijiluEntity> {
    
    /**
     * 分页查询
     */
    List<TongzhijiluEntity> selectListView(Pagination page, @Param("ew") Wrapper<TongzhijiluEntity> wrapper);
    
    /**
     * 列表查询
     */
    List<TongzhijiluEntity> selectListView(@Param("ew") Wrapper<TongzhijiluEntity> wrapper);
    
    /**
     * 单条查询
     */
    TongzhijiluEntity selectView(@Param("ew") Wrapper<TongzhijiluEntity> wrapper);
}
