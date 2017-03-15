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

public class GoodsServiceTest extends BaseTest
{
	@Autowired
	private GoodsService goodsService;

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
		for (int i = 0; i < threadCount; i++)
		{
			Future<Boolean> itemResult = fixedThreadPool.submit(new MiaoshaCaller(1));
			results.add(itemResult);
		}
		fixedThreadPool.shutdown();
		while (!fixedThreadPool.isTerminated())
		{
			Thread.sleep(200);
		}

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
