package reducers

import lib.Action
import store.LoginFormState
import utils.LogLevel
import utils.Logger

/**
 * Reducer function which applies actions to state of login form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun LoginFormReducer(state:LoginFormState,action: Action): LoginFormState {
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting LoginFormReducer. Initial state $state",
            "LoginFormReducer","LoginFormReducer")
    when(action) {
        is LoginFormState.changeLoginField -> {
            Logger.log(LogLevel.DEBUG_REDUX, "Begin applying 'login' action with value ${action.login} to state. " +
                    "State value: ${newState.login}","LoginFormReducer","LoginFormReducer");
            newState.login = action.login;
            Logger.log(LogLevel.DEBUG_REDUX, "Applied 'login' action to state. State value: ${newState.login}",
                    "LoginFormReducer","LoginFormReducer");
        }
        is LoginFormState.changePasswordField -> newState.password = action.password
        is LoginFormState.changeErrorsField -> newState.errors = action.errors
        is LoginFormState.changeShowProgressIndicatorField -> newState.showProgressIndicator = action.showProgressIndicator
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "LoginFormReducer","LoginFormReducer")
    return newState
}