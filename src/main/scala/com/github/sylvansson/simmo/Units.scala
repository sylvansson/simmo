package com.github.sylvansson.simmo

import com.github.sylvansson.simmo.Grid._
import enumeratum._
import indigo._

object Units {
  def assets: Set[AssetType] =
    (Type.values.flatMap(t => List(t.sprite, t.portrait)) :+ healthBar)
      .map(_.`type`)
      .toSet

  val healthBar = Asset("health-bar.png", 64, 8)

  sealed abstract class Type(val sprite: Asset, val portrait: Asset, val maxHp: Int) extends EnumEntry

  object Type extends Enum[Type] {
    val values = findValues

    case object Peasant
        extends Type(
          Asset("peasant-sprite.png", 16, 16),
          Asset("peasant-portrait.bmp", 64, 64),
          maxHp = 30
        )

    case object Knight
        extends Type(
          Asset("knight-sprite.png", 16, 16),
          Asset("knight-portrait.bmp", 64, 64),
          maxHp = 90
        )
  }

  case class SimmoUnit(`type`: Type, position: Point, hp: Int) {
    val sprite = `type`.sprite
    val portrait = `type`.portrait

    def drawSprite = sprite.graphic.moveTo(position.snapToGrid)

    def isAtPosition(x: Int, y: Int): Boolean =
      drawSprite.lazyBounds.isPointWithin(x, y)
  }
  object SimmoUnit {
    def apply(`type`: Type, position: Point): SimmoUnit = SimmoUnit(`type`, position, `type`.maxHp)
  }
}
