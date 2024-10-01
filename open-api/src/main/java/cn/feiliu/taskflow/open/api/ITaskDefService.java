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
package cn.feiliu.taskflow.open.api;

import cn.feiliu.taskflow.common.metadata.tasks.TaskDefinition;

import java.util.List;

/**
 * 任务定义服务
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-07-22
 */
public interface ITaskDefService {
    /**
     * 创建任务定义
     *
     * @param taskDefinition
     */

    boolean createIfAbsent(TaskDefinition taskDefinition);

    /**
     * 更新任务定义
     *
     * @param taskDefinition
     */
    boolean updateTaskDef(TaskDefinition taskDefinition);

    /**
     * 根据任务名称获取任务定义
     *
     * @param name
     * @return
     */
    TaskDefinition getTaskDef(String name);

    /**
     * 获取任务定义列表
     *
     * @return
     */
    List<TaskDefinition> getTaskDefs();

    /**
     * 发布任务定义
     *
     * @param name 任务定义名称
     */
    boolean publishTaskDef(String name);

    /**
     * 删除任务定义
     *
     * @param name
     * @return
     */
    boolean deleteTaskDef(String name);
}
