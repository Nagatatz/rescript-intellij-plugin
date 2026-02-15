package com.rescript.plugin.run

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
