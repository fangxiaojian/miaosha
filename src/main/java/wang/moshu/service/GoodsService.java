package wang.moshu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.dao.GoodsMapper;
import wang.moshu.model.Goods;

@Service
public class GoodsService
{
	@Autowired
	private GoodsMapper goodsMapper;

	@Autowired
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	private static final int TOTAL_OPERATIONS = 100000;

	/**
	 * 做秒杀操作
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public Boolean miaosha(Integer goodsId)
	{
		// 先限流
		goodsBuyCurrentLimiter.doLimit(goodsId, "很遗憾，抢购已经结束了哟");

		// 先检查库存，没有库存直接结束
		checkStore(goodsId);
		// 对于进来的客户做减库存
		return reduceStore(goodsId);
	}

	/**
	 * 做秒杀操作（直接用sql）
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public Boolean miaoshaSql(Integer goodsId)
	{
		// 先检查库存，没有库存直接结束
		checkStore(goodsId);
		// 对于进来的客户做减库存
		return reduceStore(goodsId);
	}

	/**
	 * 真正做减库存操作
	 * 
	 * @category 真正做减库存操作
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年3月20日 下午2:17:42
	 * @param goodsId
	 */
	private Boolean reduceStore(Integer goodsId)
	{
		// 做减库存
		return goodsMapper.reduceStore(goodsId) > 0;
		// if (ret > 0)
		// {
		// return true;
		// }
		// // 更新失败，则重新尝试（乐观锁版本号比对，类似java的CAS操作）
		// return reduceStore(goodsId);
	}

	private Goods checkStore(Integer goodsId)
	{
		Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
		if (goods == null || goods.getStore().intValue() <= 0)
		{
			throw new RuntimeException("很遗憾，抢购已经结束了哟"); // 库存不足，抢购失败
		}
		return goods;
	}

	public void mysqlUpdateBenchMark() throws InterruptedException
	{
		long startTime = System.currentTimeMillis();

		List<Thread> tds = new ArrayList<Thread>();
		final AtomicInteger ind = new AtomicInteger();
		for (int i = 0; i < 50; i++)
		{
			Thread hj = new Thread(new Runnable()
			{
				public void run()
				{
					for (int i = 0; (i = ind.getAndIncrement()) < TOTAL_OPERATIONS;)
					{
						goodsMapper.reduceStore(1);
					}
				}
			});
			tds.add(hj);
			hj.start();
		}

		for (Thread t : tds)
			t.join();

		long elapsed = System.currentTimeMillis() - startTime;
		System.out.println(((1000 * TOTAL_OPERATIONS) / elapsed) + " ops");
	}

}
