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
package cn.feiliu.taskflow.core;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-08-24
 */
@Slf4j
public class ScanClasses {

    public static List<Object> scan(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage cannot be null");
        List<Object> workers = new ArrayList<>();
        long s = System.currentTimeMillis();
        try {
            List<String> packagesToScan = new ArrayList<>();
            String[] packages = basePackage.split(",");
            Collections.addAll(packagesToScan, packages);
            log.info("packages to scan {}", packagesToScan);

            ClassPath.from(TaskEngine.class.getClassLoader())
                    .getAllClasses()
                    .forEach(
                            classMeta -> {
                                String name = classMeta.getName();
                                if (!includePackage(packagesToScan, name)) {
                                    return;
                                }
                                try {
                                    Class<?> clazz = classMeta.load();
                                    workers.add(clazz.getConstructor().newInstance());
                                } catch (Throwable t) {
                                    // trace because many classes won't have a default no-args
                                    // constructor and will fail
                                    log.trace(
                                            "Caught exception while loading and scanning class {}",
                                            t.getMessage());
                                }
                            });
        } catch (Exception e) {
            log.error("Error while scanning for workers: ", e);
        }
        log.info("Took {} ms to scan all the classes, scan {} worker", (System.currentTimeMillis() - s), workers.size());
        return workers;
    }

    private static boolean includePackage(List<String> packagesToScan, String name) {
        for (String scanPkg : packagesToScan) {
            if (name.startsWith(scanPkg))
                return true;
        }
        return false;
    }
}
