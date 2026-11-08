import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import org.braveopendwarves.data.*;

/** Approximate cell-level reachability using the original movement limits. A report, not proof: doors, keys and lifts are not simulated. */
public final class ReachReport {
    static OriginalLevel l;

    static boolean open(int c, int r) {
        if (c < 0 || c >= l.width || r >= l.height) return false;
        return r < 0 || l.cell(c, r)[0] == 0 || l.ladder(c, r);
    }
    static final boolean TALL = !Boolean.getBoolean("low"); static final int UP = Integer.getInteger("up", 1);
    static boolean body(int c, int r) { return open(c, r) && (!TALL || open(c, r - 1)); }
    static boolean support(int c, int r) {
        if (c < 0 || c >= l.width || r + 1 >= l.height) return true;
        int[] below = l.cell(c, r + 1);
        return below[0] == 1 || (below[5] & 0x20) != 0 || l.ladder(c, r) || l.ladder(c, r + 1);
    }
    static int land(int c, int r) {
        while (r < l.height && body(c, r)) { if (support(c, r)) return r; r++; }
        return -1;
    }
    static void add(boolean[][] seen, ArrayDeque<int[]> q, int c, int r) {
        if (c < 0 || c >= l.width || r < 0 || r >= l.height || seen[c][r]) return;
        seen[c][r] = true;
        q.add(new int[]{c, r});
    }
    static boolean[][] flood() {
        boolean[][] seen = new boolean[l.width][l.height];
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        int start = l.spawnY;
        while (start < l.height - 1 && !support(l.spawnX, start)) start++;
        add(seen, q, l.spawnX, start);
        while (!q.isEmpty()) {
            int[] p = q.poll();
            int c = p[0], r = p[1];
            for (int d = -1; d <= 1; d += 2) {
                if (body(c + d, r)) { int t = land(c + d, r); if (t >= 0) add(seen, q, c + d, t); }
                // Rise 120 px: one row up at most, about 90 px sideways while rising, more while falling.
                for (int up = 1; up <= UP && body(c, r - up); up++)
                    for (int s = 1; s <= 3 && body(c + d * s, r - up); s++) { int t = land(c + d * s, r - up); if (t >= 0) add(seen, q, c + d * s, t); }
            }
            if (body(c, r - 1) && (l.ladder(c, r) || l.ladder(c, r - 1))) add(seen, q, c, r - 1);
            if (r + 1 < l.height && body(c, r + 1) && (l.ladder(c, r + 1) || l.ladder(c, r))) add(seen, q, c, r + 1);
        }
        return seen;
    }
    static boolean near(boolean[][] seen, int x, int y, int span) {
        for (int c = Math.max(0, x); c < Math.min(l.width, x + span); c++)
            for (int r = Math.max(0, y); r < Math.min(l.height, y + span); r++) if (seen[c][r]) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        int ok = 0, total = 0;
        try (GogArchive archive = new GogArchive(new File(args[0]))) {
            for (GogArchive.Entry e : archive.entries()) {
                if (!e.name.toLowerCase().matches("data/levels/level[0-9]{4}\\.dat")) continue;
                l = new OriginalLevel(archive.read(e.name));
                boolean[][] seen = flood();
                boolean exit = near(seen, l.exitX, l.exitY, 2);
                int keys = 0, found = 0;
                for (OriginalLevel.Item i : l.items) if (i.type >= 41 && i.type <= 43) { keys++; if (near(seen, i.x / 64 - 1, i.y / 64 - 1, 3)) found++; }
                int cells = 0;
                for (boolean[] col : seen) for (boolean b : col) if (b) cells++;
                total++;
                if (exit && found == keys) ok++;
                else System.out.println(e.name.replace("Data/Levels/", "") + " exit=" + exit + " keys=" + found + "/" + keys + " cells=" + cells + " lifts=" + new LiftWorld(l).lifts.size());
            }
        }
        if (args.length > 1) { l = new OriginalLevel(new GogArchive(new File(args[0])).read(args[1])); boolean[][] seen = flood(); for (int r = 0; r < l.height; r++) { StringBuilder s = new StringBuilder(); for (int c = 0; c < l.width; c++) { int[] x = l.cell(c, r); char ch = c == l.spawnX && r == l.spawnY ? (char) 83 : c == l.exitX && r == l.exitY ? (char) 69 : seen[c][r] ? (char) 111 : l.ladder(c, r) ? (char) 72 : x[0] == 1 ? (char) 35 : x[0] == 0 ? (char) 46 : (char) (48 + Math.min(9, x[0])); s.append(ch); } System.out.println(s); } }
        System.out.println("reachable " + ok + "/" + total);
    }
}
