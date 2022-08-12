package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class PrifThieving extends Plugin {
    private static final int[] ROCKS = {113204, 113203};
    private static final int SHIMMER = 7164;
    private static final int MANIFESTED_KNOWLEDGE = 23855;

    enum State {
        MINE,
        IDLE
    }

    public State getState() {

        if (shouldMine()) {
            return State.MINE;
        }

        return State.IDLE;
    }

    private boolean shouldMine() {
        Player s = Players.self();
        SceneObject spot = SceneObjects.closest(o -> o.getId() == ROCKS[0] || o.getId() == ROCKS[1] && !o.hidden());

        if (spot != null && !s.isMoving() && !Inventory.isFull()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAtSameLocation(Vector3i a, Vector3i b) {
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    private void mine() {
        Player s = Players.self();
        Effect sprite = Effects.closest(eff -> eff.getId() == SHIMMER);

        if (sprite != null) {
            Debug.log("Sprite found!");
            SceneObject spot = SceneObjects.closest(obj -> {
                if (obj.getId() == ROCKS[0] || obj.getId() == ROCKS[1] && !obj.hidden()) {
                    return isAtSameLocation(obj.getGlobalPosition(), sprite.getGlobalPosition());
                }
                return false;
            });

            if (spot != null) {
                spot.interact("Mine");
            }
        } else {
            SceneObject spot = SceneObjects.closest(o -> o.getId() == ROCKS[0] || o.getId() == ROCKS[1] && !o.hidden());
            if (spot != null && !s.isAnimationPlaying() && !s.isMoving() && !Inventory.isFull()) {
                spot.interact("Mine");
            }
        }
    }

    private boolean clickSpirits() {
        Npc manifest = Npcs.closest(n -> n.getId() == MANIFESTED_KNOWLEDGE);

        if (manifest != null) {
            Debug.log("Siphoning manifested knowledge");
            manifest.interact("Siphon");
            return true;
        }

        return false;
    }

    private void equipPorter() {
        Player s = Players.self();
        s.getEquipment().forEach((i, k) -> {
            Debug.log(i.toString() + " : " + k.toString());
        });
        s.
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Mining");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        if (clickSpirits()) {
            return i32(300, 600);
        }
        equipPorter();

        State s = getState();
        Debug.log(s.toString());

        switch (s) {
            case MINE:
                mine();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(600, 6000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Mine!");
    }
}
