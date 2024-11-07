package net.caffeinemc.mods.lithium.fabric.mixin.util.inventory_change_listening;

import net.caffeinemc.mods.lithium.common.block.entity.SetBlockStateHandlingBlockEntity;
import net.caffeinemc.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChestBlockEntity.class, priority = 999)
public abstract class ChestBlockEntityMixin extends RandomizableContainerBlockEntity implements InventoryChangeEmitter, SetBlockStateHandlingBlockEntity {
    protected ChestBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void lithium$handleSetBlockState() {
        //Handle switching double / single chest state
        this.lithium$emitRemoved();
    }
}
