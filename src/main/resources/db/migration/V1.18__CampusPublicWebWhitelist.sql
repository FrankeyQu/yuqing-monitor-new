-- Batch 27: public web whitelist reservation.
-- This migration records allowed public web targets and keeps the actual fetcher disabled/reserved.

CREATE TABLE IF NOT EXISTS `campus_public_web_whitelist` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `whitelist_id` bigint NOT NULL COMMENT '白名单业务ID',
    `site_name` varchar(255) NOT NULL COMMENT '站点名称',
    `site_domain` varchar(255) NOT NULL COMMENT '允许域名，不含协议和路径',
    `base_url` varchar(1024) NOT NULL COMMENT '站点基础URL',
    `allowed_path_prefix` varchar(512) DEFAULT '/' COMMENT '允许路径前缀',
    `authorization_basis` varchar(1024) NOT NULL COMMENT '授权或来源依据',
    `authorization_scope` varchar(1024) NOT NULL COMMENT '授权范围',
    `robots_policy` varchar(1024) DEFAULT NULL COMMENT 'robots或站点规则说明',
    `rate_limit_seconds` int DEFAULT 60 COMMENT '建议最小采集间隔秒',
    `max_depth` int DEFAULT 0 COMMENT '预留最大深度，0表示仅当前页',
    `responsible_department_id` bigint DEFAULT NULL COMMENT '责任部门',
    `enabled` tinyint DEFAULT 1 COMMENT '是否启用(1启用,0停用)',
    `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
    `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0否,1是)',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_campus_public_web_whitelist_id` (`whitelist_id`),
    KEY `idx_campus_public_web_domain` (`site_domain`, `enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='校园舆情公开网页采集白名单表';

INSERT INTO `campus_dict_item` (`dict_type`, `item_code`, `item_name`, `item_value`, `sort_no`)
VALUES
    ('ingest_adapter_type', 'public_web_pull', '白名单公开网页', 'public_web_pull', 45)
ON DUPLICATE KEY UPDATE
    `item_name` = VALUES(`item_name`),
    `item_value` = VALUES(`item_value`),
    `sort_no` = VALUES(`sort_no`),
    `update_time` = CURRENT_TIMESTAMP;
