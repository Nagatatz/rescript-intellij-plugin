package com.rescript.plugin.run

/**
 * Available ReScript CLI commands for the run configuration.
 *
 * Each entry defines the command ID (for serialization), display name (for UI),
 * and the CLI arguments passed to the `rescript` binary.
 */
enum class RescriptCommand(
    val id: String,
    val displayName: String,
    val args: List<String>,
) {
    BUILD("build", "Build", listOf("build")),
    BUILD_WATCH("build-watch", "Build (Watch)", listOf("build", "-w")),
    CLEAN("clean", "Clean", listOf("clean")),
    ;

    companion object {
        fun fromId(id: String): RescriptCommand = entries.firstOrNull { it.id == id } ?: BUILD
    }
}
