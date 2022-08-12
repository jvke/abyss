package org.jvke;

import kraken.plugin.api.*;

import java.util.function.Supplier;

import static kraken.plugin.api.Rng.i32;

public class RuneSpan extends Plugin {
    //    public static int ESSENCE = 24227, AIR = 24215, EARTH = 24216, WATER = 24214, FIRE = 24213, MIND = 24217, BODY = 24218, CHAOS = 24221,
//            NATURE = 24220, COSMIC = 24223, ASTRAL = 24224, LAW = 24222, BLOOD = 24225, DEATH = 24219, SOUL = 24226;
    private static int WISP = 18161;
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
        return Inventory.isFull() && !Players.self().isAnimationPlaying();
    }

    private boolean shouldFinishHarvest() {
        return Inventory.count(i -> i.getName().contains("memory")) > 0;
    }

    private void harvest() {
        Npc wisp = Npcs.closest(npc -> npc.getId() == WISP);

        if (wisp != null) {
            wisp.interact("Harvest");
        }
    }

    private void convert() {
        SceneObject rift = SceneObjects.closest(obj -> obj.getId() == RIFT);

        if (rift != null) {
            if (rift.interact("Convert memories")) {
                 sleepWhile(() -> !(Inventory.count(i -> i.getName().contains("memory")) > 0), i32(27500, 32500));
            }
        }
    }

    private void sleepWhile(Supplier<Boolean> predicate, int timeout) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            if (predicate.get()) {
                return;
            }

            sleep(100);
        }
    }

    private void sleep(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        Debug.log(s.toString());

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
