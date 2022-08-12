package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class Fighter extends Plugin {
    private int NPC = 0;

    enum State {
        FIGHT,
        ADOPT,
        IDLE
    }

    private boolean shouldFight() {
        Player self = Players.self();

        return !(self.isMoving() || self.isUnderAttack() || self.isAnimationPlaying());
    }

    State getState() {
        if (shouldFight()) {
            return State.FIGHT;
        }

        return State.IDLE;
    }

    private void fight() {
        Npc npc = Npcs.closest(n -> n.getId() == NPC);

        if (npc != null) {
            npc.interact("Attack");
        }
    }


    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Auto Fighter");

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
            case FIGHT:
                fight();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Fighting!");

        NPC = ImGui.intInput("NPC ID:", NPC);
    }
}
