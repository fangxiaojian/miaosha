package wang.moshu.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.cache.MiaoshaFinishCache;
import wang.moshu.cache.MiaoshaSuccessTokenCache;
import wang.moshu.constant.CommonConstant;
import wang.moshu.constant.MessageType;
import wang.moshu.dao.GoodsMapper;
import wang.moshu.message.Message;
import wang.moshu.message.MessageTrunk;
import wang.moshu.model.Goods;
import wang.moshu.mq.message.MiaoshaRequestMessage;
import wang.moshu.smvc.framework.exception.BusinessException;
import wang.moshu.util.RedisUtil;

@Service
public class GoodsService
{
	@Autowired
	private GoodsMapper goodsMapper;

	@Autowired
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private MiaoshaSuccessTokenCache miaoshaSuccessTokenCache;

	@Autowired
	private MessageTrunk messageTrunk;

	@Autowired
	private MiaoshaFinishCache miaoshaFinishCache;

	private static final int TOTAL_OPERATIONS = 100000;

	/**
	 * 做秒杀操作
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public void miaosha(String mobile, Integer goodsId)
	{
		// 先看抢购是否已经结束了
		if (miaoshaFinishCache.isFinish(goodsId))
		{
			throw new BusinessException("您已经提交抢购，正在处理中");
		}

		// 先限流
		goodsBuyCurrentLimiter.doLimit(goodsId, "啊呀，没挤进去");

		// 判断是否处理中
		if (redisUtil.hget(CommonConstant.RedisKey.MIAOSHA_HANDLE_LIST + goodsId, mobile, String.class) != null)
		{
			throw new BusinessException("您已经提交抢购，正在处理中");
		}

		// 消息推入队列，结束处理
		Message<MiaoshaRequestMessage> message = new Message<MiaoshaRequestMessage>(MessageType.MIAOSHA_MESSAGE,
				new MiaoshaRequestMessage(mobile, goodsId));
		messageTrunk.put(message);

		// 加入正在处理队列
		redisUtil.hset(CommonConstant.RedisKey.MIAOSHA_HANDLE_LIST + goodsId, mobile, mobile);

	}

	/**
	 * 真正的减库存、下单操作
	 * 
	 * @category 真正的减库存、下单操作
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年4月12日 下午11:03:04
	 * @param goodsId
	 * @return
	 */
	public void order(String mobile, Integer goodsId, String token)
	{
		// 先检查token有效性
		if (!miaoshaSuccessTokenCache.validateToken(mobile, goodsId, token))
		{
			throw new BusinessException("token不对，不能下单哦");
		}

		// 先检查库存，没有库存直接结束
		checkStore(goodsId);

		// 对于进来的客户做减库存+生成订单
		reduceStoreAndCreateOrder(mobile, goodsId);
	}

	/**
	 * 做秒杀操作（直接用sql）
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public Boolean miaoshaSql(String mobile, Integer goodsId)
	{
		// 先检查库存，没有库存直接结束
		checkStore(goodsId);
		// 对于进来的客户做减库存
		return reduceStoreAndCreateOrder(mobile, goodsId);
	}

	/**
	 * 真正做减库存操作(这里没有采用存储过程的原因：这里并没有高并发，高并发已经在获取token时分流，所以此处没必要用存储过程)
	 * 
	 * @category 真正做减库存操作
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年3月20日 下午2:17:42
	 * @param goodsId
	 */
	private Boolean reduceStoreAndCreateOrder(String mobile, Integer goodsId)
	{
		// // 做减库存
		// if (goodsMapper.reduceStore(goodsId) > 0)
		// {
		// return false;
		// }
		//
		// Order order = new Order();
		// order.setMobile(mobile);
		// order.setGoodsId(goodsId);
		// order.setNum(1);// 默认为1，可以优化
		// orderMapper.insertSelective(order);

		Date orderTime = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("goodsId", goodsId);
		map.put("mobile", mobile);
		map.put("orderTime", orderTime);
		// map.put("result", null);
		goodsMapper.doOrder(map);

		Integer result = (Integer) map.get("result");

		if (result != null && result == 1)
		{
			return true;
		}
		return false;
	}

	private Goods checkStore(Integer goodsId)
	{
		Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
		if (goods == null || goods.getStore().intValue() <= 0)
		{
			miaoshaFinishCache.setFinish(goodsId);
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
