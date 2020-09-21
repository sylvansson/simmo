package com.github.sylvansson.simmo

import indigo._

import scala.annotation.tailrec
import scala.collection.immutable.Queue

object Grid {
  def assets: Set[AssetType] =
    Set(grid, targetPosition).map(_.`type`)

  val targetPosition = Asset("target-position.png", 16, 16)
  val grid = Asset("grid.png", 640, 480)
  val tileSize = 16

  /**
   * The number of horizontal tiles.
   */
  val width = grid.width / tileSize

  /**
   * The number of vertical tiles.
   */
  val height = grid.height / tileSize

  case class Tile(x: Int, y: Int) {
    def toPoint: Point = Point(x * tileSize, y * tileSize)

    def isInsideGrid: Boolean =
      (x >= 0 && x < width) && y >= 0 && y < height

    def neighbours: List[Tile] = {
      val offsets = List(0, 1, -1)
      for {
        xOffset <- offsets
        yOffset <- offsets
        p = Tile(x + xOffset, y + yOffset) if p != this && p.isInsideGrid
      } yield p
    }
  }
  object Tile {
    def apply(p: Point): Tile = Tile(
      p.x / tileSize,
      p.y / tileSize
    )
  }

  object Pathfinding {
    def breadthFirstSearch(start: Tile, end: Tile): List[Tile] = {
      @tailrec
      def recursiveSearch(frontier: Queue[Tile], previous: Map[Tile, Option[Tile]]): Map[Tile, Option[Tile]] = {
        frontier match {
          case Queue() => previous
          case curr +: _ if curr == end => previous
          case curr +: rest =>
            val neighbours = curr.neighbours.filterNot(previous.contains)
            recursiveSearch(
              rest.enqueueAll(neighbours),
              previous ++ neighbours.map(_ -> Some(curr))
            )
        }
      }

      val previous = recursiveSearch(Queue(start), Map(start -> None))
      @tailrec
      def resolvePath(path: List[Tile]): List[Tile] =
        previous(path.head) match {
          case Some(location) => resolvePath(location +: path)
          case None => path
        }
      resolvePath(List(end))
    }
  }
}
