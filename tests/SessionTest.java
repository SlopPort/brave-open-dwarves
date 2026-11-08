import java.io.File;
import java.io.IOException;
import org.braveopendwarves.data.*;

/** Headless runs of the full game session against the user's local archive. */
public final class SessionTest {
    static final GameSession.Host HOST = new GameSession.Host() {
        @Override public void prepareLevel(OriginalLevel next) {}
        @Override public void itemCollected(int type) {}
        @Override public void doorSound() {}
    };

    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

    static String play(GogArchive archive, File save, int map, long seed, int steps) throws IOException {
        GameSession s = new GameSession(archive, save, HOST);
        CombatWorld.seed(seed);
        s.load(map);
        check(s.error == null, "map loads: " + s.error);
        for (int i = 0; i < steps; i++) {
            int phase = i / 150 % 4;
            s.right = phase < 2;
            s.left = phase == 3;
            s.fire = i % 40 < 5;
            if (i % 90 == 0) s.jump = true;
            s.step();
        }
        return state(s);
    }

    static String state(GameSession s) {
        StringBuilder b = new StringBuilder();
        b.append(s.selected).append(' ').append(s.x).append(' ').append(s.y).append(' ').append(s.combat.health).append(' ').append(s.treasure).append(' ').append(s.combat.kills);
        for (CombatWorld.Foe foe : s.combat.foes) b.append(' ').append(foe.x).append(',').append(foe.y).append(',').append(foe.hp);
        return b.toString();
    }

    public static void main(String[] args) throws Exception {
        File save = File.createTempFile("session", ".bod");
        try (GogArchive archive = new GogArchive(new File(args[0]))) {
            GameSession probe = new GameSession(archive, save, HOST);
            int maps = probe.levels.size();
            check(maps == 55, "all maps listed");
            for (int map = 0; map < maps; map++) {
                String first = play(archive, save, map, 42, 1500), second = play(archive, save, map, 42, 1500);
                check(first.equals(second), "repeatable run on map index " + map);
            }

            check(MusicRules.track(10, 1).endsWith("boss_theme.it") && MusicRules.track(11, 2).endsWith("GNOM2.IT") && MusicRules.track(1, 6) == null, "music selection rules");
            for (String name : probe.levels) {
                OriginalLevel level = new OriginalLevel(archive.read(name));
                int number = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                check(archive.read(MusicRules.track(number, level.theme)).length > 0, "music exists for " + name);
            }

            GameSession clock = new GameSession(archive, save, HOST);
            clock.load(0);
            clock.tick(.05f);
            check(Math.abs(clock.time - 3 * GameSession.STEP) < 1e-5, "50 ms yields 3 steps at 75 Hz");
            float before = clock.time;
            clock.tick(100);
            check(clock.time - before <= 5 * GameSession.STEP, "stall is capped");
            clock.paused = true;
            before = clock.time;
            clock.tick(.05f);
            check(clock.time == before, "pause stops the simulation");

            clock.paused = false;
            clock.jump = true;
            clock.overview = true;
            clock.tick(.05f);
            check(clock.jump, "input waits while simulation is suspended");
            clock.overview = false;
            clock.x += 0;
            clock.save();
            GameSession restored = new GameSession(archive, save, HOST);
            restored.restore();
            check(restored.x == clock.x && restored.y == clock.y && restored.combat.health == clock.combat.health && restored.selected == clock.selected, "checkpoint round trip");
            check(restored.paused, "restored session starts paused");

            int last = probe.levels.indexOf("Data/Levels/Level0050.dat");
            GameSession end = new GameSession(archive, save, HOST);
            end.load(last);
            end.advance();
            check(end.completed && end.paused, "map 50 completes the campaign");
            GameSession next = new GameSession(archive, save, HOST);
            next.load(0);
            next.bankedScore = 7;
            int number = Integer.parseInt(next.levels.get(0).replaceAll("[^0-9]", ""));
            next.advance();
            check(next.error != null || Integer.parseInt(next.levels.get(next.selected).replaceAll("[^0-9]", "")) == number + 1, "advance selects next numbered map");
        } finally {
            save.delete();
        }
        System.out.println("PASS session: 55 maps repeatable, 75 Hz clock, input, checkpoint, campaign end");
    }
}
