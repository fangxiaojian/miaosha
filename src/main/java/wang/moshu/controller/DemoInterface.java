package wang.moshu.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.cache.MiaoshaSuccessTokenCache;
import wang.moshu.constant.MessageType;
import wang.moshu.message.Message;
import wang.moshu.message.RedisUtil;
import wang.moshu.mq.message.MiaoshaRequestMessage;
import wang.moshu.service.GoodsService;
import wang.moshu.smvc.framework.annotation.RequestMapping;
import wang.moshu.smvc.framework.enums.ReturnType;
import wang.moshu.smvc.framework.util.Assert;

/**
 * 接口路由
 * 
 * @category 接口路由
 * @author xiangyong.ding@weimob.com
 * @since 2017年1月23日 下午9:32:49
 */
@Controller
@RequestMapping(value = "/i/")
public class DemoInterface
{
	@Autowired
	private GoodsService goodsService;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private wang.moshu.util.RedisUtil redisUtil2;

	@Autowired
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	// @Autowired
	// private GoodsStoreCacheWorker goodsStoreCacheWorker;

	@Autowired
	private MiaoshaSuccessTokenCache miaoshaSuccessTokenCache;

	// @Autowired
	// private MiaoshaRequestHandler miaoshaRequestHandler;

	// @Intercept(value = { UserInterceptor.class })
	// @Intercept(value = { ExecuteTimeInterceptor.class })
	@RequestMapping(value = "miaosha", returnType = ReturnType.JSON)
	public String miaosha(String mobile, String goodsRandomName)
	{
		Assert.notNull(goodsRandomName);
		Assert.notNull(mobile);

		goodsService.miaosha(mobile, goodsRandomName);

		// 为什么要返回mobile，为了方便jmeter测试
		return mobile;
	}

	/**
	 * 秒杀-不走消息队列
	 * 
	 * @category 秒杀-不走消息队列
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年4月26日 下午11:44:51
	 * @param mobile
	 * @param goodsRandomName
	 * @return
	 */
	// @Intercept(value = { ExecuteTimeInterceptor.class })
	// @RequestMapping(value = "miaoshaD", returnType = ReturnType.JSON)
	// public String miaoshaD(String mobile, String goodsRandomName)
	// {
	// Assert.notNull(goodsRandomName);
	// Assert.notNull(mobile);
	//
	// miaoshaRequestHandler.handle(new MiaoshaRequestMessage(mobile,
	// goodsRandomName));
	//
	// // 为什么要返回mobile，为了方便jmeter测试
	// return mobile;
	// }

	// @RequestMapping(value = "miaoshaSql", returnType = ReturnType.JSON)
	// public void miaoshaSql(String mobile, Integer goodsId)
	// {
	// goodsService.miaoshaSql(mobile, goodsId);
	// }

	// @RequestMapping(value = "mysqlUpdateBenchMark", returnType =
	// ReturnType.JSON)
	// public void mysqlUpdateBenchMark() throws InterruptedException
	// {
	// goodsService.mysqlUpdateBenchMark();
	// }

	// @RequestMapping(value = "miaosha", returnType = ReturnType.JSON)
	// public void miaosha()
	// {
	// goodsService.miaosha(1);
	// }

	@RequestMapping(value = "empty", returnType = ReturnType.JSON)
	public void empty()
	{

	}

	@RequestMapping(value = "redis", returnType = ReturnType.JSON)
	public void redis()
	{
		// redisUtil.get("REDIS_GOODS_STORE_GOODS_RANDOM_NAME:0e67e331-c521-406a-b705-64e557c4c06c",
		// String.class);
		Message message = new Message(MessageType.MIAOSHA_MESSAGE,
				new MiaoshaRequestMessage("232323232332", "0e67e331-c521-406a-b705-64e557c4c06c"));
		redisUtil.rpush(message.getKey().toString(), message, 0);
		// redisUtil.get("ARTICLE_STORE_BY_ID_1", String.class);

	}

	@RequestMapping(value = "redis2", returnType = ReturnType.JSON)
	public void redis2()
	{
		// redisUtil.get("REDIS_GOODS_STORE_GOODS_RANDOM_NAME:0e67e331-c521-406a-b705-64e557c4c06c",
		// String.class);
		Message message = new Message(MessageType.MIAOSHA_MESSAGE,
				new MiaoshaRequestMessage("232323232332", "0e67e331-c521-406a-b705-64e557c4c06c"));
		redisUtil2.lpush(message.getKey().toString(), message, 0);
		// redisUtil.get("ARTICLE_STORE_BY_ID_1", String.class);

	}

	@RequestMapping(value = "redis3", returnType = ReturnType.JSON)
	public void redis3()
	{
		redisUtil2.set("ddddddddd", "dssssssssss");
	}

	// @RequestMapping(value = "cacheWorker", returnType = ReturnType.JSON)
	// public void cacheWorker()
	// {
	// goodsStoreCacheWorker.get(1, Integer.class);
	// }

	@RequestMapping(value = "doLimit", returnType = ReturnType.JSON)
	public void doLimit()
	{
		goodsBuyCurrentLimiter.doLimit("0e67e331-c521-406a-b705-64e557c4c06c", "来晚了咯");
	}

	@RequestMapping(value = "json", returnType = ReturnType.JSON)
	public Map<String, String> json(String name)
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		return result;
	}

	/**
	 * 获取秒杀商品的链接
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月24日 下午12:47:40
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "getMiaoshaGoodsLink", returnType = ReturnType.JSON)
	public String getMiaoshaGoodsLink(Integer goodsId)
	{
		return goodsService.getGoodsRandomName(goodsId);
	}

	/**
	 * 查询是否秒杀成功
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月12日 下午10:55:32
	 * @param mobile
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "miaoshaResult", returnType = ReturnType.JSON)
	public String isMiaoshaSuccess(String mobile, String goodsRandomName)
	{
		// 直接取缓存查询是否有成功的记录生成
		return miaoshaSuccessTokenCache.genToken(mobile, goodsRandomName);
	}

	/**
	 * 下单
	 * 
	 * @category 下单
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年4月25日 上午12:35:34
	 * @param mobile
	 * @param goodsId
	 * @param token
	 */
	@RequestMapping(value = "order", returnType = ReturnType.JSON)
	public Integer order(String mobile, Integer goodsId, String token)
	{
		return goodsService.order(mobile, goodsId, token);
	}
}
