/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.infinispan

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank
import org.infinispan.manager.EmbeddedCacheManager

/**
 * @author Andres Almiray
 */
@Singleton
class CacheManagerHolder implements InfinispanProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CacheManagerHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, EmbeddedCacheManager> cacheManagers = [:]

    String[] getEmbeddedCacheManagerNames() {
        List<String> cacheManagerNames = new ArrayList().addAll(cacheManagers.keySet())
        cacheManagerNames.toArray(new String[cacheManagerNames.size()])
    }

    EmbeddedCacheManager getEmbeddedCacheManager(String cacheManagerName = 'default') {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        retrieveEmbeddedCacheManager(cacheManagerName)
    }

    void setEmbeddedCacheManager(String cacheManagerName = 'default', EmbeddedCacheManager cacheManager) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        storeEmbeddedCacheManager(cacheManagerName, cacheManager)
    }

    Object withInfinispan(String cacheManagerName = 'default', Closure closure) {
        EmbeddedCacheManager cacheManager = fetchEmbeddedCacheManager(cacheManagerName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on cacheManager '$cacheManagerName'")
        return closure(cacheManagerName, cacheManager)
    }

    public <T> T withInfinispan(String cacheManagerName = 'default', CallableWithArgs<T> callable) {
        EmbeddedCacheManager cacheManager = fetchEmbeddedCacheManager(cacheManagerName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on cacheManager '$cacheManagerName'")
        callable.args = [cacheManagerName, cacheManager] as Object[]
        return callable.call()
    }

    boolean isEmbeddedCacheManagerConnected(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        retrieveEmbeddedCacheManager(cacheManagerName) != null
    }

    void disconnectEmbeddedCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        storeEmbeddedCacheManager(cacheManagerName, null)
    }

    private EmbeddedCacheManager fetchEmbeddedCacheManager(String cacheManagerName) {
        if (isBlank(cacheManagerName)) cacheManagerName = 'default'
        EmbeddedCacheManager cacheManager = retrieveEmbeddedCacheManager(cacheManagerName)
        if (cacheManager == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = InfinispanConnector.instance.createConfig(app)
            cacheManager = InfinispanConnector.instance.connect(app, config, cacheManagerName)
        }

        if (cacheManager == null) {
            throw new IllegalArgumentException("No such EmbeddedCacheManager configuration for name $cacheManagerName")
        }
        cacheManager
    }

    private EmbeddedCacheManager retrieveEmbeddedCacheManager(String cacheManagerName) {
        synchronized (LOCK) {
            cacheManagers[cacheManagerName]
        }
    }

    private void storeEmbeddedCacheManager(String cacheManagerName, EmbeddedCacheManager cacheManager) {
        synchronized (LOCK) {
            cacheManagers[cacheManagerName] = cacheManager
        }
    }
}
