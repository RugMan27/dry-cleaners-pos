package xyz.rugman27.drycleanerspos.utilites;

import java.lang.reflect.Field;

public class MergeUtils {

    /**
     * Merges non-null fields from source into target object.
     * Both objects must be of the same type.
     *
     * @param source The object containing updated (non-null) values.
     * @param target The object to be updated.
     * @param <T>    The type of the objects.
     */
    public static <T> void mergeNonNullFields(T source, T target) {
        if (source == null || target == null) return;

        Class<?> clazz = source.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not merge field: " + field.getName(), e);
            }
        }
    }
}
