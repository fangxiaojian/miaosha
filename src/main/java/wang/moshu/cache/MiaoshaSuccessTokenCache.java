package wang.moshu.cache;

import java.text.MessageFormat;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wang.moshu.constant.CommonConstant;
import wang.moshu.util.RedisUtil;

/**
 * 秒杀获取到了下单资格token缓存
 * @category 秒杀获取到了下单资格token缓存
 * @author xiangyong.ding@weimob.com
 * @since 2017年4月13日 上午12:00:34
 */
@Component
public class MiaoshaSuccessTokenCache
{
	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private GoodsRedisStoreCache goodsRedisStoreCache;

	public String genToken(String mobile, Integer goodsId)
	{
		String key = getKey(mobile, goodsId);
		String token = getToken();
		redisUtil.set(key + token, System.currentTimeMillis());

		return token;
	}

	/**
	 * 验证token
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月9日 下午12:55:28
	 * @param token
	 * @return false:token无效，true:token有效
	 */
	public boolean validateToken(String mobile, Integer goodsId, String token)
	{
		String key = getKey(mobile, goodsId) + token;
		Long tokenSavedTimeStamp = redisUtil.get(key, Long.class);

		// 判断token是否过了有效期
		if (tokenSavedTimeStamp != null
				&& (System.currentTimeMillis() - tokenSavedTimeStamp < CommonConstant.TOKEN_EFECTIVE_MILLISECONDS))
		{
			//已经验证了的清楚掉
			redisUtil.delete(key);
			// 如果token验证成功
			return true;
		}
		else if (tokenSavedTimeStamp != null)
		{
			//失效了的清楚掉
			redisUtil.delete(key);
			// 如果token存在，且是过期的，则回馈redis库存
			goodsRedisStoreCache.incrStore(goodsId);
		}

		return false;
	}

	/**
	 * 以KEY方式验证token是否失效
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月12日 下午11:51:17
	 * @param key
	 * @return
	 */
	public void validateTokenByKey(String key)
	{
		Long tokenSavedTimeStamp = redisUtil.get(key, Long.class);

		// 判断token是否过了有效期
		if (tokenSavedTimeStamp != null
				&& (System.currentTimeMillis() - tokenSavedTimeStamp > CommonConstant.TOKEN_EFECTIVE_MILLISECONDS))
		{
			//失效了的清楚掉
			redisUtil.delete(key);
			// 如果token存在，且是过期的，则回馈redis库存
			goodsRedisStoreCache.incrStore(Integer.parseInt(key.substring(key.lastIndexOf(":"), key.indexOf("_"))));
		}
	}

	protected String getKey(String mobile, Integer goodsId)
	{
		String key = MessageFormat.format(CommonConstant.RedisKey.MIAOSHA_SUCCESS_TOKEN,
				new Object[] { mobile, goodsId });
		return key;
	}

	public Set<String> getAllToken()
	{
		return redisUtil.keys(CommonConstant.RedisKey.MIAOSHA_SUCCESS_TOKEN_PREFIX + "*");
	}

	/**
	 * 获取随机名称
	 *
	 * @return
	 */
	public static String getToken()
	{
		return UUID.randomUUID().toString();
	}
}