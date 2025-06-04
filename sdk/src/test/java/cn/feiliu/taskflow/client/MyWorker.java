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
package cn.feiliu.taskflow.client;

import cn.feiliu.taskflow.annotations.InputParam;
import cn.feiliu.taskflow.annotations.OutputParam;
import cn.feiliu.taskflow.annotations.WorkerTask;

/**
 * 自定义工作节点
 *
 * @author kevin.luan
 * @since 2025-06-02
 */
public class MyWorker {
    /**
     * 加法方法
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个整数相加的结果
     */
    @OutputParam("result")
    @WorkerTask(value = "add")
    public int add(@InputParam("a") int a, @InputParam("b") int b) {
        System.out.println("add( " + a + " + " + b + " )");
        return a + b;
    }

    /**
     * 减法方法
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个整数相减的结果
     */
    @WorkerTask(value = "subtract")
    public int subtract(@InputParam("a") int a, @InputParam("b") int b) {
        System.out.println("subtract( " + a + " - " + b + " )");
        return a - b;
    }

    /**
     * 乘法方法
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个整数相乘的结果
     */
    @WorkerTask(value = "multiply")
    public int multiply(@InputParam("a") int a, @InputParam("b") int b) {
        System.out.println("multiply( " + a + " x " + b + " )");
        return a * b;
    }

    /**
     * 除法方法
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个整数相除的结果
     * @throws ArithmeticException 如果第二个整数为0
     */
    @WorkerTask(value = "divide")
    public double divide(@InputParam("a") int a, @InputParam("b") int b) throws ArithmeticException {
        System.out.println("divide( " + a + " / " + b + " )");
        if (b == 0) {
            throw new ArithmeticException("除数不能为0");
        }
        return (double) a / b;
    }
}
