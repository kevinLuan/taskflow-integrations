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
package cn.feiliu.taskflow.sdk.config;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-23
 */
public class WorkerPropertyManager {
    protected static final String DOMAIN             = "domain";
    protected static final String OVERRIDE_DISCOVERY = "pollOutOfDiscovery";

    /**
     * 获取任务是否为暂停状态
     *
     * @param taskDefName
     * @return
     */
    public static boolean paused(String taskDefName) {
        return PropertyFactory.getBoolean(taskDefName, "paused", false);
    }

    /**
     * 获取Worker拉取间隔时间
     *
     * @param taskDefName
     * @return
     */
    public static int getPollingInterval(String taskDefName) {
        return PropertyFactory.getInteger(taskDefName, "pollInterval", 1000);
    }

    public static boolean leaseExtendEnabled(String taskDefName) {
        return PropertyFactory.getBoolean(taskDefName, "leaseExtendEnabled", false);
    }

    /**
     * 获取批量拉取任务超时时间
     *
     * @param taskDefName
     * @return
     */
    public static int getBatchPollTimeoutInMS(String taskDefName) {
        return PropertyFactory.getInteger(taskDefName, "batchPollTimeoutInMS", 1000);
    }

    public static String getDomainWithFallback(String taskType, String allWorkers, String defaultValue) {
        return PropertyFactory.getStringWithFallback(taskType, DOMAIN, allWorkers, defaultValue);
    }

    public static boolean getPollOutOfDiscovery(String taskDefName, String allWorkers, boolean defaultValue) {
        return PropertyFactory.getBooleanWithFallback(taskDefName, OVERRIDE_DISCOVERY, allWorkers, defaultValue);
    }
}
