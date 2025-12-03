package org.nlogo.extension.store

import org.nlogo.api.{ Argument, Command, Context, DefaultClassManager, ExtensionException, ExtensionManager, FileIO, PrimitiveManager, ScalaConversions }
import org.nlogo.nvm.{ AssemblerAssistant, CustomAssembled, ExtensionContext }
import org.nlogo.core.Syntax
import org.nlogo.agent.AgentSet

import java.sql.SQLException

class StoreExtension extends DefaultClassManager {
  val store = new StoreDatabase(FileIO.perUserDir("store"))

  override def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive("list-stores",  ListStoresPrim)
    manager.addPrimitive("switch-store", SwitchStorePrim)
    manager.addPrimitive("delete-store", DeleteStorePrim)

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

  private object ListStoresPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {
      val command = args(0).getCommand
      val tables  = wrapExceptions { () => store.getStoreTables() }
      command.perform(context, Array[AnyRef](ScalaConversions.toLogoList(tables)))
    }
  }

  private object SwitchStorePrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val name = args(0).getString
      wrapExceptions { () => store.setStore(name) }
    }
  }

  private object DeleteStorePrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val name = args(0).getString

      if (name == "" || name == "Default Store") {
        throw new ExtensionException("Cannot delete the default store, but you can clear it if you want.")
      }

      if (store.isCurrentStore(name)) {
        throw new ExtensionException("Cannot delete the current store, switch to another store first.")
      }

      wrapExceptions { () => store.deleteStore(name) }
    }
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

      wrapExceptions { () =>
        store.checkDatabaseForKey(key) match {
          case true  => store.updateDatabaseValueForKey(key, value)
          case false => store.insertDatabaseValueForKey(key, value)
        }
      }

      runCommandBlock(context)
    }

    def assemble(a: AssemblerAssistant): Unit = {
      a.block()
      a.done()
    }

  }

  private object GetPrim extends Command {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {

      val key     = args(0).getString
      val command = args(1).getCommand
      val value   = wrapExceptions { () => store.getDatabaseValueForKey(key) }

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
      val keys    = wrapExceptions { () => store.getDatabaseKeys() }
      command.perform(context, Array[AnyRef](ScalaConversions.toLogoList(keys)))
    }

  }

  private object HasKeyPrim extends Command {

    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))

    override def perform(args: Array[Argument], context: Context): Unit = {
      val key     = args(0).getString
      val command = args(1).getCommand
      val hasKey  = wrapExceptions { () => store.checkDatabaseForKey(key) }
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
      wrapExceptions { () => store.removeDatabaseValueForKey(key) }
      runCommandBlock(context)
    }

    def assemble(a: AssemblerAssistant): Unit = {
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

    def assemble(a: AssemblerAssistant): Unit = {
      a.block()
      a.done()
    }

  }

  def wrapExceptions[T](f: () => T): T =
    try {
      f()
    } catch {
      case ex: SQLException => throw new ExtensionException("Error with the store database", ex)
    }

  def runCommandBlock(context: Context): Unit = {
    val nvmContext = context.asInstanceOf[ExtensionContext].nvmContext
    val agentSet   = AgentSet.fromAgent(nvmContext.agent)
    nvmContext.runExclusiveJob(agentSet, nvmContext.ip + 1)
  }

}
