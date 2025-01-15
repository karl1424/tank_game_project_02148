package dk.dtu.gamestates;

public enum Gamestate {
    MENU, LOBBY, JOIN, PLAYING, GAMEOVER;

    public static Gamestate state = MENU;
}
