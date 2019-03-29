package com.executor.lock.deal.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.executor.lock.annotation.LockKeyParam;

/**
 * 专门处理 参数的注解,得到后缀
 * @author limaojie
 *
 */
public class KeyParamHandler {
	
	private static final Logger log = LoggerFactory.getLogger(KeyParamHandler.class);
	/**
	 * 128
	 */
	private static final Map<String, Annotation[][]> annotationsCache = new ConcurrentHashMap<>(1 << 7);
	
	private static final Map<String, Class[]> classCache = new ConcurrentHashMap<>(1 << 7);
	
	private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>(1 << 7);
	/**
	 * 得到后缀
	 * @param jp
	 * @return
	 * @throws Exception 
	 */
	public static String getSuffix(ProceedingJoinPoint jp) throws Exception{
		MethodSignature signature = (MethodSignature) jp.getSignature();
		Object[] args = jp.getArgs();
		Method method = signature.getMethod();
		StringBuilder result=new StringBuilder();
		Annotation[][] parameterAnnos = getParameterAnnos(method);
		for(int i=0;i<parameterAnnos.length;i++) {
			in:
			for(Annotation annotation : parameterAnnos[i] ) {
				//为了避免多层嵌套
				if(LockKeyParam.class != annotation.annotationType()) continue in;
				//真正的处理
				//得到参数类型
				Class[] parameterClass = getParameterClass(method);
				//强转位LockKeyParam
				LockKeyParam lockKeyParam=(LockKeyParam)annotation;
				result.append(getValue(lockKeyParam,parameterClass[i],args[i]));
			}
		}
		String str = result.toString();
		return "".equals(str) ? null : str;
	}
	
	/**
	 * 获取值,不处理数组,数组特殊处理
	 * @param lockKeyParam
	 * @param parameterClass
	 * @param arg
	 * @return
	 * @throws Exception 
	 */
	private static String getValue(LockKeyParam lockKeyParam, Class parameterClass, Object arg) throws Exception {
		if(null == arg)
			throw new NullPointerException("your value is null");
		//处理int string date等,直接简单的if else,不采用策略了
		if(arg instanceof String)
			return (String)arg;
		if(arg instanceof Double)
			return arg.toString();
		if(arg instanceof Float)
			return arg.toString();
		if(arg instanceof Long)
			return arg.toString();
		if(arg instanceof Integer)
			return arg.toString();
		if(arg instanceof Character)
			return arg.toString();
		if(arg instanceof Short)
			return arg.toString();
		if(arg instanceof Date)
			return arg.toString();
		//--------特殊处理完毕-----------
		StringBuilder result=new StringBuilder();
		try {
			String key;
			for(String fieldName:lockKeyParam.value()) {
				key=new StringBuilder(parameterClass.getName()).append(".").append(fieldName).toString();
				Field field = fieldCache.get(key);
				//放进缓存当中
				if(null == field) {
					field=parameterClass.getDeclaredField(fieldName);
					//不还原了
					field.setAccessible(true);
					fieldCache.put(key, field);
				}
				//获取值了
				Object value = field.get(arg);
				//不判断空,这里为空的话,那么你就会 + null
				result.append(value);
			}
		}catch (Exception e) {
			StringBuilder errorfields=new StringBuilder();
			for(String s:lockKeyParam.value()) {
				errorfields.append(s).append(",");
			}
			log.error("the " + parameterClass + " can`t find the field,maybe in ("+errorfields.substring(0, errorfields.length()-1)+")");
			throw e;
		}
		return result.toString();
	}

	/**
	 * 得到注解
	 */
	private static Annotation[][] getParameterAnnos(Method method){
		String methodName = method.getName();
		//缓存中获取
		Annotation[][] methodAnnotations = annotationsCache.get(methodName);
		if(null != methodAnnotations) return methodAnnotations;
		methodAnnotations = method.getParameterAnnotations();
		annotationsCache.put(methodName,methodAnnotations);
		return methodAnnotations;
	}
	/**
	 * 获取方法的参数
	 * @param method
	 * @return
	 */
	private static Class[] getParameterClass(Method method) {
		String methodName=method.getName();
		Class[] parameterTypes = classCache.get(methodName);
		if( null != parameterTypes) return parameterTypes;
		parameterTypes = method.getParameterTypes();
		classCache.put(methodName, parameterTypes);
		return parameterTypes;
	}
	
}
