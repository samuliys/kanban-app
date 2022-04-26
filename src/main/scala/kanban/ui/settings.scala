package kanban

import scalafx.scene.paint.Color
import scalafx.scene.text.Font

/** Package object containing basic settings for the GUI. */
package object ui {

  // Starting window dimensions
  val StartingWidth = 1100
  val StartingHeight = 800

  // UI element dimensions
  val CardHeight = 120
  val CardWidth = 310
  val ColumnWidth = 330
  val ColumnGapSize = 20

  // Defualt fonts
  val DefaultFont = Font.font("arial", 13)
  val CardTextFont = Font.font("arial", 15)

  // Max amounts of boards and columns
  val MaxBoards = 5
  val MaxColumns = 7

  // Default colors
  val DefaultColor = Color.Black
  val DefaultBoardColor = Color.White
}