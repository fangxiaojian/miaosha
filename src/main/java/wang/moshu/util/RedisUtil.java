package wang.moshu.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil implements InitializingBean
{
	/**
	 * 日志
	 */
	private static final Log logger = LogFactory.getLog(RedisUtil.class);

	/**
	 * REDIS连接池
	 */
	private volatile JedisPool pool;

	/**
	 * 最大连接数，如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个JEDIS实例，则此时pool的状态为exhausted(耗尽
	 * )。
	 */
	private Integer maxTotal;

	/**
	 * 控制一个pool最多有多少个状态为idle(空闲的)的JEDIS实例
	 */
	private Integer maxIdle;

	private Integer minIdle;

	/**
	 * 最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
	 */
	private Integer holed;

	/**
	 * redis服务端口
	 */
	private Integer port;

	/**
	 * redis服务地址
	 */
	private String host;

	/**
	 * redis连接超时时间
	 */
	private Integer timeout;

	/**
	 * redis连接密码
	 */
	private String password;

	/**
	 * 
	 */
	private Integer DB;

	public void setHoled(Integer holed)
	{
		this.holed = holed;
	}

	public void setPort(Integer port)
	{
		this.port = port;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setTimeout(Integer timeout)
	{
		this.timeout = timeout;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setDB(Integer dB)
	{
		DB = dB;
	}

	public Integer getMaxTotal()
	{
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal)
	{
		this.maxTotal = maxTotal;
	}

	public Integer getMaxIdle()
	{
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle)
	{
		this.maxIdle = maxIdle;
	}

	public Integer getMinIdle()
	{
		return minIdle;
	}

	public void setMinIdle(Integer minIdle)
	{
		this.minIdle = minIdle;
	}

	/**
	 * 初始化redisUtil实例，配置连接池
	 */
	public void afterPropertiesSet() throws Exception
	{

		JedisPoolConfig config = new JedisPoolConfig();

		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);

		// config.setMaxWaitMillis(holed);
		// config.setTestOnBorrow(true);

		pool = new JedisPool(config, host, port, timeout, password, DB);

	}

	/**
	 * 从连接池获得一个redis连接
	 * 
	 * @return
	 */
	public Jedis getConnent()
	{
		Jedis jedis = pool.getResource();
		// jedis.select(DB);
		return jedis;
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public boolean hset(String key, String fieldName, Object object)
	{
		return hset(key, fieldName, object, -1);
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public boolean hset(String key, String fieldName, Object object, int seconds)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.hset(key, fieldName, object.toString());
			if (seconds > 0)
				jedis.expire(key.getBytes(), seconds);
		}
		catch (Exception e)
		{
			logger.error("redis hset data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public Set<String> keys(String keyPattern)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			return jedis.keys(keyPattern);
		}
		catch (Exception e)
		{
			logger.error("redis keys failed!", e);
			return new HashSet<String>();
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public Set<String> mget(Set<String> keys)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.mget((String[]) keys.toArray());
		}
		catch (Exception e)
		{
			logger.error("redis keys failed!", e);
			return new HashSet<String>();
		}
		finally
		{
			close(jedis);
		}
		return new HashSet<String>();
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public boolean set(String key, String object, int seconds)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.set(key, object);
			if (seconds > 0)
				jedis.expire(key.getBytes(), seconds);
		}
		catch (Exception e)
		{
			logger.error("redis set data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public boolean setForSerialize(String key, Serializable object, int seconds)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.set(key.getBytes(), ConvertUtil.serialize(object));
			if (seconds > 0)
				jedis.expire(key.getBytes(), seconds);
		}
		catch (Exception e)
		{
			logger.error("redis set data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 向redis中存入数据
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @return
	 */
	public boolean set(String key, String object)
	{
		return set(key, object, -1);
	}

	/**
	 * 设定json缓存
	 * 
	 * @param key
	 * @param object
	 * @return
	 */
	public boolean setForJson(String key, Object object, int seconds)
	{
		try
		{
			return set(key, JSON.toJSONString(object), seconds);
		}
		catch (JSONException ex)
		{
			return false;
		}
	}

	/*
	 * public boolean set(String key, String object, int seconds) { Jedis jedis
	 * = null; try { // byte[] bytes = key.getBytes("ISO8859-1");// //
	 * ""里面的参数为需要转化的编码，一般是ISO8859-1 // key = new String(bytes, "utf-8");//
	 * 转化为utf-8编码 jedis = getConnent(); jedis.set(key.getBytes(), object); if
	 * (seconds > 0) jedis.expire(key, seconds); } catch (Exception e) {
	 * logger.error("redis set data failed!", e); return false; } finally {
	 * close(jedis); } return true; }
	 */

	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public Object hget(String key, String fieldName)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.hget(key, fieldName);
		}
		catch (Exception e)
		{
			logger.error("redis hget data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public Object get(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			byte[] value = jedis.get(key.getBytes());

			if (value != null)
			{
				return ConvertUtil.unserialize(value);
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public String getForString(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			String value = jedis.get(key);

			return value;
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取集合数据(针对序列化JSON)
	 *
	 * @param key
	 * @return
	 */
	public List<?> getListForJson(String key, Class<?> clazz)
	{
		Jedis jedis = null;

		try
		{
			// byte[] bytes = key.getBytes("GBK");//
			// ""里面的参数为需要转化的编码，一般是ISO8859-1
			// key = new String(bytes, "utf-8");// 转化为utf-8编码
			jedis = getConnent();
			String value = jedis.get(key);
			if (value != null)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("get value from redis: " + value);
				}
				return JSON.parseArray(value, clazz);
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取数据(针对序列化JSON)
	 *
	 * @param key
	 * @return
	 */
	public Object getForJson(String key, Class<?> clazz)
	{
		Jedis jedis = null;

		try
		{
			// byte[] bytes = key.getBytes("GBK");//
			// ""里面的参数为需要转化的编码，一般是ISO8859-1
			// key = new String(bytes, "utf-8");// 转化为utf-8编码
			jedis = getConnent();
			String value = jedis.get(key);
			if (value != null)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("get value from redis: " + value);
				}
				return JSON.parseObject(value, clazz);
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取数据后自动过期
	 *
	 * @param key
	 * @return
	 */
	public Object getAfterExpire(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			byte[] value = jedis.get(key.getBytes());

			if (value != null)
			{
				// 设置过期，立刻过期
				jedis.expire(key.getBytes(), 0);
				return ConvertUtil.unserialize(value);
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 获取key的剩余过期时间，单位：秒
	 *
	 * @param key
	 * @return
	 */
	public Long getExpireSeconds(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.ttl(key.getBytes());

		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 删除redis中key对应数据
	 * 
	 * @param key 键值
	 * @return 成功\失败
	 */
	public boolean delete(String key)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.del(key);
		}
		catch (Exception e)
		{
			logger.error("redis delete data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 删除redis中key对应数据
	 * 
	 * @param key
	 * @return 删除的条数
	 */
	public long deleteRegEx(String key)
	{
		Jedis jedis = null;
		long count = 0;

		try
		{
			jedis = getConnent();
			Set<String> keys = jedis.keys(key);

			if (keys == null || keys.isEmpty())
			{
				return 0;
			}

			for (String sigleKey : keys)
			{
				jedis.del(sigleKey);
				count++;
			}
			return count;

		}
		catch (Exception e)
		{
			return -1;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 存储REDIS队列 顺序存储,可设置过期时间，过期时间以秒为单位
	 * 
	 * @param key reids键名
	 * @param value 键值
	 * @param second 过期时间(秒)
	 */
	public Long lpush(String key, Object value, int second)
	{
		Jedis jedis = null;
		Long ret = null;
		try
		{
			jedis = getConnent();
			byte[] bytes = ConvertUtil.serialize(value);
			ret = jedis.lpush(key.getBytes(), bytes);

			if (second > 0)
			{
				jedis.expire(key, second);
			}

		}
		catch (Exception e)
		{
			logger.error("redis lpush data failed , key = " + key, e);
		}
		finally
		{
			close(jedis);
		}

		return ret;
	}

	public void lpushStr(String key, String value, int second)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.lpush(key, value);

			if (second > 0)
			{
				jedis.expire(key, second);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 保存数据 类型为 Map
	 * 
	 * @param flag
	 * @param mapData
	 */
	public String setMapData(String key, Map<String, String> mapData)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.hmset(key, mapData);
		}
		catch (Exception e)
		{
			logger.error("redis set map data failed! map = " + mapData, e);
		}
		finally
		{
			close(jedis);
		}

		return "false";
	}

	/**
	 * 获取Map数据
	 * 
	 * @param flag
	 * @return
	 */
	public Map<String, String> getMapData(String key)
	{
		Map<String, String> dataMap = null;
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			dataMap = jedis.hgetAll(key);
		}
		catch (Exception e)
		{
			logger.error("redis get map data failed! ", e);
		}
		finally
		{
			close(jedis);
		}
		return dataMap;
	}

	/**
	 * 更新redis数据，当redis中不存在改键值时，返回false
	 * 
	 * @param key 键值
	 * @param obj 需要更新的值
	 * @return 更新结果
	 */
	public boolean update(String key, String obj)
	{
		if (null == get(key))
		{
			return false;
		}
		else
		{
			return set(key, obj);
		}
	}

	/**
	 * 关闭当前连接实例，将连接返回连接池
	 * 
	 * @param jedis redis连接实例
	 */
	private void close(Jedis jedis)
	{
		try
		{
			if (jedis != null)
			{
				jedis.close();
			}
		}
		catch (Exception e)
		{
			logger.error("close jedis failed!", e);
		}
	}

	/**
	 * 向redis中存入列表
	 * 
	 * @param key 键值
	 * @param object 数据
	 * @param seconds 过期时间
	 * @return
	 */
	public boolean lpush(String key, Object object)
	{
		Long ret = lpush(key, object, 0);
		if (ret > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public Object blpop(String key, int waitSeconds)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			List<byte[]> values = jedis.brpop(waitSeconds, key.getBytes());

			if (values != null && values.size() > 0)
			{
				byte[] value = values.get(1);
				return ConvertUtil.unserialize(value);
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 对有序集合中指定成员的分数加上增量 increment
	 * 
	 * @param key
	 * @param score
	 * @param object
	 */
	public boolean zincrby(String key, double score, Object object)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.zincrby(key, score, object.toString());
		}
		catch (Exception e)
		{
			logger.error("redis zadd data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 向有序集合添加元素
	 * 
	 * @param key
	 * @param score
	 * @param object
	 */
	public boolean zadd(String key, double score, Object object)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.zadd(key, score, object.toString());
		}
		catch (Exception e)
		{
			logger.error("redis zadd data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 向有序集合添加元素
	 * 
	 * @param key
	 * @param score
	 * @param object
	 */
	public boolean zadd(String key, Map<String, Double> scoreMembers, int seconds)
	{
		Jedis jedis = null;
		try
		{
			jedis = getConnent();
			jedis.zadd(key, scoreMembers);
			if (seconds > 0)
			{
				jedis.expire(key, seconds);
			}
		}
		catch (Exception e)
		{
			logger.error("redis zadd data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
		return true;
	}

	/**
	 * 计算在有序集合中指定区间分数的成员数
	 *
	 * @param key
	 * @return
	 */
	public Long zcount(String key, double minScore, double maxScore)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zcount(key, minScore, maxScore);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return 0L;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 移除有序列表成员
	 * 
	 * @category 移除有序列表成员
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月24日 下午3:43:08
	 * @param key
	 * @param members 待移除的成员
	 * @return
	 */
	public Long zrem(String key, String... members)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zrem(key, members);
		}
		catch (Exception e)
		{
			logger.error("redis zrem data failed!", e);
			return 0L;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 通过有序列表中值移除元素
	 * 
	 * @category 通过有序列表中值移除元素(包括)
	 * @author xiangyong.ding@weimob.com
	 * @since 2016年11月24日 下午3:43:08
	 * @param key
	 * @param minValue 包括
	 * @param maxValue 包括
	 * @return
	 */
	public Long zremrangeByLex(String key, String minValue, String maxValue)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zremrangeByLex(key, "[" + minValue, "[" + maxValue);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return 0L;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 有序集所有成员列表。（从小--->大）
	 * 
	 * @param key
	 * @param offset 偏移量
	 * @param count 总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeAll(String key, int offset, int count)
	{
		return zrangeByScore(key, "-inf", "+inf", offset, count);
	}

	/**
	 * 有序集所有成员列表。（从小--->大）
	 * 
	 * @param key
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeAll(String key)
	{
		return zrangeByScore(key, "-inf", "+inf");
	}

	/**
	 * 大于等于最小分数的有序集成员列表。（从小--->大）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScoreGreaterOrEqual(String key, String minScore)
	{
		return zrangeByScore(key, minScore, "+inf");
	}

	/**
	 * 大于等于最小分数的有序集成员列表。（从小--->大）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScoreGreaterOrEqual(String key, String minScore, int offset, int count)
	{
		return zrangeByScore(key, minScore, "+inf", offset, count);
	}

	/**
	 * 小于等于最大分数的有序集成员列表。（从小--->大）
	 * 
	 * @param key
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScoreLessOrEqual(String key, String maxScore)
	{
		return zrangeByScore(key, "-inf", maxScore);
	}

	/**
	 * 小于等于最大分数的有序集成员列表。（从小--->大）
	 * 
	 * @param key
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @param offset 偏移量
	 * @param count 返回总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScoreLessOrEqual(String key, String maxScore, int offset, int count)
	{
		return zrangeByScore(key, "-inf", maxScore, offset, count);
	}

	/**
	 * 指定区间内，有序集成员的列表。（从小--->大）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'表示负无穷
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScore(String key, String minScore, String maxScore)
	{
		return zrangeByScore(key, minScore, maxScore, -1, -1);
	}

	/**
	 * 指定区间内，有序集成员的列表。（从小--->大）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'表示负无穷
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @param offset 偏移量
	 * @param count 返回总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScore(String key, String minScore, String maxScore, int offset, int count)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			if (offset > -1 && count > 0)
			{
				return jedis.zrangeByScore(key, minScore, maxScore, offset, count);
			}
			else
			{
				return jedis.zrangeByScore(key, minScore, maxScore);
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 指定区间内，有序集成员的列表。（从小--->大）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括）
	 * @param maxScore 最大分数（包括）
	 * @return 有序集成员的列表
	 */
	public Set<String> zrangeByScore(String key, double minScore, double maxScore)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zrangeByScore(key, minScore, maxScore);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 有序集所有成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @param offset 偏移量
	 * @param count 总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeAll(String key, int offset, int count)
	{
		return zrevrangeByScore(key, "+inf", "-inf", offset, count);
	}

	/**
	 * 有序集所有成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeAll(String key)
	{
		return zrevrangeByScore(key, "+inf", "-inf");
	}

	/**
	 * 大于等于最小分数的有序集成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScoreGreaterOrEqual(String key, String minScore)
	{
		return zrevrangeByScore(key, "+inf", minScore);
	}

	/**
	 * 大于等于最小分数的有序集成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param minScore 最小分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScoreGreaterOrEqual(String key, String minScore, int offset, int count)
	{
		return zrevrangeByScore(key, "+inf", minScore, offset, count);
	}

	/**
	 * 小于等于最大分数的有序集成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScoreLessOrEqual(String key, String maxScore)
	{
		return zrevrangeByScore(key, maxScore, "-inf");
	}

	/**
	 * 小于等于最大分数的有序集成员列表。（从大--->小）
	 * 
	 * @param key
	 * @param maxScore 最大分数（包括），用'+inf'标识正无穷
	 * @param offset 偏移量
	 * @param count 返回总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScoreLessOrEqual(String key, String maxScore, int offset, int count)
	{
		return zrevrangeByScore(key, maxScore, "-inf", offset, count);
	}

	/**
	 * 指定区间内，有序集成员的列表。（从大--->小）
	 * 
	 * @param key
	 * @param maxScore 最小分数（包括），用'+inf'表示负无穷
	 * @param minScore 最大分数（包括），用'-inf'标识正无穷
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScore(String key, String maxScore, String minScore)
	{
		return zrevrangeByScore(key, maxScore, minScore, -1, -1);
	}

	/**
	 * 指定区间内，有序集成员的列表。（从大--->小）
	 * 
	 * @param key
	 * @param maxScore 最小分数（包括），用'+inf'表示负无穷
	 * @param minScore 最大分数（包括），用'-inf'标识正无穷
	 * @param offset 偏移量
	 * @param count 返回总数
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScore(String key, String maxScore, String minScore, int offset, int count)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			if (offset > -1 && count > 0)
			{
				return jedis.zrevrangeByScore(key, maxScore, minScore, offset, count);
			}
			else
			{
				return jedis.zrangeByScore(key, maxScore, minScore);
			}
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 指定区间内，有序集成员的列表。（从大--->小）
	 * 
	 * @param key
	 * @param maxScore 最大分数（包括）
	 * @param minScore 最小分数（包括）
	 * @return 有序集成员的列表
	 */
	public Set<String> zrevrangeByScore(String key, double maxScore, double minScore)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zrevrangeByScore(key, maxScore, minScore);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return null;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 于移除有序集中，指定分数（score）区间内的所有成员
	 * 
	 * @param key
	 * @param maxScore 最小分数（包括）
	 * @param minScore 最大分数（包括）
	 * @return 被移除成员的数量
	 */
	public Long zremrangeByScore(String key, double maxScore, double minScore)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.zremrangeByScore(key, maxScore, minScore);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return 0L;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key key
	 * @return true:存在，false:不存在
	 */
	public boolean exists(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.exists(key);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 去锁
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2016年11月28日 下午7:58:45
	 * @param key
	 * @return
	 */
	public boolean getLock(String key)
	{
		return setnx(key + "_lock", "");
	}

	/**
	 * 释放锁
	 * 
	 * @category @author xiangyong.ding@weimob.com
	 * @since 2016年11月28日 下午7:58:45
	 * @param key
	 * @return
	 */
	public boolean releaseLock(String key)
	{
		return delete(key + "_lock");
	}

	/**
	 * 指定的 key 不存在时，为 key 设置指定的值。
	 * 
	 * @param key key
	 * @return true:存在，false:不存在
	 */
	public boolean setnx(String key, Object object, int seconds)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			Long res = jedis.setnx(key, object.toString());
			if (new Long(1L).equals(res))
			{
				// 设定过期时间，最多30秒自动过期，防止长期死锁发生
				jedis.expire(key.getBytes(), seconds);
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return false;
		}
		finally
		{
			close(jedis);
		}
	}

	/**
	 * 指定的 key 不存在时，为 key 设置指定的值。
	 * 
	 * @param key key
	 * @return true:存在，false:不存在
	 */
	public boolean setnx(String key, Object object)
	{
		return setnx(key, object, 30);
	}

	/**
	 * 自增
	 * 
	 * @param key key
	 * @return 0:失败，非0:成功
	 */
	public Long incr(String key)
	{
		Jedis jedis = null;

		try
		{
			jedis = getConnent();
			return jedis.incr(key);
		}
		catch (Exception e)
		{
			logger.error("redis get data failed!", e);
			return 0L;
		}
		finally
		{
			close(jedis);
		}
	}

}
