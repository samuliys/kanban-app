package kanban

import kanban.Main.stage
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.text._
import scalafx.geometry.Pos._
import scalafx.Includes._
import scalafx.scene.control.ButtonBar.ButtonData
import settings._

import java.awt.Desktop

object CardViewDialog {

  val dialog = new Dialog {
    initOwner(stage)
  }
  var selectedCard = new Card

  def showDialog() = dialog.showAndWait()

  def reset(card: Card): Unit = {
    selectedCard = card
    update()
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)
  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  def drawContents: VBox = new VBox(10) {
    minHeight = 120
    minWidth = 300
    alignment = Center
    border = new Border(new BorderStroke(selectedCard.getBorderColor, BorderStrokeStyle.Solid, new CornerRadii(2), new BorderWidths(6)))

    children += new Label(selectedCard.getText) {
      wrapText = true
      textAlignment = TextAlignment.Center
      font = CardTextFont
      textFill = selectedCard.getTextColor
    }
    selectedCard.getDeadline match {
      case Some(deadline) => {
        if (!selectedCard.getChecklist.hasTasks) {
          children += new HBox(12) {
            alignment = Center
            children += new Label(deadline.getString) {
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
          children += new Label(deadline.getString) {
            textFill = deadline.getCorrectColor(selectedCard.getChecklist)
          }
        }
      }
      case None =>
    }

    if (selectedCard.getChecklist.hasTasks) {
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
            children += new CheckBox() {
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
              }
            }
          }
        }
      }
      case None =>
    }
  }

  dialog.dialogPane().content = drawContents

  def update() = dialog.dialogPane().content = drawContents
}