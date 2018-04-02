package reducers

import lib.Action
import lib.State
import store.LoginFormState

/**
 * Reducer function which applies actions to state of login form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun LoginFormReducer(state:LoginFormState,action: Action): LoginFormState {
    var newState = state
    when(action) {
        is LoginFormState.changeLoginField -> newState.login = action.login
        is LoginFormState.changePasswordField -> newState.password = action.password
        is LoginFormState.changeErrorsField -> newState.errors = action.errors
        is LoginFormState.changeShowProgressIndicatorField -> newState.showProgressIndicator = action.showProgressIndicator
    }
    return newState
}