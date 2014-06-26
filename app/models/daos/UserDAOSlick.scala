package models.daos

import models.User
import com.mohiva.play.silhouette.core.LoginInfo
import scala.slick.driver.MySQLDriver.simple._
import scala.concurrent.Future
import java.util.UUID

/**
 * Give access to the user object using Slick
 */
class UserDAOSlick extends UserDAO {
  
  case class DBUser (
    userID: String,
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String]
  )

  class Users(tag: Tag) extends Table[DBUser](tag, "user") {
    def id = column[String]("userID", O.PrimaryKey)
    def firstName = column[Option[String]]("firstName")
    def lastName = column[Option[String]]("lastName")
    def fullName = column[Option[String]]("fullName")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatarURL")
    def * = (id, firstName, lastName, fullName, email, avatarURL) <> (DBUser.tupled, DBUser.unapply _)
  }
  
  case class DBLoginInfo (
    id: Option[Long],
    providerID: String,
    providerKey: String,
    userID: String
  )
  
  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "logininfo") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("providerID")
    def providerKey = column[String]("providerKey")
    def userID = column[String]("userID", O.NotNull)
    def * = (id.?, providerID, providerKey, userID) <> (DBLoginInfo.tupled, DBLoginInfo.unapply _)
  }
  
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]

  val db = Database.forConfig("db.default")
  
  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    db withSession { implicit session =>
      Future.successful {
        slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).firstOption match {
          case Some(info) => {
            slickUsers.filter(
              _.id === info.userID
            ).firstOption match {
              case Some(user) => {
                Some(User(UUID.fromString(user.userID), loginInfo, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL))
              }
              case None => None
            }
          }
          case None => None
        }
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = {
    db withSession { implicit session =>
      Future.successful {
        slickUsers.filter(
          _.id === userID.toString()
        ).firstOption match {
          case Some(user) => {
            slickLoginInfos.filter(
              _.userID === user.userID
            ).firstOption match {
              case Some(info) => {
                Some(User(UUID.fromString(user.userID), LoginInfo(info.providerID, info.providerKey), user.firstName, user.lastName, user.fullName, user.email, user.avatarURL))
              }
              case None => None
            }
          }
          case None => None
        }
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    db withSession { implicit session =>
      Future.successful {
        val dbUser = DBUser(user.userID.toString(), user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
        slickUsers.insertOrUpdate(dbUser)
        slickLoginInfos.filter(
          info => info.providerID === user.loginInfo.providerID && info.providerKey === user.loginInfo.providerKey
        ).firstOption match {
          case Some(info) => {
            slickLoginInfos.insertOrUpdate(DBLoginInfo(info.id, user.loginInfo.providerID, user.loginInfo.providerKey, user.userID.toString()))
          }
          case None => {
            slickLoginInfos.insertOrUpdate(DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey, user.userID.toString()))
          }
        }
        user // We do not change the user => return it
      }
    }
  }
}

/**
 * The companion object.
object UserDAOSlick {
}
 */
