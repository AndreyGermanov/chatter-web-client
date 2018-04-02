package reducers

import lib.Action
import lib.State
import store.AppState
import store.LoginFormAction

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
    when(action) {
        is LoginFormAction -> newState.loginForm = LoginFormReducer(newState.loginForm,action)
    }
    return newState
}