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
package cn.feiliu.taskflow.open.dto;

import cn.feiliu.taskflow.common.metadata.tasks.TaskExecResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author SHOUSHEN.LUAN
 * @since 2023-12-09
 */
@Setter
@Getter
@ToString
public class TaskMessage {
    String         groupName;
    /*任务名称*/
    String         taskName;
    TaskExecResult result;
    /*推送token*/
    String         token;

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        TaskMessage that = (TaskMessage) object;
        return Objects.equals(groupName, that.groupName) && Objects.equals(taskName, that.taskName)
               && Objects.equals(result, that.result) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, taskName, result, token);
    }
}
