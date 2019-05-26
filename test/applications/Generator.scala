package applications

import scala.concurrent.ExecutionContext.Implicits._
import slick.codegen.SourceCodeGenerator
import slick.jdbc.MySQLProfile.backend.Database
import slick.jdbc.MySQLProfile
import slick.jdbc.meta.MTable

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

object Generator extends App {
  val slickProfile = "slick.jdbc.MySQLProfile"
  val jdbcDriver = "com.mysql.cj.jdbc.Driver"
  val url = "jdbc:mysql://localhost:3306/myappdb?useUnicode=true&searchpath=public&serverTimezone=CET"
  val outputDir = "./app/"
  val pkg = "models.generated"
  val username = "dev"
  val password = "12345"
  val pkType = "Long"
  /**
   * The table names to generate models for
   */
  val modelTables = Set(
    "user",
    "auth_token",
    "login_info",
    "security_role",
    "user_security_role"
  )

  val db = Database.forURL(url, username, password)
  val model = db.run(MySQLProfile.createModel(Some(
    MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW"))).map(_.filter { p: MTable => modelTables.contains(p.name.name) }))))
  // customize code generator
  val codegenFuture: Future[SourceCodeGenerator] = model.map(model => new SourceCodeGenerator(model) {
    override def code = "import models.daos.generic._\n" + super.code

    override def Table = new Table(_) {
      override type Column = ColumnDef

      override def EntityType = new EntityTypeDef {
        /* This code is adapted from the `EntityTypeDef` trait's `code` method
           within `AbstractSourceCodeGenerator`.
           All code is identical except for those lines which have a corresponding
           comment above them. */
        override def code = {
          val args = columns.map(c =>
            c.default.map(v =>
              s"${c.name}: ${c.exposedType} = $v"
            ).getOrElse(
              s"${c.name}: ${c.exposedType}"
            )
          ).mkString(", ")

          if (classEnabled) {
            /* `rowList` contains the names of the generated "Row" case classes we
                wish to have extend our `EntityAutoInc` trait. */
            val newParents = name match {
              case "UserRow" => parents ++ Seq("EntityAutoInc[%s, %s]".format(pkType, name))
              case "LoginInfoRow" => parents ++ Seq("Entity[%s]".format(pkType))
              case "AuthTokenRow" => parents ++ Seq("Entity[%s]".format(pkType))
              case "SecurityRoleRow" => parents ++ Seq("EntityAutoInc[%s, %s]".format(pkType, name))
              /* override existing Silhouette case classes */
              //case "LoginInfoRow" => parents ++ Seq("com.mohiva.play.silhouette.api.LoginInfo(providerId, providerKey)")

              case _ => parents
            }

            /* Use our modified parent class sequence in place of the old one. */
            val prns = (newParents.take(1).map(" extends " + _) ++ newParents.drop(1).map(" with " + _)).mkString("")
            val newBody = name match {
              case "UserRow" => "{\n" +
                                "  def name = {\n" +
                                "    (firstName -> lastName) match {\n" +
                                "      case (Some(f), Some(l)) => Some(f + \" \" + l)\n" +
                                "      case (Some(f), None) => Some(f)\n" +
                                "      case (None, Some(l)) => Some(l)\n" +
                                "      case _ => None\n" +
                                "    }\n" +
                                "  }\n" +
                                "}"
              case "LoginInfoRow" => "{ override def id = userId }"
              case "AuthTokenRow" => "{ override def id = userId }"
              case _ => ""
            }
            s"""case class $name($args)$prns $newBody"""
          } else {
            s"""type $name = $types
              /** Constructor for $name providing default values if available in the database schema. */
              def $name($args): $name = {
              ${compoundValue(columns.map(_.name))}
              }
            """.trim
          }
        }
      }

      override def TableClass = new TableClassDef {
        /* This code is adapted from the `TableClassDef` trait's `code` method
           within `AbstractSourceCodeGenerator`.
           All code is identical except for those lines which have a corresponding
           comment above them. */
        override def code = {
          /* `tableList` contains the names of the generated table classes we
              wish to have extend our `IdentifyableTable` trait. */
          val newParents = name match {
            case "User" => parents :+ s"IdentifyableTable[$pkType]"
            case "LoginInfo" => parents :+ s"IdentifyableTable[$pkType]"
            case "AuthToken" => parents :+ s"IdentifyableTable[$pkType]"
            case "SecurityRole" => parents :+ s"IdentifyableTable[$pkType]"
            case _ => parents
          }

          /* Use our modified parent class sequence in place of the old one. */
          val prns = newParents.map(" with " + _).mkString("")
          val args = model.name.schema.map(n => s"""Some("$n")""") ++ Seq("\"" + model.name.table + "\"")
          val newBody: Seq[Seq[String]] = name match {
            case "LoginInfo" => Seq("override def id = userId") +: body
            case "AuthToken" => Seq("override def id = userId") +: body
            case _ => body
          }
          s"""class ${name}(_tableTag: Tag) extends profile.api.Table[$elementType](_tableTag, ${args.mkString(", ")})$prns {
            ${indent(newBody.map(_.mkString("\n")).mkString("\n\n"))}
            }
          """.trim()
        }
      }
    }
  })
  Await.result(codegenFuture, Duration.Inf).writeToFile(slickProfile, outputDir, pkg, "Tables", "Tables.scala")
}