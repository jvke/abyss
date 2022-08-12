package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class GoebieSkiller extends Plugin {
    private static final int GOEBIE_SUPPLIER = 21393;
    private static final int MANIFESTED_KNOWLEDGE = 23855;

    private static final int SKILLING_SUPPLY_1 = 1513;

    private long lastBought = System.currentTimeMillis();

    enum State {
        SKILL,
        BANK,
        BUY,
        IDLE
    }

    public State getState() {
        if (shouldBank()) {
            return State.BANK;
        }

        if (shouldBuy()) {
            return State.BUY;
        }

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
        return Inventory.count(i -> i.getId() == SKILLING_SUPPLY_1) > 0 && !Players.self().isAnimationPlaying();
    }

    private boolean shouldBuy() {
        return (System.currentTimeMillis() - lastBought) > 60000;
    }

    private boolean shouldBank() {
        boolean buy = shouldBuy();
        return buy ? Inventory.isFull() || Inventory.count(i -> i.getId() == 2436) == 1 : Inventory.count(i -> i.getId() == SKILLING_SUPPLY_1) == 0;
    }

    private void skill() {
        WidgetItem item = Inventory.first(i -> i.getId() == SKILLING_SUPPLY_1);

        if (item != null && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 109445138, -1);
            sleep(i32(550,850));
            Actions.menu(Actions.MENU_EXECUTE_DIALOGUE, 0, -1, 89784350, -1);
        }
    }

    private void bank() {
        boolean buy = shouldBuy();

        if (!Bank.isOpen()) {
            Npc bank = Npcs.closest(n -> n.getId() == GOEBIE_SUPPLIER);

            if (bank != null) {
                bank.interact("Bank");
            }

            return;
        }

        if (Bank.isOpen()) {
            if (buy && Inventory.count(i -> i.getId() == 2436) == 1) {
                // withdraw preset and set new timer
                Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 1, 33882231, -1);
                lastBought = System.currentTimeMillis();
            } else if (buy) {
                Bank.depositAll();
            } else {
                // withdraw preset
                Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, 1, 33882231, -1);
            }
        }
    }

    private void buy() {
        Npc shop = Npcs.closest(n -> n.getId() == GOEBIE_SUPPLIER);

        if (shop != null) {
            shop.interact("Shop");

            sleep(i32(500,1000));

            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 7, 82903060, -1);
            sleep(i32(23,189));
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 6, 82903060, -1);
            sleep(i32(23,189));
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 5, 82903060, -1);
            sleep(i32(23,189));
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 4, 82903060, -1);
            sleep(i32(23,189));
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 3, 82903060, -1);
            sleep(i32(23,189));
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 2, 2, 82903060, -1);
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
        pluginContext.setName("GoebieSkiller");

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
            case BUY:
                buy();
                break;
            case BANK:
                bank();
                break;
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
