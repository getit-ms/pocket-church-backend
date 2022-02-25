/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.app.util;

import br.gafs.calvinista.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gabriel
 */
public final class MergeUtil<T> {

    private T first;
    private Class<?>[] views;

    private MergeUtil(T first, Class<?>... views) {
        this.first = first;
        this.views = views;
    }

    public T into(T second) {
        merge(first, second);
        return second;
    }

    public T from(T second) {
        merge(second, first);
        return first;
    }

    public static <T> MergeUtil<T> merge(T first, Class<?>... views) {
        return new MergeUtil<T>(first, views);
    }

    private <T> void merge(T from, T into) {
        List<Field> fields = getAllFields(from.getClass());
        for (Field field : fields) {
            try {
                Object old = get(into, field);
                Object val = get(from, field);
                if (old instanceof Collection &&
                        val instanceof Collection) {
                    ((Collection) old).clear();
                    ((Collection) old).addAll((Collection) val);
                } else {
                    set(into, field, val);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Object get(Object from, Field field) throws IllegalArgumentException, IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(from);
        } finally {
            field.setAccessible(accessible);
        }
    }

    private static void set(Object from, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
        if (!set(from, field.getName(), field.getType(), value)) {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                field.set(from, value);
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    private static boolean set(Object to, String name, Class<?> type, Object value) {
        try {
            Method method = to.getClass().getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), type);
            if (method.isAccessible()) {
                method.invoke(to, value);
                return true;
            }

        } catch (Exception ex) {
            Logger.getLogger(MergeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private void getAllFields(Class<?> clazz, List<Field> fields) {
        for (Field field : clazz.getDeclaredFields()) {
            if ((field.getModifiers() & Modifier.STATIC) == 0 &&
                    isMergeable(field)) {
                fields.add(field);
            }
        }
        if (clazz.getSuperclass() != null) {
            getAllFields(clazz.getSuperclass(), fields);
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        getAllFields(clazz, fields);
        return fields;
    }

    private boolean isMergeable(Field field) {
        return views.length == 0 ||
                (field.isAnnotationPresent(View.MergeViews.class) &&
                        containsAnyView(field.getAnnotation(View.MergeViews.class).value()));
    }

    private boolean containsAnyView(Class[] views) {
        for (Class<?> v0 : this.views) {
            for (Class<?> v1 : views) {
                if (v0.equals(v1)) {
                    return true;
                }
            }
        }
        return false;
    }

}
