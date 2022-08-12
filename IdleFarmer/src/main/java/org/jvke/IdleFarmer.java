package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class IdleFarmer extends Plugin {
    private static int[] CULTIVATE_BUSH_IDS = { 122508, 122509, 122510 };
    private static int HARVEST_BUSH_ID = 122511;
    private static int SCARAB = 28671;

    enum State {
        SHOO_SCARAB,
        CULTIVATE,
        HARVEST,
        IDLE
    }

    private State getState() {
        if (shouldShooScarab()) {
            return State.SHOO_SCARAB;
        }

        if (shouldCultivate()) {
            return State.CULTIVATE;
        }

        if (shouldHarvest()) {
            return State.HARVEST;
        }

        return State.IDLE;
    }

    private boolean shouldShooScarab() {
        Npc scarab = Npcs.closest(npc -> npc.getId() == SCARAB);

        return scarab != null;
    }

    private boolean shouldCultivate() {
        SceneObject bush = SceneObjects.closest(obj -> Array.contains(CULTIVATE_BUSH_IDS, obj.getId()));

        return bush != null && !Players.self().isAnimationPlaying();
    }

    private boolean shouldHarvest() {
        SceneObject bush = SceneObjects.closest(obj -> obj.getId() == HARVEST_BUSH_ID);

        return bush != null;
    }

    private void shooScarab() {
        Npc scarab = Npcs.closest(npc -> npc.getId() == SCARAB);

        if (scarab != null) {
            scarab.interact("Shoo");
        }
    }

    private void cultivate() {
        SceneObject bush = SceneObjects.closest(obj -> Array.contains(CULTIVATE_BUSH_IDS, obj.getId()));

        if (bush != null) {
            bush.interact("Cultivate");
        }
    }

    private void harvest() {
        SceneObject bush = SceneObjects.closest(obj -> obj.getId() == HARVEST_BUSH_ID);

        if (bush != null) {
            bush.interact("Harvest");
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("IdleFarmer");

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

        switch (s) {
            case SHOO_SCARAB:
                shooScarab();
                break;
            case HARVEST:
                harvest();
                break;
            case CULTIVATE:
                cultivate();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Farming!");
    }
}
