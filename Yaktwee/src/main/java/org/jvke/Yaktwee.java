package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class Yaktwee extends Plugin {
    private static int BUSH_NPC = 28241;
    private static int CANNON = 34278;

    enum State {
        IDLE
    }

    private State getState() {
        return State.IDLE;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Yaktwee");

        return super.onLoaded(pluginContext);
    }

    private void resonance() {
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 109445268, 1);
    }

    private void equipMainhand() {
        if (Inventory.contains(i -> i.getId() == 46322)) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, -1, 109445242, 1);
        }
    }

    private void equipShield() {
        if (Inventory.contains(i -> i.getId() == 1540)) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, -1, 109445255, 1);
        }
    }

    private void log() {
        Npc glacor = Npcs.closest(npc -> npc.getId() == BUSH_NPC);

        if (glacor != null) {
            if (glacor.getAnimationId() == CANNON) {
                equipShield();
                resonance();
            } else {
                equipMainhand();
            }
        }
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
            case IDLE:
                log();
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Yaktwee!");
    }
}
