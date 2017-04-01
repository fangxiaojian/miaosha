package wang.moshu.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.cache.GoodsStoreCacheWorker;
import wang.moshu.intercept.UserInterceptor;
import wang.moshu.service.GoodsService;
import wang.moshu.smvc.framework.annotation.RequestMapping;
import wang.moshu.smvc.framework.enums.ReturnType;
import wang.moshu.smvc.framework.interceptor.annotation.Intercept;
import wang.moshu.smvc.framework.util.Assert;
import wang.moshu.util.RedisUtil;

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
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	@Autowired
	private GoodsStoreCacheWorker goodsStoreCacheWorker;

	@Intercept(value = { UserInterceptor.class })
	@RequestMapping(value = "miaosha", returnType = ReturnType.JSON)
	public void miaosha(Integer goodsId)
	{
		Assert.notNull(goodsId);
		goodsService.miaosha(goodsId);
	}

	@RequestMapping(value = "miaoshaSql", returnType = ReturnType.JSON)
	public void miaoshaSql(Integer goodsId)
	{
		goodsService.miaoshaSql(goodsId);
	}

	@RequestMapping(value = "mysqlUpdateBenchMark", returnType = ReturnType.JSON)
	public void mysqlUpdateBenchMark() throws InterruptedException
	{
		goodsService.mysqlUpdateBenchMark();
	}

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
		redisUtil.get("ARTICLE_STORE_BY_ID_1_limiter", String.class);

		redisUtil.get("ARTICLE_STORE_BY_ID_1", String.class);

	}

	@RequestMapping(value = "cacheWorker", returnType = ReturnType.JSON)
	public void cacheWorker()
	{
		goodsStoreCacheWorker.get(1, String.class);
	}

	@RequestMapping(value = "doLimit", returnType = ReturnType.JSON)
	public void doLimit()
	{
		goodsBuyCurrentLimiter.doLimit(1, "来晚了咯");
	}

	@RequestMapping(value = "json", returnType = ReturnType.JSON)
	public Map<String, String> json(String name)
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		return result;
	}

}
