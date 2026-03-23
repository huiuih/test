-- 更新就诊通知表结构，添加发送状态相关字段
ALTER TABLE `jiuzhentongzhi` 
ADD COLUMN `fasongzhuangtai` int(11) DEFAULT '0' COMMENT '发送状态：0-待发送，1-发送成功，2-发送失败' AFTER `tongzhibeizhu`,
ADD COLUMN `chongshicishu` int(11) DEFAULT '0' COMMENT '重试次数' AFTER `fasongzhuangtai`,
ADD COLUMN `shibaiyuanyin` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因' AFTER `chongshicishu`,
ADD COLUMN `tongzhileixing` int(11) DEFAULT '1' COMMENT '通知类型：1-预约成功通知，2-就诊前一天提醒，3-就诊当天提醒' AFTER `shibaiyuanyin`;

-- 创建通知发送记录表
DROP TABLE IF EXISTS `tongzhijilu`;
CREATE TABLE `tongzhijilu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tongzhibianhao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知编号',
  `yuyuebianhao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预约编号',
  `yishengzhanghao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '医生账号',
  `zhanghao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户账号',
  `shouji` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户手机',
  `tongzhileixing` int(11) DEFAULT '1' COMMENT '通知类型：1-预约成功通知，2-就诊前一天提醒，3-就诊当天提醒',
  `fasongzhuangtai` int(11) DEFAULT '0' COMMENT '发送状态：0-待发送，1-发送成功，2-发送失败',
  `chongshicishu` int(11) DEFAULT '0' COMMENT '重试次数',
  `shibaiyuanyin` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `fasongshijian` datetime DEFAULT NULL COMMENT '发送时间',
  `jiuzhenshijian` datetime DEFAULT NULL COMMENT '就诊时间',
  `tongzhineirong` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知内容',
  PRIMARY KEY (`id`),
  KEY `idx_tongzhibianhao` (`tongzhibianhao`),
  KEY `idx_yuyuebianhao` (`yuyuebianhao`),
  KEY `idx_zhanghao` (`zhanghao`),
  KEY `idx_fasongzhuangtai` (`fasongzhuangtai`),
  KEY `idx_tongzhileixing` (`tongzhileixing`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送记录';

-- 添加菜单权限（可选，用于后台管理）
-- 注意：根据实际情况修改menu表中的JSON数据来添加菜单项
