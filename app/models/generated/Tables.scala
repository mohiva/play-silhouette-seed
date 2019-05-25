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
  lazy val schema: profile.SchemaDescription = Array(AuthToken.schema, LoginInfo.schema, SecurityRole.schema, User.schema, UserLoginInfo.schema, UserSecurityRole.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table AuthToken
   *  @param tokenId Database column token_id SqlType(CHAR), Length(36,false), Default(None)
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param expiry Database column expiry SqlType(TIMESTAMP) */
  case class AuthTokenRow(tokenId: Option[String] = None, userId: Long, expiry: java.sql.Timestamp) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching AuthTokenRow objects using plain SQL queries */
  implicit def GetResultAuthTokenRow(implicit e0: GR[Option[String]], e1: GR[Long], e2: GR[java.sql.Timestamp]): GR[AuthTokenRow] = GR{
    prs => import prs._
    AuthTokenRow.tupled((<<?[String], <<[Long], <<[java.sql.Timestamp]))
  }
  /** Table description of table auth_token. Objects of this class serve as prototypes for rows in queries. */
  class AuthToken(_tableTag: Tag) extends profile.api.Table[AuthTokenRow](_tableTag, Some("myappdb"), "auth_token") with IdentifyableTable[Long] {
              override def id = userId

    def * = (tokenId, userId, expiry) <> (AuthTokenRow.tupled, AuthTokenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((tokenId, Rep.Some(userId), Rep.Some(expiry))).shaped.<>({r=>import r._; _2.map(_=> AuthTokenRow.tupled((_1, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column token_id SqlType(CHAR), Length(36,false), Default(None) */
    val tokenId: Rep[Option[String]] = column[Option[String]]("token_id", O.Length(36,varying=false), O.Default(None))
    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column expiry SqlType(TIMESTAMP) */
    val expiry: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expiry")

    /** Foreign key referencing User (database name auth_token_ibfk_1) */
    lazy val userFk = foreignKey("auth_token_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)

    /** Index over (tokenId) (database name idx_token_id) */
    val index1 = index("idx_token_id", tokenId)
              }
  /** Collection-like TableQuery object for table AuthToken */
  lazy val AuthToken = new TableQuery(tag => new AuthToken(tag))

  /** Entity class storing rows of table LoginInfo
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param providerId Database column provider_id SqlType(CHAR), Length(36,false)
   *  @param providerKey Database column provider_key SqlType(CHAR), Length(36,false)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None) */
  case class LoginInfoRow(id: Long, providerId: String, providerKey: String, modified: Option[java.sql.Timestamp] = None) extends EntityAutoInc[Long, LoginInfoRow] 
  /** GetResult implicit for fetching LoginInfoRow objects using plain SQL queries */
  implicit def GetResultLoginInfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[java.sql.Timestamp]]): GR[LoginInfoRow] = GR{
    prs => import prs._
    LoginInfoRow.tupled((<<[Long], <<[String], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table login_info. Objects of this class serve as prototypes for rows in queries. */
  class LoginInfo(_tableTag: Tag) extends profile.api.Table[LoginInfoRow](_tableTag, Some("myappdb"), "login_info") with IdentifyableTable[Long] {
              def * = (id, providerId, providerKey, modified) <> (LoginInfoRow.tupled, LoginInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(providerId), Rep.Some(providerKey), modified)).shaped.<>({r=>import r._; _1.map(_=> LoginInfoRow.tupled((_1.get, _2.get, _3.get, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column provider_id SqlType(CHAR), Length(36,false) */
    val providerId: Rep[String] = column[String]("provider_id", O.Length(36,varying=false))
    /** Database column provider_key SqlType(CHAR), Length(36,false) */
    val providerKey: Rep[String] = column[String]("provider_key", O.Length(36,varying=false))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Index over (providerId) (database name idx_provider_id) */
    val index1 = index("idx_provider_id", providerId)
    /** Index over (providerKey) (database name idx_provider_key) */
    val index2 = index("idx_provider_key", providerKey)
              }
  /** Collection-like TableQuery object for table LoginInfo */
  lazy val LoginInfo = new TableQuery(tag => new LoginInfo(tag))

  /** Entity class storing rows of table SecurityRole
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(50,true) */
  case class SecurityRoleRow(id: Long, name: String) extends EntityAutoInc[Long, SecurityRoleRow] 
  /** GetResult implicit for fetching SecurityRoleRow objects using plain SQL queries */
  implicit def GetResultSecurityRoleRow(implicit e0: GR[Long], e1: GR[String]): GR[SecurityRoleRow] = GR{
    prs => import prs._
    SecurityRoleRow.tupled((<<[Long], <<[String]))
  }
  /** Table description of table security_role. Objects of this class serve as prototypes for rows in queries. */
  class SecurityRole(_tableTag: Tag) extends profile.api.Table[SecurityRoleRow](_tableTag, Some("myappdb"), "security_role") with IdentifyableTable[Long] {
              def * = (id, name) <> (SecurityRoleRow.tupled, SecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name))).shaped.<>({r=>import r._; _1.map(_=> SecurityRoleRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
              }
  /** Collection-like TableQuery object for table SecurityRole */
  lazy val SecurityRole = new TableQuery(tag => new SecurityRole(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param dateOfBirth Database column date_of_birth SqlType(DATE), Default(None)
   *  @param username Database column username SqlType(VARCHAR), Length(100,true)
   *  @param email Database column email SqlType(VARCHAR), Length(100,true)
   *  @param avatarUrl Database column avatar_url SqlType(VARCHAR), Length(200,true)
   *  @param activated Database column activated SqlType(BIT), Default(false)
   *  @param lastLogin Database column last_login SqlType(TIMESTAMP), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None) */
  case class UserRow(id: Long, firstName: Option[String] = None, lastName: Option[String] = None, dateOfBirth: Option[java.sql.Date] = None, username: String, email: String, avatarUrl: String, activated: Boolean = false, lastLogin: Option[java.sql.Timestamp] = None, modified: Option[java.sql.Timestamp] = None) extends EntityAutoInc[Long, UserRow] 
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Date]], e3: GR[String], e4: GR[Boolean], e5: GR[Option[java.sql.Timestamp]]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Long], <<?[String], <<?[String], <<?[java.sql.Date], <<[String], <<[String], <<[String], <<[Boolean], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("myappdb"), "user") with IdentifyableTable[Long] {
              def * = (id, firstName, lastName, dateOfBirth, username, email, avatarUrl, activated, lastLogin, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), firstName, lastName, dateOfBirth, Rep.Some(username), Rep.Some(email), Rep.Some(avatarUrl), Rep.Some(activated), lastLogin, modified)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5.get, _6.get, _7.get, _8.get, _9, _10)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
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
    /** Database column last_login SqlType(TIMESTAMP), Default(None) */
    val lastLogin: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("last_login", O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))
              }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table UserLoginInfo
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param loginInfoId Database column login_info_id SqlType(BIGINT UNSIGNED)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None) */
  case class UserLoginInfoRow(userId: Long, loginInfoId: Long, modified: Option[java.sql.Timestamp] = None) 
  /** GetResult implicit for fetching UserLoginInfoRow objects using plain SQL queries */
  implicit def GetResultUserLoginInfoRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserLoginInfoRow] = GR{
    prs => import prs._
    UserLoginInfoRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_login_info. Objects of this class serve as prototypes for rows in queries. */
  class UserLoginInfo(_tableTag: Tag) extends profile.api.Table[UserLoginInfoRow](_tableTag, Some("myappdb"), "user_login_info") {
              def * = (userId, loginInfoId, modified) <> (UserLoginInfoRow.tupled, UserLoginInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(loginInfoId), modified)).shaped.<>({r=>import r._; _1.map(_=> UserLoginInfoRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column login_info_id SqlType(BIGINT UNSIGNED) */
    val loginInfoId: Rep[Long] = column[Long]("login_info_id")
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

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
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param securityRoleId Database column security_role_id SqlType(BIGINT UNSIGNED)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None) */
  case class UserSecurityRoleRow(userId: Long, securityRoleId: Long, modified: Option[java.sql.Timestamp] = None) 
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityRoleRow] = GR{
    prs => import prs._
    UserSecurityRoleRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends profile.api.Table[UserSecurityRoleRow](_tableTag, Some("myappdb"), "user_security_role") {
              def * = (userId, securityRoleId, modified) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(securityRoleId), modified)).shaped.<>({r=>import r._; _1.map(_=> UserSecurityRoleRow.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_role_id SqlType(BIGINT UNSIGNED) */
    val securityRoleId: Rep[Long] = column[Long]("security_role_id")
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

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
