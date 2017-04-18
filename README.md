#miaosha

##项目目标
小米抢购。并在支撑力上和用户体验上争取超过小米抢购。

##实现结构
恶意请求过滤-->限流-->redis消息队列执行减库存和订单

##请求过滤
1、入口只有活动开启前才能获得
2、入口恶意用户检测：多秒内多少次请求---可以记录最近10次请求时间，和前第九次请求时间对比

##实时限流器
1、实时限流：限制正在处理的请求量（通过消息队列获取正在处理的请求数目）为库存的100倍请求（这个可自定义）；
2、如果出现了限流器满了，但仍然有库存的情况怎么办？直接拒绝请求，允许用户重新提交请求
##请求减库存
1、请求通过了过滤之后，交给消息队列减库存+下单
##消息队列处理
1、消息队列再次过滤请求是否是恶意的用户
2、否则，执行减库存+下单

##各个方案
### 1. 直接更新数据库：
磁盘IO，开发机器实测2280 OPS，速度太低，当出现海量请求时会导致大量请求线程被阻塞，拒绝后续请求，拖垮整个tomcat和DB。

###2. redis+消息队列+更新数据库
* a.用户请求过来，将请求入消息队列；
* b.消息处理，先减redis库存量，如果减库存成功，则生成下单token存入redis（设定有效期，比如2分钟之内下单有效），等待用户下单（这样就避免下单也面对大量并发）；如果减库存失败，则消息记录回到消息队列中，等待再次处理；
* c.用户下单：判断token是否失效（比对时间）了，如果未失效则扣减库存（也可能扣减库存失败），生成订单；如果已经失效了，则redis库存增加1；
如何确保下单token过期了释放资格？JOB 每分钟扫token缓存，如果失效了的则清除调，并回馈redis缓存（redis库存+1）；

* d、前端用户如何获知抢购成功了（获得了下单资格）：ajax轮训查询接口。
说明：为什么要采用轮询而不是用实时的websocket推送？经测试，一台tomcat最多能连接3000个websocket，如果类似抢购的大量用户抢购，机器肯定是扛不住这么多长连接的，而查询用户是否抢购成功也只是查询的redis，因此采用轮询是很好的选择。

###3. 防刷过滤器+redis+消息队列+更新数据库
针对第2方案中可能出现被辅助软件而已刷单的现象，可以增加过滤器：如果用户在指定时间内请求多少次，则认为是恶意用户，可以直接将该用户加入黑名单，并在后续的消息队列处理中不给黑名单的用户分配资格。