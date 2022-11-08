package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class MinerCutter extends Plugin {
    private static final int GEM_ROCK = 23855;
    private static final int[] GEMS = { 1623, 1621, 1619 };
    private static final int[] CUT_GEMS = { 1607, 1605, 1603 };

    enum State {
        MINE,
        CUT,
        IDLE
    }

    public State getState() {
        if (shouldDrop()) {
            return State.DROP;
        }

        if (shouldMine()) {
            return State.MINE;
        }

        if (shouldCut()) {
            return State.CUT;
        }

        return State.IDLE;
    }

    private void sleep(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldMine() {
        return !Inventory.isFull() && !Players.self().isAnimationPlaying();
    }

    private boolean shouldCut() {
        return Inventory.isFull() && !Players.self().isAnimationPlaying() && getCountOfGems() > 0;
    }

    private boolean shouldDrop() {
        return Inventory.isFull() && !Players.self().isAnimationPlaying() && getCountOfGems() == 0;
    }

    private int getCountOfGems() {
        return Inventory.count(i -> i.getId() == GEMS[0] || i.getId() == GEMS[1] || i.getId() == GEMS[2]);
    }

    private void mine() {
        SceneObject spot = SceneObjects.closest(o -> o.getId() == ROCK);

        if (spot != null && !Players.self().isAnimationPlaying()) {
            spot.interact("Mine");
        }
    }

    private void cut() {
        
    }


    private void drop() {

    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("MinerCutter");

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
            case MINE:
                mine();
                break;
            case CUT:
                cut();
                break;
            case DROP:
                drop();
                break;
            default:
                break;
        }

        if (s == State.DROP) {
            return i32(100, 200);
        }

        return i32(300, 1200);
    }

    @Override
    public void onPaint() {
        ImGui.label("Skilling!");
    }
}
