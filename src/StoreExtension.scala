package org.nlogo.extension.store

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.{ Files, InvalidPathException, Path, Paths }

import org.nlogo.api.{ Argument, Context, DefaultClassManager, ExtensionException, PrimitiveManager, Command }
import org.nlogo.core.Syntax

class StoreExtension extends DefaultClassManager {

  override def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive("put",      PutPrim)
    manager.addPrimitive("get",      GetPrim)
    manager.addPrimitive("get-keys", GetKeysPrim)
    manager.addPrimitive("has-key",  HasKeyPrim)
    manager.addPrimitive("remove",   RemovePrim)
    manager.addPrimitive("clear",    ClearPrim)
  }

  private object PutPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.StringType, Syntax.CommandBlockType | Syntax.OptionalType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

  private object GetPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

  private object GetKeysPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.CommandType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

  private object HasKeyPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

  private object RemovePrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.CommandBlockType | Syntax.OptionalType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

  private object ClearPrim extends Command {
    override def getSyntax = Syntax.commandSyntax(right = List(Syntax.CommandBlockType | Syntax.OptionalType))
    override def perform(args: Array[Argument], context: Context): Unit = ???
  }

}
