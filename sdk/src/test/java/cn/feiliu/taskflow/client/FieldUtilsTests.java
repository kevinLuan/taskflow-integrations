package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.utils.FieldUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author kevin.luan
 * @since 2025-06-04
 */
public class FieldUtilsTests {
    @Test
    public void testFieldUtils() {
        String[] fields = FieldUtils.getJavaFieldNames(Person.class);
        Assert.assertEquals(3, fields.length);
    }

    public static class Person {
        private String name; // 私有字段
        private int age;
        private boolean active;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }
    }
}
