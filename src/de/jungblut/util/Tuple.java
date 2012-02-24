package de.jungblut.util;

public final class Tuple<FIRST, SECOND> {

  private final FIRST first;
  private final SECOND second;

  public Tuple(FIRST first, SECOND second) {
    super();
    this.first = first;
    this.second = second;
  }

  public final FIRST getFirst() {
    return first;
  }

  public final SECOND getSecond() {
    return second;
  }

}
