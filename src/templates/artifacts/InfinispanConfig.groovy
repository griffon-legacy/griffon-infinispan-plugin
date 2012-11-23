cacheManager {
    caches {
        defaultCache {
        }
    }
}
environments {
    development {
        cacheManager {
            // url = 'classpath://infinispan-dev.xml'
        }
    }
    test {
        cacheManager {
            // url = 'classpath://infinispan-test.xml'
        }
    }
    production {
        cacheManager {
            // url = 'classpath://infinispan-prod.xml'
        }
    }
}
