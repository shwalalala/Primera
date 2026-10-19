// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    configurations.all {
        resolutionStrategy {
            // Resolves Alert #6: XML External Entity (XXE) Injection in JDOM
            force("org.jdom:jdom2:2.0.6.1")
            // Resolves Bouncy Castle vulnerabilities in AGP / Lint plugin classpath
            force("org.bouncycastle:bcprov-jdk18on:1.86")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

// Global dependency resolution strategy to resolve all listed Dependabot security alerts
allprojects {
    configurations.all {
        resolutionStrategy {
            // Resolves Alerts #24, #19, #33, #51, #14: Netty HTTP/2 Frame Flood, MadeYouReset DDoS, and decompression/Rapid Reset flaws
            force("io.netty:netty-codec-http2:4.2.19.Final")

            // Aligns Netty common utilities to version 4.1.132.Final
            force("io.netty:netty-common:4.2.19.Final")

            // Aligns Netty buffer management to version 4.1.132.Final
            force("io.netty:netty-buffer:4.2.19.Final")

            // Aligns Netty transport layer to version 4.1.132.Final
            force("io.netty:netty-transport:4.2.19.Final")

            // Resolves Alerts #16, #34, #35, #38, #54: Netty SslHandler native crash, IPv6 Subnet Filter bypass, SNI pre-allocation, TrustManager vulnerabilities, and SNI Routing Bypass via Fragmented TLS ClientHello
            force("io.netty:netty-handler:4.2.19.Final")

            // Resolves Alerts #44, #32, #23, #43, #42, #30: Netty SPDY/HTTP codec and request smuggling vulnerabilities
            force("io.netty:netty-codec-http:4.2.19.Final")

            // Resolves Alerts #49, #29: Netty Bzip2 infinite loop and Lz4 resource exhaustion vulnerabilities
            force("io.netty:netty-codec:4.2.19.Final")

            // Resolves Alerts #12, #11, #41: Bouncy Castle Name Constraints bypass via trailing dots, GOST 28147 CTR keystream reuse, and Lazy ASN.1 sequence nesting-depth guard resets
            force("org.bouncycastle:bcprov-jdk18on:1.86")

            // Resolves Alert #7: jose4j Denial-of-Service via compressed JWE content (zip bomb)
            force("org.bitbucket.b_c:jose4j:0.9.7")
        }
    }
}
