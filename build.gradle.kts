// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("org.bouncycastle:bcprov-jdk18on:1.79")
            force("org.bouncycastle:bcpkix-jdk18on:1.79")
            force("org.apache.commons:commons-lang3:3.17.0")
            force("com.google.guava:guava:33.4.0-jre")
            force("org.jdom:jdom2:2.0.6.1")
            force("org.apache.httpcomponents:httpclient:4.5.14")
            force("org.bitbucket.b_c:jose4j:0.9.6")
            force("io.netty:netty-codec:4.1.118.Final")
            force("io.netty:netty-codec-http:4.1.118.Final")
            force("io.netty:netty-codec-http2:4.1.118.Final")
            force("io.netty:netty-handler:4.1.118.Final")
            force("io.netty:netty-common:4.1.118.Final")
            force("io.netty:netty-buffer:4.1.118.Final")
            force("io.netty:netty-transport:4.1.118.Final")
            force("io.netty:netty-resolver:4.1.118.Final")
        }
    }
}