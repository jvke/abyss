package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class AIOSkiller extends Plugin {
    private static final int MANIFESTED_KNOWLEDGE = 23855;

    enum State {
        SKILL,
        IDLE
    }

    public State getState() {
        if (shouldSkill()) {
            return State.SKILL;
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

    private boolean shouldSkill() {
        return !Players.self().isAnimationPlaying();
    }


    private void skill() {
        if (!Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT1, 94058, 2132, 3338, -1);
            sleep(i32(550,850));
        }
    }

    private boolean clickSpirits() {
        Npc manifest = Npcs.closest(n -> n.getId() == MANIFESTED_KNOWLEDGE);

        if (manifest != null) {
            Debug.log("Siphoning manifested knowledge");
            manifest.interact("Siphon");
            return true;
        }

        return false;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("AIO Skiller");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        if (clickSpirits()) {
            return i32(300, 600);
        }

        State s = getState();
        Debug.log(s.toString());

        switch (s) {
            case SKILL:
                skill();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(600, 6000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Skilling!");
    }
}
