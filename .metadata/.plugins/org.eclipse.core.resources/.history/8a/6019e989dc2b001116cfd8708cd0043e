package com.kokakiwi.mclauncher.core.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaUtils
{
    public static List<Field> getFieldsWithType(Class<?> parentClass,
            Class<?> fieldType)
    {
        final Field[] fields = parentClass.getDeclaredFields();
        final List<Field> fieldList = new ArrayList<Field>();
        for (final Field field : fields)
        {
            if (field.getType() == fieldType)
            {
                fieldList.add(field);
            }
        }
        
        return fieldList;
    }
    
    public static List<Method> getMethodsWithType(Class<?> parentClass,
            Class<?> methodType)
    {
        final Method[] methods = parentClass.getDeclaredMethods();
        final List<Method> methodList = new ArrayList<Method>();
        
        for (final Method method : methods)
        {
            if (method.getReturnType() == methodType)
            {
                methodList.add(method);
            }
        }
        
        return methodList;
    }
    
    public static List<Method> getMethodsWithReturnTypeAndValuesType(
            Class<?> parentClass, Class<?> returnType, Class<?>... classes)
    {
        final Method[] methods = parentClass.getDeclaredMethods();
        final List<Method> methodList = new ArrayList<Method>();
        
        for (final Method method : methods)
        {
            if (method.getReturnType() == returnType)
            {
                final String methodName = method.getName();
                try
                {
                    parentClass.getDeclaredMethod(methodName, classes);
                    methodList.add(method);
                }
                catch (final SecurityException e)
                {
                    e.printStackTrace();
                }
                catch (final NoSuchMethodException e)
                {
                    
                }
            }
        }
        
        return methodList;
    }
}
