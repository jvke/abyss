package org.jvke;

import abyss.plugin.api.game.chat.ChatMessage;
import abyss.plugin.api.game.chat.GameChat;
import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class AutoCycle extends Plugin {
    private static final int BANK = 85341;
    private static final int FORGE = 120048;
    private static final int ANVIL = 113262;

    enum State {
        SMITH,
        BANK,
        IDLE
    }

    public State getState() {
        if (shouldBank()) {
            return State.BANK;
        }

        if (shouldSmith()) {
            return State.SMITH;
        }

        return State.IDLE;
    }

    private boolean shouldBank() {
        return Inventory.isFull() && !hasUnfinished();
    }

    private boolean shouldSmith() {
        return shouldReheat() || !Players.self().isAnimationPlaying();
    }

    private boolean shouldReheat() {
        ChatMessage[] x = GameChat.all();

        for (int i = x.length - 1; i >= 0; i--) {
            ChatMessage m = x[i];
            if (m.getMessage().contains("Your unfinished item is at full heat")) {
                return false;
            }

            if (m.getMessage().contains("Your item has cooled down slightly")) {
                return true;
            }
        }

        return false;
    }

    private void bank() {
        if (!Bank.isOpen()) {
            SceneObject bank = SceneObjects.closest(o -> o.getId() == BANK);

            if (bank != null && !Players.self().isAnimationPlaying()) {
                bank.interact("Use");
            }
        } else if (Bank.isOpen()) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 1, 33882231, -1);
        }
    }

    private boolean hasUnfinished() {
        return Inventory.count(i -> i.getName().contains("Unfinished")) > 0;
    }

    private void clickForge() {
        Actions.menu(Actions.MENU_EXECUTE_OBJECT1, FORGE, 3042, 3333, -1);
    }

    private int getGearLevel() {
        if (Inventory.contains(i -> !i.getName().contains("+") && i.getName().contains("platebody"))) {
            return 0;
        }

        if (Inventory.contains(i -> i.getName().contains("+ 1"))) {
            return 1;
        }

        if (Inventory.contains(i -> i.getName().contains("+ 2"))) {
            return 2;
        }

        return -1;
    }

    private void smith() {
        SceneObject anvil = SceneObjects.closest(o -> o.getId() == ANVIL);

        if (hasUnfinished()) {
            if (shouldReheat()) {
                clickForge();
                sleep(i32(1200, 1800));
                anvil.interact("Smith");
                sleep(i32(1200, 1800));
                return;
            }

            if (anvil != null && !Players.self().isAnimationPlaying()) {
                anvil.interact("Smith");
                sleep(i32(1200, 1800));
            }
        }

        if (!hasUnfinished()) {
            clickForge();
            sleep(i32(1200, 1800));

            // Select bar
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 7, 2424884, -1);
            sleep(i32(100, 200));

            // Select item
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 9, 2424935, -1);

            // Select level
            switch (getGearLevel()) {
                case 0: // click base
                    Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 2424993, -1);
                    break;
                case 1: // click +1
                    Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 2424991, -1);
                    break;
                case 2: // click +2
                    Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 2424981, -1);
                    break;
            }

            sleep(i32(100, 200));

            // Click continue
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 2424995, -1);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Smithing");

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
            case BANK:
                bank();
                break;
            case SMITH:
                smith();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(2800, 3400);
    }

    @Override
    public void onPaint() {
        ImGui.label("Smithing!");
    }
}
