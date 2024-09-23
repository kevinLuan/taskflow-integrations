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
package cn.feiliu.taskflow.open.api;

import cn.feiliu.taskflow.common.model.WorkflowRun;
import cn.feiliu.taskflow.open.dto.CorrelationIdsSearchRequest;
import cn.feiliu.taskflow.open.dto.WorkflowProgressUpdate;
import cn.feiliu.taskflow.open.exceptions.ConflictException;
import cn.feiliu.taskflow.common.metadata.workflow.WorkflowRerunRequest;
import cn.feiliu.taskflow.common.metadata.workflow.StartWorkflowRequest;
import cn.feiliu.taskflow.common.model.BulkResponseResult;
import cn.feiliu.taskflow.common.run.ExecutingWorkflow;
import cn.feiliu.taskflow.common.utils.ExternalPayloadStorage;

import java.util.List;
import java.util.Map;

public interface IWorkflowService {

    /**
     * Starts a workflow. If the size of the workflow input payload is bigger than {@link
     * ExternalPayloadStorage}, if enabled, else the workflow is rejected.
     *
     * @param startWorkflowRequest the {@link StartWorkflowRequest} object to start the workflow
     * @return the id of the workflow instance that can be used for tracking
     */
    String startWorkflow(StartWorkflowRequest startWorkflowRequest) throws ConflictException;

    /**
     * Retrieve a workflow by workflow id
     *
     * @param workflowId   the id of the workflow
     * @param includeTasks specify if the tasks in the workflow need to be returned
     * @return the requested workflow
     */
    ExecutingWorkflow getWorkflow(String workflowId, boolean includeTasks);

    /**
     * Retrieve all workflows for a given correlation id and name
     *
     * @param name          the name of the workflow
     * @param correlationId the correlation id
     * @param includeClosed specify if all workflows are to be returned or only running workflows
     * @param includeTasks  specify if the tasks in the workflow need to be returned
     * @return list of workflows for the given correlation id and name
     */
    List<ExecutingWorkflow> getWorkflows(String name, String correlationId, boolean includeClosed, boolean includeTasks);

    /**
     * Removes a workflow from the system
     *
     * @param workflowId      the id of the workflow to be deleted
     * @param archiveWorkflow flag to indicate if the workflow should be archived before deletion
     */
    void deleteWorkflow(String workflowId, boolean archiveWorkflow);

    /**
     * Retrieve all running workflow instances for a given name and version
     *
     * @param workflowName the name of the workflow
     * @param version      the version of the wokflow definition. Defaults to 1.
     * @return the list of running workflow instances
     */
    List<String> getRunningWorkflow(String workflowName, Integer version);

    /**
     * Retrieve all workflow instances for a given workflow name between a specific time period
     *
     * @param workflowName the name of the workflow
     * @param version      the version of the workflow definition. Defaults to 1.
     * @param startTime    the start time of the period
     * @param endTime      the end time of the period
     * @return returns a list of workflows created during the specified during the time period
     */
    List<String> getWorkflowsByTimePeriod(String workflowName, int version, Long startTime, Long endTime);

    /**
     * Starts the decision task for the given workflow instance
     *
     * @param workflowId the id of the workflow instance
     */
    void runDecider(String workflowId);

    /**
     * Pause a workflow by workflow id
     *
     * @param workflowId the workflow id of the workflow to be paused
     */
    void pauseWorkflow(String workflowId);

    /**
     * Resume a paused workflow by workflow id
     *
     * @param workflowId the workflow id of the paused workflow
     */
    void resumeWorkflow(String workflowId);

    /**
     * Skips a given task from a current RUNNING workflow
     *
     * @param workflowId        the id of the workflow instance
     * @param taskReferenceName the reference name of the task to be skipped
     */
    void skipTaskFromWorkflow(String workflowId, String taskReferenceName);

    /**
     * Reruns the workflow from a specific task
     *
     * @param workflowId           the id of the workflow
     * @param workflowRerunRequest the request containing the task to rerun from
     * @return the id of the workflow
     */
    String rerunWorkflow(String workflowId, WorkflowRerunRequest workflowRerunRequest);

    /**
     * Restart a completed workflow
     *
     * @param workflowId           the workflow id of the workflow to be restarted
     * @param useLatestDefinitions if true, use the latest workflow and task definitions when
     *                             restarting the workflow if false, use the workflow and task definitions embedded in the
     *                             workflow execution when restarting the workflow
     */
    void restart(String workflowId, boolean useLatestDefinitions);

    /**
     * Retries the last failed task in a workflow
     *
     * @param workflowId the workflow id of the workflow with the failed task
     */
    void retryLastFailedTask(String workflowId);

    /**
     * Terminates the execution of the given workflow instance
     *
     * @param workflowId the id of the workflow to be terminated
     * @param reason     the reason to be logged and displayed
     */
    void terminateWorkflow(String workflowId, String reason);

    /**
     * Terminates the execution of all given workflows instances
     *
     * @param workflowIds the ids of the workflows to be terminated
     * @param reason      the reason to be logged and displayed
     * @return the {@link BulkResponseResult} contains bulkErrorResults and bulkSuccessfulResults
     */
    BulkResponseResult terminateWorkflows(List<String> workflowIds, String reason);

    /**
     * Search workflows based on correlation ids and names
     *
     * @param includeClosed if set, includes workflows that are terminal.  Otherwise, only returns RUNNING workflows
     * @param includeTasks  if set, returns tasks.
     * @return Map with a key as correlation id and value as a list of matching workflow executions
     */
    Map<String, List<ExecutingWorkflow>> getWorkflowsByNamesAndCorrelationIds(Boolean includeClosed,
                                                                              Boolean includeTasks,
                                                                              CorrelationIdsSearchRequest request);

    /**
     * Update the workflow by setting variables as given.  This is similar to SET_VARIABLE task except that with
     * this API, the workflow variables can be updated any anytime while the workflow is in RUNNING state.
     * This API is useful for cases where the state of the workflow needs to be updated based on an external trigger,
     * such as terminate a long-running do_while loop with a terminating condition based on the workflow variables.
     *
     * @param workflowId Id of the workflow
     * @param variables  Workflow variables.  The variables are merged with existing variables.
     * @return Updated state of the workflow
     */
    ExecutingWorkflow updateVariables(String workflowId, Map<String, Object> variables);

    /**
     * Update a runningw workflow by updating its variables or one of the scheduled task identified by task reference name
     */
    WorkflowRun updateWorkflow(WorkflowProgressUpdate body);

    /**
     * Resets the callback times of all IN PROGRESS tasks to 0 for the given workflow
     *
     * @param workflowId the id of the workflow
     */
    void resetWorkflow(String workflowId);
}
