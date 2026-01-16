package jenkins.plugins.office365connector.helpers;

import hudson.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class ReflectionHelper {

	public static <T> T invokeMethod(Object target, String methodName, Object... args) {
		Method method;

		if (args == null || args.length == 0) {
			method = ReflectionUtils.findMethod(target.getClass(), methodName);
		} else {
			Class<?>[] paramTypes = Stream.of(args).map(arg -> {
				// mocked classes...
				if (arg.getClass().getSimpleName().contains("$Mockito")) {
					try {
						return Class.forName(StringUtils.substringBefore(arg.getClass().getName(), "$Mockito"));
					} catch (Exception ex) {
						throw new IllegalStateException(ex);
					}
				} else {
					return arg.getClass();
				}
			}).toArray(Class<?>[]::new);
			method = ReflectionUtils.findMethod(target.getClass(), methodName, paramTypes);
		}

		if (method == null) {
			throw new IllegalStateException("Could not find method " + methodName);
		}
		ReflectionUtils.makeAccessible(method);
		return (T) ReflectionUtils.invokeMethod(method, target, args);
	}

	public static <T> T getField(Object target, String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		if (field == null) {
			throw new IllegalStateException("Could not find field " + fieldName);
		}
		ReflectionUtils.makeAccessible(field);
		return (T) ReflectionUtils.getField(field, target);
	}

	public static void setField(Object target, String fieldName, Object value) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		if (field == null) {
			throw new IllegalStateException("Could not find field " + fieldName);
		}
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}
}
