package org.jvke;

import javafx.scene.Scene;
import kraken.plugin.api.*;

import static kraken.plugin.api.Rng.i32;

public class Archeology extends Plugin {
    private static final int SPRITE = 7307;

    private int DIRT = 49523;
    private int SPOT = 117100;
    private int DEPOSIT_CART = 116295;
    private int DEPOSIT_CART2 = 117403;
    private boolean USE_MATERIAL_CART = true;
    private boolean USE_BANK = true;
    private int BANK = 115427;

    public enum State {
        DIG,
        DIG_AT_SPRITE,
        DROP,
        DEPOSIT,
        IDLE
    }

    public State getState() {
        if (shouldDrop()) {
            return State.DROP;
        }

        if (shouldDeposit()) {
            return State.DEPOSIT;
        }

        if (shouldDigAtSprite()) {
            return State.DIG_AT_SPRITE;
        }

        if (shouldDig()) {
            return State.DIG;
        }

        return State.IDLE;
    }

    private boolean shouldDigAtSprite() {
        Effect sprite = Effects.closest(eff -> eff.getId() == SPRITE);
        if (sprite == null) return false;

        int distance = sprite.getGlobalPosition().distance(Players.self().getGlobalPosition());

        return sprite != null && distance > 1;
    }

    private boolean shouldDig() {
        Player self = Players.self();

        SceneObject spot = SceneObjects.closest(obj -> obj.getId() == SPOT);

        return !Inventory.isFull() && spot != null;
    }


    private boolean shouldDrop() {
        return Inventory.contains(i -> i.getId() == DIRT);
    }

    private boolean shouldDeposit() {
        return Inventory.isFull();
    }

    private void dig() {
        SceneObject spot = SceneObjects.closest(obj -> obj.getId() == SPOT && !obj.hidden());

        if (spot != null && !Players.self().isAnimationPlaying()) {
            Actions.menu(Actions.MENU_EXECUTE_OBJECT1, SPOT, spot.getGlobalPosition().getX(), spot.getGlobalPosition().getY(), -1);
        }
    }

    private boolean isAtSameLocation(Vector3i a, Vector3i b) {
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    private void digAtSprite() {
        Effect sprite = Effects.closest(eff -> eff.getId() == SPRITE);

        if (sprite != null) {
            Debug.log("Sprite found!");
            SceneObject spot = SceneObjects.closest(obj -> {
                if (obj.getId() == SPOT && !obj.hidden()) {
                    Debug.log(isAtSameLocation(obj.getGlobalPosition(), sprite.getGlobalPosition()) ? "Not at same location" : "Found sprite at same location");
                    return isAtSameLocation(obj.getGlobalPosition(), sprite.getGlobalPosition());
                }
                return false;
            });

            if (spot != null) {
//                spot.interact("Excavate");
                Actions.menu(Actions.MENU_EXECUTE_OBJECT1, SPOT, spot.getGlobalPosition().getX(), spot.getGlobalPosition().getY(), -1);
            }
        }
    }

    private void drop() {
        Actions.menu(Actions.MENU_EXECUTE_WIDGET, 8, -1, 93716544, 1);
    }

    private void deposit() {
        if (USE_BANK) {
            bank();
            return;
        }

        SceneObject cart = SceneObjects.closest(o -> o.getId() == (USE_MATERIAL_CART ? DEPOSIT_CART : DEPOSIT_CART2));

        if (cart != null) {
            cart.interact(USE_MATERIAL_CART ? "Deposit materials" : "Deposit all");
        } else {
            if (!Players.self().isMoving()) {
                Move.to(new Vector3i(3779, 3217, 0));
            }
        }
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

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Archeology");

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

        if (Inventory.count(i -> i.getName().contains("tome")) > 0 && USE_BANK) {
            s = State.IDLE;
        }

        switch (s) {
            case DROP:
                drop();
                break;
            case DIG_AT_SPRITE:
                digAtSprite();
                break;
            case DIG:
                dig();
                break;
            case DEPOSIT:
                deposit();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(1500, 5500);
    }

    @Override
    public void onPaint() {
        ImGui.label("Arch!");
        SPOT = ImGui.intInput("Spot ID", SPOT);
        DIRT = ImGui.intInput("Dirt ID", DIRT);
        USE_MATERIAL_CART = ImGui.checkbox("Use material cart", USE_MATERIAL_CART);
        USE_BANK = ImGui.checkbox("Use bank", USE_BANK);

        if (USE_MATERIAL_CART) {
            DEPOSIT_CART = ImGui.intInput("Material cart ID", DEPOSIT_CART);
        }
    }
}
