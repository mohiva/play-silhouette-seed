package constants

object SecurityRoleKeys extends Enumeration {
  type Type = Value
  val USER_ROLE = Value("user")
  val ADMINISTRATOR_ROLE = Value("administrator")

  def default = USER_ROLE
}
