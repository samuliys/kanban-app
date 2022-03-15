package kanban

import kanban.Main.stage
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.paint.Color
import java.time.LocalDate

import scala.collection.mutable.Buffer

object CardDialog {

  def showDialog() = dialog.showAndWait()

  private var kanbanapp = new Kanban
  private var selectedColumn = new Column("", Color.Black)
  private var selectedCard = new Card("", Color.Black, Buffer[String](), None)
  private var newCard = false

  private val dialog = new Dialog[Card]() {
    initOwner(stage)
    title = "Kanban - New Card"
    headerText = "Add New Card"
  }


  private val cardTags = Buffer[String]()

  private val okButtonType = new ButtonType("OK", ButtonData.OKDone)
  dialog.dialogPane().buttonTypes = Seq(okButtonType, ButtonType.Cancel)

  private val cardText = new TextField {
    promptText = "Card Text"
  }
  private val cardColor = new ColorPicker(Color.Black) {
    promptText = "Color"
    minHeight = 25
  }

  private def drawRemoveTagMenuItems: Buffer[MenuItem] = {
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

  private val drawRemoveTag = new MenuButton("Select Tag to Remove") {
    items = drawRemoveTagMenuItems
  }

  private def drawAddTagMenuItems: Buffer[MenuItem] = {
    val items = Buffer[MenuItem]()
    for (tag <- kanbanapp.getTags.filterNot(cardTags.contains(_))) {
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

  private val drawAddTag = new MenuButton("Select Tag to Add") {
    items = drawAddTagMenuItems
  }

  private val drawCurrentTags = new Label {
    if (cardTags.isEmpty) {
      text = "No tags. Add tag below."
    } else {
      text = cardTags.mkString(", ")
    }

  }

  private val drawDatePicker = new DatePicker(LocalDate.now)

  private val checkbox = new CheckBox("Include Deadline") {
    minWidth = 100
    onAction = (event) => checkCheckbox()
  }

  private def checkCheckbox(): Unit = {
    drawDatePicker.disable = !checkbox.selected()
  }

  private def drawContents: VBox = new VBox(10) {
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
          TagDialog.reset(kanbanapp)
          TagDialog.getDialog.showAndWait()
          if (newCard) {
            val correctTags = cardTags.filter(kanbanapp.getTags.contains(_))
            cardTags.clear()
            correctTags.foreach(cardTags.append(_))
          } else {
            cardTags.clear()
            selectedCard.getTags.foreach(cardTags.append(_))
          }
          resetTagEdit()
        }
      }
    }
  }

  private val okButton = dialog.dialogPane().lookupButton(okButtonType)
  okButton.disable = true

  cardText.text.onChange { (_, _, newValue) =>
    okButton.disable = newValue.trim().isEmpty
  }

  dialog.dialogPane().content = drawContents

  Platform.runLater(cardText.requestFocus())

  def reset(kanban: Kanban, column: Column, card: Card, isNew: Boolean) = {
    kanbanapp = kanban
    selectedColumn = column
    selectedCard = card
    newCard = isNew

    cardTags.clear()

    if (isNew) {
      dialog.title = "Kanban - New Card"
      dialog.headerText = "Add New Card"
      cardText.text = ""
      cardColor.value = Color.Black
    } else {
      dialog.title = "Kanban - Card Edit"
      dialog.headerText = "Edit Card"
      cardText.text = card.getText
      cardColor.value = card.getColor
      card.getTags.foreach(cardTags.append(_))
    }

    card.getDeadline match {
      case Some(deadline) => {
        drawDatePicker.disable = false
        drawDatePicker.value = deadline.getRawDate
        checkbox.selected = true
      }
      case None => {
        drawDatePicker.disable = true
        drawDatePicker.value = LocalDate.now()
        checkbox.selected = false
      }
    }
    resetTagEdit()
  }


  private def resetTagEdit() = {
    drawAddTag.items = drawAddTagMenuItems
    drawRemoveTag.items = drawRemoveTagMenuItems
    if (cardTags.isEmpty) {
      drawCurrentTags.text = "No tags"
    } else {
      drawCurrentTags.text = cardTags.mkString(", ")
    }

  }

  private def getDeadline = {
    if (checkbox.selected()) {
      Some(new Deadline(drawDatePicker.value()))
    } else {
      None
    }
  }

  private def update() = dialog.dialogPane().content = drawContents

  dialog.resultConverter = dialogButton => {
    if (dialogButton == okButtonType) {
      if (newCard) {
        selectedColumn.addCard(cardText.text(), cardColor.getValue, cardTags, getDeadline)
      } else {
        selectedCard.editCard(cardText.text(), cardColor.getValue, cardTags, getDeadline)
      }
    }
    selectedCard
  }
}
