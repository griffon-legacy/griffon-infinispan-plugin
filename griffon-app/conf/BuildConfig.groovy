griffon.project.dependency.resolution = {
    inherits "global"
    log "warn" 
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo 'http://repository.jboss.org/nexus/content/groups/public'
    }
    dependencies {
        compile 'org.infinispan:infinispan-core:5.1.8.Final'
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}