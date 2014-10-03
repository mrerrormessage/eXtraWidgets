package uk.ac.surrey.soc.cress.extrawidgets.extension.prim

import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultReporter
import org.nlogo.api.LogoList
import org.nlogo.api.Syntax.ListType
import org.nlogo.api.Syntax.StringType
import org.nlogo.api.Syntax.reporterSyntax

import uk.ac.surrey.soc.cress.extrawidgets.extension.util.tryTo
import uk.ac.surrey.soc.cress.extrawidgets.state.Reader

class Properties(reader: Reader) extends DefaultReporter {
  override def getSyntax = reporterSyntax(Array(StringType), ListType)
  def report(args: Array[Argument], context: Context): AnyRef = {
    val widgetKey = args(0).getString
    val properties = for {
      (k, v) ← tryTo(reader.properties(widgetKey))
    } yield LogoList.fromVector(Vector(k, v))
    LogoList.fromVector(properties)
  }
}
