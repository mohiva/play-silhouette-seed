package models.daos

import constants.SecurityRoleKeys
import models.generated.Tables._
import play.api.test.WithApplication

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test suite for the [[SecurityRoleDaoImpl]]
 */
class SecurityRoleDaoSpec extends DaoSpecLike {
  sequential

  "The security role dao" should {
    "find the correct roles for an user" in new Context {
      val (user, securityRoles): (UserRow, Seq[SecurityRoleRow]) = for {
        user <- userDao.create(testUser, testLoginInfo)
        securityRoles <- securityRoleDao.find(user)
      } yield (user, securityRoles)

      securityRoles.size should beEqualTo(1)
      securityRoles.head.name should beEqualTo(SecurityRoleKeys.default.toString)
    }

    "correctly grants a security role to an user" in new Context {
      val user = await(userDao.create(testUser, testLoginInfo))
      val adminRoleOpt = await(securityRoleDao.findAll()).filter(_.name == SecurityRoleKeys.ADMINISTRATOR.toString).headOption

      adminRoleOpt should not be None

      val result = await(securityRoleDao.grantRole(user, adminRoleOpt.get))
      result should beEqualTo(1)

      val securityRoles = await(securityRoleDao.find(user))
      securityRoles.size should beEqualTo(2)
      securityRoles.filter(_.name == SecurityRoleKeys.ADMINISTRATOR.toString).size should beEqualTo(1)

    }
  }

  /**
   * Context reused by all tests
   */
  trait Context extends WithApplication with DaoSpecScope {
    val securityRoleDao: SecurityRoleDao = daoContext.securityRoleDao

    // ensure repeatability of the test
    await(userDao.deleteAll)
  }
}
