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
  import slick.jdbc.{ GetResult => GR }

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = AuthToken.schema ++ LoginInfo.schema ++ SecurityRole.schema ++ User.schema ++ UserSecurityRole.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /**
   * Entity class storing rows of table AuthToken
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param tokenId Database column token_id SqlType(CHAR), Length(36,false)
   *  @param expiry Database column expiry SqlType(TIMESTAMP)
   */
  case class AuthTokenRow(userId: Long, tokenId: String, expiry: java.sql.Timestamp) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching AuthTokenRow objects using plain SQL queries */
  implicit def GetResultAuthTokenRow(implicit e0: GR[Long], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[AuthTokenRow] = GR {
    prs =>
      import prs._
      AuthTokenRow.tupled((<<[Long], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table auth_token. Objects of this class serve as prototypes for rows in queries. */
  class AuthToken(_tableTag: Tag) extends profile.api.Table[AuthTokenRow](_tableTag, Some("myappdb"), "auth_token") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, tokenId, expiry) <> (AuthTokenRow.tupled, AuthTokenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(tokenId), Rep.Some(expiry))).shaped.<>({ r => import r._; _1.map(_ => AuthTokenRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column token_id SqlType(CHAR), Length(36,false) */
    val tokenId: Rep[String] = column[String]("token_id", O.Length(36, varying = false))
    /** Database column expiry SqlType(TIMESTAMP) */
    val expiry: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expiry")

    /** Foreign key referencing User (database name auth_token_ibfk_1) */
    lazy val userFk = foreignKey("auth_token_ibfk_1", userId, User)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    /** Index over (tokenId) (database name idx_token_id) */
    val index1 = index("idx_token_id", tokenId)
  }
  /** Collection-like TableQuery object for table AuthToken */
  lazy val AuthToken = new TableQuery(tag => new AuthToken(tag))

  /**
   * Entity class storing rows of table LoginInfo
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param providerId Database column provider_id SqlType(CHAR), Length(36,false)
   *  @param providerKey Database column provider_key SqlType(CHAR), Length(36,false)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class LoginInfoRow(userId: Long, providerId: String, providerKey: String, modified: Option[java.sql.Timestamp] = None) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching LoginInfoRow objects using plain SQL queries */
  implicit def GetResultLoginInfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[java.sql.Timestamp]]): GR[LoginInfoRow] = GR {
    prs =>
      import prs._
      LoginInfoRow.tupled((<<[Long], <<[String], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table login_info. Objects of this class serve as prototypes for rows in queries. */
  class LoginInfo(_tableTag: Tag) extends profile.api.Table[LoginInfoRow](_tableTag, Some("myappdb"), "login_info") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, providerId, providerKey, modified) <> (LoginInfoRow.tupled, LoginInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(providerId), Rep.Some(providerKey), modified)).shaped.<>({ r => import r._; _1.map(_ => LoginInfoRow.tupled((_1.get, _2.get, _3.get, _4))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column provider_id SqlType(CHAR), Length(36,false) */
    val providerId: Rep[String] = column[String]("provider_id", O.Length(36, varying = false))
    /** Database column provider_key SqlType(CHAR), Length(36,false) */
    val providerKey: Rep[String] = column[String]("provider_key", O.Length(36, varying = false))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Foreign key referencing User (database name login_info_ibfk_1) */
    lazy val userFk = foreignKey("login_info_ibfk_1", userId, User)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    /** Index over (providerId,providerKey) (database name idx_provider_id_key) */
    val index1 = index("idx_provider_id_key", (providerId, providerKey))
  }
  /** Collection-like TableQuery object for table LoginInfo */
  lazy val LoginInfo = new TableQuery(tag => new LoginInfo(tag))

  /**
   * Entity class storing rows of table SecurityRole
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(50,true)
   */
  case class SecurityRoleRow(id: Long, name: String) extends EntityAutoInc[Long, SecurityRoleRow]
  /** GetResult implicit for fetching SecurityRoleRow objects using plain SQL queries */
  implicit def GetResultSecurityRoleRow(implicit e0: GR[Long], e1: GR[String]): GR[SecurityRoleRow] = GR {
    prs =>
      import prs._
      SecurityRoleRow.tupled((<<[Long], <<[String]))
  }
  /** Table description of table security_role. Objects of this class serve as prototypes for rows in queries. */
  class SecurityRole(_tableTag: Tag) extends profile.api.Table[SecurityRoleRow](_tableTag, Some("myappdb"), "security_role") with IdentifyableTable[Long] {
    def * = (id, name) <> (SecurityRoleRow.tupled, SecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name))).shaped.<>({ r => import r._; _1.map(_ => SecurityRoleRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50, varying = true))
  }
  /** Collection-like TableQuery object for table SecurityRole */
  lazy val SecurityRole = new TableQuery(tag => new SecurityRole(tag))

  /**
   * Entity class storing rows of table User
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param dateOfBirth Database column date_of_birth SqlType(DATE), Default(None)
   *  @param username Database column username SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param email Database column email SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param avatarUrl Database column avatar_url SqlType(VARCHAR), Length(200,true), Default(None)
   *  @param activated Database column activated SqlType(BIT), Default(false)
   *  @param lastLogin Database column last_login SqlType(TIMESTAMP), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class UserRow(id: Long, firstName: Option[String] = None, lastName: Option[String] = None, dateOfBirth: Option[java.sql.Date] = None, username: Option[String] = None, email: Option[String] = None, avatarUrl: Option[String] = None, activated: Boolean = false, lastLogin: Option[java.sql.Timestamp] = None, modified: Option[java.sql.Timestamp] = None) extends EntityAutoInc[Long, UserRow] with com.mohiva.play.silhouette.api.Identity {
    def fullName = {
      (firstName -> lastName) match {
        case (Some(f), Some(l)) => Some(f + " " + l)
        case (Some(f), None) => Some(f)
        case (None, Some(l)) => Some(l)
        case _ => None
      }
    }
  }
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[java.sql.Date]], e3: GR[Boolean], e4: GR[Option[java.sql.Timestamp]]): GR[UserRow] = GR {
    prs =>
      import prs._
      UserRow.tupled((<<[Long], <<?[String], <<?[String], <<?[java.sql.Date], <<?[String], <<?[String], <<?[String], <<[Boolean], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("myappdb"), "user") with IdentifyableTable[Long] {
    def * = (id, firstName, lastName, dateOfBirth, username, email, avatarUrl, activated, lastLogin, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), firstName, lastName, dateOfBirth, username, email, avatarUrl, Rep.Some(activated), lastLogin, modified)).shaped.<>({ r => import r._; _1.map(_ => UserRow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8.get, _9, _10))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first_name SqlType(VARCHAR), Length(50,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(50, varying = true), O.Default(None))
    /** Database column last_name SqlType(VARCHAR), Length(50,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(50, varying = true), O.Default(None))
    /** Database column date_of_birth SqlType(DATE), Default(None) */
    val dateOfBirth: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("date_of_birth", O.Default(None))
    /** Database column username SqlType(VARCHAR), Length(100,true), Default(None) */
    val username: Rep[Option[String]] = column[Option[String]]("username", O.Length(100, varying = true), O.Default(None))
    /** Database column email SqlType(VARCHAR), Length(100,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(100, varying = true), O.Default(None))
    /** Database column avatar_url SqlType(VARCHAR), Length(200,true), Default(None) */
    val avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url", O.Length(200, varying = true), O.Default(None))
    /** Database column activated SqlType(BIT), Default(false) */
    val activated: Rep[Boolean] = column[Boolean]("activated", O.Default(false))
    /** Database column last_login SqlType(TIMESTAMP), Default(None) */
    val lastLogin: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("last_login", O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /**
   * Entity class storing rows of table UserSecurityRole
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param securityRoleId Database column security_role_id SqlType(BIGINT UNSIGNED)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class UserSecurityRoleRow(userId: Long, securityRoleId: Long, modified: Option[java.sql.Timestamp] = None)
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]]): GR[UserSecurityRoleRow] = GR {
    prs =>
      import prs._
      UserSecurityRoleRow.tupled((<<[Long], <<[Long], <<?[java.sql.Timestamp]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends profile.api.Table[UserSecurityRoleRow](_tableTag, Some("myappdb"), "user_security_role") {
    def * = (userId, securityRoleId, modified) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(securityRoleId), modified)).shaped.<>({ r => import r._; _1.map(_ => UserSecurityRoleRow.tupled((_1.get, _2.get, _3))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_role_id SqlType(BIGINT UNSIGNED) */
    val securityRoleId: Rep[Long] = column[Long]("security_role_id")
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("modified", O.Default(None))

    /** Primary key of UserSecurityRole (database name user_security_role_PK) */
    val pk = primaryKey("user_security_role_PK", (userId, securityRoleId))

    /** Foreign key referencing SecurityRole (database name user_security_role_ibfk_2) */
    lazy val securityRoleFk = foreignKey("user_security_role_ibfk_2", securityRoleId, SecurityRole)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
    /** Foreign key referencing User (database name user_security_role_ibfk_1) */
    lazy val userFk = foreignKey("user_security_role_ibfk_1", userId, User)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table UserSecurityRole */
  lazy val UserSecurityRole = new TableQuery(tag => new UserSecurityRole(tag))
}
