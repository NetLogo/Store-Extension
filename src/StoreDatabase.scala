package org.nlogo.extension.store

import scala.collection.mutable.ArrayBuffer

import org.h2.Driver
import java.lang.AutoCloseable
import java.sql.DriverManager

class StoreDatabase(folder: String) {
  val driver     = new Driver()
  val connection = DriverManager.getConnection(s"jdbc:h2:$folder/store-h2")

  {
    val statement = connection.createStatement
    val metadata  = connection.getMetaData
    val results   = metadata.getTables(null, null, "STORE", null)
    if (!results.next) {
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
    withClosable(connection.createStatement()) { statement => {
      val records = statement.executeQuery("select key from store")
      while (records.next) {
        keys += records.getString(1)
      }
      keys
    } }
  }

  def getDatabaseValueForKey(key: String): Option[String] = {
    withClosable(connection.prepareStatement("select value from store where key=?")) { query => {
      query.setString(1, key)
      val records = query.executeQuery
      if (records.next) {
        Some(records.getString(1))
      } else {
        None
      }
    } }
  }

  def checkDatabaseForKey(key: String): Boolean = {
    withClosable(connection.prepareStatement("select count(*) > 0 from store where key=?")) { query => {
      query.setString(1, key)
      val records = query.executeQuery
      if (records.next) {
        records.getBoolean(1)
      } else {
        false
      }
    } }
  }

  def insertDatabaseValueForKey(key: String, value: String): Unit = {
    withClosable(connection.prepareStatement("insert into store values (?, ?)")) { insert => {
      insert.setString(1, key)
      insert.setString(2, value)
      insert.executeUpdate
    } }
  }

  def updateDatabaseValueForKey(key: String, value: String): Unit = {
    withClosable(connection.prepareStatement("update store set value=? where key=?")) { update => {
      update.setString(1, value)
      update.setString(2, key)
      update.executeUpdate
    } }
  }

  def removeDatabaseValueForKey(key: String): Unit = {
    withClosable(connection.prepareStatement("delete from store where key=?")) { remove => {
      remove.setString(1, key)
      remove.executeUpdate
    } }
  }

  def clearDatabase(): Unit = {
    withClosable(connection.createStatement()) { statement => {
      statement.executeUpdate("truncate table store")
    } }
  }

  def withClosable[ T, C <: AutoCloseable ]( closable: C )( f: C => T ) =
    try {
      f( closable )
    }
    finally {
      closable.close
    }
}
