package com.china.superbox.bluetoothappv1;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileLock;

/**
 * Created by Administrator on 2019/4/25.
 */

public class SPUtil {

    //保存在手机里面的文件名
    public static final String FILE_NAME = "share_data";

    //保存数据的方法
    public static void put(Context context, String key, Object object) {
        put(context, FILE_NAME, key, object);
    }

    private static void put(Context context, String fileName, String key, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //判断需要保存的数据类型
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        //记得apply
        SharedPreferencesCompat.apply(editor);
    }

    public static Object get(Context context, String spName, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(spName,
                Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    public static String getString(Context context,  String key, String stringDataDefault) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getString(key, stringDataDefault);
    }

    /**
     * 移除某个key值已经对应的值
     */
    public static void remove(Context context, String key) {
        remove(context, FILE_NAME, key);
    }

    public static void remove(Context context, String spFileName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spFileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 删除sp中的所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        clear(context, FILE_NAME);
    }

    public static void clear(Context context, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**  查询是否包含某个key
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        return contains(context, key, FILE_NAME);
    }

    private static boolean contains(Context context, String key, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    /**
     * 创建一个用来解决SharedPreferencesCompat.apply的兼容类
     */
    private static class SharedPreferencesCompat {
        private static final Method spMethod = findApplyMethod();

        /**
         * 反射查找apply方法
         *
         * @return
         */
        private static Method findApplyMethod() {
            Class<SharedPreferences.Editor> editorClass = SharedPreferences.Editor.class;
            try {
                return editorClass.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static void apply(SharedPreferences.Editor editor) {
            if (spMethod != null) {
                try {
                    spMethod.invoke(editor);
                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            editor.apply();
        }
    }
}