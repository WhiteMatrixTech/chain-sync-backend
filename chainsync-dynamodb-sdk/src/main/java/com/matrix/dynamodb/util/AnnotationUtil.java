package com.matrix.dynamodb.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class AnnotationUtil {

  public static String findGetterName(final Class<?> clazz, final String name)
      throws NoSuchMethodException, IntrospectionException, NoSuchFieldException {
    Method getter = findGetter(clazz, name);
    if (getter == null) {
      throw new NoSuchMethodException(clazz + " has no " + name + " getter");
    }
    return getter.getName();
  }

  public static Method findGetter(final Class<?> clazz, final String name)
      throws NoSuchFieldException, IntrospectionException {
    BeanInfo info = Introspector.getBeanInfo(clazz);
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      if (name.equals(pd.getName())) {
        return pd.getReadMethod();
      }
    }
    throw new NoSuchFieldException(clazz + " has no field " + name);
  }

  public static String findSetterName(final Class<?> clazz, final String name)
      throws NoSuchMethodException, IntrospectionException, NoSuchFieldException {
    Method setter = findSetter(clazz, name);
    if (setter == null) {
      throw new NoSuchMethodException(clazz + " has no " + name + " setter");
    }
    return setter.getName();
  }

  public static Method findSetter(final Class<?> clazz, final String name)
      throws IntrospectionException, NoSuchFieldException {
    BeanInfo info = Introspector.getBeanInfo(clazz);
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      if (name.equals(pd.getName())) {
        return pd.getWriteMethod();
      }
    }
    throw new NoSuchFieldException(clazz + " has no field " + name);
  }
}
