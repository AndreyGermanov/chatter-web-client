package store

import core.MessageCenter
import core.MessageCenterResponseListener
import lib.Action
import lib.State
import utils.LogLevel
import utils.Logger
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
    var offset = 0
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
     * Action which loads users list from MessageCenter, using current filtering settings
     */
    class loadList: MessageCenterResponseListener {
        /**
         * Action executor, prepares request to server and places it to pendingRequests queue of MessageCenter
         *
         * @param state: data filter,sorting and limit options, used to send to MessageCenter
         */
        fun exec(props:UsersListState?=null) {
            Logger.log(LogLevel.DEBUG,"Begin loadList actions to load list of users. Preparing request",
                    "UsersListState","loadList.exec")
            var state:UsersListState?
            if (props == null) {
                state = (appStore.state as AppState).usersList!!
            } else {
                state = props
            }
            appStore.dispatch(UsersListState.Change_error_Action(""))
            if (!MessageCenter.isConnected) {
                appStore.dispatch(UsersListState.Change_error_Action(UsersListError.RESULT_ERROR_CONNECTION_ERROR.getMessage()))
                return
            }
            appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(true))
            val request = hashMapOf(
                    "action" to "admin_get_users_list",
                    "sender" to this,
                    "offset" to state.offset,
                    "sort" to hashMapOf(state.sort),
                    "fields" to arrayOf("_id","login","email","role","first_name","last_name","default_room","active")
            )
            if (!state.filter.trim().isEmpty()) {
                request["filter"] = state.filter.trim()
            }
            if (state.limit>0) {
                request["limit"] = state.limit
            }
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UsersListState","loadList.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UsersListState","loadList.exec")
            } else {
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}")
            }
        }

        /**
         * Function receives response from MessageCenter, after it processes request placed to queue
         *
         * @param request_id: ID of processed request
         * param response: Body of response from server
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>) {
            Logger.log(LogLevel.DEBUG,"Received response from WebSocket server for request $request_id. " +
                    "Response body: ${stringifyJSON(response)}",
                    "UsersListState","loadList.handleWebSocketResponse")
            appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(false))
            if (response["status"]==null) {
                Logger.log(LogLevel.WARNING,"Response does not contain 'status' field",
                        "UsersListState","loadList.handleWebSocketResponse")
                return
            }
            if (response["status"] == "ok" && response["list"]!=null) {
                val list = response["list"] as? HashMap<String,Any>
                if (list != null) {
                    val items = ArrayList<HashMap<String,String>>(list.count())

                    val state = appStore.state as AppState
                    val selectedItems = state.usersList.selectedItems
                    for ((index,item) in list) {
                        console.log(index)
                        val item = item as HashMap<String,Any>
                        var role = "User"
                        if (item["role"].toString() == "2") {
                            role = "Admin"
                        }
                        val active = if (item["active"].toString().toBoolean()) "Active" else "Inactive"
                        var first_name = ""
                        if (item["first_name"] != null) {
                            first_name = item["first_name"].toString().trim()
                        }
                        var last_name = ""
                        if (item["last_name"] != null) {
                            last_name = item["last_name"].toString().trim()
                        }
                        var default_room = ""
                        if (item["default_room"] != null) {
                            default_room = item["default_room"].toString().trim()
                        }
                        var user_id = item["_id"].toString().trim()
                        items.add(hashMapOf(
                                "_id" to user_id,
                                "login" to item["login"].toString().trim(),
                                "email" to item["email"].toString().trim(),
                                "role" to role,
                                "active" to active,
                                "first_name" to first_name,
                                "last_name" to last_name,
                                "room" to default_room
                        ))
                    }
                    MessageCenter.removeFromRequestsWaitingResponses(request_id)
                    appStore.dispatch(UsersListState.Change_items_Action(items))
                }
            }
        }
    }
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

/**
 * Definitions of possible User list form errors
 */
enum class UsersListError(val value:String):SmartEnum {
    RESULT_OK("RESULT_OK"),
    RESULT_ERROR_CONNECTION_ERROR("RESULT_ERROR_CONNECTION_ERROR"),
    RESULT_ERROR_UNKNOWN("RESULT_ERROR_UNKNOWN"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR");
    override fun getMessage(): String {
        var result = ""
        when (this) {
            RESULT_ERROR_CONNECTION_ERROR -> result = "Server connection error."
            RESULT_ERROR_UNKNOWN -> result = "Unknown error. Please contact support."
            INTERNAL_ERROR -> result = "System error. Please contact support."
            AUTHENTICATION_ERROR -> result = "Authentication error. Please contact support."
        }
        return result;
    }
}