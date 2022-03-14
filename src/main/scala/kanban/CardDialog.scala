package kanban

import kanban.Main.{activeCard, activeColumn, cardEditActive, kanbanApp, stage}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import java.time.LocalDate

import scala.collection.mutable.Buffer

object CardDialog {
  val dialog = new Dialog[Card]() {
    initOwner(stage)
    title = "Kanban - New Card"
    headerText = "Add New Card"
  }

  val cardTags = Buffer[String]()

  val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  val cardText = new TextField {
    promptText = "Card Text"
  }
  val cardColor = new ColorPicker(Color.Black) {
    promptText = "Color"
    minHeight = 25
  }

  def drawRemoveTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- cardTags) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          cardTags.remove(cardTags.indexOf(tag))
          resetTagEdit()
        }
      }
    }
    items
  }

  val drawRemoveTag = new MenuButton("Select Tag to Remove") {
    items = drawRemoveTagMenuItems
  }

  def drawAddTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- kanbanApp.getTags.filterNot(cardTags.contains(_))) {
      items += new MenuItem(tag) {
        onAction = (event) => {
          println(tag)
          cardTags += tag
          resetTagEdit()
        }
      }
    }
    items
  }

  val drawAddTag = new MenuButton("Select Tag to Add") {
    items = drawAddTagMenuItems
  }

  val drawCurrentTags = new Label {
    if (cardTags.isEmpty) {
      text = "No tags. Add tag below."
    } else {
      text = cardTags.mkString(", ")
    }

  }

  val drawDatePicker = new DatePicker(LocalDate.now)

  val checkbox = new CheckBox("Include Deadline") {
    minWidth = 100
    onAction = (event) => checkCheckbox()
  }

  def checkCheckbox(): Unit = {
    drawDatePicker.disable = !checkbox.selected()
  }

  def drawContents: VBox = new VBox(10) {
    minWidth = 500
    minHeight = 400
    children += new HBox(10) {
      children += new Label("Text:")
      children += cardText
    }
    children += new HBox(10) {
      children += new Label("Color:")
      children += cardColor
    }
    children += new Separator
    children += new HBox(10) {
      children += new Label("Deadline: ")
      children += checkbox
      children += drawDatePicker
    }
    children += new Separator
    children += new HBox(10) {
      children += new Label("Tags: ")
      children += drawCurrentTags
    }
    children += new HBox(10) {
      children += new Label("Edit tags: ")
      children += drawAddTag
      children += drawRemoveTag
      children += new Button("Manage Tags") {
        onAction = (event) => {
          TagDialog.reset()
          TagDialog.dialog.showAndWait()
          if (cardEditActive) {
            cardTags.clear()
            activeCard.getTags.foreach(cardTags.append(_))
          } else {
            val correctTags = cardTags.filter(kanbanApp.getTags.contains(_))
            cardTags.clear()
            correctTags.foreach(cardTags.append(_))
          }
          resetTagEdit()
        }
      }
    }
  }

  val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  cardText.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(cardText.requestFocus())

  def reset() = {
    dialog.title = "Kanban - New Card"
    dialog.headerText = "Add New Card"
    cardText.text = ""
    cardColor.value = Color.Black
    cardTags.clear()
    checkbox.selected = false
    drawDatePicker.disable = true
    drawDatePicker.value = LocalDate.now()
    resetTagEdit()

  }

  def setCardEdit(card: Card) = {
    reset()
    dialog.title = "Kanban - Card Edit"
    dialog.headerText = "Edit Card"
    cardText.text = card.getText
    cardColor.value = card.getColor
    card.getTags.foreach(cardTags.append(_))

    card.getDeadline match {
      case Some(deadline) => {
        drawDatePicker.disable = false
        drawDatePicker.value = deadline.getRawDate
        checkbox.selected = false
      }
      case None => {
        drawDatePicker.disable = true
        drawDatePicker.value = LocalDate.now()
        checkbox.selected = true
      }
    }
    resetTagEdit()
  }

  def resetTagEdit() = {
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems
    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }

  }

  def getDeadline = {
    if (checkbox.selected()) {
      Some(new Deadline(drawDatePicker.value()))
    } else {
      None
    }
  }

  def update() = dialog.dialogPane().content = drawContents

  dialog.resultConverter = dialogButton =>
    if (dialogButton == okButtonType) {
      if (cardEditActive) {
        activeCard.editCard(cardText.text(), cardColor.getValue, cardTags, getDeadline)
        new Card(cardText.text(), cardColor.getValue, cardTags, getDeadline)
      } else {
        var newCard = activeColumn.addCard(cardText.text(), cardColor.getValue, cardTags, getDeadline)
        new Card(cardText.text(), cardColor.getValue, cardTags, getDeadline)
      }

    } else
      null
}
