package reducers

import lib.Action
import store.LoginFormState
import store.UserState
import utils.LogLevel
import utils.Logger

/**
 * Reducer function which applies actions to state of login form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun UserStateReducer(state: UserState, action: Action): UserState {
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting UserStateReducer. Initial state $state",
            "UserStateReducer","UserStateReducer")
    when(action) {
        is UserState.Change_user_id_Action -> newState.user_id = action.user_id
        is UserState.Change_session_id_Action -> newState.session_id = action.session_id
        is UserState.Change_login_Action -> newState.login = action.login
        is UserState.Change_email_Action -> newState.email = action.email
        is UserState.Change_isLogin_Action -> newState.isLogin = action.isLogin
        is UserState.Change_first_name_Action -> newState.first_name = action.first_name
        is UserState.Change_last_name_Action -> newState.last_name = action.last_name
        is UserState.Change_birthDate_Action -> newState.birthDate = action.birthDate
        is UserState.Change_gender_Action -> newState.gender = action.gender
        is UserState.Change_default_room_Action -> newState.default_room = action.default_room
        is UserState.Change_role_Action -> newState.role = action.role
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "UserStateReducer","UserStateReducer")
    return newState
}