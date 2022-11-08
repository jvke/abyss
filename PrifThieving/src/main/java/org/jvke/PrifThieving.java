package org.jvke;

import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class PrifThieving extends Plugin {
    private int[] ROCKS = {113131,113132,113133};
    private int[] ROCKS2 = {113128, 113130};
    private int[] SHIMMERS = { 7165, 7164 };
    private static final int MANIFESTED_KNOWLEDGE = 23855;
    private static final int BANK = 113259;
    private static Vector3i bankTile = new Vector3i(3171, 3412, 0);

    private boolean mineOre1 = true;

    private int ore = 44824;
    private int ore2 = 44822;


    enum State {
        MINE,
        BANK,
        IDLE
    }

    public State getState() {
        if (shouldMine()) {
            return State.MINE;
        }

        if (shouldBank()) {
            return State.BANK;
        }

        return State.MINE;
    }

    private boolean shouldMine() {
        Player s = Players.self();

        if (!s.isMoving() && !Inventory.isFull()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldBank() {
        return Inventory.isFull();
    }

    private boolean isAtSameLocation(Vector3i a, Vector3i b) {
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    private void bank() {
        Player s = Players.self();
        SceneObject b = SceneObjects.closest(o -> o.getId() == BANK);

        if (b != null) {
            if (!s.isMoving() && Inventory.isFull()) {
                b.interact("Deposit-all (into metal bank)");
            }
        } else {
            if (!s.isMoving()) Move.to(bankTile);
        }
    }

    private void mine() {
        Player s = Players.self();
        Effect sprite = Effects.closest(eff -> eff.getId() == SHIMMERS[0] || eff.getId() == SHIMMERS[1]);

        if (sprite != null) {
            Debug.log("Sprite found!");
            SceneObject spot = SceneObjects.closest(obj -> {

                if (mineOre1 && (obj.getId() == ROCKS[0] || obj.getId() == ROCKS[1] || obj.getId() == ROCKS[2] && !obj.hidden())) {
                    return isAtSameLocation(obj.getGlobalPosition(), sprite.getGlobalPosition());
                }

                if (!mineOre1 && (obj.getId() == ROCKS2[0] || obj.getId() == ROCKS2[1] && !obj.hidden())) {
                    return isAtSameLocation(obj.getGlobalPosition(), sprite.getGlobalPosition());
                }

                return false;
            });

            if (spot != null) {
                spot.interact("Mine");
            }
        } else {
            SceneObject spot;
            if (mineOre1) {
                spot = SceneObjects.closest(o -> o.getId() == ROCKS[0] || o.getId() == ROCKS[1] || o.getId() == ROCKS[2] && !o.hidden());
            } else {
                spot = SceneObjects.closest(o -> o.getId() == ROCKS2[0] || o.getId() == ROCKS2[1] && !o.hidden());
            }

            if (spot != null && !Inventory.isFull()) {
                spot.interact("Mine");
            }

//            if (spot == null) {
//                if (!s.isMoving()) Move.to(bankTile);
//            }
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

    private void equipPorter() {
        Player s = Players.self();
        s.getEquipment().forEach((i, k) -> {
            Debug.log(i.toString() + " : " + k.toString());
        });
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Mining");

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

        int c = Inventory.count(i -> i.getId() == ore);

        if (s == State.MINE && c >= 0 && c <= 5) {
            mineOre1 = true;
            if (c > 0) {
                Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 93716544, 1);
            }
        }

        if (c > 6) {
            mineOre1 = false;

            int c2 = Inventory.count(i -> i.getId() == ore2);

            if (s == State.MINE && c2 > 0 && c2 <= 5) {
                Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 93716544, 1);
            }
        }

        Debug.log(s.toString());

        switch (s) {
            case MINE:
                mine();
                break;
            case BANK:
            case IDLE:
            default:
                break;
        }

        return i32(1800, 2400);
    }

    @Override
    public void onPaint() {
        ImGui.label("Mine!");
    }
}
