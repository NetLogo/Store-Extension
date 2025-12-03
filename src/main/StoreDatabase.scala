package org.nlogo.extension.store

import scala.collection.mutable.ArrayBuffer

import org.h2.Driver
import java.lang.AutoCloseable
import java.sql.DriverManager

class StoreDatabase(folder: String) {
  val driver       = new Driver()
  val connection   = DriverManager.getConnection(s"jdbc:h2:$folder/store-h2")
  var currentStore = "default_store"

  {
    val statement = connection.createStatement
    val metadata  = connection.getMetaData

    val storesTable = metadata.getTables(null, null, "__STORES_TABLE", null)
    if (!storesTable.next) {
      statement.execute("create table __stores_table(name varchar(max), store_number integer)")
      statement.execute("alter table __stores_table add constraint __stores_table_unique_keys unique(name)")
    }
    storesTable.close

    val results = metadata.getTables(null, null, "DEFAULT_STORE", null)
    if (!results.next) {
      statement.execute("create table default_store(key varchar(max), value varchar(max))")
      statement.execute("alter table default_store add constraint default_store_unique_keys unique(key)")
    }
    results.close

    statement.close
  }

  def close(): Unit = {
    if (connection != null) {
      connection.close
    }
  }

  def getNextStoreNumber(): Int = {
    withClosable(connection.prepareStatement("select max(store_number) from __stores_table")) { maxQuery => {
      val maxRecords = maxQuery.executeQuery
      if (maxRecords.next) {
        maxRecords.getInt(1) + 1
      } else {
        0
      }
    } }
  }

  def getStoreTables(): Seq[String] = {
    val tables = ArrayBuffer.empty[String]
    withClosable(connection.createStatement()) { statement =>
      val records = statement.executeQuery("select name from __stores_table")
      while (records.next) {
        tables += records.getString(1)
      }
      tables.toSeq
    }
  }

  def getStoreNumber(name: String): Option[Int] = {
    withClosable(connection.prepareStatement("select store_number from __stores_table where name=?")) { query =>
      query.setString(1, name)
      val records = query.executeQuery
      if (records.next) {
        Some(records.getInt(1))
      } else {
        None
      }
    }
  }

  def isCurrentStore(name: String): Boolean = {
    getStoreNumber(name) match {
      case Some(storeNumber) => currentStore == storeTableNameFromNumber(storeNumber)
      case None              => false
    }
  }

  def createStore(name: String): Int = {

    val storeNumber = getNextStoreNumber()

    withClosable(connection.prepareStatement(s"insert into __stores_table values (?, ?)")) { insert => {
      insert.setString(1, name)
      insert.setInt(2, storeNumber)
      insert.executeUpdate
    } }

    val storeName = storeTableNameFromNumber(storeNumber)
    withClosable(connection.createStatement()) { statement => {
      statement.execute(s"create table ${storeName}(key varchar(max), value varchar(max))")
      statement.execute(s"alter table ${storeName} add constraint ${storeName}_unique_keys unique(key)")
    } }

    storeNumber
  }

  def storeTableNameFromNumber(storeNumber: Int) = s"store_$storeNumber"

  def setStore(name: String): Unit = {
    if (name == "" || name == "Default Store") {
      currentStore = "default_store"
    } else {
      val storeNumber = getStoreNumber(name) match {
        case Some(n) => n
        case None    => createStore(name)
      }
      currentStore = storeTableNameFromNumber(storeNumber)
    }
  }

  def deleteStore(name: String, storeNumber: Int): Unit = {
    val storeName = storeTableNameFromNumber(storeNumber)

    withClosable(connection.prepareStatement(s"delete from __stores_table where name=?")) { remove =>
      remove.setString(1, name)
      remove.executeUpdate
    }

    withClosable(connection.createStatement()) { statement =>
      statement.execute(s"drop table ${storeName}")
    }
  }

  def deleteStore(name: String): Unit = {
    getStoreNumber(name) match {
      case Some(storeNumber) => deleteStore(name, storeNumber)
      case _ =>
    }
  }

  def getDatabaseKeys(): Seq[String] = {
    val keys = ArrayBuffer.empty[String]
    withClosable(connection.createStatement()) { statement =>
      val records = statement.executeQuery(s"select key from ${currentStore}")
      while (records.next) {
        keys += records.getString(1)
      }
      keys.toSeq
    }
  }

  def getDatabaseValueForKey(key: String): Option[String] = {
    withClosable(connection.prepareStatement(s"select value from ${currentStore} where key=?")) { query =>
      query.setString(1, key)
      val records = query.executeQuery
      if (records.next) {
        Some(records.getString(1))
      } else {
        None
      }
    }
  }

  def checkDatabaseForKey(key: String): Boolean = {
    withClosable(connection.prepareStatement(s"select count(*) > 0 from ${currentStore} where key=?")) { query => {
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
    withClosable(connection.prepareStatement(s"insert into ${currentStore} values (?, ?)")) { insert => {
      insert.setString(1, key)
      insert.setString(2, value)
      insert.executeUpdate
    } }
  }

  def updateDatabaseValueForKey(key: String, value: String): Unit = {
    withClosable(connection.prepareStatement(s"update ${currentStore} set value=? where key=?")) { update => {
      update.setString(1, value)
      update.setString(2, key)
      update.executeUpdate
    } }
  }

  def removeDatabaseValueForKey(key: String): Unit = {
    withClosable(connection.prepareStatement(s"delete from ${currentStore} where key=?")) { remove => {
      remove.setString(1, key)
      remove.executeUpdate
    } }
  }

  def clearDatabase(): Unit = {
    withClosable(connection.createStatement()) { statement => {
      statement.executeUpdate(s"truncate table ${currentStore}")
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
