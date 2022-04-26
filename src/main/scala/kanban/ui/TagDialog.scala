package kanban.ui

import kanban._
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color


/** Dialog object for managing tags. */
object TagDialog {

  /** Shows dialog window on screen */
  def showDialog() = dialog.showAndWait()

  private val dialog = new Dialog { // create main dialog
    initOwner(Main.getStage)
    title = "Kanban - Tags"
    headerText = "Manage Tags"
  }

  private var kanbanapp = new Kanban // store current kanban session to variable

  private val tagLabelWidth = 80
  private val tagEntryWidth = 200

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  private val okButton = dialog.dialogPane().lookupButton(okButtonType)

  dialog.dialogPane().buttonTypes = Seq(ButtonType.OK) // add button to dialog

  private val tagText = new TextField { // text field for entering tag name
    minWidth = tagEntryWidth
    promptText = "Tag Name"
  }

  private val addTagButton = new Button("Add Tag") { // button for creating a new tag
    disable = true
    minWidth = tagLabelWidth
    onAction = (event) => {
      kanbanapp.addTag(tagText.text()) // add the new tag
      tagText.text = ""
      errorLabel.text = ""
      tagRemoveMenu.items = ObservableBuffer(getTagList) // update new tag to the remove menu
    }
  }

  private val errorLabel = new Label { // label for displaying error message
    textFill = Color.Red
  }

  /** Returns list of current tags
   *
   * @return current tags in a List */
  private def getTagList: List[String] = kanbanapp.getTags.toList

  private val tagRemoveMenu: ComboBox[String] = new ComboBox(getTagList) { // combobox for selecting a tag to be remove
    promptText = "Select Tag to Remove"
    minWidth = tagEntryWidth
    onAction = (event) => {
      deleteTagButton.disable = false
    }
  }

  private val deleteTagButton = new Button("Delete Tag") { // button for deleting a selected tag
    disable = true
    minWidth = tagLabelWidth
    onAction = (event) => {
      kanbanapp.removeTag(tagRemoveMenu.value())
      tagRemoveMenu.items = ObservableBuffer(getTagList) // update list of tags
      tagRemoveMenu.promptText = "Select Tag to Remove"
      disable = true
      if (!getTagList.contains(tagText.text())) { // if a tag is deleted the same one can be immediately added
        errorLabel.text = ""
        addTagButton.disable = false
      }
    }
  }

  /** Forms VBox component used as the root of all dialog components
   *
   * @return VBox component with all dialog window components */
  private def drawContents: VBox = new VBox(10) {
    minWidth = 500
    children += new HBox(10) {
      children += new Label("New Tag") {
        minWidth = tagLabelWidth
      }
      children += tagText
      children += addTagButton
      children += errorLabel
    }
    children += new HBox(10) {
      children += new Label("Remove Tag") {
        minWidth = tagLabelWidth
      }
      children += tagRemoveMenu
      children += deleteTagButton
    }
  }

  tagText.text.onChange { (_, _, newValue) => // check tag tag name is ok
    if (newValue == "") {
      errorLabel.text = "Tag name can't be empty"
      addTagButton.disable = true
    } else if (newValue.length > 20) { // don't allow too long tag names
      errorLabel.text = "Tag name too long"
      addTagButton.disable = true
    } else if (getTagList.contains(newValue)) { // tag names must be unique
      errorLabel.text = "Tag '" + tagText.text() + "' already exits"
      addTagButton.disable = true
    } else { // tag name is ok
      errorLabel.text = ""
      addTagButton.disable = false
    }
  }

  dialog.dialogPane().content = drawContents // set content to view

  Platform.runLater(tagText.requestFocus()) // focus on text field

  /** Resets the dialog in order to view current tags
   *
   * @param kanban current Kanban session */
  def reset(kanban: Kanban): Unit = {
    kanbanapp = kanban
    tagText.text = ""
    errorLabel.text = ""
    dialog.dialogPane().content = drawContents // update view
  }
}
