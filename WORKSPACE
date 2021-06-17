workspace(name = "depends-on")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "f96f4bce9ffafeaa200fc009a378921c512fcb0a",
)

load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
    "gerrit_api",
)

# Load release Plugin API
gerrit_api()

load("//:external_plugin_deps.bzl", "external_plugin_deps")
external_plugin_deps()
