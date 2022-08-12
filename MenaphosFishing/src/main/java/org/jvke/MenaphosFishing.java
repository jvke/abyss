package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class MenaphosFishing extends Plugin {
    private static int FISHING_SPOT = 24572;
    private static int BANK = 107489;

    enum State {
        FISH,
        BANK,
        IDLE
    }

    public State getState() {
        if (shouldFish()) {
            return State.FISH;
        }

        if (shouldBank()) {
            return State.BANK;
        }

        return State.IDLE;
    }

    private boolean shouldFish() {
        Player s = Players.self();
        Npc spot = Npcs.closest(n -> n.getId() == FISHING_SPOT);

        if (spot != null && !s.isAnimationPlaying() && !s.isMoving() && !Inventory.isFull()) {
            return true;
        } else {
            return false;
        }
    }

    private void fish() {
        Player s = Players.self();
        Npc spot = Npcs.closest(n -> n.getId() == FISHING_SPOT);

        if (spot != null && !s.isAnimationPlaying() && !s.isMoving() && !Inventory.isFull()) {
            spot.interact("Bait");
        }
    }

    private boolean shouldBank() {
        return Inventory.isFull();
    }

    private void bank() {
        SceneObject b = SceneObjects.closest(o -> o.getId() == BANK);

        if (b != null) {
//            b.interact("Deposit all fish");
            Actions.menu(Actions.MENU_EXECUTE_OBJECT2, 107489, 3217, 2623, -1426063359);
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("MenaphosFishing");

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
            case FISH:
                fish();
                break;
            case BANK:
                bank();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(1500, 5500);
    }

    @Override
    public void onPaint() {
        ImGui.label("Fish!");
    }
}
