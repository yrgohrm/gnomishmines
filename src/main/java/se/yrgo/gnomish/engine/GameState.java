package se.yrgo.gnomish.engine;

/** Whether the game is still going, and if not, how it ended. */
public enum GameState {
  RUNNING,
  WON,
  DIED,
  QUIT;

  public boolean isOver() {
    return this != RUNNING;
  }
}
