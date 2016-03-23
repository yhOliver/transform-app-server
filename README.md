# 接口列表 [ 只开放POST ]
 
## 用户账号相关的接口

* 检查账号是否被注册:    POST /api/account/checkUser
* 发送注册验证码:        POST /api/account/sendCode
* 注册:                  POST /api/account/register
* 登录：                 POST /api/account/login
* 查询用户资料:          POST /api/account/view
* 修改用户资料:          POST /api/account/update
* 修改密码:              POST /api/account/password
* 修改头像:              POST /api/account/avatar
 
## 公共模块的api
 
* 意见反馈:              POST /api/feedback

## 场馆相关的接口

* 获取运动类别:          POST /api/venue/types
* 分页获取场馆列表:      POST /api/venue/venues
* 模糊查询场馆列表:      POST /api/venue/search
* 场馆详情:              POST /api/venue/detail
* 场馆评价更多分页:      POST /api/venue/comments