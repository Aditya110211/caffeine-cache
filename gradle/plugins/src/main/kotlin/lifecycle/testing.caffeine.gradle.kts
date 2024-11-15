import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  `java-library`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
  testImplementation(libs.guava)
  testImplementation(libs.guice)
  testImplementation(libs.truth)
  testImplementation(libs.testng)
  testImplementation(libs.mockito)
  testImplementation(libs.hamcrest)
  testImplementation(libs.awaitility)
  testImplementation(libs.bundles.junit)
  testImplementation(libs.guava.testlib)
  testImplementation(libs.bundles.osgi.test.compile)

  testImplementation(platform(libs.asm.bom))
  testImplementation(platform(libs.kotlin.bom))
  testImplementation(platform(libs.junit5.bom))

  testRuntimeOnly(libs.junit5.launcher)
  testRuntimeOnly(libs.bundles.junit.engines)
  testRuntimeOnly(libs.bundles.osgi.test.runtime)

  mockitoAgent(libs.mockito) {
    isTransitive = false
  }
}

tasks.withType<Test>().configureEach {
  inputs.property("javaVendor", java.toolchain.vendor.get().toString())

  // Use --debug-jvm to remotely attach to the test task
  jvmArgs("-XX:SoftRefLRUPolicyMSPerMB=0", "-XX:+EnableDynamicAgentLoading", "-Xshare:off")
  jvmArgs("-javaagent:${mockitoAgent.asPath}")
  jvmArgs(defaultJvmArgs())
  if (isCI()) {
    reports.junitXml.includeSystemOutLog = false
    reports.junitXml.includeSystemErrLog = false
  }
  testLogging {
    events = setOf(SKIPPED, FAILED)
    exceptionFormat = FULL
    showStackTraces = true
    showExceptions = true
    showCauses = true
  }
}
