package wang.moshu.model;

/**
 * 用户实体
 * 
 * @category 用户实体
 * @author xiangyong.ding@weimob.com
 * @since 2016年10月17日 下午10:36:58
 */
public class User
{
	private Integer id;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 密码（密文）
	 */
	private String psw;

	private Boolean delFlag;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getPsw()
	{
		return psw;
	}

	public void setPsw(String psw)
	{
		this.psw = psw;
	}

	public Boolean getDelFlag()
	{
		return delFlag;
	}

	public void setDelFlag(Boolean delFlag)
	{
		this.delFlag = delFlag;
	}

}