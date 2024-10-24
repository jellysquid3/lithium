@MixinConfigOption(
        description = """
                A faster code path is used for determining what kind of path-finding node type is associated with a
                given block. Additionally, a faster chunk cache will be used for accessing blocks while evaluating
                paths.
                """,
        depends = @MixinConfigDependency(
                dependencyPath = "mixin.util.chunk_access"
        ), enabled = false // TODO: Broken by 1.21.2 or 1.21.3 Update due to initialization order change. Needs new injection points for cache initialization.
)
package net.caffeinemc.mods.lithium.mixin.ai.pathing;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;