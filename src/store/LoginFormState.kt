package store

import core.MessageCenter
import core.MessageCenterResponseListener
import lib.Action
import lib.State
import utils.LogLevel
import utils.Logger
import utils.stringifyJSON
import kotlin.browser.localStorage

/**
 * Class holds state of Login Form
 * and action classes to change this state
 */
class LoginFormState : State {
    // Value of "Login" field
    var login = ""
    // Value of "password" field
    var password = ""
    // Array of validation errors
    var errors = HashMap<String,LoginFormError>()
    // Current status of login progress indicator
    var showProgressIndicator = false

    /**
     * Action to change "Login" field
     */
    data class changeLoginField(var login: String) : LoginFormAction

    /**
     * Action to change "Password" field
     */
    data class changePasswordField(var password: String) : LoginFormAction

    /**
     * Action to change "errors" field
     */
    data class changeErrorsField(var errors: HashMap<String,LoginFormError>) : LoginFormAction

    /**
     * Action to change "showProgressIndicator" field
     */
    data class changeShowProgressIndicatorField(var showProgressIndicator:Boolean): LoginFormAction

    /**
     * Action to implement "Login" function
     */
    class doLogin(): MessageCenterResponseListener {
        /**
         * Function used to run this action, do login and password validation
         * and send request to MessageCenter WebSocket server
         */
        fun exec(user_id:String="",session_id:String="") {
            Logger.log(LogLevel.DEBUG,"Begin login user action")
            if (!user_id.toString().isEmpty() && !session_id.toString().isEmpty()) {
                Logger.log(LogLevel.DEBUG,"Begin login user action using user_id and session_id",
                        "LoginFormState.doLogin","exec")
                val request = hashMapOf("action" to "login_user","login" to user_id,"password" to session_id,
                        "sender" to this)
                MessageCenter.addToPendingRequests(request)
                return
            }
            val state = (appStore.state as AppState).loginForm
            Logger.log(LogLevel.DEBUG,"Begin login user action using login form state $state",
                    "LoginFormState",
                    "doLogin.exec")
            var errors = HashMap<String,LoginFormError>()
            appStore.dispatch(LoginFormState.changeErrorsField(errors))
            if (state.login.isEmpty()) {
                errors["login"] = LoginFormError.RESULT_ERROR_INCORRECT_LOGIN
            }
            if (state.password.isEmpty()) {
                errors["password"] = LoginFormError.RESULT_ERROR_INCORRECT_PASSWORD
            }
            if (errors.count()>0) {
                Logger.log(LogLevel.DEBUG,"Validation errors: $errors",
                        "LoginFormState.doLogin","exec")
                appStore.dispatch(LoginFormState.changeErrorsField(errors))
                return
            }
            if (!MessageCenter.isConnected) {
                Logger.log(LogLevel.DEBUG, "WebSocket server Connection error during login",
                        "LoginFormState.doLogin","exec")
                appStore.dispatch(
                        LoginFormState.changeErrorsField(
                            hashMapOf("general" to LoginFormError.RESULT_ERROR_CONNECTION_ERROR)
                        )
                )
                return
            }
            val request = hashMapOf("action" to "login_user","login" to state.login, "password" to state.password,
                    "sender" to this)
            Logger.log(LogLevel.DEBUG,"Created login request: ${stringifyJSON(request)}",
                    "LoginFormState.doLogin","exec")
            MessageCenter.addToPendingRequests(request)
            appStore.dispatch(LoginFormState.changeShowProgressIndicatorField(true))
        }

        /**
         * Handles responses from MessageCenter WebSocket server, related to requests sent by
         * this action
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>) {
            Logger.log(LogLevel.DEBUG,"Received response to request with id: $request_id. " +
                    "Response body: ${stringifyJSON(response)}","LoginFormState","handleWebSocketResponse")
            var failed_response = false;
            if (!response.containsKey("status")) {
                Logger.log(LogLevel.WARNING,"Response for request_id=$request_id does not " +
                        "contain 'status' field", "LoginFormState.doLogin","exec")
                return
            }
            if (response["status"]!="ok" && response["status"] != "error") {
                Logger.log(LogLevel.WARNING,"Response for request_id=$request_id contains incorrect " +
                        " value of 'status' field. Response body: $response", "LoginFormState.doLogin","exec")
                return
            }

            if (!response.containsKey("status_code")) {
                Logger.log(LogLevel.WARNING,"Response for request_id=$request_id does not contain 'status_code'",
                        "LoginFormState.doLogin","exec")
                return
            }
            appStore.dispatch(LoginFormState.changeShowProgressIndicatorField(false))
            MessageCenter.removeFromRequestsWaitingResponses(request_id)
            if (response["status"]=="error") {
                if (response["status_code"].toString()!="RESULT_ERROR_SESSION_TIMEOUT") {
                    var error: LoginFormError? = null
                    try {
                        error = LoginFormError.valueOf(response["status_code"].toString())
                    } catch (e: Exception) {
                        Logger.log(LogLevel.WARNING, "Response for request_id=$request_id has unknown " +
                                "'status_code'=${response["status_code"]}", "LoginFormState.doLogin", "exec")
                        error = LoginFormError.RESULT_ERROR_UNKNOWN
                    }
                    appStore.dispatch(LoginFormState.changeErrorsField(hashMapOf("general" to error!!)))
                }
                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                localStorage.removeItem("user_id");localStorage.removeItem("session_id")
                return
            }
            if (!response.containsKey("user_id") || !response.containsKey("session_id")) {
                appStore.dispatch(LoginFormState.changeErrorsField(
                        hashMapOf("general" to LoginFormError.AUTHENTICATION_ERROR)
                ))
                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                localStorage.removeItem("user_id");localStorage.removeItem("session_id")
                return
            }
            var role: UserRole? = null
            try {
                role = UserRole.getValueByCode(response["role"].toString().toDouble().toInt())
            } catch (e:Exception) {
                Logger.log(LogLevel.WARNING,"Error parsing received role ${e.message}",
                        "LoginFormState.doLogin","exec")
            }
            if (role == null) {
                Logger.log(LogLevel.WARNING,"Response for request_id=$request_id has incorrect role: $role",
                        "LoginFormState.doLogin","exec")
                appStore.dispatch(LoginFormState.changeErrorsField(
                        hashMapOf("general" to LoginFormError.AUTHENTICATION_ERROR)
                ))
                localStorage.removeItem("user_id");localStorage.removeItem("session_id")
                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                return
            }
            if (role != UserRole.ADMIN) {
                Logger.log(LogLevel.WARNING,"User in response for request_id=$request_id does not ADMIN",
                        "LoginFormState.doLogin","exec")
                appStore.dispatch(LoginFormState.changeErrorsField(
                        hashMapOf("general" to LoginFormError.AUTHENTICATION_ERROR)
                ))
                localStorage.removeItem("user_id");localStorage.removeItem("session_id")
                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                return
            }
            MessageCenter.user_id = response["user_id"].toString()
            MessageCenter.session_id = response["session_id"].toString()
            localStorage.setItem("user_id",MessageCenter.user_id)
            localStorage.setItem("session_id",MessageCenter.session_id)
            appStore.dispatch(LoginFormState.changeErrorsField(HashMap()))
            appStore.dispatch(UserState.Change_user_id_Action(MessageCenter.user_id))
            appStore.dispatch(UserState.Change_session_id_Action(MessageCenter.session_id))
            appStore.dispatch(UserState.Change_login_Action(response["login"]?.toString() ?: ""))
            appStore.dispatch(UserState.Change_email_Action(response["email"]?.toString() ?: ""))
            appStore.dispatch(UserState.Change_first_name_Action(response["first_name"]?.toString() ?: ""))
            appStore.dispatch(UserState.Change_last_name_Action(response["last_name"]?.toString() ?: ""))
            appStore.dispatch(UserState.Change_gender_Action(response["gender"] as? Gender ?: Gender.M))
            appStore.dispatch(UserState.Change_birthDate_Action(response["birthDate"]?.toString()?.toInt() ?: 0))
            appStore.dispatch(UserState.Change_default_room_Action(response["default_room"]?.toString() ?: ""))
            appStore.dispatch(UserState.Change_isLogin_Action(true))
            appStore.dispatch(UserState.Change_role_Action(UserRole.ADMIN))
            appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.USERS_LIST))
        }
    }

    /**
     * Function returns custom string representation of LoginFormState object
     *
     * @return String, representing all fields of LoginFormState
     */
    override fun toString():String {
        return "{login:$login,password:$password,showProcessWindowAction:$showProgressIndicator,errors:$errors}"
    }
}

/**
 * Abstract interface for all actions of LoginForm state
 */
interface LoginFormAction: Action {}

/**
 * Definitions of possible login form errors
 */
enum class LoginFormError(val value:String):SmartEnum {
    RESULT_OK("RESULT_OK"),
    RESULT_ERROR_NOT_ACTIVATED("RESULT_ERROR_NOT_ACTIVATED"),
    RESULT_ERROR_INCORRECT_LOGIN("RESULT_ERROR_INCORRECT_LOGIN"),
    RESULT_ERROR_INCORRECT_PASSWORD("RESULT_ERROR_INCORRECT_PASSWORD"),
    RESULT_ERROR_CONNECTION_ERROR("RESULT_ERROR_CONNECTION_ERROR"),
    RESULT_ERROR_ALREADY_LOGIN("RESULT_ERROR_ALREADY_LOGIN"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR"),
    RESULT_ERROR_UNKNOWN("RESULT_ERROR_UNKNOWN");
    override fun getMessage(): String {
        var result = ""
        when (this) {
            RESULT_ERROR_NOT_ACTIVATED -> result = "Please, activate this account. Open activation email."
            RESULT_ERROR_INCORRECT_LOGIN -> result = "Incorrect login."
            RESULT_ERROR_INCORRECT_PASSWORD -> result = "Incorrect password."
            RESULT_ERROR_CONNECTION_ERROR -> result = "Server connection error."
            RESULT_ERROR_ALREADY_LOGIN -> result = "User already in the system."
            RESULT_ERROR_UNKNOWN -> result = "Unknown error. Please contact support."
            INTERNAL_ERROR -> result = "System error. Please contact support."
            AUTHENTICATION_ERROR -> result = "Authentication error. Please contact support."
        }
        return result;
    }
}
