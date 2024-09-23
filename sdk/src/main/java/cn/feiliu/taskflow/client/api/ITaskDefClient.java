/*
 * Copyright 2024 taskflow, Inc.
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
package cn.feiliu.taskflow.client.api;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;
import cn.feiliu.taskflow.open.api.ITaskDefService;

import java.util.List;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-07-23
 */
public interface ITaskDefClient extends ITaskDefService,AutoCloseable {
    /**
     * 批量注册或更新任务定义
     *
     * @param list
     */
    default void registerTaskDefs(List<TaskDefinition> list) {
        for (TaskDefinition taskDefinition : list) {
            if (!createIfAbsent(taskDefinition)) {
                updateTaskDef(taskDefinition);
            }
        }
    }
}
