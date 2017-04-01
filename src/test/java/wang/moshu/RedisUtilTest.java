package wang.moshu;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import wang.moshu.util.RedisUtil;

public class RedisUtilTest extends BaseTest
{
	@Autowired
	private RedisUtil redisUtil;

	@Test
	public void getAndSet()
	{
		redisUtil.set("test_key", "test get and set!!!", 60);
		System.out.println(redisUtil.get("test_key", String.class));
	}
}
