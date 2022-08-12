package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class RuneSpan extends Plugin {
    //    public static int ESSENCE = 24227, AIR = 24215, EARTH = 24216, WATER = 24214, FIRE = 24213, MIND = 24217, BODY = 24218, CHAOS = 24221,
//            NATURE = 24220, COSMIC = 24223, ASTRAL = 24224, LAW = 24222, BLOOD = 24225, DEATH = 24219, SOUL = 24226;
    private static int WISP = 18155;
    private static int RIFT = 87306;

    enum State {
        HARVEST,
        CONVERT,
        IDLE
    }

    public State getState() {
        if (shouldHarvest()) {
            return State.HARVEST;
        }

        if (shouldConvert()) {
            return State.CONVERT;
        }

        return State.IDLE;
    }

    private boolean shouldHarvest() {
        return !Inventory.isFull() && !Players.self().isAnimationPlaying();
    }

    private boolean shouldConvert() {
        return Inventory.isFull() && !Players.self().isAnimationPlaying();;
    }

    private boolean shouldFinishHarvest() {
        return Inventory.count(i -> i.getName().contains("memory")) > 0;
    }

    private void harvest() {
        Npc wisp = Npcs.closest(npc -> npc.getId() == WISP);

        if (wisp != null) {
            wisp.interact("Harvest");

            sleepWhile(() ->  Inventory.count(i -> i.getName().contains("memory")) > 0, 20000);
        }
    }

    private void sleepWhile(Runnable runnable, int timeout) {
        long start = System.currentTimeMillis();

        while (runnable.run() && System.currentTimeMillis() - start < timeout) {
            sleep(100);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void convert() {
        SceneObject rift = SceneObjects.closest(obj -> obj.getId() == RIFT);

        if (rift != null) {
            rift.interact("Convert memories");
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Divining");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        State s = getState();

        switch (s) {
            case HARVEST:
                harvest();
                break;
            case CONVERT:
                convert();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Divining!");
    }
}
