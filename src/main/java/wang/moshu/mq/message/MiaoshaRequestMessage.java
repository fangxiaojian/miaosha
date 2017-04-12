package wang.moshu.mq.message;

/**
 * 秒杀请求消息
 * 
 * @category 秒杀请求消息
 * @author xiangyong.ding@weimob.com
 * @since 2017年4月7日 下午5:34:13
 */
public class MiaoshaRequestMessage
{
	/**
	 * 手机号，标识用户唯一身份
	 */
	private String mobile;

	/**
	 * 秒杀商品编号
	 */
	private Integer goodsId;

	public String getMobile()
	{
		return mobile;
	}

	public void setMobile(String mobile)
	{
		this.mobile = mobile;
	}

	public Integer getGoodsId()
	{
		return goodsId;
	}

	public void setGoodsId(Integer goodsId)
	{
		this.goodsId = goodsId;
	}
	
	

	public MiaoshaRequestMessage()
	{
		super();
	}

	public MiaoshaRequestMessage(String mobile, Integer goodsId)
	{
		super();
		this.mobile = mobile;
		this.goodsId = goodsId;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MiaoshaRequestMessage [mobile=");
		builder.append(mobile);
		builder.append(", goodsId=");
		builder.append(goodsId);
		builder.append("]");
		return builder.toString();
	}



}
