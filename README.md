# 接口列表 [ 只开放GET与POST ]
 
## 用户账号相关的接口

* 检查账号是否被注册:    GET  /api/account/checkUser
* 发送注册验证码:        POST /api/account/sendCode
* 注册:                  POST /api/account/register
* 登录：                 POST /api/account/login
* 查询用户资料:          GET  /api/account/profile
* 修改用户资料:          POST /api/account/profile
* 修改密码:              POST /api/account/password
* 修改头像:              POST /api/account/avatar
 
## 公共模块的api
 
* 意见反馈:              POST /api/feedback

## 场馆相关的接口

* 获取运动类别:          GET  /api/venue/types
* 分页获取场馆列表:      GET  /api/venue/venues TODO 修改返回值
* 场馆详情:              GET  /api/venue/detail