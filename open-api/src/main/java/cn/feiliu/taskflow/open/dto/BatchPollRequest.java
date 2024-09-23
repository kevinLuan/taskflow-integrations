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
package cn.feiliu.taskflow.open.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author SHOUSHEN.LUAN
 * @since 2023-12-09
 */
@Getter
@Setter
@ToString
public class BatchPollRequest {
    //任务组名称
    String  groupName;
    //任务名称
    String  taskName;
    //工作节点ID
    String  workerId;
    //拉取数量
    Integer count;
    //超时时间
    Integer timeout;
    /*应用token*/
    String  token;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String  groupName;
        private String  taskName;
        private String  workerId;
        private Integer count;
        private Integer timeout;
        private String  token;

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder workerId(String workerId) {
            this.workerId = workerId;
            return this;
        }

        public Builder count(Integer count) {
            this.count = count;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public BatchPollRequest build() {
            BatchPollRequest request = new BatchPollRequest();
            request.setGroupName(this.groupName);
            request.setTaskName(this.taskName);
            request.setWorkerId(this.workerId);
            request.setCount(this.count);
            request.setTimeout(this.timeout);
            request.setToken(this.token);
            return request;
        }
    }
}
