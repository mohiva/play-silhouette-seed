package models.daos

import java.util.UUID
import org.specs2.mock._
import org.specs2.mutable._
import utils.AwaitUtil
import javax.inject.Inject
import models.generated.Tables.LoginInfoRow

class LoginInfoDaoSpec @Inject() (loginInfoDao: LoginInfoDao) extends Specification with Mockito with AwaitUtil {

  "Creating a new LoginInfo" should {
    "save it in the empty database" in {
      loginInfoDao.create(LoginInfoRow(0, UUID.randomUUID().toString, UUID.randomUUID().toString))
      loginInfoDao.findAll.size should beEqualTo(1)
    }
  }
}
