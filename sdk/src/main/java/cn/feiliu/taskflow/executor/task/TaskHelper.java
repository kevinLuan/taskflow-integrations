package cn.feiliu.taskflow.executor.task;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kevin.luan
 * @since 2025-06-04
 */
class TaskHelper {
    static Set<Class<?>> primary = new HashSet<Class<?>>();
    static Set<Class<?>> primaryArray = new HashSet<Class<?>>();

    static {
        primary.add(short.class);
        primary.add(int.class);
        primary.add(long.class);
        primary.add(float.class);
        primary.add(double.class);
        primary.add(boolean.class);
        primary.add(boolean.class);
        primary.add(char.class);

        primaryArray.add(short[].class);
        primaryArray.add(int[].class);
        primaryArray.add(long[].class);
        primaryArray.add(float[].class);
        primaryArray.add(double[].class);
        primaryArray.add(boolean[].class);
        primaryArray.add(boolean[].class);
        primaryArray.add(char[].class);

    }

    public static boolean isJavaType(Class<?> type) {
        if (primary.contains(type)) {
            return true;
        } else if (primaryArray.contains(type)) {
            return true;
        }
        String packName = type.getPackage().getName();
        if (packName.startsWith("java.") || packName.startsWith("javax.")) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前POJO类型所有的字段名称
     *
     * @param type
     * @return
     */
    public static String[] getJavaFieldNames(Class<?> type) {
        for (Field field : type.getFields()) {
            field.getName(); //
            //并检查是否包含对应的Getter 方法，并且是公共的方法,
        }
        return null;
    }
}