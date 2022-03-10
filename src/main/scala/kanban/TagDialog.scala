package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._


object TagDialog {

  val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Tags"
    headerText = "Manage Tags"
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val tagText = new TextField() {
    promptText = "Tag Name"
  }

  val addTagButton = new Button("Add Tag") {
    onAction = (event) => {
      println(tagText.text())
      tagText.text = ""
    }
  }
  val tagRemoveMenu = new MenuButton("Select Tag to Remove") {


  }

  val grid = new GridPane() {
    hgap = 10
    vgap = 10
    padding = Insets(20, 200, 10, 10)

    add(new Label("New Tag:"), 0, 0)
    add(tagText, 1, 0)
    add(addTagButton, 2, 0)
    add(new Label("Remove Tag:"), 0, 1)
    add(tagRemoveMenu, 1, 1)
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().content = grid

  Platform.runLater(tagText.requestFocus())

  def reset() = {
    dialog.title = "Kanban - Tags"
    dialog.headerText = "ManageTags"
    tagText.text = ""
  }
}