package uk.ac.surrey.soc.cress.extrawidgets.extension

import org.nlogo.api.DefaultClassManager
import org.nlogo.api.ExtensionException
import org.nlogo.api.ExtensionManager
import org.nlogo.api.PrimitiveManager

import uk.ac.surrey.soc.cress.extrawidgets.api.WidgetKind
import uk.ac.surrey.soc.cress.extrawidgets.core.WidgetsLoader
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Add
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.AddWidget
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Get
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.GetProperty
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Properties
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.PropertyKeys
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Remove
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Set
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.SetProperty
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.Version
import uk.ac.surrey.soc.cress.extrawidgets.extension.prim.WidgetKeys
import uk.ac.surrey.soc.cress.extrawidgets.state.Reader
import uk.ac.surrey.soc.cress.extrawidgets.state.Writer
import uk.ac.surrey.soc.cress.extrawidgets.state.getOrCreateModel

class ExtraWidgetsExtension extends DefaultClassManager {

  private var writer: Writer = null
  private var reader: Reader = null

  override def runOnce(em: ExtensionManager): Unit = {
    val tuple: (Reader, Writer) = getOrCreateModel(em)
    reader = tuple._1
    writer = tuple._2
  }

  def load(primitiveManager: PrimitiveManager): Unit = {
    println("load() " + this)

    val widgetKinds: Map[String, WidgetKind] =
      WidgetsLoader.loadWidgetKinds().fold(
        exceptions ⇒ { exceptions.foreach(e ⇒ throw new ExtensionException(e)); Map.empty },
        identity
      )

    val staticPrimitives = Seq(
      "VERSION" -> new Version("0.0.0-wip"),
      "__ADD" -> new Add(writer),
      "__SET" -> new Set(writer),
      "__GET" -> new Get(reader),
      "REMOVE" -> new Remove(writer),
      "WIDGET-KEYS" -> new WidgetKeys(reader),
      "PROPERTY-KEYS" -> new PropertyKeys(reader),
      "PROPERTIES" -> new Properties(reader)
    )

    val widgetPrimitives = widgetKinds.keys.map { kindName ⇒
      ("ADD-" + kindName) -> new AddWidget(writer, kindName)
    }

    val propertyPrimitives = widgetKinds.values.flatMap { kind ⇒
      kind.propertyKeys.flatMap { key ⇒
        Seq(
          ("GET-" + key) -> new GetProperty(reader, key),
          ("SET-" + key) -> new SetProperty(writer, key))
      }
    }

    val primitives = staticPrimitives ++ widgetPrimitives ++ propertyPrimitives
    println("Loaded primitives: " + primitives.unzip._1.toList)
    for ((name, prim) ← primitives)
      primitiveManager.addPrimitive(name, prim)
  }

  override def unload(em: ExtensionManager): Unit = {
    println("unload() " + this)
  }

  override def clearAll(): Unit = {
    println("unload clearAll() " + this)
  }

}
