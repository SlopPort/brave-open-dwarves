package org.braveopendwarves.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/** Android-free game state and rules. Advances in fixed steps at the nominal original rate. */
public final class GameSession {
    /** Platform services needed by the rules. */
    public interface Host {
        /** Prepares resources for a level before it becomes current; a failure leaves the old level active. */
        void prepareLevel(OriginalLevel next) throws IOException;
        void itemCollected(int type);
        void doorSound();
    }

    public static final float STEP = 1f / OriginalClock.HZ;
    private static final float MAX_FRAME = .05f;
    /** Pixels per original step; jump height is a pixel budget set at 0x40a920. */
    public static final int WALK_SPEED = 3, AIR_SPEED = 4, WATER_SPEED = 2, CLIMB_SPEED = 2, JUMP_HEIGHT = 120;
    private float rise;
    /** Jump button currently down; releasing it ends the rise. */
    public boolean jumpHeld;

    private final GogArchive archive;
    private final File saveFile;
    private final Host host;
    private float accumulator;

    public final ArrayList<String> levels = new ArrayList<String>();
    public OriginalLevel level;
    public CombatWorld combat;
    public LiftWorld lifts;
    public ExitDoor door;
    public EnvironmentWorld environment;
    public int selected, keys, treasure, facing = 1, bankedScore;
    public boolean wizard = true, ground, paused, overview, completed;
    public boolean left, right, up, down, jump, fire, climbing;
    public float x, y, vy, time;
    public String error, levelDigest;

    public GameSession(GogArchive archive, File saveFile, Host host) throws IOException {
        this.archive = archive;
        this.saveFile = saveFile;
        this.host = host;
        for (GogArchive.Entry entry : archive.entries())
            if (entry.name.toLowerCase(Locale.ROOT).matches("data/levels/level[0-9]{4}\\.dat")) levels.add(entry.name);
        Collections.sort(levels);
        if (levels.isEmpty()) throw new IOException("Archiwum nie zawiera poziomow");
    }

    public int totalScore() { return bankedScore + ItemRules.score(level); }

    public void resetInput() { left = right = up = down = jump = jumpHeld = fire = false; }

    public void load(int index) {
        try {
            byte[] bytes = archive.read(levels.get(index));
            OriginalLevel next = new OriginalLevel(bytes);
            host.prepareLevel(next);
            level = next;
            levelDigest = RunState.digest(bytes);
            combat = new CombatWorld(next);
            lifts = new LiftWorld(next);
            selected = index;
            bankedScore = 0;
            completed = false;
            environment = new EnvironmentWorld(next);
            door = new ExitDoor(next);
            climbing = false;
            x = level.spawnX * 64 + 16;
            y = level.spawnY * 64;
            vy = 0;
            keys = treasure = 0;
            paused = false;
            error = null;
            accumulator = 0;
            resetInput();
        } catch (IOException e) {
            error = e.getMessage();
            paused = true;
        }
    }

    public void advance() {
        int number = Integer.parseInt(levels.get(selected).replaceAll("[^0-9]", ""));
        if (number == 50 || number >= 1000) { completed = true; paused = true; resetInput(); save(); return; }
        String nextName = String.format(Locale.ROOT, "Data/Levels/Level%04d.dat", number + 1);
        int next = levels.indexOf(nextName);
        if (next < 0) { error = "Brak nastepnego poziomu"; paused = true; return; }
        int score = totalScore(), health = combat.health, spell = combat.selectedSpell;
        int[] energy = combat.energy.clone(), abilities = combat.abilityLevels.clone();
        boolean character = wizard;
        load(next);
        if (error == null) {
            bankedScore = score;
            combat.health = health;
            System.arraycopy(energy, 0, combat.energy, 0, 4);
            System.arraycopy(abilities, 0, combat.abilityLevels, 0, 4);
            combat.selectedSpell = spell;
            wizard = character;
            combat.resetChapterEnergy(number + 1, wizard);
            save();
        }
    }

    public void save() {
        if (level == null || combat.health <= 0) return;
        RunState state = new RunState();
        state.levelName = levels.get(selected);
        state.digest = levelDigest;
        state.x = x;
        state.y = y;
        state.health = combat.health;
        state.keys = keys;
        state.treasure = treasure;
        state.kills = combat.kills;
        state.facing = facing;
        state.wizard = wizard;
        state.taken = new boolean[level.items.length];
        for (int i = 0; i < state.taken.length; i++) state.taken[i] = level.items[i].taken;
        state.abilityLevels = combat.abilityLevels.clone();
        state.selectedSpell = combat.selectedSpell;
        state.energyClock = combat.energyClock;
        state.energy = combat.energy.clone();
        state.captureFoes(combat);
        state.captureLifts(lifts);
        state.doorProgress = door.progress;
        state.environment = environment.snapshot();
        state.immunity = combat.immunity;
        state.webbed = combat.webbed;
        state.bankedScore = bankedScore;
        state.completed = completed;
        try { state.write(saveFile); } catch (IOException e) { error = "Nie udalo sie zapisac: " + e.getMessage(); }
    }

    public void restore() throws IOException {
        RunState state = RunState.read(saveFile);
        int index = levels.indexOf(state.levelName);
        if (index < 0) throw new IOException("Brak zapisanej mapy");
        load(index);
        if (error != null) throw new IOException(error);
        state.validate(levels.get(selected), levelDigest, level, combat);
        state.restoreLifts(lifts);
        x = state.x;
        y = state.y;
        vy = 0;
        facing = state.facing;
        wizard = state.wizard;
        keys = state.keys;
        treasure = state.treasure;
        combat.health = state.health;
        combat.kills = state.kills;
        combat.immunity = state.immunity;
        combat.webbed = state.webbed;
        door.progress = state.doorProgress;
        environment.restore(state.environment);
        bankedScore = state.bankedScore;
        completed = state.completed;
        climbing = level.ladder((int) (x + 16) / 64, (int) (y + 25) / 64);
        for (int i = 0; i < state.taken.length; i++) level.items[i].taken = state.taken[i];
        state.restoreFoes(combat);
        System.arraycopy(state.energy, 0, combat.energy, 0, 4);
        combat.energyClock = state.energyClock;
        System.arraycopy(state.abilityLevels, 0, combat.abilityLevels, 0, 4);
        combat.selectedSpell = state.selectedSpell;
        paused = true;
    }

    /** Consumes real elapsed time; long frames are capped so a stall cannot cause a burst of steps. */
    public void tick(float elapsed) {
        accumulator += Math.min(MAX_FRAME, elapsed);
        while (accumulator >= STEP) {
            accumulator -= STEP;
            step();
        }
    }

    private boolean solid(int c, int r, boolean climb) { return level.solid(c, r) && !(climb && level.ladder(c, r)); }

    /** Runs one fixed simulation step. */
    public void step() {
        float dt = STEP;
        if (paused || overview || completed || level == null) return;
        time += dt;
        if (combat.health <= 0) { resetInput(); return; }
        LiftWorld.Lift passenger = null;
        if (vy >= 0) for (LiftWorld.Lift lift : lifts.lifts) if (lift.supports(x, y + 50)) { passenger = lift; break; }
        lifts.update(dt);
        if (passenger != null) {
            float carriedX = x + passenger.x - passenger.previousX, carriedY = y + passenger.y - passenger.previousY;
            // Stop carrying at a ceiling or wall; full original crush handling is still unknown.
            if (!level.solid((int) (carriedX + 2) / 64, (int) (carriedY + 2) / 64) && !level.solid((int) (carriedX + 29) / 64, (int) (carriedY + 2) / 64)) { x = carriedX; y = carriedY; ground = true; }
        }
        if (combat.webbed <= 0) move(dt); else { vy = 0; jump = false; }
        combat.regenerateEnergy(dt, wizard);
        if (fire) combat.attack(x, y, facing, wizard);
        combat.viewportX = Math.max(0, Math.min(level.width * 64 - 960, x - 450));
        combat.viewportY = Math.max(0, Math.min(level.height * 64 - 420, y - 180));
        combat.viewportWidth = 960;
        combat.update(dt, x, y);
        environment.update(dt, x, y, combat);
        if (combat.health <= 0) { resetInput(); return; }
        collectItems();
        boolean bossPresent = false, bossAlive = false;
        for (OriginalLevel.Enemy enemy : level.enemies) if (enemy.type >= 8 && enemy.type <= 12) bossPresent = true;
        for (CombatWorld.Foe foe : combat.foes) if (foe.hp > 0) bossAlive = true;
        if (bossPresent && !bossAlive && door.progress < 0) { door.unlock(); host.doorSound(); }
        if (door.update(dt, keys, x + 16, y + 49, ground)) advance();
        else if (door.started) host.doorSound();
    }

    private void move(float dt) {
        boolean onLadder = level.ladder((int) (x + 16) / 64, (int) (y + 25) / 64) || level.ladder((int) (x + 16) / 64, (int) (y + 51) / 64);
        boolean climb = onLadder && (up || down || climbing);
        // Snap to the ladder column when grabbing it (0x40a87d).
        if (climb && !climbing) x = (int) (x + 16) / 64 * 64 + 16;
        climbing = climb;
        // Original movement is constant pixels per step: 0x40a6ed walk, 0x40aa56 rise, 0x40ae12 fall.
        boolean water = environment.water;
        float walk = (water ? WATER_SPEED : WALK_SPEED) * OriginalClock.HZ, vertical = (water ? WATER_SPEED : AIR_SPEED) * OriginalClock.HZ;
        float vx = right ? walk : left ? -walk : 0;
        if (vx != 0) facing = vx > 0 ? 1 : -1;
        if (jump && (ground || climb)) { rise = JUMP_HEIGHT; ground = false; climb = climbing = false; }
        jump = false;
        if (!jumpHeld) rise = 0;
        float nx = x + vx * dt;
        if (vx > 0) { int c = (int) ((nx + 31) / 64); for (int r = (int) (y + 2) / 64; r <= (int) (y + 49) / 64; r++) if (solid(c, r, climb)) { nx = c * 64 - 32; break; } }
        if (vx < 0) { int c = (int) Math.floor(nx / 64); for (int r = (int) (y + 2) / 64; r <= (int) (y + 49) / 64; r++) if (solid(c, r, climb)) { nx = (c + 1) * 64; break; } }
        x = Math.max(0, Math.min(level.width * 64 - 32, nx));
        if (climb) vy = ((down ? CLIMB_SPEED : 0) - (up ? CLIMB_SPEED : 0)) * OriginalClock.HZ;
        else if (rise > 0) { vy = -vertical; rise -= vertical * dt; }
        else vy = vertical;
        float ny = y + vy * dt;
        ground = false;
        if (vy >= 0) { int r = (int) ((ny + 50) / 64); for (int c = (int) (x + 2) / 64; c <= (int) (x + 29) / 64; c++) if (solid(c, r, climb)) { ny = r * 64 - 50; vy = 0; ground = true; break; } }
        else { int r = (int) Math.floor(ny / 64); for (int c = (int) (x + 2) / 64; c <= (int) (x + 29) / 64; c++) if (solid(c, r, climb)) { ny = (r + 1) * 64; vy = 0; rise = 0; break; } }
        if (vy >= 0) for (LiftWorld.Lift lift : lifts.lifts) {
            float floor = lift.floor();
            if (x + 29 > lift.x - lift.halfWidth() && x + 2 < lift.x + lift.halfWidth() && y + 50 <= floor + 3 && ny + 50 >= floor && floor - 50 <= ny) { ny = floor - 50; vy = 0; ground = true; }
        }
        y = Math.max(0, Math.min(level.height * 64 - 50, ny));
    }

    private void collectItems() {
        for (OriginalLevel.Item item : level.items) {
            if (item.taken || Math.abs(item.x - x - 16) >= 25 || Math.abs(item.y - y - 25) >= 35 || !ItemRules.supported(item.type)) continue;
            item.taken = true;
            treasure++;
            if (item.type >= 41 && item.type <= 43) keys |= 1 << (item.type - 41);
            combat.collectPowerOrb(item.type);
            combat.replenishEnergy(item.type);
            combat.health = Math.min(20, combat.health + ItemRules.healing(item.type));
            if (ItemRules.healing(item.type) > 0) environment.remainder = 0;
            if (ItemRules.protectionTicks(item.type) > 0) combat.immunity = Math.max(combat.immunity, OriginalClock.seconds(ItemRules.protectionTicks(item.type)));
            host.itemCollected(item.type);
        }
    }
}
