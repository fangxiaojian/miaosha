//package wang.moshu.cache;
//
//import java.text.MessageFormat;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import wang.moshu.cache.base.CacheWorker;
//import wang.moshu.constant.CommonConstant;
//import wang.moshu.dao.GoodsMapper;
//import wang.moshu.util.RedisUtil;
//
///**
// * 获取商品库存缓存工作器
// * 
// * @author dingxiangyong 2016年8月26日 上午11:17:38
// */
//@Component
//public class GoodsStoreCacheWorker extends CacheWorker<Integer, Integer>
//{
//	@Autowired
//	private RedisUtil redisUtil;
//	
//	@Autowired
//	private GoodsMapper goodsMapper;
//
//	@Override
//	protected Integer getDataWhenNoCache(Integer goodsId)
//	{
//		return goodsMapper.selectStoreByPrimaryKey(goodsId);
//	}
//
//	@Override
//	protected String getKey(Integer goodsId)
//	{
//		String key = MessageFormat.format(CommonConstant.RedisKey.GOODS_STORE_BY_ID, new Object[] { goodsId });
//		return key;
//	}
//
//	@Override
//	protected int getExpireSeconds()
//	{
//		return CommonConstant.RedisKeyExpireSeconds.GOODS_STORE_BY_ID;
//	}
//
//}
