package wang.moshu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import wang.moshu.service.GoodsService;
import wang.moshu.smvc.framework.annotation.RequestMapping;
import wang.moshu.smvc.framework.enums.ReturnType;
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

	@RequestMapping(value = "miaosha", returnType = ReturnType.JSON)
	public Boolean miaosha(Integer goodsId)
	{
		Assert.notNull(goodsId);
		return goodsService.miaosha(goodsId);
	}

	@RequestMapping(value = "empty", returnType = ReturnType.JSON)
	public void empty()
	{

	}

	@RequestMapping(value = "redis", returnType = ReturnType.JSON)
	public void redis()
	{
		redisUtil.getForString("ARTICLE_STORE_BY_ID_1_limiter");
	}

}
