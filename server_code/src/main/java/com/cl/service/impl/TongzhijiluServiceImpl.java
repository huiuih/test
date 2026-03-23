package com.cl.service.impl;

import com.cl.dao.TongzhijiluDao;
import com.cl.entity.TongzhijiluEntity;
import com.cl.service.TongzhijiluService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.cl.utils.PageUtils;
import com.cl.utils.Query;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

/**
 * 通知发送记录 服务实现类
 */
@Service("tongzhijiluService")
public class TongzhijiluServiceImpl extends ServiceImpl<TongzhijiluDao, TongzhijiluEntity> implements TongzhijiluService {

    @Override
    public Page<TongzhijiluEntity> queryPage(Map<String, Object> params) {
        Page<TongzhijiluEntity> page = this.selectPage(
                new Query<TongzhijiluEntity>(params).getPage(),
                new Wrapper<TongzhijiluEntity>() {
                    @Override
                    public String getSqlSegment() {
                        return null;
                    }
                }
        );
        return page;
    }

    @Override
    public Page<TongzhijiluEntity> queryPage(Map<String, Object> params, Wrapper<TongzhijiluEntity> wrapper) {
        Page<TongzhijiluEntity> page = new Query<TongzhijiluEntity>(params).getPage();
        return this.selectPage(page, wrapper);
    }

    @Override
    public List<TongzhijiluEntity> selectListView(Wrapper<TongzhijiluEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public TongzhijiluEntity selectView(Wrapper<TongzhijiluEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }
}
