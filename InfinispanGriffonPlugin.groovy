/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
 class InfinispanGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-infinispan-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Infinispan support'
    String description = '''
The Infinispan plugin enables lightweight access to [Infinispan][1] caches.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * InfinispanConfig.groovy - contains the cache definitions.
 * BootstrapInfinispan.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withInfinispan` will be injected into all controllers,
giving you access to `org.infinispan.manager.EmbeddedCacheManager`, with which you'll be able
to make calls to the cache. Remember to make all cache calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple caches. If no cacheManagerName is specified when calling
it then the default cache will be selected. Here are two example usages, the first
queries against the default cache while the second queries a cache whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllStores = {
            withInfinispan { cacheManagerName, cacheManager -> ... }
            withInfinispan('internal') { cacheManagerName, cacheManager -> ... }
        }
    }

This method is also accessible to any component through the singleton `griffon.plugins.infinispan.InfinispanConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`InfinispanEnhancer.enhance(metaClassInstance, infinispanProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withInfinispan()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.infinispan.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * InfinispanConnectStart[config, cacheManagerName] - triggered before connecting to the cache
 * InfinispanConnectEnd[cacheManagerName, cacheManager] - triggered after connecting to the cache
 * InfinispanDisconnectStart[config, cacheManagerName, cacheManager] - triggered before disconnecting from the cache
 * InfinispanDisconnectEnd[config, cacheManagerName] - triggered after disconnecting from the cache

### Multiple Caches

The config file `InfinispanConfig.groovy` defines a default cacheManager block. As the name
implies this is the cacheManager used by default, however you can configure named cacheManagers
by adding a new config block. For example connecting to a cacheManager whose name is 'internal'
can be done in this way

    cacheManagers {
        internal {
            url = 'classpath://infinispan-internal.xml'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default cacheManager block is used.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/infinispan][2]

Testing
-------
The `withInfinispan()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `InfinispanEnhancer.enhance(metaClassInstance, infinispanProviderInstance)` where 
`infinispanProviderInstance` is of type `griffon.plugins.infinispan.InfinispanProvider`. The contract for this interface looks like this

    public interface InfinispanProvider {
        Object withInfinispan(Closure closure);
        Object withInfinispan(String cacheManagerName, Closure closure);
        <T> T withInfinispan(CallableWithArgs<T> callable);
        <T> T withInfinispan(String cacheManagerName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyInfinispanProvider implements InfinispanProvider {
        Object withInfinispan(String cacheManagerName = 'default', Closure closure) { null }
        public <T> T withInfinispan(String cacheManagerName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            InfinispanEnhancer.enhance(service.metaClass, new MyInfinispanProvider())
            // exercise service methods
        }
    }


[1]: http://www.jboss.org/infinispan/
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/infinispan
'''
}
