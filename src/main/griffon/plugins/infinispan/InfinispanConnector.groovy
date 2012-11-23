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
import griffon.util.CallableWithArgs
import griffon.util.ConfigUtils
import org.infinispan.config.Configuration
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.manager.EmbeddedCacheManager

/**
 * @author Andres Almiray
 */
@Singleton
final class InfinispanConnector implements InfinispanProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(InfinispanConnector)
    private static final String CLASSPATH_PREFIX = 'classpath://'

    Object withInfinispan(String cacheManagerName = 'default', Closure closure) {
        CacheManagerHolder.instance.withInfinispan(cacheManagerName, closure)
    }

    public <T> T withInfinispan(String cacheManagerName = 'default', CallableWithArgs<T> callable) {
        return CacheManagerHolder.instance.withInfinispan(cacheManagerName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        ConfigUtils.loadConfigWithI18n('InfinispanConfig')
    }

    private ConfigObject narrowConfig(ConfigObject config, String cacheManagerName) {
        return cacheManagerName == 'default' ? config.cacheManager : config.cacheManagers[cacheManagerName]
    }

    EmbeddedCacheManager connect(GriffonApplication app, ConfigObject config, String cacheManagerName = 'default') {
        if (CacheManagerHolder.instance.isEmbeddedCacheManagerConnected(cacheManagerName)) {
            return CacheManagerHolder.instance.getEmbeddedCacheManager(cacheManagerName)
        }

        config = narrowConfig(config, cacheManagerName)
        app.event('InfinispanConnectStart', [config, cacheManagerName])
        EmbeddedCacheManager cacheManager = startInfinispan(app, config)
        CacheManagerHolder.instance.setEmbeddedCacheManager(cacheManagerName, cacheManager)
        bootstrap = app.class.classLoader.loadClass('BootstrapInfinispan').newInstance()
        bootstrap.metaClass.app = app
        bootstrap.init(cacheManagerName, cacheManager)
        app.event('InfinispanConnectEnd', [cacheManagerName, cacheManager])
        cacheManager
    }

    void disconnect(GriffonApplication app, ConfigObject config, String cacheManagerName = 'default') {
        if (CacheManagerHolder.instance.isEmbeddedCacheManagerConnected(cacheManagerName)) {
            config = narrowConfig(config, cacheManagerName)
            EmbeddedCacheManager cacheManager = CacheManagerHolder.instance.getEmbeddedCacheManager(cacheManagerName)
            app.event('InfinispanDisconnectStart', [config, cacheManagerName, cacheManager])
            bootstrap.destroy(cacheManagerName, cacheManager)
            stopInfinispan(config, cacheManager)
            app.event('InfinispanDisconnectEnd', [config, cacheManagerName])
            CacheManagerHolder.instance.disconnectEmbeddedCacheManager(cacheManagerName)
        }
    }

    private EmbeddedCacheManager startInfinispan(GriffonApplication app, ConfigObject config) {
        if (config.url) {
            InputStream is = null
            if (config.url instanceof URL) {
                is = config.url.openStream()
            } else {
                String url = config.url.toString()
                if (url.startsWith(CLASSPATH_PREFIX)) {
                    is = app.getResourceAsURL(url.substring(CLASSPATH_PREFIX.length())).openStream()
                } else {
                    is = url.toURL().openStream()
                }
            }
            return new DefaultCacheManager(is)
        }

        EmbeddedCacheManager cacheManager = new DefaultCacheManager(false)
        config.each { key, value ->
            if (key == 'caches') {
                value.each { String cacheName, cacheConfigProps ->
                    Configuration cacheConfiguration = new Configuration()
                    cacheConfigProps.each { k, v ->
                        cacheConfiguration[k] = v
                    }
                    cacheManager.defineConfiguration(cacheName, cacheConfiguration)
                }
            }
        }

        cacheManager.start()
        cacheManager
    }

    private void stopInfinispan(ConfigObject config, EmbeddedCacheManager cacheManager) {
        cacheManager.stop()
    }
}
