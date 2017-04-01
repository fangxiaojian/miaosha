package wang.moshu.dao;

import org.apache.ibatis.annotations.Param;

import wang.moshu.model.Goods;

public interface GoodsMapper
{
	int deleteByPrimaryKey(Integer id);

	int insert(Goods record);

	int insertSelective(Goods record);

	Goods selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(Goods record);

	int updateByPrimaryKey(Goods record);

	/**
	 * 减库存
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:03:58
	 * @param record
	 * @return
	 */
	int reduceStore(@Param("goodsId") Integer goodsId);

	/**
	 * 根据主键ID查库存
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2017年3月15日 下午5:05:46
	 * @param id
	 * @return
	 */
	Integer selectStoreByPrimaryKey(Integer id);
}