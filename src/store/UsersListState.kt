package store

import lib.Action
import lib.State
import utils.stringifyJSON

/**
 * Abstract interface, which used as superclass for all actions
 * which change UsersListState
 */
interface UsersListStateAction: Action {}

/**
 * Function represents state of User List screen
 */
class UsersListState: State {


    // List of items in a table
    var items = ArrayList<HashMap<String,String>>()
    // List of selected user_id's in a table
    var selectedItems = ArrayList<String>()
    // Is All items in list selected
    var isAllItemsSelected = false
    // Total number of items in collection
    var total = 93
    // Offset of list
    var offset = 20
    // Limit (number of items on page)
    var limit = 10
    // Search string which used to filter items
    var filter = ""
    // Sort order of list
    var sort = "login" to "ASC"
    // Error message if exists
    var error = ""
    // Show Progress indicator
    var showProgressIndicator = false
    // User ID, which used as a part of authentication token,sent with each request to WebSocket server

    /**
     * Actions, which used to send to reducer to change UsersListState fields
     */
    data class Change_items_Action(val items:ArrayList<HashMap<String,String>>):UsersListStateAction
    data class Change_selectedItems_Action(val selectedItems:ArrayList<String>):UsersListStateAction
    data class Change_isAllItemsSelected_Action(val isAllItemsSelected:Boolean):UsersListStateAction
    data class Change_total_Action(val total:Int):UsersListStateAction
    data class Change_offset_Action(val offset:Int):UsersListStateAction
    data class Change_limit_Action(val limit:Int):UsersListStateAction
    data class Change_filter_Action(val filter:String):UsersListStateAction
    data class Change_sort_Action(val sort:Pair<String,String>):UsersListStateAction
    data class Change_error_Action(val error:String):UsersListStateAction
    data class Change_showProgressIndicator_Action(val shoProgressIndicator:Boolean):UsersListStateAction

    /**
     * Function returns custom string representation of UsersListState object
     *
     * @return String, representing all fields of UserState
     */
    override fun toString():String {
        var items_hash = HashMap<String,Any>()
        var counter = 0
        for (item in items) {
            items_hash[(counter++).toString()] = item
        }
        return "{items:${stringifyJSON(items_hash)},offset:$offset,limit:$limit,filter:$filter,sort:$sort,error:$error," +
                "showProgressIndicator:$showProgressIndicator}"
    }
}