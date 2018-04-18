package store

import core.MessageCenter
import core.MessageCenterResponseListener
import lib.Action
import lib.State
import utils.LogLevel
import utils.Logger
import utils.isValidEmail
import utils.stringifyJSON
import kotlin.browser.window
import kotlin.js.Date

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
         * @param callback: Callback function which will be executed after request finished
         */
        fun exec(user_id:String,callback:(()->Unit)? = null) {
            Logger.log(LogLevel.DEBUG,"Begin LoadItem actions to load user. Preparing request",
                    "UserDetailState","LoadItem.exec")
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
         *
         * @returns custom result or nothing, depending on sense of this request. In this case, it returns
         * nothing
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>): Any? {
            Logger.log(LogLevel.DEBUG, "Received response from WebSocket server for request $request_id. " +
                    "Response body: ${stringifyJSON(response)}",
                    "UserDetailState", "LoadItem.handleWebSocketResponse")
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
            val state = appStore.state as AppState
            state.userDetail.processUserDetailResponse(response)
            Logger.log(LogLevel.DEBUG, "Parsed response to request: $request_id. " +
                    "Initial response body: ${stringifyJSON(response)}",
                    "UserDetailState", "LoadItem.handleWebSocketResponse")
            MessageCenter.removeFromRequestsWaitingResponses(request_id)
            if (callback != null) {
                callback!!()
                callback = null
            }
            return null
        }
    }

    /**
     * Action used to reset all user detail fields to default values
     */
    class ClearData {
        /**
         * Action executor
         */
        fun exec() {
            appStore.dispatch(UserDetailState.Change_errors_action(HashMap<String,UserDetailError>()))
            appStore.dispatch(UserDetailState.Change_birthDate_action(0))
            appStore.dispatch(UserDetailState.Change_gender_action(Gender.M))
            appStore.dispatch(UserDetailState.Change_last_name_action(""))
            appStore.dispatch(UserDetailState.Change_first_name_action(""))
            appStore.dispatch(UserDetailState.Change_default_room_action(""))
            appStore.dispatch(UserDetailState.Change_password_action(""))
            appStore.dispatch(UserDetailState.Change_confirm_password_action(""))
            appStore.dispatch(UserDetailState.Change_login_action(""))
            appStore.dispatch(UserDetailState.Change_email_action(""))
            appStore.dispatch(UserDetailState.Change_active_action(false))
            appStore.dispatch(UserDetailState.Change_role_action(1))
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
            appStore.dispatch(UserDetailState.Change_successMessage_action(""))
        }
    }

    /**
     * Action used to send "Save user" request to server and process response
     */
    class SaveItem: MessageCenterResponseListener {

        // Link to callback method which will be called after response processed
        var callback: (()->Unit)? = null

        /**
         * Action executor
         *
         * @param callback: Callback function which will be called after request processes and response returned
         * from message center
         */
        fun exec(callback:(()->Unit)? = null) {
            val state = appStore.state as AppState
            val user = state.userDetail
            Logger.log(LogLevel.DEBUG,"Starting SaveItem action for User with data: {$user}.",
                    "UserDetailState","SaveUser.exec")
            if (user.showProgressIndicator) {
                Logger.log(LogLevel.WARNING,"Could not start request, because other request already going",
                        "UserDetailState","SaveUser.exec")
                if (callback != null) {
                    callback()
                }
                return
            }
            this.callback = callback
            appStore.dispatch(UserDetailState.Change_errors_action(HashMap<String,UserDetailError>()))
            appStore.dispatch(UserDetailState.Change_successMessage_action(""))
            Logger.log(LogLevel.DEBUG,"Validating data and preparing request body.",
                    "UserDetailState","SaveUser.exec")
            val request = user.prepareUserUpdateRequest(this) ?: return
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(true))
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UsersDetailState","SaveItem.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UsersDetailState","SaveItem.exec")
            } else {
                appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}","UsersDetailState","SaveItem.exec")
            }
        }

        /**
         * Function receives response from MessageCenter, after it processes request placed to queue
         *
         * @param request_id: ID of processed request
         * param response: Body of response from server
         *
         * @returns custom result or nothing, depending on sense of this request. In this case, it returns
         * nothing
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>): Any? {
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
            var state = appStore.state as AppState
            if (!state.userDetail.processUserDetailResponse(response)) {
                return null
            }
            state = appStore.state as AppState
            var request = response["request"] as HashMap<String,Any>
            if (request["action"].toString()=="admin_add_user") {
                val user = response["user"] as HashMap<String,Any>
                val user_id = user["_id"].toString()
                Logger.log(LogLevel.DEBUG, "New user added for request with id: $request_id. " +
                        "Redirecting to this user page: ${stringifyJSON(response)}",
                        "UserDetailState", "SaveItem.handleWebSocketResponse")
                window.location.assign("#/user/$user_id")
                window.location.reload()
            } else if (request["action"] == "admin_update_user") {
                appStore.dispatch(UserDetailState.Change_successMessage_action("User data updated succesfully"))
                Logger.log(LogLevel.DEBUG, "Item '${state.userDetail.login}' updated successfully",
                        "UserDetailState","SaveItem.handleWebSocketResponse")
            }
            return null
        }
    }

    /**
     * Action used to request list of chat rooms from MessageCenter, process response and set returned list
     * to application state as HashMap<String,String> where keys are room ids and values are room names
     */
    class GetRoomsList: MessageCenterResponseListener {

        var callback: (()->Unit)? = null

        fun exec(callback:(()->Unit)? = null) {
            val state = appStore.state as AppState
            val user = state.userDetail
            Logger.log(LogLevel.DEBUG,"Starting GetRoomsList action.",
                    "UserDetailState","GetRoomsList.exec")
            if (user.showProgressIndicator) {
                Logger.log(LogLevel.WARNING,"Could not start request, because other request already going",
                        "UserDetailState","GetRoomsList.exec")
                if (callback != null) {
                    callback()
                }
                return
            }
            if (!MessageCenter.isConnected) {
                appStore.dispatch(UserDetailState.Change_errors_action(
                        hashMapOf("general" to UserDetailError.RESULT_ERROR_CONNECTION_ERROR)))
                Logger.log(LogLevel.DEBUG,"Server connection error.",
                        "UserDetailState","GetRoomsList.exec")
                return
            }
            this.callback = callback
            appStore.dispatch(UserDetailState.Change_errors_action(HashMap<String,UserDetailError>()))
            appStore.dispatch(UserDetailState.Change_successMessage_action(""))
            Logger.log(LogLevel.DEBUG,"Validating data and preparing request body.",
                    "UserDetailState","GetRoomsList.exec")
            val request = hashMapOf(
                    "action" to "admin_get_rooms_list",
                    "sender" to this,
                    "sort" to hashMapOf("name" to "ASC") as HashMap<String,String>
            )

            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(true))
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UsersDetailState","GetRoomsList.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UsersDetailState","GetRoomsList.exec")
            } else {
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}","UsersDetailState","GetRoomsList.exec")
            }
        }

        /**
         * Function receives response from MessageCenter, after it processes request placed to queue
         *
         * @param request_id: ID of processed request
         * param response: Body of response from server
         *
         * @returns custom result or nothing, depending on sense of this request. In this case, it returns
         * nothing
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>): Any? {
            appStore.dispatch(UserDetailState.Change_showProgressIndicator_action(false))
            var state = appStore.state as AppState
            if (!state.userDetail.processResponse(response)) {
                return null
            }
            if (response["list"] == null) {
                Logger.log(LogLevel.WARNING, "Response does not contain 'list' field",
                        "UserDetailState","GetRoomsList.handleWebSocketResponse")
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
                return null
            }
            val list = response["list"] as? HashMap<String, Any>
            if (list == null) {
                Logger.log(LogLevel.WARNING, "Incorrect 'list' field format in response",
                        "UserDetailState","GetRoomsList.handleWebSocketResponse")
                appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
                return null
            }
            val rooms = HashMap<String,String>()
            rooms[""] = ""
            for ((_,obj) in list) {
                var item: HashMap<String, Any>? = null
                if (obj is HashMap<*, *>) {
                    item = obj as HashMap<String, Any>
                } else {
                    Logger.log(LogLevel.WARNING, "Incorrect format of item $item in returned rooms list",
                            "UserDetailState,", "GetRoomsList.handleWebSocketResponse")
                    continue
                }
                if (!item.containsKey("_id") || !item.contains("name")) {
                    Logger.log(LogLevel.WARNING, "Incorrect format of item $item in returned rooms list",
                            "UserDetailState,", "GetRoomsList.handleWebSocketResponse")
                    continue
                }
                rooms[item["_id"].toString()] = item["name"].toString()
            }
            Logger.log(LogLevel.DEBUG,"Processed rooms list: $rooms","UserDetailState",
                    "GetRoomsList.handleWebSocketResponse")
            appStore.dispatch(UserDetailState.Change_rooms_action(rooms))
            if (this.callback != null) {
                this.callback!!()
            }
            return null
        }
    }

    /**
     * Function returns string representation of Application state
     */
    override fun toString(): String {
        return "User_id: $user_id, Login: $login, email: $email, First name: $first_name, Last name: $last_name, " +
                "active: $active, default_room: $default_room, gender: $gender, birthDate: $birthDate, role: $role, " +
                "errors: ${stringifyJSON(errors as HashMap<String, Any>)},showProgressIndicator: $showProgressIndicator, " +
                "showSuccessMessage: $successMessage, rooms: $rooms"

    }

    /**
     * Validates user data and returns request object to send to MessageCenter or null, in case of errors
     *
     * @returns Request object or null in case of errors
     */
    fun prepareUserUpdateRequest(sender:MessageCenterResponseListener): HashMap<String,Any>? {
        val user = this
        val errors = HashMap<String,UserDetailError>()
        val request = HashMap<String,Any>()
        request["sender"] = sender
        if (user.user_id != null && !user.user_id!!.isEmpty()) {
            request["id"] = user.user_id.toString()
            request["action"] = "admin_update_user"
        } else {
            request["action"] = "admin_add_user"
        }
        val fields = ArrayList<HashMap<String,Any>>()
        if (user.login.trim().isEmpty()) {
            errors["login"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
        } else {
            fields.add(hashMapOf("login" to user.login.trim()))
        }
        if (user.email.trim().isEmpty()) {
            errors["email"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
        } else if (!isValidEmail(user.email.trim())) {
            errors["email"] = UserDetailError.RESULT_ERROR_INCORRECT_FIELD_VALUE
        } else {
            fields.add(hashMapOf("email" to user.email.trim()))
        }
        when {
            user.role == 0 -> errors["role"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
            UserRole.getValueByCode(user.role) == null -> {
                errors["role"] = UserDetailError.RESULT_ERROR_INCORRECT_FIELD_VALUE
            }
            else -> fields.add(hashMapOf("role" to user.role))
        }
        when {
            user.default_room.toString().isEmpty() -> {
                errors["default_room"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
            }
            (user.rooms.filterKeys { it == user.default_room }).count() == 0 -> {
                errors["default_room"] = UserDetailError.RESULT_ERROR_INCORRECT_FIELD_VALUE
            }
            else -> fields.add(hashMapOf("default_room" to user.default_room))
        }
        if (request["action"].toString() == "admin_add_user" && user.password.trim().isEmpty()) {
            errors["password"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
        } else if (user.password.trim() != user.confirm_password.trim()) {
            errors["password"] = UserDetailError.RESULT_ERROR_PASSWORDS_SHOULD_MATCH
        } else if (!user.password.trim().isEmpty()) {
            fields.add(hashMapOf("password" to user.password.trim()))
        }
        if (user.first_name.trim().isEmpty()) {
            errors["first_name"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
        } else {
            fields.add(hashMapOf("first_name" to user.first_name.trim()))
        }
        if (user.last_name.trim().isEmpty()) {
            errors["last_name"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
        } else {
            fields.add(hashMapOf("last_name" to user.last_name.trim()))
        }
        fields.add(hashMapOf("gender" to user.gender.toString()))
        when {
            user.birthDate == 0 -> errors["birthDate"] = UserDetailError.RESULT_ERROR_FIELD_IS_EMPTY
            user.birthDate > (Date().getTime()/1000).toInt() -> {
                errors["birthDate"] = UserDetailError.RESULT_ERROR_INCORRECT_FIELD_VALUE
            }
            else -> fields.add(hashMapOf("birthDate" to user.birthDate))
        }
        fields.add(hashMapOf("active" to user.active))
        request["fields"] = fields
        if (errors.count()>0) {
            appStore.dispatch(UserDetailState.Change_errors_action(errors))
            Logger.log(LogLevel.DEBUG,"Validation errors: ${stringifyJSON(errors as HashMap<String,Any>)}.",
                    "UserDetailState","prepareUserUpdateRequest")
            return null
        }
        if (!MessageCenter.isConnected) {
            appStore.dispatch(UserDetailState.Change_errors_action(
                    hashMapOf("general" to UserDetailError.RESULT_ERROR_CONNECTION_ERROR)))
            Logger.log(LogLevel.DEBUG,"Server connection error: ${stringifyJSON(errors as HashMap<String,Any>)}.",
                    "UserDetailState","prepareUserUpdateRequest")
            return null
        }
        return request
    }

    /**
     * Function used to implement general process and check for responses, which came
     * from WebSocket server. It checks general response format and response codes
     *
     * @param response: Response body
     * returns True if general fields ok or false otherwise
     */
    fun processResponse(response:HashMap<String,Any>):Boolean {
        if (response["status"] == null) {
            Logger.log(LogLevel.WARNING, "Response does not contain 'status' field",
                    "UserDetailState", "processUserDetailResponse")
            return false
        }
        var response_code = UserDetailError.RESULT_ERROR_UNKNOWN_ERROR
        if (response["status"] == "error") {
            var field = "general"
            try {
                response_code = UserDetailError.valueOf(response["status_code"].toString())
            } catch (e:Exception) {
                Logger.log(LogLevel.WARNING, "Could not parse result code ${response["status_code"]}",
                        "UserDetailState","processUserDetailResponse")
            }
            if (response["field"] != null) {
                field = response["field"].toString()
            }
            appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf(field to response_code)))
            return false
        }
        if (response["status"] != "ok") {
            Logger.log(LogLevel.WARNING, "Unknown status returned ${response["status"]}",
                    "UserDetailState","processUserDetailResponse")
            appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
            return false
        }
        return true
    }

    /**
     * Function used to process responses from Message Center, which contains user information.
     * Used to process responses for actions like admin_get_user or admin_add_user.
     *
     * @param response: Body of response
     * @returns True if received successful response or false if unable to process response or it is response
     * about failure
     */
    fun processUserDetailResponse(response:HashMap<String,Any>):Boolean {
        if (!processResponse(response)) {
            return false
        }
        if (response["user"] == null) {
            Logger.log(LogLevel.WARNING, "Response does not contain 'user' field",
                    "UserDetailState","processUserDetailResponse")
            appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
            return false
        }
        val item = response["user"] as? HashMap<String, Any>
        if (item == null) {
            Logger.log(LogLevel.WARNING, "Incorrect 'user' field format in response",
                    "UserDetailState","processUserDetailResponse")
            appStore.dispatch(UserDetailState.Change_errors_action(hashMapOf("general" to UserDetailError.INTERNAL_ERROR)))
            return false
        }
        if (item["_id"] != null) {
            appStore.dispatch(UserDetailState.Change_user_id_action(item["_id"].toString()))
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
                        "${item["active"]}","UserDetailState","processUserDetailResponse")
            }
        }
        if (item["role"] != null) {
            try {
                appStore.dispatch(UserDetailState.Change_role_action(item["role"].toString().toInt()))
            } catch (e:Exception) {
                Logger.log(LogLevel.WARNING,"Could not convert 'role' to Integer. Role: " +
                        "${item["role"]}", "UserDetailState","processUserDetailResponse")
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
                        "${item["gender"]}", "UserDetailState","processUserDetailResponse")
            }
        }
        if (item["birthDate"] != null) {
            try {
                appStore.dispatch(UserDetailState.Change_birthDate_action(item["birthDate"].toString().toInt()))
            } catch (e:Exception) {
                Logger.log(LogLevel.WARNING,"Could not convert 'birthDate' to Int. BirthDate: " +
                        "${item["birthDate"]}", "UserDetailState","processUserDetailResponse")
            }
        }
        return true
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
    RESULT_ERROR_OBJECT_NOT_FOUND("RESULT_OBJECT_NOT_FOUND"),
    RESULT_ERROR_FIELD_ALREADY_EXISTS("RESULT_ERROR_FIELD_ALREADY_EXISTS"),
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
            RESULT_ERROR_OBJECT_NOT_FOUND -> "User with specified ID not found. Could not update"
            RESULT_ERROR_FIELD_ALREADY_EXISTS -> "User with provided value already exists"
            AUTHENTICATION_ERROR -> "Authentication error. Please, login again"
            INTERNAL_ERROR -> "System error. Please,contact support"
        }
    }
}