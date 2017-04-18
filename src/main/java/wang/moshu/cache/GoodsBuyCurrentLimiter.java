package wang.moshu.cache;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wang.moshu.cache.base.CurrentLimiter;
import wang.moshu.constant.CommonConstant;
import wang.moshu.constant.MessageType;
import wang.moshu.dao.GoodsMapper;
import wang.moshu.message.MessageMonitor;

/**
 * 商品购买限流器
 * 
 * @category 商品购买限流器
 * @author xiangyong.ding@weimob.com
 * @since 2017年3月16日 下午1:54:05
 */
@Component
public class GoodsBuyCurrentLimiter extends CurrentLimiter<Integer>
{
	@Autowired
	private GoodsMapper goodsMapper;

	@Autowired
	private MessageMonitor messageMonitor;

	@Override
	protected String getLimiterName(Integer goodsId)
	{
		String key = MessageFormat.format(CommonConstant.RedisKey.GOODS_STORE_BY_ID, new Object[] { goodsId });
		return key;
	}

	@Override
	protected int getLimit(Integer goodsId)
	{
		return Integer.valueOf(goodsMapper.selectStoreByPrimaryKey(goodsId))
				* CommonConstant.CurrentLimitMultiple.GOODS_BUY;
	}

	@Override
	protected int getCurrentLimit()
	{
		return messageMonitor.getMessageLeft(MessageType.MIAOSHA_MESSAGE);
	}

}
