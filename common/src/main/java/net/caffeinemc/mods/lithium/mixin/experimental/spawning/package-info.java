@MixinConfigOption(
        description = "Experimental optimizations to spawning conditions. Reorders the iteration over entities to match the chunks and chunk sections, reducing the number of cache misses.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.accessors")
)
package net.caffeinemc.mods.lithium.mixin.experimental.spawning;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;