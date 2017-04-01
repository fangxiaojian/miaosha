package wang.moshu.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import wang.moshu.constant.CommonConstant;

/**
 * cookie 工具类
 * 
 * @category cookie 工具类
 * @author xiangyong.ding@weimob.com
 * @since 2016年11月22日 下午7:08:57
 */
public class CookieUtil
{
	/**
	 * 设定登陆cookie
	 * 
	 * @category 设定登陆cookie
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月22日 下午7:09:21
	 * @param response
	 * @param user
	 */
	public static void setLoginCookie(HttpServletResponse response, Integer userId, String userName)
	{
		// 设置cookie，cookie文本：{userId}:{userName}
		String toEncrptStr = userId + ":" + userName; // 待加密文本
		Cookie cookie = new Cookie(CommonConstant.COOKIE_NAME, EncryptUtil.encrypt(toEncrptStr));
		cookie.setMaxAge(CommonConstant.COOKIE_EXPIRE_SECONDS);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/**
	 * 移除登陆cookie
	 * 
	 * @category 移除登陆cookie
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月24日 下午4:20:42
	 * @param response
	 */
	public static void remLoginCookie(HttpServletResponse response)
	{
		Cookie cookie = new Cookie(CommonConstant.COOKIE_NAME, StringUtils.EMPTY);
		cookie.setMaxAge(0); // 设定过期时间0，移除cookie
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/**
	 * 获取登陆cookie的值
	 * 
	 * @category 获取登陆cookie的值
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月22日 下午7:21:37
	 * @param request
	 * @return
	 */
	public static String getLoginCookie(HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();

		if (cookies == null || cookies.length == 0)
		{
			return StringUtils.EMPTY;
		}

		// 迭代查询登录cookie
		for (Cookie cookie : cookies)
		{
			if (CommonConstant.COOKIE_NAME.equals(cookie.getName()))
			{
				return cookie.getValue();
			}
		}

		return StringUtils.EMPTY;
	}
}
