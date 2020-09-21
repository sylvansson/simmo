package com.github.sylvansson.simmo

import com.github.sylvansson.simmo.Units._
import enumeratum._
import indigo._

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Simmo extends IndigoDemo[Unit, Unit, Model, Unit] {

  val eventFilters: EventFilters = EventFilters.Default

  val config: GameConfig = GameConfig.default
    .withViewport(
      Grid.grid.width,
      Grid.grid.height
    )
    .withClearColor(ClearColor.fromRGB(29, 97, 236))

  val assets: Set[AssetType] = Units.assets ++ Grid.assets

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult.noData(config).withAssets(assets)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Model = Model.initial

  def initialViewModel(startupData: Unit, model: Model): Unit = ()

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] = {
    // Mark a unit as selected if it's been clicked.
    case MouseEvent.Click(x, y) => Outcome(model.handleClick(x, y))
    // Cycle through units when Tab is clicked.
    case k: KeyboardEvent.KeyUp =>
      Outcome(
        k.keyCode match {
          case Keys.TAB => model.selectNext
          case Keys.KEY_M if model.selected.nonEmpty => model.setMode(Mode.AwaitingTargetPosition)
          case Keys.ESCAPE => model.setMode(Mode.Idle)
          case _ => model
        }
      )
    case FrameTick => Outcome(model.moveUnitsToNextPosition.regenerateUnits)
    // Do nothing.
    case _ => Outcome(model)
  }

  def updateViewModel(context: FrameContext[Unit], model: Model, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[Unit], model: Model, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment()
      .addGameLayerNodes(model.sprites)
      .addUiLayerNodes(Grid.grid.graphic)
      .addUiLayerNodes(model.maybePortrait(context).toList)
      .addUiLayerNodes(model.maybeTargetPosition(context).toList)
}

sealed trait Mode extends EnumEntry
object Mode extends Enum[Mode] {
  val values = findValues
  case object Idle extends Mode
  case object AwaitingTargetPosition extends Mode
}

/**
 * The game state.
 * @param units An array of units.
 * @param selected The index of the currently selected unit, if any.
 */
case class Model(units: List[SimmoUnit], selected: Option[Int], mode: Mode = Mode.Idle) {

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
          .scaleBy(unit.hp / unit.`type`.maxHp, 1),
        unit.portrait.graphic
          .moveTo(0, healthBar.height + 2)
      )
      val bottom = Simmo.config.viewport.asRectangle.bottom
      group.moveTo(5, bottom - 5 - group.bounds(context.boundaryLocator).height)
    }

  /**
   * Get a marker to indicate that we are in AwaitingTargetPosition
   * mode, if applicable.
   * @return Zero or one marker.
   */
  def maybeTargetPosition(context: FrameContext[Unit]): Option[Graphic] =
    Option.when(mode == Mode.AwaitingTargetPosition)(
      Grid.targetPosition.graphic
        .withAlpha(0.9)
        .withRef(8, 8)
        .moveTo(context.mouse.position)
    )

  def handleClick(x: Int, y: Int): Model = {
    (mode, selected) match {
      // Detect whether a unit has been clicked. If so, mark the unit as selected.
      // Otherwise, deselect the currently selected unit.
      case (Mode.Idle, _) =>
        copy(selected = units.indices.find(i => units(i).isAtPosition(x, y)))
      case (Mode.AwaitingTargetPosition, Some(i)) =>
        copy(
          units = units.updated(i, units(i).setTargetPosition(x, y)),
          mode = Mode.Idle
        )
      case _ => this
    }
  }

  def setMode(mode: Mode): Model = copy(mode = mode)

  /**
   * Select the next unit in the array. If none is selected, select the first.
   * @return The updated model.
   */
  def selectNext: Model =
    copy(selected = selected.map(i => (i + 1) % units.size).orElse(Some(0)))

  /**
   * Update each moving unit's position.
   * @return The updated model.
   */
  def moveUnitsToNextPosition: Model = copy(units = units.map(_.moveToNextPosition))

  def regenerateUnits: Model = copy(units = units.map(_.regenerate))
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
