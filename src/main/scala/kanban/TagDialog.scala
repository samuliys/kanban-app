package kanban

import kanban.Main.{kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color


object TagDialog {

  val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Tags"
    headerText = "Manage Tags"
  }

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

  val tagText = new TextField() {
    promptText = "Tag Name"
  }

  val addTagButton = new Button("Add Tag") {
    disable = true
    onAction = (event) => {
      kanbanApp.addTag(tagText.text())
      tagText.text = ""
      errorLabel.text = ""
      tagRemoveMenu.items = ObservableBuffer(getTagList)
    }
  }

  val errorLabel = new Label {
    textFill = Color.Red
  }
  def getTagList = kanbanApp.getTagNames.toList

  val tagRemoveMenu: ComboBox[String] = new ComboBox(getTagList) {
    promptText = "Select Tag to Remove"
    onAction = (event) => {
      deleteTagButton.disable = false
    }
  }



  val deleteTagButton = new Button("Delete Tag") {
    disable = true
    onAction = (event) => {
      kanbanApp.removeTag(tagRemoveMenu.value())
      tagRemoveMenu.items = ObservableBuffer(getTagList)
      tagRemoveMenu.promptText = "Select Tag to Remove"
      disable = true
    }
  }


  def drawContents = new VBox(10) {
    minWidth = 500
    children += new HBox(10) {
      children += new Label("New Tag:")
      children += tagText
      children += addTagButton
      children += errorLabel
    }
    children += new HBox(5) {
      children += new Label("Remove Tag:")
      children += tagRemoveMenu
      children += deleteTagButton
    }
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)

  tagText.text.onChange { (_, _, newValue) =>
    if (newValue == "") {
      errorLabel.text = "Tag name can't be empty"
      addTagButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "Tag name too long"
      addTagButton.disable = true
    } else if (getTagList.contains(newValue)) {
      errorLabel.text = "Tag name can't be samuli"
      addTagButton.disable = true
    } else {
      errorLabel.text = ""
      addTagButton.disable = false
    }
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(tagText.requestFocus())

  def reset() = {
    dialog.title = "Kanban - Tags"
    dialog.headerText = "Manage Tags"
    tagText.text = ""
    errorLabel.text = ""
    dialog.dialogPane().content = drawContents


  }
}
