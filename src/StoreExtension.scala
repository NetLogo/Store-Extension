package org.nlogo.extension.store

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.{ Files, InvalidPathException, Path, Paths }

import org.nlogo.api.{ Argument, Context, DefaultClassManager, ExtensionException, ExtensionManager, PrimitiveManager, ScalaConversions, Command }
import org.nlogo.nvm.{ AssemblerAssistant, CustomAssembled, ExtensionContext }
import org.nlogo.core.Syntax
import org.nlogo.agent.AgentSet

class StoreExtension extends DefaultClassManager {
  val store = new StoreDatabase()

  override def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive("put",      PutPrim)
    manager.addPrimitive("get",      GetPrim)
    manager.addPrimitive("get-keys", GetKeysPrim)
    manager.addPrimitive("has-key",  HasKeyPrim)
    manager.addPrimitive("remove",   RemovePrim)
    manager.addPrimitive("clear",    ClearPrim)
  }

  override def unload(em: ExtensionManager): Unit = {
    store.close()
  }

  private object PutPrim extends Command with CustomAssembled {

    override def getSyntax = Syntax.commandSyntax(
      right = List(
        Syntax.StringType,
        Syntax.StringType,
        Syntax.CommandBlockType | Syntax.OptionalType
      )
    )

    override def perform(args: Array[Argument], context: Context): Unit = {

      val key   = args(0).getString
      val value = args(1).getString

      store.checkDatabaseForKey(key) match {
        case true  => store.updateDatabaseValueForKey(key, value)
        case false => store.insertDatabaseValueForKey(key, value)
      }

      runCommandBlock(context)
    }

    def assemble(a: AssemblerAssistant) {
      a.block()
      a.done()
    }

  }

  private object GetPrim extends Command {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val key     = args(0).getString
      val command = args(1).getCommand
      val value   = store.getDatabaseValueForKey(key)

      value match {
        case Some(v) => command.perform(context, Array[AnyRef](v))
        case None    => throw new ExtensionException(s"Could not find a value for key: '$key'.")
      }

    }

  }

  private object GetKeysPrim extends Command {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val command = args(0).getCommand
      val keys    = store.getDatabaseKeys()

      command.perform(context, Array[AnyRef](ScalaConversions.toLogoList(keys)))
    }

  }

  private object HasKeyPrim extends Command {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val key     = args(0).getString
      val command = args(1).getCommand
      val hasKey  = store.checkDatabaseForKey(key)

      command.perform(context, Array[AnyRef](Boolean.box(hasKey)))
    }

  }

  private object RemovePrim extends Command with CustomAssembled {

    override def getSyntax = Syntax.commandSyntax(
      right = List(
        Syntax.StringType,
        Syntax.CommandBlockType | Syntax.OptionalType
      )
    )

    override def perform(args: Array[Argument], context: Context): Unit = {

      val key = args(0).getString
      store.removeDatabaseValueForKey(key)

      runCommandBlock(context)
    }

    def assemble(a: AssemblerAssistant) {
      a.block()
      a.done()
    }

  }

  private object ClearPrim extends Command with CustomAssembled {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.CommandBlockType | Syntax.OptionalType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      store.clearDatabase()

      runCommandBlock(context)
    }

    def assemble(a: AssemblerAssistant) {
      a.block()
      a.done()
    }

  }

  def runCommandBlock(context: Context) {
    val nvmContext = context.asInstanceOf[ExtensionContext].nvmContext
    val agentSet   = AgentSet.fromAgent(nvmContext.agent)
    nvmContext.runExclusiveJob(agentSet, nvmContext.ip + 1)
  }

}
