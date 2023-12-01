load("//tools/bzl:junit.bzl", "junit_tests")
load("//tools/bzl:js.bzl", "gerrit_js_bundle")
load("//tools/js:eslint.bzl", "eslint")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)
load("@rules_java//java:defs.bzl", "java_library", "java_plugin")

plugin_name = "depends-on"

java_plugin(
    name = "auto-annotation-plugin",
    processor_class = "com.google.auto.value.processor.AutoAnnotationProcessor",
    deps = [
        "@auto-value-annotations//jar",
        "@auto-value//jar",
    ],
)

java_plugin(
    name = "auto-value-plugin",
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    deps = [
        "@auto-value-annotations//jar",
        "@auto-value//jar",
    ],
)

java_library(
    name = "auto-value",
    exported_plugins = [
        ":auto-annotation-plugin",
        ":auto-value-plugin",
    ],
    visibility = ["//visibility:public"],
    exports = ["@auto-value//jar"],
)

java_library(
    name = "auto-value-annotations",
    exported_plugins = [
        ":auto-annotation-plugin",
        ":auto-value-plugin",
    ],
    visibility = ["//visibility:public"],
    exports = ["@auto-value-annotations//jar"],
)

gerrit_plugin(
    name = plugin_name,
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: " + plugin_name,
        "Implementation-Title: Depends-on Plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/" + plugin_name,
        "Gerrit-Module: com.googlesource.gerrit.plugins.depends.on.Module",
    ],
    resource_jars = [":gr-depends-on-plugin"],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        ":auto-value",
        ":auto-value-annotations",
    ],
)

gerrit_js_bundle(
    name = "gr-depends-on-plugin",
    srcs = glob(["gr-depends-on-plugin/*.js"]),
    entry_point = "gr-depends-on-plugin/plugin.js",
)

eslint(
    name = "lint",
    srcs = glob(["gr-depends-on-plugin/**/*.js"]),
    config = ".eslintrc.json",
    data = [],
    extensions = [
        ".js",
    ],
    ignore = ".eslintignore",
    plugins = [],
)

junit_tests(
    name = "depends-on_tests",
    size = "small",
    srcs = glob(["src/test/java/**/*Test.java"]),
    tags = [plugin_name],
    deps = [":depends-on__plugin_test_deps"],
)

java_library(
    name = "depends-on__plugin_test_deps",
    testonly = True,
    srcs = glob(
        ["src/test/java/**/*.java"],
        exclude = ["src/test/java/**/*Test.java"],
    ),
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [plugin_name],
)

sh_test(
    name = "docker-tests",
    size = "medium",
    srcs = ["test/docker/run.sh"],
    args = [
        "--plugin", plugin_name,
        "$(location :depends-on)",
    ],
    data = [plugin_name] + glob(["test/**"]),
    local = True,
    tags = ["docker"],
)
