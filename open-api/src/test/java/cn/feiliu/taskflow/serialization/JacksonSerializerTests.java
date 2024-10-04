/*
 * Copyright 2024 Taskflow, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.feiliu.taskflow.serialization;

import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-07
 */
public class JacksonSerializerTests {
    final JacksonSerializer serializer = new JacksonSerializer();
    final String            dateStr    = "2024-01-01 10:15:30.0";
    final Date              date;

    {
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void encode() {
        MyBean myBean = new MyBean();
        myBean.setDate(date);
        System.out.println(myBean.getDate());
        assertEquals(dateStr, new Timestamp(myBean.getDate().getTime()).toString());
        JacksonSerializer jacksonSerializer = new JacksonSerializer();
        System.out.println(jacksonSerializer.writeAsString(myBean));
    }

    @Test
    public void decodeByYmdHms() throws IOException {
        String json = "{\"date\":\"2024-01-01 10:15:30.0\"}";
        MyBean myBean = serializer.objectMapper.readValue(json, MyBean.class);
        assertEquals(dateStr, new Timestamp(myBean.getDate().getTime()).toString());
    }

    @Test
    public void decodeByTimestamp() throws Exception {
        String json = "{\"date\":" + date.getTime() + "}";
        MyBean myBean = serializer.objectMapper.readValue(json, MyBean.class);
        assertEquals(dateStr, new Timestamp(myBean.date.getTime()).toString());
    }

    @Data
    public static class MyBean {
        Date date;
    }
}
