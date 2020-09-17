package com.github.sylvansson.simmo

import enumeratum._
import indigo._

object Units {
  val assets: Set[AssetType] = Type.values
    .flatMap(t => List(t.sprite, t.portrait))
    .map(_.`type`)
    .toSet

  case class Asset(name: String, width: Int, height: Int, x: Int = 0, y: Int = 0) {
    val `type` = AssetType.Image(AssetName(name), AssetPath(s"assets/$name"))
    val graphic = Graphic(x, y, width, height, 1, Material.Textured(`type`.name))
  }

  sealed abstract class Type(val sprite: Asset, val portrait: Asset) extends EnumEntry

  object Type extends Enum[Type] {
    val values = findValues

    case object Peasant
        extends Type(
          Asset("peasant-sprite.png", 16, 16),
          Asset("peasant-portrait.bmp", 64, 64)
        )

    case object Knight
        extends Type(
          Asset("knight-sprite.png", 16, 16),
          Asset("knight-portrait.bmp", 64, 64)
        )
  }

  case class SimmoUnit(`type`: Type, position: Point) {
    val sprite = `type`.sprite
    val portrait = `type`.portrait

    def drawSprite = sprite.graphic.moveTo(position)

    def isAtPosition(x: Int, y: Int): Boolean =
      drawSprite.lazyBounds.isPointWithin(x, y)
  }
}
