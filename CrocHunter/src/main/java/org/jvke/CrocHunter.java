package org.jvke;

import kraken.plugin.api.*;

import java.util.Arrays;

import static kraken.plugin.api.Rng.i32;

public class CrocHunter extends Plugin {
    static int CROC = 28659;
    static int[] WHIRLIGIGS = {28711, 28712, 28713, 28714, 28715, 28716, 28717, 28718};

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
        if (shouldGrabCroc()) {
            Npc c = Npcs.closest(npc -> npc.getId() == CROC);

            if (c != null) {
                c.interact("Handle");
            }
        }
    }
}
