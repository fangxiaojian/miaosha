package wang.moshu.constant;

/**
 * 常量
 * 
 * @category @author xiangyong.ding@weimob.com
 * @since 2017年3月16日 下午12:02:44
 */
public class CommonConstant
{
	public interface RedisKey
	{
		String GOODS_STORE_BY_ID = "ARTICLE_STORE_BY_ID_{0}";
	}

	public interface RedisKeyExpireSeconds
	{
		int GOODS_STORE_BY_ID = 3 * 24 * 60 * 60;
	}

	/**
	 * 限流倍数
	 * 
	 * @category 限流倍数
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年3月16日 下午2:08:54
	 */
	public interface CurrentLimitMultiple
	{
		// 商品购买限流倍数
		int GOODS_BUY = 2;
	}
}
