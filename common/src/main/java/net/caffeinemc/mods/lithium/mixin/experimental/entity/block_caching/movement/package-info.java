@MixinConfigOption(description = "Use the block and collider entity listening system to skip block collisions when movement attempts fail (e.g. when a mob stands on the ground or on top of a boat). This optimization comes with a lot of complexity, but measurements do not show a performance benefit over other optimizations (experimental.entity.block_caching.block_support) in normal worlds, which is why this optimization is disabled by default.",
depends = @MixinConfigDependency(dependencyPath = "mixin.util.block_tracking"), enabled = false)
package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching.movement;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;