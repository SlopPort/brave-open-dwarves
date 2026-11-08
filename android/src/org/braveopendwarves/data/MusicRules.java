package org.braveopendwarves.data;

/** Level music choice from the original at VA 0x413367..0x413402. */
public final class MusicRules {
    private static final String[] THEMES = {"GNOM3.IT", "GNOM2.IT", "GNOM7.IT", "GNOM9.IT", "GNOM8.IT"};

    private MusicRules() {}

    /** Every tenth map uses the boss theme; other maps select by level theme 1..5. */
    public static String track(int levelNumber, int theme) {
        if (levelNumber % 10 == 0) return "Data/Music/boss_theme.it";
        if (theme < 1 || theme > THEMES.length) return null;
        return "Data/Music/" + THEMES[theme - 1];
    }
}
