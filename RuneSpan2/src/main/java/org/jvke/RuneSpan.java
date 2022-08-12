package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class RuneSpan extends Plugin {
    private static String FLOATING_ESSENCE = "Floating essence";
    private static int[] SIPHON_SPOTS = { 70455, 70456, 70459, 70460, 70461, 70463, 70464, 70465 };
    private static int[] ESS_SPOTS = { 15403, 15406, 15405, 15408, 15409, 15410, 15411, 15412 };
    private static int ESS = 24227;

    private boolean shouldCollectEss = true;

    enum State {
        COLLECT,
        SIPHON,
        IDLE
    }

    private State getState() {
        if (shouldCollect()) {
            return State.COLLECT;
        }

        if (shouldSiphon()) {
            return State.SIPHON;
        }

        return State.IDLE;
    }

    private boolean shouldCollect() {
        return getEssCount() == 0;
    }

    private boolean shouldSiphon() {
        SceneObject spot = SceneObjects.closest(o -> Array.contains(SIPHON_SPOTS, o.getId()));
        Npc essSpot = Npcs.closest(n -> Array.contains(ESS_SPOTS, n.getId()));

        return getEssCount() > 0 && (spot != null || essSpot != null);
    }

    private int getEssCount() {
        return Inventory.count(i -> i.getId() == ESS);
    }

    private void collect() {
        shouldCollectEss = true;
        SceneObject ess = SceneObjects.closest(o -> o.getName().equals(FLOATING_ESSENCE));

        if (ess != null) {
            ess.interact("Collect");
        }
    }

    private void siphonNpc() {
        Npc spot = Npcs.closest(n -> {
            if (Array.contains(ESS_SPOTS, n.getId())) {
                if (n.getGlobalPosition().distance(Players.self().getGlobalPosition()) != -1) {
                    return true;
                }
            }

            return false;
        });

        if (spot != null && !Players.self().isAnimationPlaying() && !Players.self().isMoving()) {
            spot.interact("Siphon");
        }
    }

    private void siphon() {
        if (shouldCollectEss) {
            siphonNpc();
            return;
        }

        SceneObject spot = SceneObjects.closest(o -> {
            if (Array.contains(SIPHON_SPOTS, o.getId())) {
                if (o.getGlobalPosition().distance(Players.self().getGlobalPosition()) != -1) {
                    return true;
                }
            }

            return false;
        });

        if (spot != null) {
            if (!Players.self().isAnimationPlaying() && !Players.self().isMoving()) {
                spot.interact("Siphon");
            }
        } else {
            siphonNpc();
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("RuneSpan");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        State s = getState();
        Debug.log(s.toString());

        if (getEssCount() >= 1000) {
            shouldCollectEss = false;
        }

        switch (s) {
            case COLLECT:
                collect();
                break;
            case SIPHON:
                siphon();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("RuneSpan!");
    }
}
