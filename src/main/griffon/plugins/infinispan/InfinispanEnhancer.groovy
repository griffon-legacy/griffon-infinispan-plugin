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
import griffon.util.CallableWithArgs

/**
 * @author Andres Almiray
 */
final class InfinispanEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanEnhancer)

    private InfinispanEnhancer() {}

    static void enhance(MetaClass mc, InfinispanProvider provider = CacheManagerHolder.instance) {
        if (LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withInfinispan = {Closure closure ->
            provider.withInfinispan('default', closure)
        }
        mc.withInfinispan << {String cacheManagerName, Closure closure ->
            provider.withInfinispan(cacheManagerName, closure)
        }
        mc.withInfinispan << {CallableWithArgs callable ->
            provider.withInfinispan('default', callable)
        }
        mc.withInfinispan << {String cacheManagerName, CallableWithArgs callable ->
            provider.withInfinispan(cacheManagerName, callable)
        }
    }
}
