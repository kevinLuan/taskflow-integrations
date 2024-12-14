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

import cn.feiliu.taskflow.common.enums.TaskStatus;
import cn.feiliu.taskflow.common.metadata.tasks.TaskLog;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通过更新其变量或由任务引用名称标识的计划任务之一来更新正在运行的工作流
 * Update a running workflow by updating its variables or one of the scheduled task identified by task reference name
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-30
 */
@Data
public class WorkflowProgressUpdate {
    @NotNull(message = "requestId Cannot be empty")
    private String              requestId;
    /*更新工作流变量*/
    @NotNull
    private Map<String, Object> variables             = new HashMap<>();
    /*工作流实例ID */
    @NotNull(message = "workflowId Cannot be empty")
    private String              workflowId;
    /**
     * 等待的任务引用名称列表。api调用将等待这些任务中的任何一个在工作流中可用。
     * List of task reference names to wait for.  The api call will  wait for ANY of these tasks to be availble in workflow.
     */
    @NotNull
    private List<String>        waitUntilTaskRefNames = new ArrayList<>();
    /**
     * 最大等待时间。如果此时工作流没有完成或到达waitUntilTaskRefNames中列出的任务之一，则调用将返回工作流的当前状态
     * （总感觉场景没啥用）
     * Maximum time to wait for.  If the workflow does not complete or reach one of the tasks listed  in waitUntilTaskRefNames by this time,
     *  *                              the call will return with the current status of the workflow
     */
    private Integer             waitForSeconds        = 5;
    @Valid
    private TaskRefUpdate       taskRefUpdate;

    public void addTaskRef(String waitTaskRef) {
        Objects.requireNonNull(waitTaskRef);
        waitUntilTaskRefNames.add(waitTaskRef);
    }

    @Data
    public static class TaskRefUpdate {
        /*任务引用名称*/
        @NotNull
        private String              taskReferenceName;
        /**
         * <b>IN_PROGRESS<b>的状态:用于长时间运行的任务，表示任务仍在进行中，应该在以后的时间再次检查。
         * 例如，当作业由另一个进程执行时，worker在DB中检查作业的状态。<p>
         * <b>FAILED, FAILED_WITH_TERMINAL_ERROR, COMPLETED<b>:任务的终端状态。当您不希望重试任务时，使用FAILED_WITH_TERMINAL_ERROR。
         */
        @NotNull
        private TaskStatus          status     = TaskStatus.COMPLETED;
        /*任务执行输出数据*/
        private Map<String, Object> outputData = new HashMap<>();
        private List<TaskLog>       logs       = new CopyOnWriteArrayList<>();
    }
}
