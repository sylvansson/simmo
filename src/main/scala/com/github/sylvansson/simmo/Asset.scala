package com.github.sylvansson.simmo

import indigo._

case class Asset(name: String, width: Int, height: Int, x: Int = 0, y: Int = 0) {
  val `type` = AssetType.Image(AssetName(name), AssetPath(s"assets/$name"))
  val graphic = Graphic(x, y, width, height, 1, Material.Textured(`type`.name))
}
