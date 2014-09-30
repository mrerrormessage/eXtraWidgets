package uk.ac.surrey.soc.cress.extrawidgets.plugin

import org.nlogo.app.App
import org.nlogo.app.ToolsMenu

import javax.swing.JPanel
import uk.ac.surrey.soc.cress.extrawidgets.plugin.controller.Controller
import uk.ac.surrey.soc.cress.extrawidgets.plugin.gui.GUI
import uk.ac.surrey.soc.cress.extrawidgets.plugin.util.Swing.enrichComponent
import uk.ac.surrey.soc.cress.extrawidgets.plugin.view.View
import uk.ac.surrey.soc.cress.extrawidgets.state.getOrCreateModel

object ExtraWidgetsPlugin {
  val name = "eXtraWidgets"
}

class ExtraWidgetsPlugin(val app: App, val toolsMenu: ToolsMenu) extends JPanel {

  val manager = new ExtraWidgetsManager(app, toolsMenu)

  app.frame.onComponentShown(_ ⇒ manager.gui.removeTab(this))

}
