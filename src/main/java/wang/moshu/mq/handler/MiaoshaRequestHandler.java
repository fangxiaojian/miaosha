package wang.moshu.mq.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import wang.moshu.cache.GoodsBuyCurrentLimiter;
import wang.moshu.cache.GoodsRedisStoreCache;
import wang.moshu.cache.MiaoshaSuccessTokenCache;
import wang.moshu.constant.MessageType;
import wang.moshu.message.AbstarctMessageHandler;
import wang.moshu.mq.message.MiaoshaRequestMessage;
import wang.moshu.service.GoodsService;
import wang.moshu.smvc.framework.exception.BusinessException;
import wang.moshu.util.RedisUtil;

/**
 * DemoMessage消息的处理器
 * 
 * @category @author xiangyong.ding@weimob.com
 * @since 2017年2月3日 下午9:21:41
 */
@Service
public class MiaoshaRequestHandler extends AbstarctMessageHandler<MiaoshaRequestMessage>
{
	private static Log logger = LogFactory.getLog(MiaoshaRequestHandler.class);

	@Autowired
	private GoodsBuyCurrentLimiter goodsBuyCurrentLimiter;

	@Autowired
	private GoodsRedisStoreCache goodsRedisStoreCache;

	@Autowired
	private MiaoshaSuccessTokenCache miaoshaSuccessTokenCache;

	public MiaoshaRequestHandler()
	{
		// 说明该handler监控的消息类型
		super(MessageType.MIAOSHA_MESSAGE, MiaoshaRequestMessage.class);
	}

	/**
	 * 监听到消息后处理方法
	 */
	public void handle(MiaoshaRequestMessage message)
	{
		// 先减redis库存
		if (!goodsRedisStoreCache.decrStore(message.getGoodsId()))
		{

			// 本次请求处理完成，降低流量值
			goodsBuyCurrentLimiter.decrCurrentFlow(message.getGoodsId());
			// 减库存失败
			throw new BusinessException("占redis名额失败，等待重试");
		}

		// 减库存成功：生成下单token，并存入redis供前端获取
		String token = miaoshaSuccessTokenCache.genToken(message.getMobile(), message.getGoodsId());
		logger.error(message.getMobile()+"获得抢购资格，token："+token);
		
		// 本次请求处理完成，降低流量值
		goodsBuyCurrentLimiter.decrCurrentFlow(message.getGoodsId());
	}

	public void handleFailed(MiaoshaRequestMessage obj)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("msg:[").append(obj).append("], 超过失败次数，停止重试。");
		logger.warn(sb.toString());

	}

}
