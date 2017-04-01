package wang.moshu.util;

import wang.moshu.model.User;

/**
 * 登录回话工具类
 * 
 * @author dingxiangyong 2016年8月30日 上午11:21:15
 */
public class SessionUtil
{
	private static ThreadLocal<User> user = new ThreadLocal<User>();

	/**
	 * 设定会话
	 * 
	 * @param userInfoP 用户信息
	 */
	public static void set(User userP)
	{
		user.set(userP);
	}

	/**
	 * 获取用户会话信息
	 * 
	 * @return
	 */
	public static User get()
	{
		return user.get();
	}

	/**
	 * 判断是否登陆
	 * 
	 * @category 判断是否登陆
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月21日 下午4:59:17
	 * @return true:登陆，false：未登陆
	 */
	public static boolean isLogin()
	{
		return user.get() != null;
	}
}
