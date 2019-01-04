package migrator

import com.relexsolutions.assignment.migrator.dbapi.Column
import com.relexsolutions.assignment.migrator.dbapi.Database
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.*
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Files
import java.nio.file.Paths

class StringTest : StringSpec() {
  init {
    "low level API works" {
      db.addTable("users")

      val users = db.getTable("users")
      users.addColumn("username", Column.DataType.STRING)
      users.addColumn("password", Column.DataType.STRING)
      users.addColumn("age", Column.DataType.INT)
      users.addColumn("height", Column.DataType.FLOAT)

      val tables = db.tables.map { it.name }
      tables shouldBe listOf("users")

      val columns = users.columns.map { it.name }
      columns shouldBe listOf("username", "password", "age", "height")

      db.disconnect()
    }

    "method getTable returns useable table" {
      db.addTable("users")
      val users = db.getTable("users")
      users.addColumn("username", Column.DataType.STRING)
      users.addColumn("age", Column.DataType.INT)

      db.getTable("users").name shouldBe "users"
      db.getTable("users").columns.map { it.name } shouldBe listOf("username", "age")
    }

    "inserting data" {
      db.addTable("users")
      val users = db.getTable("users")
      users.addColumn("username", Column.DataType.STRING)
      users.addColumn("age", Column.DataType.INT)

      db.getTable("users").insert(mapOf(
        "username" to "foo",
        "age" to 16
      ))

      val fooUsers = db.getTable("users").findAllBy("username", "foo")
      fooUsers.size shouldBe 1
      fooUsers.first().shouldContain("age", 16)
    }

    "updating data" {
      db.addTable("users")
      val users = db.getTable("users")
      users.addColumn("username", Column.DataType.STRING)
      users.addColumn("age", Column.DataType.INT)

      db.getTable("users").insert(mapOf(
        "username" to "foo",
        "age" to 16
      ))
      // TODO
      // UPDATE age to 18
      // TRANSACTIONS!

      val fooUsers = db.getTable("users").findAllBy("username", "foo")
      fooUsers.size shouldBe 1
      fooUsers.first().shouldContain("age", 18)
    }

  }

  companion object Listener : TestListener {
    private val ROOT = Paths.get("dbs")
    lateinit var db: Database

    override fun beforeTest(description: Description): Unit {
      val dbDir = Files.createTempDirectory(ROOT.toAbsolutePath(), "dbapi")
      db = Database.connect(dbDir)
    }

    override fun afterTest(description: Description, result: TestResult) {
      db.disconnect()
    }
  }

  override fun listeners() = listOf(Listener)
}

