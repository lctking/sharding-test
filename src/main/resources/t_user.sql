CREATE TABLE `t_user_template` (
                            `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `username` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                            `password` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
                            `real_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
                            `region` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '国家/地区',
                            `id_type` int(3) DEFAULT NULL COMMENT '证件类型',
                            `id_card` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证件号',
                            `phone` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                            `telephone` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '固定电话',
                            `mail` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
                            `user_type` int(3) DEFAULT NULL COMMENT '旅客类型',
                            `verify_status` int(3) DEFAULT NULL COMMENT '审核状态',
                            `post_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮编',
                            `address` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
                            `deletion_time` bigint(20) DEFAULT '0' COMMENT '注销时间戳',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                            `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1695720264773 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `t_user_0` (
                                   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                   `username` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                                   `password` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
                                   `status` tinyint(1) DEFAULT NULL COMMENT '删除标识',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1695720264773 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
CREATE TABLE `t_user_1` (
                            `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `username` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                            `password` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
                            `status` tinyint(1) DEFAULT NULL COMMENT '删除标识',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1695720264773 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';