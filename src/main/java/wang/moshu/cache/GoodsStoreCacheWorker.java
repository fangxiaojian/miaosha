package wang.moshu.cache;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import wang.moshu.cache.base.JsonObjCacheWorker;
import wang.moshu.constant.CommonConstant;
import wang.moshu.dao.GoodsMapper;

/**
 * 说说简单信息（不带评论信息）缓存工作器
 * 
 * @author dingxiangyong 2016年8月26日 上午11:17:38
 */
@Component
public class GoodsStoreCacheWorker extends JsonObjCacheWorker<Integer, Integer>
{
	@Autowired
	private GoodsMapper goodsMapper;

	@Override
	protected Integer getDataWhenNoCache(Integer goodsId)
	{
		return goodsMapper.selectStoreByPrimaryKey(goodsId);
	}

	@Override
	protected String getKey(Integer goodsId)
	{
		String key = MessageFormat.format(CommonConstant.RedisKey.GOODS_STORE_BY_ID, new Object[] { goodsId });
		return key;
	}

	@Override
	protected int getExpireSeconds()
	{
		return CommonConstant.RedisKeyExpireSeconds.GOODS_STORE_BY_ID;
	}

}
