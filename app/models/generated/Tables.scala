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
  import com.github.tototoshi.slick.MySQLJodaSupport._
  val schemaName: Option[String] = None
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{ GetResult => GR }

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(AuthToken.schema, LoginInfo.schema, OAuth2Info.schema, OAuth2InfoParam.schema, PasswordInfo.schema, ScratchCode.schema, SecurityRole.schema, TotpInfo.schema, User.schema, UserSecurityRole.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /**
   * Entity class storing rows of table AuthToken
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param tokenId Database column token_id SqlType(CHAR), Length(36,false)
   *  @param expiry Database column expiry SqlType(TIMESTAMP)
   */
  case class AuthTokenRow(userId: Long, tokenId: String, expiry: org.joda.time.DateTime) extends Entity[Long] {
    override def id = userId
    def tokenUuId = java.util.UUID.fromString(tokenId)
  }
  /** GetResult implicit for fetching AuthTokenRow objects using plain SQL queries */
  implicit def GetResultAuthTokenRow(implicit e0: GR[Long], e1: GR[String], e2: GR[org.joda.time.DateTime]): GR[AuthTokenRow] = GR {
    prs =>
      import prs._
      AuthTokenRow.tupled((<<[Long], <<[String], <<[org.joda.time.DateTime]))
  }
  /** Table description of table auth_token. Objects of this class serve as prototypes for rows in queries. */
  class AuthToken(_tableTag: Tag) extends profile.api.Table[AuthTokenRow](_tableTag, schemaName, "auth_token") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, tokenId, expiry) <> (AuthTokenRow.tupled, AuthTokenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(tokenId), Rep.Some(expiry))).shaped.<>({ r => import r._; _1.map(_ => AuthTokenRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column token_id SqlType(CHAR), Length(36,false) */
    val tokenId: Rep[String] = column[String]("token_id", O.Length(36, varying = false))
    /** Database column expiry SqlType(TIMESTAMP) */
    val expiry: Rep[org.joda.time.DateTime] = column[org.joda.time.DateTime]("expiry")

    /** Foreign key referencing LoginInfo (database name auth_token_ibfk_1) */
    lazy val loginInfoFk = foreignKey("auth_token_ibfk_1", userId, LoginInfo)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

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
  case class LoginInfoRow(userId: Long, providerId: String, providerKey: String, modified: Option[org.joda.time.DateTime] = None) extends Entity[Long] {
    override def id = userId
    def toExt = com.mohiva.play.silhouette.api.LoginInfo(providerId, providerKey)
  }
  /** GetResult implicit for fetching LoginInfoRow objects using plain SQL queries */
  implicit def GetResultLoginInfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[org.joda.time.DateTime]]): GR[LoginInfoRow] = GR {
    prs =>
      import prs._
      LoginInfoRow.tupled((<<[Long], <<[String], <<[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table login_info. Objects of this class serve as prototypes for rows in queries. */
  class LoginInfo(_tableTag: Tag) extends profile.api.Table[LoginInfoRow](_tableTag, schemaName, "login_info") with IdentifyableTable[Long] {
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
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))

    /** Foreign key referencing User (database name login_info_ibfk_1) */
    lazy val userFk = foreignKey("login_info_ibfk_1", userId, User)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    /** Index over (providerId,providerKey) (database name idx_provider_id_key) */
    val index1 = index("idx_provider_id_key", (providerId, providerKey))
  }
  /** Collection-like TableQuery object for table LoginInfo */
  lazy val LoginInfo = new TableQuery(tag => new LoginInfo(tag))

  /**
   * Entity class storing rows of table OAuth2Info
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param accessToken Database column access_token SqlType(VARCHAR), Length(200,true)
   *  @param tokenType Database column token_type SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param expiresIn Database column expires_in SqlType(INT), Default(None)
   *  @param refreshToken Database column refresh_token SqlType(VARCHAR), Length(200,true), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class OAuth2InfoRow(userId: Long, accessToken: String, tokenType: Option[String] = None, expiresIn: Option[Int] = None, refreshToken: Option[String] = None, modified: Option[org.joda.time.DateTime] = None) extends Entity[Long] {
    override def id = userId
    def toExt(params: Option[Map[String, String]]) = com.mohiva.play.silhouette.impl.providers.OAuth2Info(accessToken, tokenType, expiresIn, refreshToken, params)
  }
  /** GetResult implicit for fetching OAuth2InfoRow objects using plain SQL queries */
  implicit def GetResultOAuth2InfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[Int]], e4: GR[Option[org.joda.time.DateTime]]): GR[OAuth2InfoRow] = GR {
    prs =>
      import prs._
      OAuth2InfoRow.tupled((<<[Long], <<[String], <<?[String], <<?[Int], <<?[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table o_auth2_info. Objects of this class serve as prototypes for rows in queries. */
  class OAuth2Info(_tableTag: Tag) extends profile.api.Table[OAuth2InfoRow](_tableTag, schemaName, "o_auth2_info") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, accessToken, tokenType, expiresIn, refreshToken, modified) <> (OAuth2InfoRow.tupled, OAuth2InfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(accessToken), tokenType, expiresIn, refreshToken, modified)).shaped.<>({ r => import r._; _1.map(_ => OAuth2InfoRow.tupled((_1.get, _2.get, _3, _4, _5, _6))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column access_token SqlType(VARCHAR), Length(200,true) */
    val accessToken: Rep[String] = column[String]("access_token", O.Length(200, varying = true))
    /** Database column token_type SqlType(VARCHAR), Length(50,true), Default(None) */
    val tokenType: Rep[Option[String]] = column[Option[String]]("token_type", O.Length(50, varying = true), O.Default(None))
    /** Database column expires_in SqlType(INT), Default(None) */
    val expiresIn: Rep[Option[Int]] = column[Option[Int]]("expires_in", O.Default(None))
    /** Database column refresh_token SqlType(VARCHAR), Length(200,true), Default(None) */
    val refreshToken: Rep[Option[String]] = column[Option[String]]("refresh_token", O.Length(200, varying = true), O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))

    /** Foreign key referencing LoginInfo (database name o_auth2_info_ibfk_1) */
    lazy val loginInfoFk = foreignKey("o_auth2_info_ibfk_1", userId, LoginInfo)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table OAuth2Info */
  lazy val OAuth2Info = new TableQuery(tag => new OAuth2Info(tag))

  /**
   * Entity class storing rows of table OAuth2InfoParam
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param key Database column key SqlType(VARCHAR), Length(100,true)
   *  @param value Database column value SqlType(VARCHAR), Length(100,true)
   */
  case class OAuth2InfoParamRow(userId: Long, key: String, value: String) extends Entity[Long] { override def id = userId }
  /** GetResult implicit for fetching OAuth2InfoParamRow objects using plain SQL queries */
  implicit def GetResultOAuth2InfoParamRow(implicit e0: GR[Long], e1: GR[String]): GR[OAuth2InfoParamRow] = GR {
    prs =>
      import prs._
      OAuth2InfoParamRow.tupled((<<[Long], <<[String], <<[String]))
  }
  /** Table description of table o_auth2_info_param. Objects of this class serve as prototypes for rows in queries. */
  class OAuth2InfoParam(_tableTag: Tag) extends profile.api.Table[OAuth2InfoParamRow](_tableTag, schemaName, "o_auth2_info_param") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, key, value) <> (OAuth2InfoParamRow.tupled, OAuth2InfoParamRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(key), Rep.Some(value))).shaped.<>({ r => import r._; _1.map(_ => OAuth2InfoParamRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column key SqlType(VARCHAR), Length(100,true) */
    val key: Rep[String] = column[String]("key", O.Length(100, varying = true))
    /** Database column value SqlType(VARCHAR), Length(100,true) */
    val value: Rep[String] = column[String]("value", O.Length(100, varying = true))

    /** Primary key of OAuth2InfoParam (database name o_auth2_info_param_PK) */
    val pk = primaryKey("o_auth2_info_param_PK", (userId, key))

    /** Foreign key referencing OAuth2Info (database name o_auth2_info_param_ibfk_1) */
    lazy val oAuth2InfoFk = foreignKey("o_auth2_info_param_ibfk_1", userId, OAuth2Info)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table OAuth2InfoParam */
  lazy val OAuth2InfoParam = new TableQuery(tag => new OAuth2InfoParam(tag))

  /**
   * Entity class storing rows of table PasswordInfo
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED), PrimaryKey
   *  @param hasher Database column hasher SqlType(VARCHAR), Length(50,true)
   *  @param password Database column password SqlType(VARCHAR), Length(100,true)
   *  @param salt Database column salt SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class PasswordInfoRow(userId: Long, hasher: String, password: String, salt: Option[String] = None, modified: Option[org.joda.time.DateTime] = None) extends Entity[Long] {
    override def id = userId
    def toExt = com.mohiva.play.silhouette.api.util.PasswordInfo(hasher, password, salt)
  }
  /** GetResult implicit for fetching PasswordInfoRow objects using plain SQL queries */
  implicit def GetResultPasswordInfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[org.joda.time.DateTime]]): GR[PasswordInfoRow] = GR {
    prs =>
      import prs._
      PasswordInfoRow.tupled((<<[Long], <<[String], <<[String], <<?[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table password_info. Objects of this class serve as prototypes for rows in queries. */
  class PasswordInfo(_tableTag: Tag) extends profile.api.Table[PasswordInfoRow](_tableTag, schemaName, "password_info") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, hasher, password, salt, modified) <> (PasswordInfoRow.tupled, PasswordInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(hasher), Rep.Some(password), salt, modified)).shaped.<>({ r => import r._; _1.map(_ => PasswordInfoRow.tupled((_1.get, _2.get, _3.get, _4, _5))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val userId: Rep[Long] = column[Long]("user_id", O.PrimaryKey)
    /** Database column hasher SqlType(VARCHAR), Length(50,true) */
    val hasher: Rep[String] = column[String]("hasher", O.Length(50, varying = true))
    /** Database column password SqlType(VARCHAR), Length(100,true) */
    val password: Rep[String] = column[String]("password", O.Length(100, varying = true))
    /** Database column salt SqlType(VARCHAR), Length(50,true), Default(None) */
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Length(50, varying = true), O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))

    /** Foreign key referencing LoginInfo (database name password_info_ibfk_1) */
    lazy val loginInfoFk = foreignKey("password_info_ibfk_1", userId, LoginInfo)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table PasswordInfo */
  lazy val PasswordInfo = new TableQuery(tag => new PasswordInfo(tag))

  /**
   * Entity class storing rows of table ScratchCode
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param hasher Database column hasher SqlType(VARCHAR), Length(50,true)
   *  @param password Database column password SqlType(VARCHAR), Length(100,true)
   *  @param salt Database column salt SqlType(VARCHAR), Length(50,true), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class ScratchCodeRow(userId: Long, hasher: String, password: String, salt: Option[String] = None, modified: Option[org.joda.time.DateTime] = None) extends Entity[Long] {
    override def id = userId
    def toExt = com.mohiva.play.silhouette.api.util.PasswordInfo(hasher, password, salt)
  }
  /** GetResult implicit for fetching ScratchCodeRow objects using plain SQL queries */
  implicit def GetResultScratchCodeRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[org.joda.time.DateTime]]): GR[ScratchCodeRow] = GR {
    prs =>
      import prs._
      ScratchCodeRow.tupled((<<[Long], <<[String], <<[String], <<?[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table scratch_code. Objects of this class serve as prototypes for rows in queries. */
  class ScratchCode(_tableTag: Tag) extends profile.api.Table[ScratchCodeRow](_tableTag, schemaName, "scratch_code") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, hasher, password, salt, modified) <> (ScratchCodeRow.tupled, ScratchCodeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(hasher), Rep.Some(password), salt, modified)).shaped.<>({ r => import r._; _1.map(_ => ScratchCodeRow.tupled((_1.get, _2.get, _3.get, _4, _5))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column hasher SqlType(VARCHAR), Length(50,true) */
    val hasher: Rep[String] = column[String]("hasher", O.Length(50, varying = true))
    /** Database column password SqlType(VARCHAR), Length(100,true) */
    val password: Rep[String] = column[String]("password", O.Length(100, varying = true))
    /** Database column salt SqlType(VARCHAR), Length(50,true), Default(None) */
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Length(50, varying = true), O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))

    /** Foreign key referencing TotpInfo (database name scratch_code_ibfk_1) */
    lazy val totpInfoFk = foreignKey("scratch_code_ibfk_1", userId, TotpInfo)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table ScratchCode */
  lazy val ScratchCode = new TableQuery(tag => new ScratchCode(tag))

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
  class SecurityRole(_tableTag: Tag) extends profile.api.Table[SecurityRoleRow](_tableTag, schemaName, "security_role") with IdentifyableTable[Long] {
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
   * Entity class storing rows of table TotpInfo
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED), PrimaryKey
   *  @param sharedKey Database column shared_key SqlType(CHAR), Length(36,false)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class TotpInfoRow(userId: Long, sharedKey: String, modified: Option[org.joda.time.DateTime] = None) extends Entity[Long] {
    override def id = userId
    def toExt(scratchCodes: Seq[com.mohiva.play.silhouette.api.util.PasswordInfo]) = com.mohiva.play.silhouette.impl.providers.TotpInfo(sharedKey, scratchCodes)
  }
  /** GetResult implicit for fetching TotpInfoRow objects using plain SQL queries */
  implicit def GetResultTotpInfoRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[org.joda.time.DateTime]]): GR[TotpInfoRow] = GR {
    prs =>
      import prs._
      TotpInfoRow.tupled((<<[Long], <<[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table totp_info. Objects of this class serve as prototypes for rows in queries. */
  class TotpInfo(_tableTag: Tag) extends profile.api.Table[TotpInfoRow](_tableTag, schemaName, "totp_info") with IdentifyableTable[Long] {
    override def id = userId

    def * = (userId, sharedKey, modified) <> (TotpInfoRow.tupled, TotpInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(sharedKey), modified)).shaped.<>({ r => import r._; _1.map(_ => TotpInfoRow.tupled((_1.get, _2.get, _3))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val userId: Rep[Long] = column[Long]("user_id", O.PrimaryKey)
    /** Database column shared_key SqlType(CHAR), Length(36,false) */
    val sharedKey: Rep[String] = column[String]("shared_key", O.Length(36, varying = false))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))

    /** Foreign key referencing LoginInfo (database name totp_info_ibfk_1) */
    lazy val loginInfoFk = foreignKey("totp_info_ibfk_1", userId, LoginInfo)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table TotpInfo */
  lazy val TotpInfo = new TableQuery(tag => new TotpInfo(tag))

  /**
   * Entity class storing rows of table User
   *  @param id Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(50,true)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(50,true)
   *  @param birthDate Database column birth_date SqlType(DATE)
   *  @param gender Database column gender SqlType(ENUM), Length(6,false)
   *  @param email Database column email SqlType(VARCHAR), Length(100,true)
   *  @param phoneNumber Database column phone_number SqlType(VARCHAR), Length(20,true), Default(None)
   *  @param avatarUrl Database column avatar_url SqlType(VARCHAR), Length(200,true), Default(None)
   *  @param activated Database column activated SqlType(BIT), Default(false)
   *  @param lastLogin Database column last_login SqlType(TIMESTAMP), Default(None)
   *  @param modified Database column modified SqlType(TIMESTAMP), Default(None)
   */
  case class UserRow(id: Long, firstName: String, lastName: String, birthDate: org.joda.time.LocalDate, gender: String, email: String, phoneNumber: Option[String] = None, avatarUrl: Option[String] = None, activated: Boolean = false, lastLogin: Option[org.joda.time.DateTime] = None, modified: Option[org.joda.time.DateTime] = None) extends EntityAutoInc[Long, UserRow] with com.mohiva.play.silhouette.api.Identity {
    def fullName = s"$firstName $lastName"
  }
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Long], e1: GR[String], e2: GR[org.joda.time.LocalDate], e3: GR[Option[String]], e4: GR[Boolean], e5: GR[Option[org.joda.time.DateTime]]): GR[UserRow] = GR {
    prs =>
      import prs._
      UserRow.tupled((<<[Long], <<[String], <<[String], <<[org.joda.time.LocalDate], <<[String], <<[String], <<?[String], <<?[String], <<[Boolean], <<?[org.joda.time.DateTime], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, schemaName, "user") with IdentifyableTable[Long] {
    def * = (id, firstName, lastName, birthDate, gender, email, phoneNumber, avatarUrl, activated, lastLogin, modified) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(firstName), Rep.Some(lastName), Rep.Some(birthDate), Rep.Some(gender), Rep.Some(email), phoneNumber, avatarUrl, Rep.Some(activated), lastLogin, modified)).shaped.<>({ r => import r._; _1.map(_ => UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9.get, _10, _11))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column first_name SqlType(VARCHAR), Length(50,true) */
    val firstName: Rep[String] = column[String]("first_name", O.Length(50, varying = true))
    /** Database column last_name SqlType(VARCHAR), Length(50,true) */
    val lastName: Rep[String] = column[String]("last_name", O.Length(50, varying = true))
    /** Database column birth_date SqlType(DATE) */
    val birthDate: Rep[org.joda.time.LocalDate] = column[org.joda.time.LocalDate]("birth_date")
    /** Database column gender SqlType(ENUM), Length(6,false) */
    val gender: Rep[String] = column[String]("gender", O.Length(6, varying = false))
    /** Database column email SqlType(VARCHAR), Length(100,true) */
    val email: Rep[String] = column[String]("email", O.Length(100, varying = true))
    /** Database column phone_number SqlType(VARCHAR), Length(20,true), Default(None) */
    val phoneNumber: Rep[Option[String]] = column[Option[String]]("phone_number", O.Length(20, varying = true), O.Default(None))
    /** Database column avatar_url SqlType(VARCHAR), Length(200,true), Default(None) */
    val avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url", O.Length(200, varying = true), O.Default(None))
    /** Database column activated SqlType(BIT), Default(false) */
    val activated: Rep[Boolean] = column[Boolean]("activated", O.Default(false))
    /** Database column last_login SqlType(TIMESTAMP), Default(None) */
    val lastLogin: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("last_login", O.Default(None))
    /** Database column modified SqlType(TIMESTAMP), Default(None) */
    val modified: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("modified", O.Default(None))
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /**
   * Entity class storing rows of table UserSecurityRole
   *  @param userId Database column user_id SqlType(BIGINT UNSIGNED)
   *  @param securityRoleId Database column security_role_id SqlType(BIGINT UNSIGNED)
   */
  case class UserSecurityRoleRow(userId: Long, securityRoleId: Long)
  /** GetResult implicit for fetching UserSecurityRoleRow objects using plain SQL queries */
  implicit def GetResultUserSecurityRoleRow(implicit e0: GR[Long]): GR[UserSecurityRoleRow] = GR {
    prs =>
      import prs._
      UserSecurityRoleRow.tupled((<<[Long], <<[Long]))
  }
  /** Table description of table user_security_role. Objects of this class serve as prototypes for rows in queries. */
  class UserSecurityRole(_tableTag: Tag) extends profile.api.Table[UserSecurityRoleRow](_tableTag, schemaName, "user_security_role") {
    def * = (userId, securityRoleId) <> (UserSecurityRoleRow.tupled, UserSecurityRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(securityRoleId))).shaped.<>({ r => import r._; _1.map(_ => UserSecurityRoleRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(BIGINT UNSIGNED) */
    val userId: Rep[Long] = column[Long]("user_id")
    /** Database column security_role_id SqlType(BIGINT UNSIGNED) */
    val securityRoleId: Rep[Long] = column[Long]("security_role_id")

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
