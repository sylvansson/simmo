package com.github.sylvansson.simmo

import indigo._

object Grid {
  def assets: Set[AssetType] =
    Set(grid.`type`)

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

  implicit class PointOnGrid(p: Point) {

    /**
     * Get the top left corner coordinates of a point's tile.
     */
    def snapToGrid: Point = Point(
      p.x / tileSize * tileSize,
      p.y / tileSize * tileSize
    )
  }
}
