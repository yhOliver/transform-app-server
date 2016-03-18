SET FOREIGN_KEY_CHECKS=0;

drop database IF EXISTS `transform2`;

create database `transform2` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

use `transform2`;

-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
# 用户表
CREATE TABLE `t_user` (
  `userId` char(32) NOT NULL DEFAULT '',
  `loginName` varchar(20) NOT NULL COMMENT '用户登录名',
  `nickName` varchar(20) NOT NULL COMMENT '昵称',
  `password` varchar(32) NOT NULL COMMENT 'md5加密后的密码',
  `sex` tinyint(2) NOT NULL COMMENT '性别，1表示男，0表示女',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(2) DEFAULT '1' COMMENT '帐号状态. 1表示开启 ，0表示禁用',
  `creationDate` bigint(20) DEFAULT NULL COMMENT '帐号创建日期时间戳',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像地址',
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -------------------------------------
-- Table structure for `t_register_code`
-- -------------------------------------
DROP TABLE IF EXISTS `t_register_code`;
# 注册验证码表
CREATE TABLE `t_register_code` (
  `mobile` char(11) NOT NULL COMMENT '接收短信的手机号码',
  `code` char(6) DEFAULT NULL,
  PRIMARY KEY (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------
-- Table structure for `t_feedback`
-- --------------------------------
DROP TABLE IF EXISTS `t_feedback`;
# 意见反馈表
CREATE TABLE `t_feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `userId` char(32) DEFAULT NULL COMMENT '用户ID',
  `creationDate` bigint(20) NOT NULL COMMENT '反馈的时间戳',
  `suggestion` varchar(300) NOT NULL COMMENT '反馈内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

commit;