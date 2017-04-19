package wang.moshu.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.cache.GoodsInfoCacheWorker;
import wang.moshu.cache.MiaoshaFinishCache;
import wang.moshu.cache.MiaoshaHandlingListCache;
import wang.moshu.cache.MiaoshaSuccessTokenCache;
import wang.moshu.constant.MessageType;
import wang.moshu.dao.GoodsMapper;
import wang.moshu.message.Message;
import wang.moshu.message.MessageTrunk;
import wang.moshu.model.Goods;
import wang.moshu.mq.message.MiaoshaRequestMessage;
import wang.moshu.smvc.framework.exception.BusinessException;

@Service
public class GoodsService
{
	@Autowired
	private GoodsMapper goodsMapper;

	@Autowired
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	@Autowired
	private MiaoshaSuccessTokenCache miaoshaSuccessTokenCache;

	@Autowired
	private MessageTrunk messageTrunk;

	@Autowired
	private MiaoshaFinishCache miaoshaFinishCache;

	@Autowired
	private MiaoshaHandlingListCache miaoshaHandlingListCache;

	@Autowired
	private GoodsInfoCacheWorker goodsInfoCacheWorker;

	/**
	 * 做秒杀操作
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:11:02
	 * @param goodsId
	 * @return
	 */
	public void miaosha(String mobile, String goodsRandomName)
	{
		// 先看抢购是否已经结束了
		if (miaoshaFinishCache.isFinish(goodsRandomName))
		{
			throw new BusinessException("您已经提交抢购，正在处理中");
		}

		// 先限流
		goodsBuyCurrentLimiter.doLimit(goodsRandomName, "啊呀，没挤进去");

		// 判断是否处理中(是否在处理列表中)
		if (miaoshaHandlingListCache.isInHanleList(mobile, goodsRandomName))
		{
			throw new BusinessException("您已经提交抢购，正在处理中");
		}

		// 请求消息推入处理队列，结束
		Message<MiaoshaRequestMessage> message = new Message<MiaoshaRequestMessage>(MessageType.MIAOSHA_MESSAGE,
				new MiaoshaRequestMessage(mobile, goodsRandomName));
		messageTrunk.put(message);

		// 加入正在处理列表
		miaoshaHandlingListCache.add2HanleList(mobile, goodsRandomName);

	}

	// /**
	// * 做秒杀操作（直接用sql）
	// *
	// * @category @author xiangyong.ding@weimob.com
	// * @since 2017年3月15日 下午5:11:02
	// * @param goodsId
	// * @return
	// */
	// public Boolean miaoshaSql(String mobile, Integer goodsId)
	// {
	// // 先检查库存，没有库存直接结束
	// checkStore(goodsId);
	// // 对于进来的客户做减库存
	// return reduceStoreAndCreateOrder(mobile, goodsId);
	// }

	private Goods checkStore(String goodsRandomName)
	{
		Goods goods = goodsMapper.selectByRandomName(goodsRandomName);
		if (goods == null || goods.getStore().intValue() <= 0)
		{
			miaoshaFinishCache.setFinish(goodsRandomName);
			throw new RuntimeException("很遗憾，抢购已经结束了哟"); // 库存不足，抢购失败
		}
		return goods;
	}

	/**
	 * 真正做减库存操作(这里没有采用存储过程的原因：这里并没有高并发，高并发已经在获取token时分流，所以此处没必要用存储过程)
	 * 
	 * @category 真正做减库存操作
	 * @author xiangyong.ding@weimob.com
	 * @since 2017年3月20日 下午2:17:42
	 * @param goodsId
	 */
	public Boolean reduceStoreAndCreateOrder(String mobile, Integer goodsId)
	{
		Date orderTime = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("goodsId", goodsId);
		map.put("mobile", mobile);
		map.put("orderTime", orderTime);
		map.put("o_result", -2);
		goodsMapper.doOrder(map);

		Integer result = (Integer) map.get("o_result");

		if (result != null && result == 1)
		{
			return true;
		}
		return false;
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
		Goods goodsInfo = goodsInfoCacheWorker.get(goodsId, Goods.class);
		if (!miaoshaSuccessTokenCache.validateToken(mobile, goodsInfo.getRandomName(), token))
		{
			throw new BusinessException("token不对，不能下单哦");
		}

		// 先检查库存，没有库存直接结束
		checkStore(goodsInfo.getRandomName());

		// 对于进来的客户做减库存+生成订单
		reduceStoreAndCreateOrder(mobile, goodsId);
	}

}
