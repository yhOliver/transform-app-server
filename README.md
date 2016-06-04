# 接口列表 [ 只开放POST ]
 
## 用户账号相关的接口*

* 检查账号是否被注册:    POST /api/account/checkUser
* 发送注册验证码:        POST /api/account/sendCode
* 注册:                  POST /api/account/register
* 登录：                 POST /api/account/login
* 查询用户资料:          POST /api/account/view           (也可查询他人资料)
* 修改用户资料:          POST /api/account/update
* 修改密码:              POST /api/account/password
* 修改头像:              POST /api/account/avatar
* 获取头像:              POST /api/account/getAvatar      (也可获取他人头像)
* 获取用户粉丝列表:      POST /api/account/fans
* 获取用户关注列表:      POST /api/account/concerns
* 用户动态:              POST /api/account/posts
* 关注用户/取消关注:     POST /api/account/concern
* 修改手机号:            POST /api/account/changeMobile
* 重置密码（忘记密码）:  POST /api/account/resetPwd
* 我的预约:              POST /api/account/reservations

## 公共模块接口*
 
* 意见反馈:              POST /api/feedback

## 文件上传总接口
 
* 文件上传:              POST /api/fs/upload

## 场馆相关的接口*

* 获取运动类别:          POST /api/venue/types
* 分页获取场馆列表:      POST /api/venue/venues
* 模糊查询场馆列表:      POST /api/venue/search
* 场馆详情:              POST /api/venue/detail
* 场馆评价更多分页:      POST /api/venue/comments
* 评价场馆:              POST /api/venue/comment

## 部落相关的接口*

* 创建部落:              POST /api/tribe/create
* 更新部落信息:          POST /api/tribe/update
* 更新部落头像:          POST /api/tribe/avatar
* 查看部落信息:          POST /api/tribe/view
* 加入部落:              POST /api/tribe/join
* 退出部落:              POST /api/tribe/leave
* 我的部落               POST /api/tribe/mine
* 其他部落【未加入部落】 POST /api/tribe/others
* 部落成员【包括创建者】 POST /api/tribe/members

## 帖子相关的接口*

* 发帖:                  POST /api/post/add
* 回复:                  POST /api/post/reply
* 查看部落内帖子列表(分页): POST /api/post/thread
* 帖子详情:              POST /api/post/detail
* 帖子回复更多分页:      POST /api/post/replies
* 点赞/取消赞:           POST /api/post/zan
* 最新帖子:              POST /api/post/latest
* 删帖:                  POST /api/post/del
* 删回复:                POST /api/post/delReply
* 点赞人员列表:           POST /api/post/zans

## 商城相关接口*
* 查看类别          POST /api/goods/categories
* 查看商品列表       POST /api/goods/list
* 查看商品详细信息   POST /api/goods/view
* 查看商品评价       POST /api/goods/comments
* 商品确认收货      POST /api/goods/confirmReceipt  <<<<= 待实现
* 评价商品          POST /api/goods/comment

---
#### 替换触发器
* 发帖成功，用户状态数+1      V
* 评论成功，帖子评论数+1      V
* 点赞/取消赞，更新帖子赞数   V
* 关注用户/取消关注           V
* 加入(退出)部落              V   
* 删帖                        V
* 删评论                      V

---
#### 增加软删除(或禁用)
* 登陆                        V
* 删帖                        V
* 删评论                      V

---
LEFT JOIN  左关联检查         V

---
修改git服务器地址

## 收货地址相关接口*

* 我的收货地址 POST /api/consignee/mine                   V
* 查看收货地址 POST /api/consignee/view                   V
* 增加收货地址 POST /api/consignee/add                    V
* 删除收货地址 POST /api/consignee/del                    V
* 修改收货地址 POST /api/consignee/update                 V
* 设置默认收货地址 POST /api/consignee/defaultAddress      V