package wang.moshu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wang.moshu.dao.GoodsMapper;
import wang.moshu.model.Goods;

@Service
public class GoodsService
{
	@Autowired
	private GoodsMapper goodsMapper;

	/**
	 * 做秒杀操作
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public boolean miaosha(Integer goodsId)
	{
		Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
		if (goods == null || goods.getStore().intValue() <= 0)
		{
			return false; // 库存不足，抢购失败
		}
		// 做减库存

		int ret = goodsMapper.reduceStore(goods);
		if (ret > 0)
		{
			return true;
		}
		// 更新失败，则重新尝试（乐观锁版本号比对，类似java的CAS操作）
		return miaosha(goodsId);
	}

}
