package com.github.sylvansson.simmo

import com.github.sylvansson.simmo.Units._
import indigo._

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Simmo extends IndigoDemo[Unit, Unit, Model, Unit] {

  val eventFilters: EventFilters = EventFilters.Default

  val config: GameConfig = GameConfig.default

  val assets: Set[AssetType] = Units.assets

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult.noData(defaultGameConfig).withAssets(assets)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Model = Model.initial

  def initialViewModel(startupData: Unit, model: Model): Unit = ()

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] = {
    // Mark a unit as selected if it's been clicked.
    case MouseEvent.Click(x, y) => Outcome(model.select(x, y))
    // Cycle through units when Tab is clicked.
    case KeyboardEvent.KeyUp(Keys.TAB) => Outcome(model.selectNext)
    // Do nothing.
    case _ => Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[Unit], model: Model, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment()
      .addGameLayerNodes(model.sprites)
      .addUiLayerNodes(model.maybePortrait(context).toList)
}

/**
 * The game state.
 * @param units An array of units.
 * @param selected The index of the currently selected unit, if any.
 */
case class Model(units: List[SimmoUnit], selected: Option[Int]) {

  /**
   * Get the current units' sprites.
   * @return Zero or more sprites.
   */
  def sprites: List[Graphic] =
    units.zipWithIndex.map { case (unit, i) =>
      unit.drawSprite.withBorder(
        if (selected.contains(i)) Border.inside(color = RGBA.Green)
        else Border.default
      )
    }

  /**
   * Get the currently selected unit's portrait, if any.
   * @return Zero or one portrait.
   */
  def maybePortrait(context: FrameContext[Unit]): Option[Group] =
    selected.map(units).map { unit =>
      val group = Group(
        healthBar.graphic
          .scaleBy(unit.hp.toDouble / unit.`type`.maxHp, 1),
        unit.portrait.graphic
          .moveTo(0, healthBar.height + 2)
      )
      val bottom = Simmo.config.viewport.asRectangle.bottom
      group.moveTo(5, bottom - 5 - group.bounds(context.boundaryLocator).height)
    }

  /**
   * Detect whether a unit has been clicked. If so, mark the unit as selected.
   * Otherwise, deselect the currently selected unit.
   *
   * @param x The click's X coordinate.
   * @param y The click's Y coordinate.
   * @return The updated model.
   */
  def select(x: Int, y: Int): Model =
    copy(selected = units.indices.find(i => units(i).isAtPosition(x, y)))

  /**
   * Select the next unit in the array. If none is selected, select the first.
   * @return The updated model.
   */
  def selectNext: Model =
    copy(selected = selected.map(i => (i + 1) % units.size).orElse(Some(0)))
}

object Model {
  val initial = Model(
    List(
      SimmoUnit(Type.Peasant, Point(30, 30), 10),
      SimmoUnit(Type.Knight, Point(110, 110))
    ),
    None
  )
}
