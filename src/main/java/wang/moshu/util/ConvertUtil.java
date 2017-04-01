package wang.moshu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ConvertUtil
{

	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	private static <T> Schema<T> getSchema(Class<T> cls)
	{
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null)
		{
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null)
			{
				cachedSchema.put(cls, schema);
			}
		}
		return schema;
	}

	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj)
	{
		Class<T> cls = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try
		{
			Schema<T> schema = getSchema(cls);
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e.getMessage(), e);
		}
		finally
		{
			buffer.clear();
		}
	}

	public static <T> T unserialize(byte[] data, Class<T> cls)
	{
		if (data == null || data.length == 0)
		{
			return null;
		}
		try
		{
			T message = cls.newInstance();
			Schema<T> schema = getSchema(cls);
			ProtostuffIOUtil.mergeFrom(data, message, schema);
			return message;
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static <T> List<T> unserialize(List<byte[]> data, Class<T> clazz)
	{
		List<T> result = new ArrayList<T>();
		for (byte[] itemBytes : data)
		{
			result.add(unserialize(itemBytes, clazz));
		}
		return result;
	}

}
