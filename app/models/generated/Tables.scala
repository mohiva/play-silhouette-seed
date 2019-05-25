package models.generated
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import models.daos.generic._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = LoginInfo.schema ++ SecurityRole.schema ++ User.schema ++ UserLoginInfo.schema ++ UserSecurityRole.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table LoginInfo
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param providerId Database column provider_id SqlType(VARCHAR), Length(100,true)
   *  @param providerKey Database column provider_key SqlType(VARCHAR), Length(50,true)
   *  @param modified Database column modified SqlType(TIMESTAMP) */
  case class LoginInfoRow(id: Int, providerId: String, providerKey: String, modified: java.sql.Timestamp) 
  /** GetResult implicit for fetching LoginInfoRow objects using plain SQL queries */
  implicit def GetResultLoginInfoRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[LoginInfoRow] = GR{
    prs => import prs._
    LoginInfoRow.tupled((<<[Int], <<[String], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table login_info. Objects of this class serve as prototypes for rows in queries. */
  class LoginInfo(_tableTag: Tag) extends profile.api.Table[LoginInfoRow](_tableTag, Some("myappdb"), "login_info") {
              def * = (id, providerId, providerKey, modified) <> (LoginInfoRow.tupled, LoginInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(providerId), Rep.Some(providerKey), Rep.Some(modified))).shaped.<>({r=>import r._; _1.map(_=> LoginInfoRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column provider_id SqlType(VARCHAR), Length(100,true) */
    val providerId: Rep[String] = column[String]("provider_id", O.Length(100,varying=true))
    /** Database column provider_key SqlType(VARCHAR), Length(50,true) */
    val providerKey: Rep[String] = column[String]("provider_key", O.Length(50,varying=true))
    /** Database column modified SqlType(TIMESTAMP) */
    val modified: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("modified")

    /** Index over (providerId) (database name idx_provider_id) */
    val index1 = index("idx_provider_id", providerId)
    /** Index over (providerKey) (database name idx_provider_key) */
    val index2 = index("idx_provider_key", providerKey)
              }
  /** Collection-like TableQuery object for table LoginInfo */
  lazy val LoginInfo = new TableQuery(tag => new LoginInfo(tag))

  /** Entity class storing rows of table SecurityRole
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(255,true) */
  case class SecurityRoleRow(id: Int, name: String) extends EntityAutoInc[Int, SecurityRoleRow] 
  /** GetResult implicit for fetching SecurityRoleRow objects using plain SQL queries */
  implicit def GetResultSecurityRoleRow(implicit e0: GR[Int], e1: GR[String]): GR[SecurityRoleRow] = GR{
    prs => import prs._
    SecurityRoleRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table security_role. Objects of this class serve as prototypes for rows in queries. */
  class SecurityRole(_tableTag: Tag) extends profile.api.Table[SecurityRoleRow](_tableTag, Some("myappdb"), "security_role") with IdentifyableTable[Int] {
              def * = (id, name) <> (SecurityRoleRow.tupled, SecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name))).shaped.<>({r=>import r._; _1.map(_=> SecurityRoleRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
              }
  /** Collection-like TableQuery object for table SecurityRole */
  lazy val SecurityRole = new TableQuery(tag => new SecurityRole(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param dateOfBirth Database column date_of_birth SqlType(DATE), Default(None)
   *  @param username Database column username SqlType(VARCHAR), Length(100,true)
   *  @param email Database column email SqlType(VARCHAR), Length(100,true)
   *  @param avatarUrl Database column avatar_url SqlType(VARCHAR), Length(200,true)
   *  @param activated Database column activated SqlType(BIT), Default(false)
   *  @param lastLogin Database column last_login SqlType(TIMESTAMP)
   *  @param modified Database column modified SqlType(TIMESTAMP) */
  case class UserRow(id: Int, firstName: Option[String] = None, lastName: Option[String] = None, dateOfBirth: Option[java.sql.Date] = None, username: String, email: String, avatarUrl: String, activated: Boolean = false, lastLogin: java.sql.Timestamp, modified: java.sql.Timestamp) extends EntityAutoInc[Int, UserRow] 
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[java.sql.Date]], e3: GR[String], e4: GR[Boolean], e5: GR[java.sql.Timestamp]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<?[String], <<?[String], <<?[java.sql.Date], <<[String], <<[String], <<[String], <<[Boolean], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("myappdb"), "user") with IdentifyableTable[Int] {
              def * = (id, firstName, lastName, dateOfBirth, username, email, avatarUrl, activated, lastLogin, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), firstName, lastName, dateOfBirth, Rep.Some(username), Rep.Some(email), Rep.Some(avatarUrl), Rep.Some(activated), Rep.Some(lastLogin), Rep.Some(modified))).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first_name SqlType(VARCHAR), Length(50,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(50,varying=true), O.Default(None))
    /** Database column last_name SqlType(VARCHAR), Length(50,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(50,varying=true), O.Default(None))
    /** Database column date_of_birth SqlType(DATE), Default(None) */
    val dateOfBirth: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("date_of_birth", O.Default(None))
    /** Database column username SqlType(VARCHAR), Length(100,true) */
    val username: Rep[String] = column[String]("username", O.Length(100,varying=true))
    /** Database column email SqlType(VARCHAR), Length(100,true) */
    val email: Rep[String] = column[String]("email", O.Length(100,varying=true))
    /** Database column avatar_url SqlType(VARCHAR), Length(200,true) */
    val avatarUrl: Rep[String] = column[String]("avatar_url", O.Length(200,varying=true))
    /** Database column activated SqlType(BIT), Default(false) */
    val activated: Rep[Boolean] = column[Boolean]("activated", O.Default(false))
    /** Database column last_login SqlType(TIMESTAMP) */
    val lastLogin: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_login")
    /** Database column modified SqlType(TIMESTAMP) */
    val modified: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("modified")
              }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table UserLoginInfo
   *  @param userId Database column user_id SqlType(INT)
   *  @param loginInfoId Database column login_info_id SqlType(INT)
   *  @param modified Database column modified SqlType(TIMESTAMP) */
  case class UserLoginInfoRow(userId: Int, loginInfoId: Int, modified: java.sql.Timestamp) 
  /** GetResult implicit for fetching UserLoginInfoRow objects using plain SQL queries */
  implicit def GetResultUserLoginInfoRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[UserLoginInfoRow] = GR{
    prs => import prs._
    UserLoginInfoRow.tupled((<<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table user_login_info. Objects of this class serve as prototypes for rows in queries. */
  class UserLoginInfo(_tableTag: Tag) extends profile.api.Table[UserLoginInfoRow](_tableTag, Some("myappdb"), "user_login_info") {
              def * = (userId, loginInfoId, modified) <> (UserLoginInfoRow.tupled, UserLoginInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(loginInfoId), Rep.Some(modified))).shaped.<>({r=>import r._; _1.map(_=> UserLoginInfoRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column login_info_id SqlType(INT) */
    val loginInfoId: Rep[Int] = column[Int]("login_info_id")
    /** Database column modified SqlType(TIMESTAMP) */
    val modified: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("modified")

    /** Primary key of UserLoginInfo (database name user_login_info_PK) */
    val pk = primaryKey("user_login_info_PK", (userId, loginInfoId))

    /** Foreign key referencing LoginInfo (database name user_login_info_ibfk_2) */
    lazy val loginInfoFk = foreignKey("user_login_info_ibfk_2", loginInfoId, LoginInfo)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_login_info_ibfk_1) */
    lazy val userFk = foreignKey("user_login_info_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
              }
  /** Collection-like TableQuery object for table UserLoginInfo */
  lazy val UserLoginInfo = new TableQuery(tag => new UserLoginInfo(tag))

  /** Entity class storing rows of table UserSecurityRole
   *  @param userId Database column user_id SqlType(INT)
   *  @param securityRoleId Database column security_role_id SqlType(INT)
   *  @param modified Database column modified SqlType(TIMESTAMP) */
  case class UserSecurityRoleRow(userId: Int, securityRoleId: Int, modified: java.sql.Timestamp) 
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[UserSecurityRoleRow] = GR{
    prs => import prs._
    UserSecurityRoleRow.tupled((<<[Int], <<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends profile.api.Table[UserSecurityRoleRow](_tableTag, Some("myappdb"), "user_security_role") {
              def * = (userId, securityRoleId, modified) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(securityRoleId), Rep.Some(modified))).shaped.<>({r=>import r._; _1.map(_=> UserSecurityRoleRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column security_role_id SqlType(INT) */
    val securityRoleId: Rep[Int] = column[Int]("security_role_id")
    /** Database column modified SqlType(TIMESTAMP) */
    val modified: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("modified")

    /** Primary key of UserSecurityRole (database name user_security_role_PK) */
    val pk = primaryKey("user_security_role_PK", (userId, securityRoleId))

    /** Foreign key referencing SecurityRole (database name user_security_role_ibfk_2) */
    lazy val securityRoleFk = foreignKey("user_security_role_ibfk_2", securityRoleId, SecurityRole)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_security_role_ibfk_1) */
    lazy val userFk = foreignKey("user_security_role_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
              }
  /** Collection-like TableQuery object for table UserSecurityRole */
  lazy val UserSecurityRole = new TableQuery(tag => new UserSecurityRole(tag))
}
