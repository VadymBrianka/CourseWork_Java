package org.carrent.coursework.resolver;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
public class MultiLevelCacheResolver implements CacheResolver {
    private final CacheManager localCacheManager;

    public MultiLevelCacheResolver(CacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        return Stream.of(
                        localCacheManager.getCache("cars"),
                        localCacheManager.getCache("customers"),
                        localCacheManager.getCache("employees"),
                        localCacheManager.getCache("orders"),
                        localCacheManager.getCache("services")
                )
                .filter(Objects::nonNull)
                .toList();
    }

}