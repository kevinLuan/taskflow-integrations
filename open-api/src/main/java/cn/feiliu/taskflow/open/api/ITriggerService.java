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

import cn.feiliu.taskflow.open.dto.WorkflowScheduleExecution;

/**
 * 触发服务API
 * @author SHOUSHEN.LUAN
 * @since 2024-10-04
 */
public interface ITriggerService {
    /**
     * 触发webhook业务处理
     *
     * @param token
     * @return
     */
    WorkflowScheduleExecution triggerWebhook(String token);
}
