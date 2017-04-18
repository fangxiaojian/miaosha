package wang.moshu.cache;

import org.springframework.stereotype.Component;

import wang.moshu.model.Goods;

/**
 * 秒杀开启缓存
 * 
 * @category 秒杀开启缓存
 * @author xiangyong.ding@weimob.com
 * @since 2017年4月18日 下午11:24:59
 */
@Component
public class MiaoshaStartCache
{
	/**
	 * 
	 */
	private GoodsInfoCacheWorker goodsInfoCacheWorker;

	/**
	 * 是否开始
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年4月18日 下午11:30:46
	 * @param goodsId
	 * @return
	 */
	public boolean isStart(Integer goodsId)
	{
		Goods goods = goodsInfoCacheWorker.get(goodsId, Goods.class);
		long now = System.currentTimeMillis();
		return goods.getStartTime().getTime() < now && now < goods.getEndTime().getTime();
	}
}
