package reducers

import lib.Action
import lib.State
import store.AppState
import store.LoginFormAction
import utils.LogLevel
import utils.Logger

/**
 * Root reducer, which used to apply actions to differnent parts
 * of Applilcation state
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun RootReducer(state: State, action: Action):AppState {
    val state = state as AppState
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting root reducer. Initial state: $state",
            "RootReducer","RootReducer")
    when(action) {
        is LoginFormAction -> newState.loginForm = LoginFormReducer(newState.loginForm,action)
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "RootReducer","RootReducer")
    return newState
}