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
package cn.feiliu.taskflow.common.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External service factory.
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-03-08
 */
public class ExternalServiceFactory {
    private static final Map<Class<?>, Collection<Object>> SERVICES = new ConcurrentHashMap<>();

    /**
     * Register service.
     *
     * @param serviceInterface service interface
     */
    public static void register(final Class<?> serviceInterface) {
        if (!SERVICES.containsKey(serviceInterface)) {
            SERVICES.put(serviceInterface, load(serviceInterface));
        }
    }

    /**
     * Get service instances.
     *
     * @param serviceInterface service interface
     * @param <T>              type of service
     * @return service instances
     */
    public static <T> Collection<T> getServiceInstances(final Class<T> serviceInterface) {
        return createNewServiceInstances(serviceInterface);
    }

    /**
     * Get first service instances.
     *
     * @param serviceInterface service interface
     * @param <T>              type of service
     * @return service instances
     */
    public static <T> Optional<T> getFirstServiceInstance(final Class<T> serviceInterface) {
        Collection<T> collection = createNewServiceInstances(serviceInterface);
        Iterator<T> iterator = collection.iterator();
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        }
        return Optional.empty();
    }

    /**
     * Load service.
     *
     * @param serviceInterface
     * @param <T>
     * @return
     */
    private static <T> Collection<Object> load(final Class<T> serviceInterface) {
        Collection<Object> result = new LinkedList<>();
        for (T each : ServiceLoader.load(serviceInterface)) {
            result.add(each);
        }
        return result;
    }

    /**
     * Create new service instances.
     *
     * @param serviceInterface
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> createNewServiceInstances(final Class<T> serviceInterface) {
        if (!SERVICES.containsKey(serviceInterface)) {
            return Collections.emptyList();
        }
        Collection<Object> services = SERVICES.get(serviceInterface);
        if (services.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<T> result = new LinkedList<>();
        for (Object each : services) {
            try {
                result.add((T) each.getClass().getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
