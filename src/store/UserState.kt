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
        fun getValueByCode(code:Int):UserRole? {
            return when(code) {
                1 -> USER
                2 -> ADMIN
                else -> null
            }
        }
        /**
         * Returns list of roles as HashMap<String,String>,
         * suitable to use as source for Dropdown list
         *
         * @returns Hashmap with intems in form of <roleCode,roleString>
         */
        fun getHashMap():HashMap<String,String> {
            val result = HashMap<String,String>()
            for (item in enumValues<UserRole>()) {
                result[item.getCodeByValue().toString()] = item.toString()
            }
            return result
        }
    }
    /**
     * Returns numeric code of role for selected role
     *
     * @return Int
     */
    fun getCodeByValue():Int {
        return when(this) {
            USER -> 1
            ADMIN -> 2
        }
    }
    /**
     * Returns text representation of role
     *
     * @return String
     */
    override fun toString():String {
        return when(this) {
            USER -> "User"
            ADMIN -> "Administrator"
        }
    }
}

/**
 * User genders definitions
 */
enum class Gender(val value:String):SmartEnum {
    M("M"),
    F( "F");
    companion object {
        /**
         * Function returns gender enum by code
         *
         * @return Gender
         */
        fun getValueByCode(code:String):Gender {
            return when (code) {
                "M" -> M
                "F" -> F
                else -> M
            }
        }
        /**
         * Returns list of roles as HashMap<String,String>,
         * suitable to use as source for Dropdown list
         *
         * @returns Hashmap with intems in form of <gender abbreviation,gender string>
         */
        fun getHashMap():HashMap<String,String> {
            val result = HashMap<String,String>()
            for (item in enumValues<Gender>()) {
                result[item.name] = item.getMessage()
            }
            return result
        }
    }

    /**
     * Function used to get string presentation of Gender object
     *
     * @returns String presenting gender object
     */
    override fun getMessage():String {
        return when(this) {
            M -> "Male"
            F -> "Female"
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