package wang.moshu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import wang.moshu.BaseTest;
import wang.moshu.util.RedisUtil;

public class GoodsServiceTest extends BaseTest
{
	@Autowired
	private GoodsService goodsService;

	@Autowired
	private RedisUtil redisUtil;

	@Test
	public void doMiaosha0()
	{
		System.out.print(goodsService.miaosha(1));
	}

	@Test
	public void doMiaosha() throws InterruptedException, ExecutionException
	{
		int threadCount = 1000;
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadCount);
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < threadCount; i++)
		{
			Future<Boolean> itemResult = fixedThreadPool.submit(new MiaoshaCaller(1));
			results.add(itemResult);
		}
		fixedThreadPool.shutdown();
		while (!fixedThreadPool.isTerminated())
		{
			Thread.sleep(10);
		}
		System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
		int miaoshaResult = 0;
		// 抢购结果检查
		for (Future<Boolean> itemResult : results)
		{
			if (itemResult.get())
			{
				miaoshaResult++;
			}
		}
		System.out.println("秒杀结果：" + miaoshaResult);
	}

	@Test
	public void redis()
	{
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++)
		{
			redisUtil.get("ARTICLE_STORE_BY_ID_1_limiter", Integer.class);
		}

		System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
	}

	class MiaoshaCaller implements Callable<Boolean>
	{
		private Integer miaoShaGoodsId;

		public MiaoshaCaller(Integer miaoShaGoodsId)
		{
			this.miaoShaGoodsId = miaoShaGoodsId;
		}

		@Override
		public Boolean call() throws Exception
		{
			return goodsService.miaosha(miaoShaGoodsId);
		}

	}
}
