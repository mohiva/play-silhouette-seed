package constants

object SecurityRoleKeys extends Enumeration {
  type Type = Value
  val USER = Value("user")
  val ADMINISTRATOR = Value("administrator")

  def default = USER
}
