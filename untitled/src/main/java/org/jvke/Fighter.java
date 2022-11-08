package org.jvke;

import kraken.plugin.api.*;

import java.util.Arrays;

import static kraken.plugin.api.Rng.i32;

public class Fighter extends Plugin {
    private String NPC = null;

    enum State {
        FIGHT,
        LOOT,
        IDLE
    }

    private boolean shouldFight() {
        Player self = Players.self();
        self.getInteractingIndex()

        return !(self.isMoving());
    }

    State getState() {
        if (shouldFight()) {
            return State.FIGHT;
        }

        return State.IDLE;
    }

    private void fight() {
        Npc npc = Npcs.closest(
            n ->
                n.getName()
                    .toLowerCase()
                    .contains(NPC.toLowerCase())
                && Arrays.stream(n.getOptionNames())
                    .anyMatch(s -> s.contains("Attack"))
        );

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
        ImGui.label("AutoFighter");

        ImGui.input("Name of NPC:", NPC.getBytes());
    }
}
