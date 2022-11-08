package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class MenaphosFishing extends Plugin {
    private int FISHING_SPOT = 24572;
    private int BANK = 107489;

    private boolean menaphos = true;

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
            spot.interact(menaphos ? "Bait" : "Catch");
        }
    }

    private boolean shouldBank() {
        return Inventory.isFull();
    }

    private void bank() {
        SceneObject b = SceneObjects.closest(o -> o.getId() == BANK);

        if (b != null) {
//            b.interact("Deposit all fish");
            if (menaphos) {
                Actions.menu(Actions.MENU_EXECUTE_OBJECT4, 107489, 3217, 2623, -1426063359);
            } else {
                if (Bank.isOpen()) {
                    Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 1, 33882231, -1);
                } else {
                    if (!Players.self().isMoving()) {
                        b.interact("Use");
                    }
                }
            }
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

        menaphos = ImGui.checkbox("Menaphos", menaphos);
        BANK = ImGui.intInput("Bank ID", BANK);
//        FISHING_SPOT = ImGui.intInput("Fishing Spot ID", FISHING_SPOT);
        FISHING_SPOT = menaphos ? 24572 : 25219;
    }
}
