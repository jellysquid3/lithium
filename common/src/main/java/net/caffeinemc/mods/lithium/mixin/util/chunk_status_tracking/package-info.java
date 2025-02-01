@MixinConfigOption(
        description = "Allows reacting to changes of the load status of chunks.",
        depends = @MixinConfigDependency(dependencyPath = "mixin.util.accessors")

)
package net.caffeinemc.mods.lithium.mixin.util.chunk_status_tracking;

import net.caffeinemc.gradle.MixinConfigDependency;
import net.caffeinemc.gradle.MixinConfigOption;
