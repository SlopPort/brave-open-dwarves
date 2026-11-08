import java.io.File;
import java.util.Random;
import java.util.TreeMap;
import org.braveopendwarves.data.*;

/** Random play on every map, archive media checks and an inventory of unhandled map content. */
public final class FuzzTest {
    static final GameSession.Host HOST = new GameSession.Host() {
        @Override public void prepareLevel(OriginalLevel next) {}
        @Override public void itemCollected(int type) {}
        @Override public void doorSound() {}
    };

    static void valid(GameSession s, String where) {
        check(s.error == null, where + " error " + s.error);
        check(!Float.isNaN(s.x) && !Float.isInfinite(s.x) && !Float.isNaN(s.y) && !Float.isInfinite(s.y) && !Float.isNaN(s.vy), where + " non-finite position");
        check(s.x >= 0 && s.x <= s.level.width * 64 - 32 && s.y >= 0 && s.y <= s.level.height * 64 - 50, where + " outside map " + s.x + "," + s.y);
        check(s.combat.health <= 20, where + " health above maximum " + s.combat.health);
        for (int e : s.combat.energy) check(e >= 0, where + " negative energy");
        for (CombatWorld.Foe f : s.combat.foes) check(!Float.isNaN(f.x) && !Float.isNaN(f.y), where + " non-finite foe");
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

    static void input(GameSession s, Random r) {
        s.left = r.nextInt(4) == 0;
        s.right = r.nextInt(2) == 0;
        s.up = r.nextInt(5) == 0;
        s.down = r.nextInt(5) == 0;
        s.fire = r.nextBoolean();
        s.jumpHeld = r.nextBoolean();
        s.jump = s.jumpHeld;
        if (r.nextInt(8) == 0) s.wizard = !s.wizard;
        if (r.nextInt(6) == 0) s.combat.cycleSpell();
        if (r.nextInt(30) == 0) { int channel = r.nextInt(4); s.combat.abilityLevels[channel] = Math.max(s.combat.abilityLevels[channel], 1 + r.nextInt(3)); s.combat.energy[r.nextInt(4)] = 100; }
    }

    static int fuzz(GogArchive archive, File save, int map, long seed) throws Exception {
        Random r = new Random(seed);
        CombatWorld.seed(seed);
        GameSession s = new GameSession(archive, save, HOST);
        s.load(map);
        int steps = 0;
        for (int i = 0; i < 4000; i++) {
            if (i % 15 == 0) input(s, r);
            String where = "map " + map + " seed " + seed + " step " + i;
            try { s.step(); } catch (RuntimeException e) { throw new AssertionError(where + " crashed", e); }
            steps++;
            valid(s, where);
            if (s.combat.health <= 0) s.load(s.selected);
            if (s.completed) return steps;
            if (i % 700 == 699) {
                s.save();
                GameSession restored = new GameSession(archive, save, HOST);
                try { restored.restore(); } catch (Exception e) { throw new AssertionError(where + " restore failed: " + e.getMessage() + " spell=" + s.combat.selectedSpell, e); }
                check(restored.x == s.x && restored.y == s.y && restored.combat.health == s.combat.health, where + " checkpoint mismatch");
                restored.paused = false;
                s = restored;
            }
        }
        return steps;
    }

    public static void main(String[] args) throws Exception {
        File save = File.createTempFile("fuzz", ".bod");
        long total = 0;
        try (GogArchive archive = new GogArchive(new File(args[0]))) {
            int waves = 0, tracks = 0;
            for (GogArchive.Entry e : archive.entries()) {
                String n = e.name.toLowerCase();
                byte[] b = n.endsWith(".wav") || n.endsWith(".it") ? archive.read(e.name) : null;
                if (n.endsWith(".wav")) { check(new String(b, 0, 4, "ISO-8859-1").equals("RIFF") && new String(b, 8, 4, "ISO-8859-1").equals("WAVE"), e.name + " not WAV"); waves++; }
                if (n.endsWith(".it")) { check(new String(b, 0, 4, "ISO-8859-1").equals("IMPM"), e.name + " not IT"); tracks++; }
            }
            check(waves == 30, "sample count " + waves);
            check(tracks == 7, "module count " + tracks);

            GameSession probe = new GameSession(archive, save, HOST);
            int maps = probe.levels.size();
            TreeMap<Integer, Integer> items = new TreeMap<Integer, Integer>(), foes = new TreeMap<Integer, Integer>();
            for (String name : probe.levels) {
                OriginalLevel l = new OriginalLevel(archive.read(name));
                for (OriginalLevel.Item i : l.items) if (!ItemRules.supported(i.type)) items.merge(i.type, 1, Integer::sum);
                for (OriginalLevel.Enemy e : l.enemies) foes.merge(e.type, 1, Integer::sum);
            }
            System.out.println("unsupported item types (id=count): " + items);
            System.out.println("enemy types on maps (id=count): " + foes);
            for (int map = 0; map < maps; map++) for (long seed = 1; seed <= 4; seed++) total += fuzz(archive, save, map, seed * 1000 + map);
        } finally {
            save.delete();
        }
        System.out.println("PASS fuzz: " + total + " random steps on 55 maps, media headers, checkpoints");
    }
}
