package org.cloud.sonic.common.tools;

import org.cloud.sonic.common.exception.BeanToolException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean工具类 - 提供对象属性复制、类型转换等Bean操作工具方法
 *
 * @author JayWenStar
 */
public class BeanTool {

    /**
     * 私有构造函数，防止实例化工具类
     */
    private BeanTool() {
    }

    /**
     * Transforms from the source object. (copy same properties only)
     *
     * @param source      source data
     * @param targetClass target class must not be null
     * @param <T>         target class type
     * @return instance with specified type copying from source data; or null if source data is null
     * @throws BeanToolException if newing target instance failed or copying failed
     */
    @Nullable
    public static <T> T transformFrom(@Nullable Object source, @NonNull Class<T> targetClass) {
        Assert.notNull(targetClass, "Target class must not be null");

        if (source == null) {
            return null;
        }

        // Init the instance
        try {
            // New instance for the target class
            T targetInstance = targetClass.getDeclaredConstructor().newInstance();
            // Copy properties
            org.springframework.beans.BeanUtils.copyProperties(source, targetInstance, getNullPropertyNames(source));
            // Return the target instance
            return targetInstance;
        } catch (Exception e) {
            throw new BeanToolException("Failed to new " + targetClass.getName() + " instance or copy properties", e);
        }
    }

    /**
     * Transforms from source data collection in batch.
     *
     * @param sources     source data collection
     * @param targetClass target class must not be null
     * @param <T>         target class type
     * @return target collection transforming from source data collection.
     * @throws BeanToolException if newing target instance failed or copying failed
     */
    @NonNull
    public static <T> List<T> transformFromInBatch(Collection<?> sources, @NonNull Class<T> targetClass) {
        if (CollectionUtils.isEmpty(sources)) {
            return Collections.emptyList();
        }

        // Transform in batch
        return sources.stream()
                .map(source -> transformFrom(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * Update properties (non null).
     *
     * @param source source data must not be null
     * @param target target data must not be null
     * @throws BeanToolException if copying failed
     */
    public static void updateProperties(@NonNull Object source, @NonNull Object target) {
        Assert.notNull(source, "source object must not be null");
        Assert.notNull(target, "target object must not be null");

        // Set non null properties from source properties to target properties
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
        } catch (BeansException e) {
            throw new BeanToolException("Failed to copy properties", e);
        }
    }

    /**
     * Gets null names array of property.
     *
     * @param source object data must not be null
     * @return null name array of property
     */
    @NonNull
    private static String[] getNullPropertyNames(@NonNull Object source) {
        return getNullPropertyNameSet(source).toArray(new String[0]);
    }

    /**
     * Gets null names set of property.
     *
     * @param source object data must not be null
     * @return null name set of property
     */
    @NonNull
    private static Set<String> getNullPropertyNameSet(@NonNull Object source) {

        Assert.notNull(source, "source object must not be null");
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);

            // if property value is equal to null, add it to empty name set
            if (propertyValue == null) {
                emptyNames.add(propertyName);
            }
        }

        return emptyNames;
    }

    /**
     * 将源对象转换为指定类型的Bean对象
     *
     * @param source 源对象，不能为空
     * @param clazz 目标类型，不能为空
     * @param <T> 目标类型泛型
     * @return 转换后的Bean对象
     * @throws IllegalArgumentException 如果source或clazz为null
     * @throws StackOverflowError 注意：此方法存在递归调用自身的问题，会导致栈溢出
     */
    @NonNull
    public static <T> T toBean(Object source, Class<T> clazz) {
        Assert.notNull(source, "object must not be null");
        Assert.notNull(clazz, "clazz must not be null");

        return BeanTool.toBean(source, clazz);
    }
}
