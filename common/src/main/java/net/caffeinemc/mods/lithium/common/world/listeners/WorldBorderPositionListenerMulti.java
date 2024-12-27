package net.caffeinemc.mods.lithium.common.world.listeners;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

import java.util.WeakHashMap;

public class WorldBorderPositionListenerMulti implements BorderChangeListener {

    private final WeakHashMap<WorldBorderListenerOnce, Object> delegate;

    public WorldBorderPositionListenerMulti() {
        this.delegate = new WeakHashMap<>();
    }

    public void add(WorldBorderListenerOnce listener) {
        this.delegate.put(listener, null);
    }

    public void onAreaReplaced(WorldBorder border) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onAreaReplaced(border);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSizeSet(WorldBorder border, double size) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSizeSet(border, size);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSizeLerping(WorldBorder border, double fromSize, double toSize, long time) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderSizeLerping(border, fromSize, toSize, time);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderCenterSet(WorldBorder border, double centerX, double centerZ) {
        for (WorldBorderListenerOnce listener : this.delegate.keySet()) {
            listener.onBorderCenterSet(border, centerX, centerZ);
        }
        this.delegate.clear();
    }

    @Override
    public void onBorderSetWarningTime(WorldBorder border, int warningTime) {
    }

    @Override
    public void onBorderSetWarningBlocks(WorldBorder border, int warningBlockDistance) {
    }

    @Override
    public void onBorderSetDamagePerBlock(WorldBorder border, double damagePerBlock) {
    }

    @Override
    public void onBorderSetDamageSafeZOne(WorldBorder border, double safeZoneRadius) {
    }
}
