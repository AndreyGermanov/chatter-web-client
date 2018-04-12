package reducers

import lib.Action
import store.UsersListState
import utils.LogLevel
import utils.Logger

/**
 * Reducer function which applies actions to state of login form
 *
 * @param state Link to source application state
 * @param action Action to apply to source state
 * @return Result state after apply action
 */
fun UsersListReducer(state: UsersListState, action: Action): UsersListState {
    var newState = state
    Logger.log(LogLevel.DEBUG_REDUX,"Starting UsersListReducer. Initial state $state",
            "UsersListReducer","UsersListReducer")
    when(action) {
        is UsersListState.Change_error_Action -> newState.error = action.error
        is UsersListState.Change_filter_Action -> newState.filter = action.filter
        is UsersListState.Change_items_Action -> newState.items = action.items
        is UsersListState.Change_limit_Action -> newState.limit = action.limit
        is UsersListState.Change_offset_Action -> newState.offset = action.offset
        is UsersListState.Change_sort_Action -> newState.sort = action.sort
        is UsersListState.Change_selectedItems_Action -> newState.selectedItems = action.selectedItems
        is UsersListState.Change_isAllItemsSelected_Action -> newState.isAllItemsSelected = action.isAllItemsSelected
        is UsersListState.Change_total_Action -> newState.total = action.total
        is UsersListState.Change_showProgressIndicator_Action -> newState.showProgressIndicator = action.shoProgressIndicator
    }
    Logger.log(LogLevel.DEBUG_REDUX,"Returning new state after reducing. New state $newState",
            "UsersListReducer","UsersListReducer")
    return newState
}