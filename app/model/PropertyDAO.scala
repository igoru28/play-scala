package model

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Author igor on 21.05.16.
 */
case class Property(name: String, value: String)

@Singleton
class PropertyDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  class PropertiesTable(tag: Tag) extends Table[Property](tag, "PROPERTIES") {
    def name = column[String]("NAME", O.PrimaryKey)
    def value = column[String]("VALUE")
    override def * : ProvenShape[Property] = (name, value) <> (Property.tupled, Property.unapply _)
  }

  val properties = TableQuery[PropertiesTable]

  private def await[R](action : slick.dbio.DBIOAction[R, slick.dbio.NoStream, scala.Nothing]): R =
    Await.result(db.run(action), Duration.Inf)
  private def awaitUpdate[R](action : slick.dbio.DBIOAction[R, slick.dbio.NoStream, scala.Nothing]): Unit =
    Await.result(db.run(action).map{_ => ()}, Duration.Inf)
  def insertOrUpdate(property: Property) = Await.result(db.run{
    val filter = properties.filter(_.name === property.name)
    filter.result.headOption.flatMap{
      case Some(prop) => filter.update(prop.copy(prop.name, property.value))
      case None => properties += property
    }
  }, Duration.Inf)
  def insert(property: Property) = Await.result(db.run((properties += property)), Duration.Inf)
  def insert(props: Seq[Property]) = await((properties ++= props))
  def all = await(properties.result).map(Property.unapply(_).get).toMap


}
