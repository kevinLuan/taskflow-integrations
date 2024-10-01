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

import cn.feiliu.taskflow.sdk.config.PropertyFactory;
import cn.feiliu.taskflow.sdk.config.SpringPropertyReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Any;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-01
 */
@ConditionalOnClass(JacksonAutoConfiguration.class)
public class TaskflowAutoConfiguration implements InitializingBean {
    @Autowired
    private Environment environment;

    /**
     * JsonProtoModule can be registered into an {@link ObjectMapper} to enable the serialization and
     * deserialization of ProtoBuf objects from/to JSON.
     *
     * <p>Right now this module only provides (de)serialization for the {@link Any} ProtoBuf type, as
     * this is the only ProtoBuf object which we're currently exposing through the REST API.
     *
     * <p>Annotated as {@link Component} so Spring can register it with {@link ObjectMapper}
     *
     * @see JsonProtoModule.AnySerializer
     * @see JsonProtoModule.AnyDeserializer
     * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
     */
    @Bean(JsonProtoModule.NAME)
    public JsonProtoModule jsonProtoModule() {
        return new JsonProtoModule();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PropertyFactory.enabledSpringEnv();
        SpringPropertyReader.init(environment);
    }
}
