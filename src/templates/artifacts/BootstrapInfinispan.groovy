import org.infinispan.manager.EmbeddedCacheManager

class BootstrapInfinispan {
    def init = { String cacheManagerName, EmbeddedCacheManager cacheManager ->
    }

    def destroy = { String cacheManagerName, EmbeddedCacheManager cacheManager ->
    }
}