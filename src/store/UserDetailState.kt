package store

import lib.Action
import lib.State
import utils.stringifyJSON

/**
 * Class which holds Redux state for UserDetail page
 */
class UserDetailState: State {

    // ID of user, which details displayed (if null, then form works as "Add new user")
    var user_id:String? = null
    // Login of user
    var login = ""
    // Email of user
    var email = ""
    // Password of user
    var password = ""
    // Confirm password
    var confirm_password = ""
    // Chat Room, which user enters by default when login
    var default_room = ""
    // Gender of user
    var gender = Gender.M
    // Date of Birth of user as Integer timestamp
    var birthDate = 0
    // Is Account active flage.
    var active = false
    // Role of user 1 - USER, 2 - ADMIN
    var role = 1
    // First name of user
    var first_name = ""
    // Last name of user
    var last_name = ""
    // Is some action is going (progress indicator showed on screen)
    var showProgressIndicator = false
    // Message, which displayed in case of successful action finish (e.g. user saved successfully)
    var successMessage = ""
    // Array of validation or server errors, related to each field of form (or to whole form in general)
    var errors = HashMap<String,UserDetailError>()
    // Array of rooms for "Rooms" dropdown
    var rooms = HashMap<String,String>()

    /**
     * Actions, which can be sent to Reducer to change state fields
     */
    data class Change_user_id_action(val user_id:String):UserDetailAction
    data class Change_login_action(val login:String):UserDetailAction
    data class Change_email_action(val email:String):UserDetailAction
    data class Change_password_action(val password:String):UserDetailAction
    data class Change_confirm_password_action(val confirm_password:String):UserDetailAction
    data class Change_default_room_action(val default_room:String):UserDetailAction
    data class Change_gender_action(val gender: Gender):UserDetailAction
    data class Change_birthDate_action(val birthDate: Int):UserDetailAction
    data class Change_active_action(val active: Boolean):UserDetailAction
    data class Change_role_action(val role: Int):UserDetailAction
    data class Change_first_name_action(val first_name:String):UserDetailAction
    data class Change_last_name_action(val last_name:String):UserDetailAction
    data class Change_showProgressIndicator_action(val showProgressIndicator:Boolean):UserDetailAction
    data class Change_successMessage_action(val successMessage:String):UserDetailAction
    data class Change_errors_action(val errors:HashMap<String,UserDetailError>):UserDetailAction
    data class Change_rooms_action(val rooms:HashMap<String,String>):UserDetailAction


    /**
     * Function returns string representation of Application state
     */
    override fun toString(): String {
        return "User_id: $user_id, Login: $login, email: $email, First name: $first_name, Last name: $last_name, default_room: $default_room " +
                "gender: $gender, birthDate: $birthDate, role: $role, errors: ${stringifyJSON(errors as HashMap<String, Any>)}," +
                "showProgressIndicator: $showProgressIndicator, showSuccessMessage: $successMessage, rooms: $rooms"

    }
}

/**
 * Abstract interface for all actions of this state
 */
interface UserDetailAction: Action {}

/**
 * User detail form submit results definitions
 */
enum class UserDetailError(value:String): SmartEnum {
    RESULT_OK("RESULT_OK"),
    RESULT_ERROR_FIELD_IS_EMPTY("RESULT_ERROR_FIELD_IS_EMPTY"),
    RESULT_ERROR_INCORRECT_FIELD_VALUE("RESULT_ERROR_INCORRECT_FIELD_VALUE"),
    RESULT_ERROR_PASSWORDS_SHOULD_MATCH("RESULT_ERROR_PASSWORDS_SHOULD_MATCH"),
    RESULT_ERROR_INCORRECT_EMAIL("RESULT_ERROR_INCORRECT_EMAIL"),
    RESULT_ERROR_CONNECTION_ERROR("RESULT_ERROR_CONNECTION_ERROR");
    override fun getMessage():String {
        return when (this) {
            RESULT_OK -> ""
            RESULT_ERROR_FIELD_IS_EMPTY -> "This field is required"
            RESULT_ERROR_INCORRECT_FIELD_VALUE -> "Incorrect field value"
            RESULT_ERROR_INCORRECT_EMAIL -> "Incorrect email"
            RESULT_ERROR_PASSWORDS_SHOULD_MATCH -> "Passwords should match"
            RESULT_ERROR_CONNECTION_ERROR -> "Connection error"
        }
    }
}