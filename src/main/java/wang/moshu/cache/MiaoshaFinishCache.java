package wang.moshu.cache;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wang.moshu.constant.CommonConstant;
import wang.moshu.util.RedisUtil;

/**
 * 秒杀结束缓存
 * 
 * @category 秒杀结束缓存
 * @author xiangyong.ding@weimob.com
 * @since 2017年4月18日 下午10:23:23
 */
@Component
public class MiaoshaFinishCache
{
	@Autowired
	private RedisUtil redisUtil;

	/**
	 * 设定是否秒杀结束
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月18日 下午10:26:27
	 * @param goodsId
	 */
	public void setFinish(Integer goodsId)
	{
		redisUtil.set(getKey(goodsId), "");
	}

	/**
	 * 指定商品秒杀是否结束
	 * 
	 * @category 指定商品秒杀是否结束
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年4月18日 下午10:31:01
	 * @param goodsId
	 * @return
	 */
	public boolean isFinish(Integer goodsId)
	{
		return redisUtil.get(getKey(goodsId), String.class) != null;
	}

	private String getKey(Integer goodsId)
	{
		String key = MessageFormat.format(CommonConstant.RedisKey.MIAOSHA_FINISH_FLAG, new Object[] { goodsId });
		return key;
	}
}
