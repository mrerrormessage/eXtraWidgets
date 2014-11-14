package uk.ac.surrey.xw.gui

import java.awt.Container
import scala.Array.canBuildFrom
import scala.Option.option2Iterable
import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.Publisher
import scala.collection.mutable.Subscriber
import org.nlogo.app.App
import org.nlogo.awt.EventQueue.invokeLater
import Strings.DefaultTabName
import Strings.TabIDQuestion
import Swing.inputDialog
import Swing.warningDialog
import javax.swing.SwingUtilities.getAncestorOfClass
import uk.ac.surrey.xw.api.ExtraWidget
import uk.ac.surrey.xw.api.PropertyKey
import uk.ac.surrey.xw.api.PropertyMap
import uk.ac.surrey.xw.api.PropertyValue
import uk.ac.surrey.xw.api.RichWorkspace.enrichWorkspace
import uk.ac.surrey.xw.api.Tab
import uk.ac.surrey.xw.api.WidgetKey
import uk.ac.surrey.xw.api.WidgetKind
import uk.ac.surrey.xw.api.XWException
import uk.ac.surrey.xw.api.enrichOption
import uk.ac.surrey.xw.api.makeKey
import uk.ac.surrey.xw.api.normalizeString
import uk.ac.surrey.xw.api.toRunnable
import uk.ac.surrey.xw.api.tryTo
import uk.ac.surrey.xw.state.AddWidget
import uk.ac.surrey.xw.state.RemoveWidget
import uk.ac.surrey.xw.state.SetProperty
import uk.ac.surrey.xw.state.StateEvent
import uk.ac.surrey.xw.state.Writer
import uk.ac.surrey.xw.api.TabKind

class GUI(
  val app: App,
  val writer: Writer,
  val widgetKinds: Map[String, WidgetKind[_]])
  extends Subscriber[StateEvent, Publisher[StateEvent]] {

  writer.subscribe(this)

  val tabs = app.tabs
  val tabPropertyKey = new TabKind[Tab].name

  override def notify(pub: Publisher[StateEvent], event: StateEvent): Unit =
    invokeLater {
      event match {
        case AddWidget(widgetKey, propertyMap) ⇒
          addWidget(widgetKey, propertyMap)
        case SetProperty(widgetKey, propertyKey, propertyValue) ⇒
          setProperty(widgetKey, propertyKey, propertyValue)
        case RemoveWidget(widgetKey) ⇒
          removeWidget(widgetKey)
      }
    }

  def getWidget(widgetKey: WidgetKey): Option[ExtraWidget] = {
    def getWidgetsIn(container: Container) =
      container.getComponents.collect {
        case w: ExtraWidget ⇒ w
      }
    val extraTabs = getWidgetsIn(tabs)
    extraTabs
      .find(_.key == widgetKey)
      .orElse {
        extraTabs
          .collect { case t: Container ⇒ t }
          .flatMap(getWidgetsIn)
          .find(_.key == widgetKey)
      }
  }

  private def addWidget(widgetKey: WidgetKey, propertyMap: PropertyMap): Unit =
    for {
      kindName ← propertyMap.get("KIND").map(_.toString).orException(
        "Can't find KIND for " + widgetKey + " in " + propertyMap).right
      kind ← widgetKinds.get(normalizeString(kindName)).orException(
        "Kind " + kindName + " not loaded.").right
    } {
      val w = kind.newWidget(widgetKey, writer, app.workspace)
      w.init(propertyMap)
      if (!w.isInstanceOf[Tab])
        getTabFor(widgetKey, propertyMap).right.foreach(_.add(w))
    }

  private def setProperty(
    widgetKey: WidgetKey,
    propertyKey: PropertyKey,
    propertyValue: PropertyValue): Unit =
    for (w ← getWidget(widgetKey))
      w.setProperty(propertyKey, propertyValue)

  def getTabOf(w: ExtraWidget): Option[Tab] =
    Option(getAncestorOfClass(classOf[Tab], w))
      .collect { case t: Tab ⇒ t }

  private def removeWidget(widgetKey: WidgetKey): Unit =
    for (w ← getWidget(widgetKey)) w match {
      case tab: Tab ⇒ tab.removeFromAppTabs()
      case _ ⇒ for (tab ← getTabOf(w)) tab.remove(w)
    }

  private def getTabFor(widgetKey: WidgetKey, propertyMap: PropertyMap): Either[XWException, Tab] = {
    val tabs = app.workspace.xwTabs
    for {
      tabKey ← propertyMap
        .get(tabPropertyKey)
        .collect { case s: String ⇒ s }
        .map(normalizeString)
        .orException("Tab not defined for widget " + widgetKey + ".").right
      tab ← tabs
        .find(_.key == tabKey)
        .orException("Tab " + tabKey + " does not exist for widget " + widgetKey + ".").right
    } yield tab
  }

  def createNewTab(): Unit = {
    def askName(default: String) = inputDialog(TabIDQuestion, default)
    Iterator
      .iterate(askName(DefaultTabName))(_.flatMap(askName))
      .takeWhile(_.isDefined)
      .flatten
      .map(key ⇒ tryTo(writer.add(key, Map("kind" -> tabPropertyKey))))
      .takeWhile(_.isLeft)
      .flatMap(_.left.toSeq)
      .foreach(warningDialog)
  }
}