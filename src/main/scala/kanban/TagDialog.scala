package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color


object TagDialog {

  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog {
    initOwner(stage)
    title = "Kanban - Tags"
    headerText = "Manage Tags"
  }

  private var kanbanapp = new Kanban

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

  private val tagText = new TextField() {
    promptText = "Tag Name"
  }

  private val addTagButton = new Button("Add Tag") {
    disable = true
    onAction = (event) => {
      kanbanapp.addTag(tagText.text())
      tagText.text = ""
      errorLabel.text = ""
      tagRemoveMenu.items = ObservableBuffer(getTagList)
    }
  }

  private val errorLabel = new Label {
    textFill = Color.Red
  }
  private def getTagList = kanbanapp.getTags.toList

  private val tagRemoveMenu: ComboBox[String] = new ComboBox(getTagList) {
    promptText = "Select Tag to Remove"
    onAction = (event) => {
      deleteTagButton.disable = false
    }
  }



  private val deleteTagButton = new Button("Delete Tag") {
    disable = true
    onAction = (event) => {
      kanbanapp.removeTag(tagRemoveMenu.value())
      tagRemoveMenu.items = ObservableBuffer(getTagList)
      tagRemoveMenu.promptText = "Select Tag to Remove"
      disable = true
      if (!getTagList.contains(tagText.text())) {
        errorLabel.text = ""
        addTagButton.disable = false
      }
    }
  }

  private def drawContents = new VBox(10) {
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

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  tagText.text.onChange { (_, _, newValue) =>
    if (newValue == "") {
      errorLabel.text = "Tag name can't be empty"
      addTagButton.disable = true
    } else if (newValue.length > 10) {
      errorLabel.text = "Tag name too long"
      addTagButton.disable = true
    } else if (getTagList.contains(newValue)) {
      errorLabel.text = "Tag \"" + tagText.text() + "\" already exits"
      addTagButton.disable = true
    } else {
      errorLabel.text = ""
      addTagButton.disable = false
    }
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(tagText.requestFocus())

  def reset(kanban: Kanban) = {
    kanbanapp = kanban
    dialog.title = "Kanban - Tags"
    dialog.headerText = "Manage Tags"
    tagText.text = ""
    errorLabel.text = ""
    dialog.dialogPane().content = drawContents
  }
}
