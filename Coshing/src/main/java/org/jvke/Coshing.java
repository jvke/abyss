package org.jvke;

import kraken.plugin.api.*;

import java.util.function.Supplier;

import static kraken.plugin.api.Rng.i32;

public class Coshing extends Plugin {
    private static final int[] DOOR_IDS = { 52302, 52304 };
    private static final int[] OPEN_DOOR_IDS = { 52303, 52305 };
    private static final int[] WORLD_LIST = { 1, 2, 4, 5, 6, 9, 10, 12, 14, 15, 16, 18, 21, 22, 23, 24, 25, 26, 27, 28, 30, 31, 32, 35, 36 };

    private int currentWorldIndex = -1;

    enum State {
        OPEN_DOOR,
        HOP_WORLD,
        IDLE
    }

    private State getState() {
        if (shouldOpenDoor()) {
            return State.OPEN_DOOR;
        }

        if (shouldHopWorld()) {
            return State.HOP_WORLD;
        }

        return State.IDLE;
    }

    private boolean shouldOpenDoor() {
        SceneObject door = SceneObjects.closest(obj -> Array.contains(DOOR_IDS, obj.getId()) && !obj.hidden());

        return door != null;
    }

    private boolean shouldHopWorld() {
        return !shouldOpenDoor();
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

    private void hop(int w) {
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 96796768, 1);
        sleep(600);
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 93913152, 1);
        sleep(600);
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, w, 104005640, 1);
        sleep(1800);
    }

    // get next world in list, wrapping to first if at end
    private void hopWorld() {
        currentWorldIndex = (currentWorldIndex + 1) % WORLD_LIST.length;
        hop(WORLD_LIST[currentWorldIndex]);

        sleepWhile(() -> Players.self() != null, 5000);
    }

    private void openDoor() {
        SceneObject[] openDoors = SceneObjects.all(obj -> Array.contains(OPEN_DOOR_IDS, obj.getId()));

        SceneObject door = SceneObjects.closest(obj -> {
            boolean ret = false;
            if (Array.contains(DOOR_IDS, obj.getId()) && !obj.hidden()) {
                ret = true;
                for (SceneObject openDoor : openDoors) {
                    if (obj.getGlobalPosition().distance(openDoor.getGlobalPosition()) <= 1) {
                        ret = false;
                    }
                }
            }

            return ret;
        });

        if (door != null) {
            door.interact("Open");
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Coshing");

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
            case OPEN_DOOR:
                openDoor();
                break;
            case HOP_WORLD:
                hopWorld();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(1000, 1400);
    }

    @Override
    public void onPaint() {
        ImGui.label("Opening doors!");
    }
}
