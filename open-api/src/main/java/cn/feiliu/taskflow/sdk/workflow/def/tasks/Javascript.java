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
package cn.feiliu.taskflow.sdk.workflow.def.tasks;

import cn.feiliu.taskflow.sdk.workflow.def.ValidationException;
import com.google.common.base.Strings;
import cn.feiliu.taskflow.common.metadata.tasks.TaskType;
import cn.feiliu.taskflow.common.metadata.workflow.FlowTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * JQ Transformation task See https://stedolan.github.io/jq/ for how to form the queries to parse
 * JSON payloads
 */
public class Javascript extends Task<Javascript> {

    private static final Logger LOGGER                   = LoggerFactory.getLogger(Javascript.class);

    private static final String EXPRESSION_PARAMETER     = "expression";

    private static final String EVALUATOR_TYPE_PARAMETER = "evaluatorType";

    private static final String ENGINE                   = "nashorn";

    /**
     * Javascript tasks are executed on the Conductor server without having to write worker code
     *
     * <p>Use {@link Javascript#validate()} method to validate the javascript to ensure the script
     * is valid.
     *
     * @param taskReferenceName
     * @param script script to execute
     */
    public Javascript(String taskReferenceName, String script) {
        super(taskReferenceName, TaskType.INLINE);
        if (Strings.isNullOrEmpty(script)) {
            throw new IllegalArgumentException("Null/Empty script");
        }
        super.input(EVALUATOR_TYPE_PARAMETER, "javascript");
        super.input(EXPRESSION_PARAMETER, script);
    }

    /**
     * Javascript tasks are executed on the Conductor server without having to write worker code
     *
     * <p>Use {@link Javascript#validate()} method to validate the javascript to ensure the script
     * is valid.
     *
     * @param taskReferenceName
     * @param stream stream to load the script file from
     */
    public Javascript(String taskReferenceName, InputStream stream) {
        super(taskReferenceName, TaskType.INLINE);
        if (stream == null) {
            throw new IllegalArgumentException("Stream is empty");
        }
        super.input(EVALUATOR_TYPE_PARAMETER, "javascript");
        try {
            String script = new String(stream.readAllBytes());
            super.input(EXPRESSION_PARAMETER, script);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Javascript(FlowTask workflowTask) {
        super(workflowTask);
    }

    public String getExpression() {
        return (String) getInput().get(EXPRESSION_PARAMETER);
    }

    /**
     * Validates the script.
     *
     * @return
     */
    public Javascript validate() {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(ENGINE);
        if (scriptEngine == null) {
            LOGGER.error("missing " + ENGINE + " engine.  Ensure you are running supported JVM");
            return this;
        }

        try {

            Bindings bindings = scriptEngine.createBindings();
            bindings.put("$", new HashMap<>());
            scriptEngine.eval(getExpression(), bindings);

        } catch (ScriptException e) {
            String message = e.getMessage();
            throw new ValidationException(message);
        }
        return this;
    }

    /**
     * Helper method to unit test your javascript. The method is not used for creating or executing
     * workflow but is meant for testing only.
     *
     * @param input Input that against which the script will be executed
     * @return Output of the script
     */
    public Object test(Map<String, Object> input) {

        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(ENGINE);
        if (scriptEngine == null) {
            LOGGER.error("missing " + ENGINE + " engine.  Ensure you are running supported JVM");
            return this;
        }

        try {

            Bindings bindings = scriptEngine.createBindings();
            bindings.put("$", input);
            return scriptEngine.eval(getExpression(), bindings);

        } catch (ScriptException e) {
            String message = e.getMessage();
            throw new ValidationException(message);
        }
    }
}
