package com.codenjoy.dojo.tetris.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.client.AbstractJsonSolver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.tetris.model.Elements;
import java.util.PriorityQueue;

class PositionScore implements Comparable<PositionScore> {

  private final int height;
  private final int completeness;

  PositionScore(int height, int completeness) {
    this.height = height;
    this.completeness = completeness;
  }

  @Override
  public int compareTo(PositionScore o) {
    int height = Integer.compare(this.height, o.height);
    return height != 0 ? height : Integer.compare(o.completeness, completeness);
  }

  @Override
  public String toString() {
    return "{h=" + height + ", c=" + completeness + '}';
  }
}

class Position implements Comparable<Position> {

  private final int x;
  private final PositionScore score;

  public Position(int x, PositionScore score) {
    this.x = x;
    this.score = score;
  }

  public int getX() {
    return x;
  }

  @Override
  public int compareTo(Position o) {
    return score.compareTo(o.score);
  }
}

/**
 * User: your name Это твой алгоритм AI для игры. Реализуй его на свое усмотрение. Обрати внимание
 * на {@see YourSolverTest} - там приготовлен тестовый фреймворк для тебя.
 */
public class YourSolver extends AbstractJsonSolver<Board> {

  private Dice dice;

  public YourSolver(Dice dice) {
    this.dice = dice;
  }

  @Override
  public String getAnswer(Board board) {
    try {
      Position position = findBestPosition(board);
      if (position == null) {
        return Direction.DOWN.name();
      }

      int dx = position.getX() - board.getCurrentFigurePoint().getX();
      if (dx == 0) {
        return Direction.DOWN.name();
      }

      StringBuilder sb = new StringBuilder();
      Direction direction = dx > 0 ? Direction.RIGHT : Direction.LEFT;
      for (int i = 0; i < Math.abs(dx); i++) {
        sb.append(direction);
        sb.append(',');
      }
      sb.append(Direction.DOWN.name());

      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return Direction.DOWN.name();
    }
  }

  private Position findBestPosition(Board board) {
    PriorityQueue<Position> heap = new PriorityQueue<>();

    Elements element = board.getCurrentFigureType();
    Point point = board.getCurrentFigurePoint();
    GlassBoard glass = board.getGlass();
    for (int x = 0; x < glass.size(); x++) {
      GlassBoard copy = (GlassBoard) new GlassBoard().forString(glass.boardAsString());
      Point[] points = getFigurePoints(glass, element, point);
      clear(copy, points);
      int dx = x - point.getX();
      if (!canMove(glass.size(), points, dx, 0)) {
        continue;
      }
      move(points, dx, 0);
      moveDown(copy, element, points);
      set(copy, points, element);
      PositionScore score = getPositionScore(copy);
      heap.add(new Position(x, score));
    }

    return heap.peek();
  }

  private PositionScore getPositionScore(GlassBoard glass) {
    int height = glass.size();
    boolean touched = false;
    for (int y = glass.size() - 1; y >= 0; y--) {
      for (int x = 0; x < glass.size(); x++) {
        if (!glass.isFree(x, y)) {
          touched = true;
          break;
        }
      }
      if (touched) {
        break;
      }
      height--;
    }
    int completeness = 0;
    for (int y = 0; y < glass.size(); y++) {
      int current = 0;
      for (int x = 0; x < glass.size(); x++) {
        if (!glass.isFree(x, y)) {
          current++;
        }
      }
      completeness = Math.max(completeness, current);
    }
    return new PositionScore(height, completeness);
  }

  private Point[] getFigurePoints(GlassBoard glass, Elements element, Point point) {
    switch (element) {
      case BLUE:
        return getBluePoints(glass, point);
      case YELLOW:
      default:
        return getYellowPoints(glass, point);
    }
  }

  private void clear(GlassBoard glass, Point[] points) {
    set(glass, points, Elements.NONE);
  }

  private void set(GlassBoard glass, Point[] points, Elements element) {
    for (Point point : points) {
      glass.set(point.getX(), point.getY(), element.ch());
    }
  }

  private boolean isFree(GlassBoard glass, Point[] points) {
    for (Point point : points) {
      if (!glass.isFree(point.getX(), point.getY())) {
        return false;
      }
    }
    return true;
  }

  private void moveDown(GlassBoard glass, Elements figure, Point[] points) {
    boolean moved = false;
    while (isFree(glass, points)) {
      move(points, 0, -1);
      moved = true;
    }
    if (moved) {
      move(points, 0, 1);
    }
  }

  private Point[] getYellowPoints(GlassBoard glass, Point point) {
    Point[] points = new Point[4];
    points[0] = point.copy();
    points[1] = point.copy();
    points[1].move(point.getX() + 1, point.getY());
    points[2] = point.copy();
    points[2].move(point.getX(), point.getY() - 1);
    points[3] = point.copy();
    points[3].move(point.getX() + 1, point.getY() - 1);
    return points;
  }

  private Point[] getBluePoints(GlassBoard glass, Point point) {
    Point[] points = new Point[4];
    points[0] = point.copy();
    points[0].move(point.getX(), point.getY() - 1);
    points[1] = point.copy();
    points[1].move(point.getX(), point.getY() - 2);
    points[2] = point.copy();
    points[2].move(point.getX(), point.getY() - 3);
    points[3] = point.copy();
    points[3].move(point.getX(), point.getY() - 4);
    return points;
  }

  private boolean canMove(int size, Point[] points, int dx, int dy) {
    for (Point point : points) {
      int x = point.getX() + dx;
      int y = point.getY() + dy;
      if (x < 0 || x >= size || y < 0 || y >= size) {
        return false;
      }
    }
    return true;
  }

  private void move(Point[] points, int dx, int dy) {
    for (Point point : points) {
      point.move(point.getX() + dx, point.getY() + dy);
    }
  }

  public static void main(String[] args) {
    WebSocketRunner.runClient(
        // paste here board page url from browser after registration
        "http://codenjoy.mgo.one/codenjoy-contest/board/player/ffpdgizn8glxd5qbjz48?code=3423212880424965051",
        new YourSolver(new RandomDice()),
        new Board());
  }

}
