package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.geometry.Pos._
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.text._
import java.awt.Desktop


/** Dialog object for viewing a single card. */
object CardViewDialog {

  private val dialog = new Dialog { // create dialog to be shown
    initOwner(Main.getStage)
  }
  private var selectedCard = new Card // save card to be displayed to this variable

  /** Opens dialog window */
  def showDialog() = dialog.showAndWait()

  /** Resets the dialog in order to show a new card
   *
   * @param card card to be shown */
  def reset(card: Card): Unit = {
    selectedCard = card
    update()
  }

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK) // add button to view

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents: VBox = new VBox(10) {
    minHeight = 120
    minWidth = 300
    alignment = Center
    border = new Border(new BorderStroke(selectedCard.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))

    children += new Label(selectedCard.getText) { // display card text
      wrapText = true
      textAlignment = TextAlignment.Center
      font = CardTextFont
      textFill = selectedCard.getTextColor
    }
    selectedCard.getDeadline match { // display card deadline
      case Some(deadline) => {
        if (!selectedCard.getChecklist.hasTasks) {
          children += new HBox(12) {
            alignment = Center
            children += new Label(deadline.toString) {
              textFill = deadline.getCorrectColor(selectedCard.getChecklist)
            }
            children += new CheckBox("Done") {
              selected = deadline.getStatus
              onAction = (event) => {
                deadline.toggleStatus()
                update()
              }
            }
          }
        } else {
          children += new Label(deadline.toString) {
            textFill = deadline.getCorrectColor(selectedCard.getChecklist)
          }
        }
      }
      case None =>
    }

    if (selectedCard.getChecklist.hasTasks) { // display card tasks
      children += new HBox(10) {
        alignment = Center
        children += new Label(selectedCard.getChecklist.toString)
        children += new ProgressBar {
          progress = selectedCard.getChecklist.getProgress
          minHeight = 20
          minWidth = 120
        }
      }
      children += new HBox {
        children += new Pane {
          minWidth = 20
        }
        children += new VBox(2) {
          for (task <- selectedCard.getChecklist.getTasks) {
            children += new CheckBox() { // display each task with checkbox
              text = task._2
              selected = task._1
              onAction = (event) => {
                selectedCard.getChecklist.toggleStatus(task._2)
                update()
              }
            }
          }
        }
      }

    }
    selectedCard.getFile match {
      case Some(file) => {
        children += new HBox(3) {
          alignment = Center
          children += new Label("File: " + file.getName)
          children += new Button("Open File") {
            onAction = (event) => {
              if (file.canRead && Desktop.isDesktopSupported) {
                Desktop.getDesktop.open(file)
              } else {
                selectedCard.resetFile()
                new Alert(AlertType.Warning, "There was an error opening the file.").showAndWait()
              }
            }
          }
        }
      }
      case None =>
    }
  }

  dialog.dialogPane().content = drawContents // set contents to screen

  /** Updates dialog view with up to date information. */
  private def update(): Unit = dialog.dialogPane().content = drawContents
}