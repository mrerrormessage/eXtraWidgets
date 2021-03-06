package uk.ac.surrey.xw.extension.prim

import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultReporter
import org.nlogo.api.Syntax.reporterSyntax

import uk.ac.surrey.xw.extension.WidgetContextManager
import uk.ac.surrey.xw.state.Reader

class GetProperty(
  reader: Reader,
  propertyKey: String,
  outputType: Int,
  wcm: WidgetContextManager)
  extends DefaultReporter {
  override def getSyntax = reporterSyntax(outputType)
  def report(args: Array[Argument], context: Context): AnyRef = {
    val widgetKey = wcm.currentContext
    reader.get(propertyKey, widgetKey)
  }
}
