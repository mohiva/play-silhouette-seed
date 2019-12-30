import com.mohiva.play.silhouette.api.actions.{ SecuredRequest, UserAwareRequest }
import utils.auth.DefaultEnv

package object controllers {
  type SecuredEnvRequest[A] = SecuredRequest[DefaultEnv, A]
  type UserAwareEnvRequest[A] = UserAwareRequest[DefaultEnv, A]
}
