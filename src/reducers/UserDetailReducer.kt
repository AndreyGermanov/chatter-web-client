package reducers

import lib.Action
import store.UserDetailState
import store.UsersListState
import utils.LogLevel
import utils.Logger

/**
 * Reducer function which applies actions to state of User detail form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun UserDetailReducer(state: UserDetailState, action: Action): UserDetailState {
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting UserDetailReducer. Initial state $state",
            "UserDetailReducer","UserDetailReducer")
    when(action) {
        is UserDetailState.Change_user_id_action -> newState.user_id = action.user_id
        is UserDetailState.Change_active_action -> newState.active = action.active
        is UserDetailState.Change_confirm_password_action -> newState.confirm_password = action.confirm_password
        is UserDetailState.Change_default_room_action -> newState.default_room = action.default_room
        is UserDetailState.Change_email_action -> newState.email = action.email
        is UserDetailState.Change_errors_action -> newState.errors = action.errors
        is UserDetailState.Change_first_name_action -> newState.first_name = action.first_name
        is UserDetailState.Change_last_name_action -> newState.last_name = action.last_name
        is UserDetailState.Change_gender_action -> newState.gender = action.gender
        is UserDetailState.Change_birthDate_action -> {
            console.log("REDUCE BIRTHDATE ${action.birthDate}")
            newState.birthDate = action.birthDate
        }
        is UserDetailState.Change_login_action -> newState.login = action.login
        is UserDetailState.Change_password_action -> newState.password = action.password
        is UserDetailState.Change_role_action -> newState.role = action.role
        is UserDetailState.Change_showProgressIndicator_action -> newState.showProgressIndicator = action.showProgressIndicator
        is UserDetailState.Change_successMessage_action -> newState.successMessage = action.successMessage
        is UserDetailState.Change_rooms_action -> newState.rooms = action.rooms
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "UserDetailReducer","UserDetailReducer")
    return newState
}