package org.jvke;

import kraken.plugin.api.*;

import java.util.Arrays;

import static kraken.plugin.api.Rng.i32;

public class CrocHunter extends Plugin {
    static int CROC = 28661;
    static int[] WHIRLIGIGS = {28711, 28712, 28713, 28714, 28715, 28716, 28717, 28718, 28729};
    static int BASKET1 = 122490;
    static int BASKET2 = 122489;

    private enum States {
        GRAB_CROC,
        HUNT,
        IDLE
    }

    private boolean shouldGrabCroc() {
        Npc c = Npcs.closest(npc -> npc.getId() == CROC);

        return (c != null && Arrays.stream(c.getOptionNames()).anyMatch(name -> name.equals("Handle")));
    }

    private boolean shouldHunt() {
        return true;
    }

    public States getState() {
        if (shouldGrabCroc()) {
            return States.GRAB_CROC;
        }

        if (shouldHunt()) {
            return States.HUNT;
        }

        return States.IDLE;
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("CrocHunter");

        return super.onLoaded(pluginContext);
    }

    @Override
    public int onLoop() {
        Debug.log("Loop");
        Player self = Players.self();

        if (self == null) {
            return 600;
        }

        States s = getState();

        switch (s) {
            case GRAB_CROC:
                grabCroc();
                break;
            case HUNT:
                try {
                    hunt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case IDLE:
            default:
                break;
        }

        return 0;
    }

    @Override
    public void onPaint() {
        ImGui.label("Fishing!");
    }

    private void hunt() throws InterruptedException {
        Debug.log("Hunting");
        Player s = Players.self();

        Npcs.forEach(npc -> {
            if (npc.getName().contains("whirligig")) {
                npc.interact("Catch");
                try {
                    Thread.sleep(i32(440, 720));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void grabCroc() {
        if (Players.self().isAnimationPlaying() || Players.self().isMoving()) return;

        SceneObject b1 = SceneObjects.closest(o -> o.getId() == BASKET1);
        SceneObject b2 = SceneObjects.closest(o -> o.getId() == BASKET2);

        if (b1 != null) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT4, b1.getId(), b1.getGlobalPosition().getX(), b1.getGlobalPosition().getY(), -1);
            sleep(i32(6600, 8800));
        }

        if (b2 != null) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT4, b2.getId(), b2.getGlobalPosition().getX(), b2.getGlobalPosition().getY(), -1);
            sleep(i32(6600, 8800));
        }

        Npc c = Npcs.closest(npc -> npc.getId() == CROC);

        if (c != null) {
            c.interact("Handle");
            sleep(i32(6600, 8800));
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
