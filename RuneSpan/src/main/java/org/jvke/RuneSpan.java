package org.jvke;

import kraken.plugin.api.*;

import java.util.function.Supplier;

import static kraken.plugin.api.Rng.i32;

public class RuneSpan extends Plugin {
    private int WISP = 18161;
    private int RIFT = 87306;

    private String ancient = "Ancient automaton";
    private String confused = "Confused automaton";
    private String enraged = "Enraged automaton";

    private String disable = "Disable";

    private boolean shouldCache = true;
    private boolean shouldStayLoggedIn = true;

    enum State {
        HARVEST,
        CONVERT,
        ENTER_CACHE,
        EQUIP_SUIT,
        DISABLE_AUTOMATON,
        IDLE
    }

    public State getState() {
        if (shouldEnterCache()) {
            return State.ENTER_CACHE;
        }

        if (shouldEquipSuit()) {
            return State.EQUIP_SUIT;
        }

        if (shouldDisableAutomaton()) {
            return State.DISABLE_AUTOMATON;
        }

        if (shouldHarvest()) {
            return State.HARVEST;
        }

        if (shouldConvert()) {
            return State.CONVERT;
        }

        return State.IDLE;
    }

    private boolean isInArea(Vector3i ne, Vector3i sw) {
        Player self = Players.self();

        if (self == null) {
            return false;
        }

        Vector3i pos = self.getGlobalPosition();

        return pos.getX() >= sw.getX() && pos.getX() <= ne.getX() && pos.getY() >= sw.getY() && pos.getY() <= ne.getY();
    }

    private boolean isInCache() {
        SceneObject suit = SceneObjects.closest(o -> o.getId() == 93484);
        return suit != null;
    }
    private boolean shouldEnterCache() {
        SceneObject rift = SceneObjects.closest(o -> o.getId() == RIFT);
        SceneObject cacheRift = SceneObjects.closest(o -> o.getId() == 93489);
        return (rift != null && rift.hidden()) || (cacheRift != null && cacheRift.interact("Enter cache"));
    }

    private boolean isSuitEquipped() {
        return Players.self().getEquipment().get(EquipmentSlot.HELMET).getId() == -1;
    }

    private boolean shouldEquipSuit() {
        return isInCache() && !isSuitEquipped();
    }

    private boolean shouldDisableAutomaton() {
        return isInCache() && isSuitEquipped() && !Players.self().isMoving();
    }

    private boolean shouldHarvest() {
        return !Inventory.isFull() && !Players.self().isAnimationPlaying();
    }

    private boolean shouldConvert() {
        return Inventory.isFull() && !Players.self().isAnimationPlaying();
    }

    private boolean shouldFinishHarvest() {
        return Inventory.count(i -> i.getName().contains("memory")) > 0;
    }

    private void enterCache() {
        SceneObject rift = SceneObjects.closest(o -> o.getId() == 93489);
        if (!isInCache() && Players.self().isMoving()) {
            if (!rift.interact("Enter cache")) {
                Actions.menu(Actions.MENU_EXECUTE_OBJECT5, 93489, rift.getGlobalPosition().getX(), rift.getGlobalPosition().getY(), -1);
            }
        }
    }

    private void equipSuit() {
        SceneObject suit = SceneObjects.closest(o -> o.getId() == 93484);
        Player s = Players.self();
        if (suit != null && !s.isMoving() && !s.isAnimationPlaying() && !isSuitEquipped()) {
            suit.interact("Transform");
            sleepWhile(() -> isSuitEquipped(), 10000);
        }
    }

    private void disableAutomaton() {
        Npc automaton = Npcs.closest(n -> n.getId() == 20197 || n.getId() == 20198 || n.getId() == 20193 || n.getId() == 20194);
        Player s = Players.self();
        if (automaton != null && !s.isMoving() && isSuitEquipped()) {
            Actions.menu(Actions.MENU_EXECUTE_WIDGET, 1, -1, 101253156, -1);
            automaton.interact(disable);
        }
    }

    private void harvest() {
        Npc wisp = Npcs.closest(npc -> npc.getId() == WISP);

        if (wisp != null) {
            wisp.interact("Harvest");
        }
    }

    private void convert() {
        SceneObject rift = SceneObjects.closest(obj -> obj.getId() == RIFT);

        if (rift != null) {
            if (rift.interact("Convert memories")) {
                 sleepWhile(() -> !(Inventory.count(i -> i.getName().contains("memory")) > 0), i32(27500, 32500));
            }
        }
    }

    private void sleepWhile(Supplier<Boolean> predicate, int timeout) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            if (predicate.get()) {
                return;
            }

            sleep(100);
        }
    }

    private void stayLoggedIn() {
        int x = i32(1, 4);
        int y = i32(0, 450);
        if (y > 400) {
            switch (x) {
                case 1:
                    Input.key(0x25);
                    break;
                case 2:
                    Input.key(0x26);
                    break;
                case 3:
                    Input.key(0x27);
                    break;
                case 4:
                    Input.key(0x28);
                    break;
            }
        }
    }

    private void sleep(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLoaded(PluginContext pluginContext) {
        pluginContext.setName("Divining");

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

        if (shouldStayLoggedIn && isInCache()) {
            stayLoggedIn();
        }

        switch (s) {
            case HARVEST:
                harvest();
                break;
            case CONVERT:
                convert();
                break;
            case ENTER_CACHE:
                enterCache();
                break;
            case EQUIP_SUIT:
                equipSuit();
                break;
            case DISABLE_AUTOMATON:
                disableAutomaton();
                break;
            case IDLE:
            default:
                break;
        }

        return i32(500, 1000);
    }

    @Override
    public void onPaint() {
        ImGui.label("Divining!");

        RIFT = ImGui.intInput("Rift", RIFT);
        WISP = ImGui.intInput("Wisp", WISP);
        shouldCache = ImGui.checkbox("Cache", shouldCache);
        shouldStayLoggedIn = ImGui.checkbox("Stay logged in (keep this on if doing caches):", shouldStayLoggedIn);
    }
}
