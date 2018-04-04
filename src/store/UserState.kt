package store
import lib.Action
import lib.State

/**
 * User role definitions
 */
enum class UserRole(val value:Int) {
    USER(1),
    ADMIN(2);
    companion object {
        /**
         * Function returns role enum by code of role
         *
         * @return UserRole
         */
        public fun getValueByCode(code:Int):UserRole {
            var result = USER
            when (code) {
                1 -> result = USER
                2 -> result = ADMIN
            }
            return result
        }
    }
}

/**
 * User genders definitions
 */
enum class Gender(val value:String) {
    M("M"),
    F( "F");
    companion object {
        /**
         * Function returns gender enum by code
         *
         * @return Gender
         */
        public fun getValueByCode(code:String):Gender {
            var result = M
            when (code) {
                "M" -> result = M
                "F" -> result = F
            }
            return result
        }
    }
}

/**
 * Abstract interface, which used as superclass for all actions
 * which change UserState
 */
interface UserStateAction: Action {}

/**
 * Function represents user state
 */
class UserState: State {

    // User ID, which used as a part of authentication token,sent with each request to WebSocket server
    var user_id = ""
    // Session ID, which used as a part of authentication token,sent with each request to WebSocket server
    var session_id = ""
    // Login of user in profile
    var login = ""
    // Email of user in profile
    var email = ""
    // First name of user in profile
    var first_name = ""
    // Last name of user in profile
    var last_name = ""
    // User data of birth
    var birthDate = 0
    // User default chat room
    var default_room = ""
    // User gender
    var gender = Gender.M
    // Determines if user logged or not
    var isLogin = false
    // User role
    var role = UserRole.USER

    /**
     * Actions, which used to send to reducer to change UserState fields
     */
    data class Change_user_id_Action(val user_id:String):UserStateAction
    data class Change_session_id_Action(val session_id:String):UserStateAction
    data class Change_login_Action(val login:String):UserStateAction
    data class Change_email_Action(val email:String):UserStateAction
    data class Change_first_name_Action(val first_name:String):UserStateAction
    data class Change_last_name_Action(val last_name:String):UserStateAction
    data class Change_birthDate_Action(val birthDate:Int):UserStateAction
    data class Change_default_room_Action(val default_room:String):UserStateAction
    data class Change_gender_Action(val gender:Gender):UserStateAction
    data class Change_isLogin_Action(val isLogin:Boolean):UserStateAction
    data class Change_role_Action(val role:UserRole):UserStateAction

    /**
     * Function returns custom string representation of UserState object
     *
     * @return String, representing all fields of UserState
     */
    override fun toString():String {
        return "{login:$login,user_id:$user_id,session_id:$session_id,email:$email,first_name:$first_name," +
                "last_name:$last_name,birthDate:$birthDate,default_room:$default_room,gender:$gender,isLogin:$isLogin," +
                "role:$role}"
    }
}