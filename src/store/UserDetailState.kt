package store

import core.MessageCenter
import core.MessageCenterResponseListener
import lib.Action
import lib.State
import utils.LogLevel
import utils.Logger
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
     * Action which loads user data from MessageCenter
     */
    class LoadItem: MessageCenterResponseListener {

        // Link to callback method which will be called after response processed
        var callback: (()->Unit)? = null
        /**
         * Action executor, prepares request to server and places it to pendingRequests queue of MessageCenter
         *
         * @param user_id: ID of user
         */
        fun exec(user_id:String,callback:(()->Unit)? = null) {
            Logger.log(LogLevel.DEBUG,"Begin LoadItem actions to load user. Preparing request",
                    "UserDetailState","LoadItem.exec")
            appStore.dispatch(UserDetailState.Change_errors_action(HashMap<String,UserDetailError>()))
            if (!MessageCenter.isConnected) {
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.RESULT_ERROR_CONNECTION_ERROR)))
                return
            }
            if (user_id.isEmpty()) {
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("user_id" to UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY)))
                Logger.log(LogLevel.WARNING,"Provided user_id is empty","UserDetailState","LoadItem.exec")
                return
            }
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(true))
            val request = hashMapOf(
                    "action" to "admin_get_user",
                    "sender" to this,
                    "query" to hashMapOf("_id" to user_id)
            )
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UserDetailState","LoadItem.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UserDetailState","LoadItem.exec")
            } else {
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}","UserDetailState","LoadItem.exec")
            }
            if (callback != null) {
                this.callback = callback
            }
        }

        /**
         * Function receives response from MessageCenter, after it processes request placed to queue
         *
         * @param request_id: ID of processed request
         * param response: Body of response from server
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>) {
            Logger.log(LogLevel.DEBUG, "Received response from WebSocket server for request $request_id. " +
                    "Response body: ${stringifyJSON(response)}",
                    "UserDetailState", "LoadItem.handleWebSocketResponse")
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
            if (response["status"] == null) {
                Logger.log(LogLevel.WARNING, "Response does not contain 'status' field",
                        "UserDetailState", "LoadItem.handleWebSocketResponse")
                return
            }
            var response_code = UserDetailError.RESULT_ERROR_UNKNOWN_ERROR
            if (response["status"] == "error") {
                try {
                    response_code = UserDetailError.valueOf(response["status_code"].toString())
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING, "Could not parse result code ${response["status_code"]}",
                            "UserDetailState","LoadItem.handleWebSocketResponse")
                }
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to response_code)))
                return
            }
            if (response["status"] != "ok") {
                Logger.log(LogLevel.WARNING, "Unknown status returned ${response["status"]}",
                        "UserDetailState","LoadItem.handleWebSocketResponse")
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
                return
            }
            if (response["user"] == null) {
                Logger.log(LogLevel.WARNING, "Response does not contain 'user' field",
                        "UserDetailState","LoadItem.handleWebSocketResponse")
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
                return
            }
            val item = response["user"] as? HashMap<String, Any>
            if (item == null) {
                Logger.log(LogLevel.WARNING, "Incorrect 'user' field format in response",
                        "UserDetailState","LoadItem.handleWebSocketResponse")
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
                return
            }
            if (item["login"] != null) {
                appStore.dispatch(UserDetailState.Change_login_action(item["login"].toString()))
            }
            if (item["email"] != null) {
                appStore.dispatch(UserDetailState.Change_email_action(item["email"].toString()))
            }
            appStore.dispatch(UserDetailState.Change_password_action(""))
            appStore.dispatch(UserDetailState.Change_confirm_password_action(""))
            if (item["default_room"]!=null) {
                appStore.dispatch(UserDetailState.Change_default_room_action(item["default_room"].toString()))
            }
            if (item["active"] != null) {
                try {
                    appStore.dispatch(UserDetailState.Change_active_action(item["active"].toString().toBoolean()))
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING,"Could not convert 'active' to Boolean. Active: " +
                            "${item["active"]}","UserDetailState","LoadItem.handleWebSocketResponse")
                }
            }
            if (item["role"] != null) {
                try {
                    appStore.dispatch(UserDetailState.Change_role_action(item["role"].toString().toInt()))
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING,"Could not convert 'role' to Integer. Role: " +
                            "${item["role"]}", "UserDetailState","LoadItem.handleWebSocketResponse")
                }
            }
            if (item["first_name"] != null) {
                appStore.dispatch(UserDetailState.Change_first_name_action(item["first_name"].toString()))
            }
            if (item["last_name"] != null) {
                appStore.dispatch(UserDetailState.Change_last_name_action(item["last_name"].toString()))
            }
            if (item["gender"] != null) {
                try {
                    appStore.dispatch(UserDetailState.Change_gender_action(Gender.valueOf(item["gender"].toString())))
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING,"Could not convert 'gender' field. Gender: " +
                            "${item["gender"]}", "UserDetailState","LoadItem.handleWebSocketResponse")
                }
            }
            if (item["birthDate"] != null) {
                try {
                    appStore.dispatch(UserDetailState.Change_birthDate_action(item["birthDate"].toString().toInt()))
                } catch (e:Exception) {
                    Logger.log(LogLevel.WARNING,"Could not convert 'birthDate' to Int. BirthDate: " +
                            "${item["birthDate"]}", "UserDetailState","LoadItem.handleWebSocketResponse")
                }
            }
            Logger.log(LogLevel.DEBUG, "Parsed response to request: $request_id. " +
                    "Initial response body: ${stringifyJSON(response)}",
                    "UserDetailState", "LoadItem.handleWebSocketResponse")
            if (callback != null) {
                callback!!()
                callback = null
            }
        }
    }

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
    RESULT_ERROR_CONNECTION_ERROR("RESULT_ERROR_CONNECTION_ERROR"),
    RESULT_ERROR_UNKNOWN_ERROR("RESULT_ERROR_UNKNOWN_ERROR"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR"),
    INTERNAL_ERROR("INTERNAL_ERROR");
    override fun getMessage():String {
        return when (this) {
            RESULT_OK -> ""
            RESULT_ERROR_FIELD_IS_EMPTY -> "This field is required"
            RESULT_ERROR_INCORRECT_FIELD_VALUE -> "Incorrect field value"
            RESULT_ERROR_INCORRECT_EMAIL -> "Incorrect email"
            RESULT_ERROR_PASSWORDS_SHOULD_MATCH -> "Passwords should match"
            RESULT_ERROR_CONNECTION_ERROR -> "Connection error"
            RESULT_ERROR_UNKNOWN_ERROR -> "Unknown error. Please, contact support"
            AUTHENTICATION_ERROR -> "Authentication error. Please, login again"
            INTERNAL_ERROR -> "System error. Please,contact support"
        }
    }
}