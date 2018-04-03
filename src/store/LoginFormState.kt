package store

import core.MessageCenter
import core.MessageCenterResponseListener
import lib.Action
import lib.State
import utils.LogLevel
import utils.Logger
import utils.stringifyJSON

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
        fun exec() {
            Logger.log(LogLevel.DEBUG,"Begin login user action")
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
                        "LoginState.doLogin","exec")
                appStore.dispatch(LoginFormState.changeErrorsField(errors))
                return
            }
            if (!MessageCenter.isConnected) {
                Logger.log(LogLevel.DEBUG, "WebSocket server Connection error during login",
                        "LoginState.doLogin","exec")
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
                    "LoginState.doLogin","exec")
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
        }
        return result;
    }
}
