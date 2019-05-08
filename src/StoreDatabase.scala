package org.nlogo.extension.store

import scala.collection.mutable.ArrayBuffer

import org.h2.Driver
import java.sql.{ DriverManager, PreparedStatement }

class StoreDatabase {
  val driver     = new Driver()
  val connection = DriverManager.getConnection("jdbc:h2:~/store-h2")

  {
    val statement = connection.createStatement()
    val metadata  = connection.getMetaData()
    val results   = metadata.getTables(null, null, "STORE", null)
    if (!results.next()) {
      statement.execute("create table store(key varchar(max), value varchar(max))")
      statement.execute("alter table store add constraint unique_keys unique(key)")
      statement.close
    }
  }

  def close() = {
    if (connection != null) {
      connection.close
    }
  }

  def getDatabaseKeys(): Seq[String] = {
    val keys = ArrayBuffer.empty[String]
    try {
      val statement = connection.createStatement()
      val records   = statement.executeQuery("select key from store")
      while (records.next()) {
        keys += records.getString(1)
      }
    } catch {
      case _: Exception => keys
    }
    return keys
  }

  def getDatabaseValueForKey(key: String): Option[String] = {
    var query: PreparedStatement = null
    val value = try {
      query = connection.prepareStatement("select value from store where key=?")
      query.setString(1, key)
      val records = query.executeQuery()
      if (records.next()) {
        Some(records.getString(1))
      } else {
        None
      }
    } catch {
      case _: Exception => None
    } finally {
      query.close
    }
    return value
  }

  def checkDatabaseForKey(key: String): Boolean = {
    var query: PreparedStatement = null
    val value = try {
      query = connection.prepareStatement("select count(*) > 0 from store where key=?")
      query.setString(1, key)
      val records = query.executeQuery()
      if (records.next()) {
        records.getBoolean(1)
      } else {
        false
      }
    } catch {
      case _: Exception => false
    } finally {
      query.close
    }
    return value
  }

  def insertDatabaseValueForKey(key: String, value: String): Unit = {
    val insert = connection.prepareStatement("insert into store values (?, ?)")
    insert.setString(1, key)
    insert.setString(2, value)
    insert.executeUpdate
    insert.close
  }

  def updateDatabaseValueForKey(key: String, value: String): Unit = {
    val update = connection.prepareStatement("update store set value=? where key=?")
    update.setString(1, value)
    update.setString(2, key)
    update.executeUpdate
    update.close
  }

  def removeDatabaseValueForKey(key: String): Unit = {
    val remove = connection.prepareStatement("delete from store where key=?")
    remove.setString(1, key)
    remove.executeUpdate
    remove.close
  }

  def clearDatabase(): Unit = {
    val statement = connection.createStatement
    statement.executeUpdate("truncate table store")
    statement.close
  }

}
