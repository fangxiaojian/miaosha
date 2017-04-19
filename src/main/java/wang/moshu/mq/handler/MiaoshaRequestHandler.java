package wang.moshu.mq.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wang.moshu.cache.GoodsRedisStoreCache;
import wang.moshu.cache.MiaoshaFinishCache;
import wang.moshu.cache.MiaoshaSuccessTokenCache;
import wang.moshu.cache.UserBlackListCache;
import wang.moshu.constant.MessageType;
import wang.moshu.message.AbstarctMessageHandler;
import wang.moshu.mq.message.MiaoshaRequestMessage;
import wang.moshu.smvc.framework.exception.BusinessException;

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
	private GoodsRedisStoreCache goodsRedisStoreCache;

	@Autowired
	private MiaoshaSuccessTokenCache miaoshaSuccessTokenCache;

	@Autowired
	private UserBlackListCache userBlackListCache;

	@Autowired
	private MiaoshaFinishCache miaoshaFinishCache;

	public MiaoshaRequestHandler()
	{
		// 说明该handler监控的消息类型；失败重试次数设定为MAX_VALUE
		super(MessageType.MIAOSHA_MESSAGE, MiaoshaRequestMessage.class, Integer.MAX_VALUE);
	}

	/**
	 * 监听到消息后处理方法
	 */
	public void handle(MiaoshaRequestMessage message)
	{
		// 查看请求用户是否在黑名单中
		if (userBlackListCache.isIn(message.getMobile()))
		{
			logger.error(message.getMobile() + "检测为黑名单用户，拒绝抢购");
			return;
		}

		// 先看抢购是否已经结束了
		if (miaoshaFinishCache.isFinish(message.getGoodsRandomName()))
		{
			logger.error("抱歉，您来晚了，抢购已经结束了");
			return;
		}

		// 先减redis库存
		if (!goodsRedisStoreCache.decrStore(message.getGoodsRandomName()))
		{
			// 减库存失败
			throw new BusinessException("占redis名额失败，等待重试");
		}

		// 减库存成功：生成下单token，并存入redis供前端获取
		String token = miaoshaSuccessTokenCache.genToken(message.getMobile(), message.getGoodsRandomName());
		logger.error(message.getMobile() + "获得抢购资格，token：" + token);

	}

	public void handleFailed(MiaoshaRequestMessage obj)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("msg:[").append(obj).append("], 超过失败次数，停止重试。");
		logger.warn(sb.toString());

	}

}
