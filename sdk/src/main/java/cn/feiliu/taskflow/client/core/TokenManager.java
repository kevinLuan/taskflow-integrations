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
package cn.feiliu.taskflow.client.core;

import cn.feiliu.common.api.utils.AuthTokenUtil;
import cn.feiliu.taskflow.client.api.IAuthClient;
import cn.feiliu.taskflow.dto.TokenResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 平台令牌管理
 *
 * @author SHOUSHEN.LUAN
 * @since 2024-06-11
 */
public class TokenManager implements AutoCloseable {
    private static final Logger            log                 = LoggerFactory.getLogger(TokenManager.class);
    private long                           tokenRefreshInSeconds;
    private IAuthClient                    authClient;
    private final Cache<String, String>    CACHE;
    private final ScheduledExecutorService tokenRefreshService = Executors.newSingleThreadScheduledExecutor();
    private String                         keyId;
    private String                         keySecret;
    private final String                   TOKEN               = "tf_token";
    private final RateLimiter              rateLimiter         = RateLimiter.create(1);

    public TokenManager(IAuthClient authClient, String keyId, String keySecret) {
        this.authClient = Objects.requireNonNull(authClient, "authClient Cannot be null");
        this.keyId = Objects.requireNonNull(keyId);
        this.keySecret = Objects.requireNonNull(keySecret);
        this.tokenRefreshInSeconds = getRefreshIntervalTimes();
        log.info("Setting token refresh interval to {} seconds", this.tokenRefreshInSeconds);
        this.CACHE = CacheBuilder.newBuilder().expireAfterWrite(tokenRefreshInSeconds, TimeUnit.SECONDS).build();
    }

    /**
     * 获取刷新间隔时间
     *
     * @return
     */
    private Integer getRefreshIntervalTimes() {
        String key = "feiliu_security_token_refresh_interval";
        String refreshInterval = System.getenv(key);
        if (refreshInterval == null) {
            refreshInterval = System.getProperty(key);
        }
        if (refreshInterval != null) {
            try {
                return Integer.parseInt(refreshInterval);
            } catch (Exception ignored) {
            }
        }
        return (int) TimeUnit.HOURS.toSeconds(2);
    }

    /**
     * 应该启动调度程序并初始化令牌
     */
    public void shouldStartSchedulerAndInitializeToken() {
        scheduleTokenRefresh();
        try {
            getBearerToken();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     * 调度令牌刷新
     */
    private void scheduleTokenRefresh() {
        log.info("Starting token refresh thread to run at every {} seconds", tokenRefreshInSeconds);
        this.tokenRefreshService.scheduleAtFixedRate(() -> {
            try {
                String token = refreshAndGetBearerToken();
                CACHE.put(TOKEN, token);
            } catch (Exception e) {
                log.error("Token refresh failed", e);
            }
        }, tokenRefreshInSeconds, tokenRefreshInSeconds, TimeUnit.SECONDS);
    }

    /**
     * 强制刷新token并返回Bearer格式的token
     *
     * @return Bearer格式的token字符串
     */
    private String refreshAndGetBearerToken() {
        log.info("Refreshing Token {}", new Timestamp(System.currentTimeMillis()));
        TokenResponse response = authClient.refreshToken();
        return AuthTokenUtil.constructBearerToken(response.getAccessToken());
    }

    @SneakyThrows
    public String getBearerToken() {
        try {
            return CACHE.get(TOKEN, () -> refreshAndGetBearerToken());
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public String constructCredentials() {
        return AuthTokenUtil.constructCredentials(keyId, keySecret);
    }

    @Override
    public void close() throws Exception {
        tokenRefreshService.shutdown();
    }

    /**
     * 尝试刷新令牌采用手动清理缓存，等待下一次获取主动获取token时才会真正的获取token
     */
    public void tryFlushToken() {
        if (rateLimiter.tryAcquire()) {
            log.info("flush token");
            CACHE.invalidateAll();
        }
    }
}
