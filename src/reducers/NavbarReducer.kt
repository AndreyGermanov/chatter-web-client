package reducers

import lib.Action
import store.NavbarState
import utils.LogLevel
import utils.Logger

/**
 * Reducer function which applies actions to state of login form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun NavbarReducer(state: NavbarState, action: Action): NavbarState {
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting NavbarReducer. Initial state $state",
            "NavbarReducer","NavbarReducer")
    when(action) {
        is NavbarState.changeUserMenuDropdownClass -> newState.userMenuDropdownClass = action.dropdownClass
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "NavbarReducer","NavbarReducer")
    return newState
}