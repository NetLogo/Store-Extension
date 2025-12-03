// (C) Uri Wilensky. https://github.com/NetLogo/Python-Extension

package org.nlogo.extensions.store

import org.scalatest.BeforeAndAfterAll

import java.io.File
import org.nlogo.headless.TestLanguage

object Tests {
  val testFileNames = Seq("tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles) with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    val file = new File("tmp/store")
    def deleteRec(f: File): Unit = {
      if (f.isDirectory) {
        f.listFiles().foreach(deleteRec)
      }
      f.delete()
    }
    deleteRec(file)
  }
}
