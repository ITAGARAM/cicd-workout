package com.agaramtech.qualis.global;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import jakarta.persistence.Table;

@Component
public class ClassUtilityFunction {

	private static final java.util.logging.Logger log = java.util.logging.Logger
			.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
	private static Set<Class<?>> getWrapperTypes() {

		Set<Class<?>> wrapperClassNames = new HashSet<>();
		wrapperClassNames.add(Boolean.class);
		wrapperClassNames.add(Byte.class);
		wrapperClassNames.add(Short.class);
		wrapperClassNames.add(Integer.class);
		wrapperClassNames.add(Float.class);
		wrapperClassNames.add(Double.class);
		wrapperClassNames.add(Long.class);
		wrapperClassNames.add(Character.class);
		return wrapperClassNames;
	}

	public boolean isWrapperClass(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	public String getEntityTableName(Class<?> entityClass) {
		Table table = entityClass.getAnnotation(Table.class);
		log.info("Class Name" + table.name().toLowerCase());
		return table.name().toLowerCase();
	}

	public Object getNewInstanceOfAnClass(String clazz) {
		try {
			Class<?> targetClass = Class.forName(clazz);
			Object targetClassInstance = null;
			//Class<?> targetCastingClass = null;
			try {
				/*
				 * if (isWrapperClass(targetClass)) { Constructor<?> constructor = null; if
				 * (clazz.equals(WrapperClasses.BOOLEAN.getWrapperClassName())) {
				 * targetCastingClass = boolean.class; constructor =
				 * targetClass.getConstructor(targetCastingClass);
				 * constructor.setAccessible(true); targetClassInstance = constructor.
				 * newInstance(false); } else if
				 * (clazz.equals(WrapperClasses.BYTE.getWrapperClassName())) {
				 * targetCastingClass = byte.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance(0); } else if
				 * (clazz.equals(WrapperClasses.SHORT.getWrapperClassName())) {
				 * targetCastingClass = short.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance(0); } else if
				 * (clazz.equals(WrapperClasses.INTEGER.getWrapperClassName())) {
				 * targetCastingClass = int.class; constructor =
				 * targetClass.getConstructor(targetCastingClass);
				 * constructor.setAccessible(true); targetClassInstance =
				 * constructor.newInstance(0); } else if
				 * (clazz.equals(WrapperClasses.FLOAT.getWrapperClassName())) {
				 * targetCastingClass = float.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance(0.0f); } else if
				 * (clazz.equals(WrapperClasses.DOUBLE.getWrapperClassName())) {
				 * targetCastingClass = double.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance(0.0d);
				 * 
				 * } else if (clazz.equals(WrapperClasses.LONG.getWrapperClassName())) {
				 * targetCastingClass = long.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance(0L);
				 * 
				 * } else if (clazz.equals(WrapperClasses.CHARACTER.getWrapperClassName())) {
				 * targetCastingClass = char.class; targetClassInstance =
				 * targetClass.getConstructor(targetCastingClass).newInstance('\u0000');
				 * 
				 * } }
				 * 
				 * else {
				 */
				targetClassInstance = targetClass.getDeclaredConstructor().newInstance();
				/* } */

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return targetClassInstance;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}

