package org.jvke;

import kraken.plugin.api.*;

import java.util.function.Supplier;

import static kraken.plugin.api.Rng.i32;

public class AutoCycle extends Plugin {
    private static int CYCLE = 105569;

    enum State {
        CYCLE,
        IDLE
    }

    public State getState() {
        if (shouldCycle()) {
            return State.CYCLE;
        }

        return State.IDLE;
    }

    private int getDistance2i(Vector3i a, Vector3i b) {
        int aX = a.getX();
        int aY = a.getY();
        int bX = b.getX();
        int bY = b.getY();

        int diffX = Math.abs(aX - bX);
        int diffY = Math.abs(aY - bY);

        return diffX + diffY;
    }

    private boolean shouldCycle() {
        SceneObject cycle = SceneObjects.closest(o -> o.getId() == CYCLE);

        if (cycle != null) {
            Debug.log(String.valueOf(cycle.getGlobalPosition().distance(Players.self().getGlobalPosition())));
            Debug.log(Players.self().getGlobalPosition().toString());
            Debug.log(cycle.getGlobalPosition().toString());
        }

        return cycle != null && getDistance2i(cycle.getGlobalPosition(), Players.self().getGlobalPosition()) > 2;
    }

    private void cycle() {
        SceneObject cycle = SceneObjects.closest(o -> o.getId() == CYCLE);
        Player p = Players.self();

        if (cycle != null && !p.isMoving() && !p.isAnimationPlaying()) {
            cycle.interact("Pedal");
            sleep(3050);
            cycle.interact("Pedal");
            sleepWhile(() -> shouldCycle(), 6000);
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
        pluginContext.setName("AutoCycle");

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
            case CYCLE:
                cycle();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(1000, 2000);
    }

    @Override
    public void onPaint() {
        ImGui.label("AutoCycle!");
    }
}