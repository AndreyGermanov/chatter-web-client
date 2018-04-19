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
    class LoadList: MessageCenterResponseListener {
        /**
         * Action executor, prepares request to server and places it to pendingRequests queue of MessageCenter
         *
         * @param state: data filter,sorting and limit options, used to send to MessageCenter
         */
        fun exec(props:UsersListState?=null) {
            Logger.log(LogLevel.DEBUG,"Begin loadList actions to load list of users. Preparing request",
                    "UsersListState","LoadList.exec")
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
                    "fields" to arrayOf("_id","login","email","role","first_name","last_name","default_room","active"),
                    "get_total" to true,
                    "get_presentations" to true
            )
            if (!state.filter.trim().isEmpty()) {
                request["filter"] = state.filter.trim()
            }
            if (state.limit>0) {
                request["limit"] = state.limit
            }
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UsersListState","LoadList.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UsersListState","LoadList.exec")
            } else {
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}","UsersListState","LoadList.exec")
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
                    "UsersListState","LoadList.handleWebSocketResponse")
            appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(false))
            if (response["status"]==null) {
                Logger.log(LogLevel.WARNING,"Response does not contain 'status' field",
                        "UsersListState","LoadList.handleWebSocketResponse")
                return
            }
            if (response["status"] == "ok" && response["list"]!=null) {
                val list = response["list"] as? HashMap<String,Any>
                if (list != null) {
                    val items = ArrayList<HashMap<String,String>>(list.count())

                    val state = appStore.state as AppState
                    val selectedItems = ArrayList<String>()
                    for ((index,obj) in list) {
                        var item:HashMap<String,Any>? = null
                        if (obj is HashMap<*,*>) {
                            item = obj as HashMap<String,Any>
                        } else {
                            var count = 0
                            if (obj is String) {
                                try {
                                    count = obj.toString().toInt()
                                } catch (e:Exception) {
                                    Logger.log(LogLevel.WARNING,"Could not parse 'total' row from '$obj' value",
                                            "UsersListState","LoadList.handleWebSocketResponse")
                                }
                            } else if (obj is Int) {
                                count = obj as Int
                            }
                            appStore.dispatch(UsersListState.Change_total_Action(count))
                            continue
                        }
                        val role = item["role_text"]?.toString() ?: ""
                        val active = item["active_text"]?.toString() ?: ""
                        val firstName = item["first_name"]?.toString()?.trim() ?: ""
                        val lastName = item["last_name"]?.toString()?.trim() ?: ""
                        val defaultRoom = item["default_room_text"]?.toString()?.trim() ?: ""
                        val userId = item["_id"].toString().trim()
                        items.add(hashMapOf(
                                "_id" to userId,
                                "login" to item["login"].toString().trim(),
                                "email" to item["email"].toString().trim(),
                                "role" to role,
                                "active" to active,
                                "first_name" to firstName,
                                "last_name" to lastName,
                                "default_room" to defaultRoom
                        ))
                        if (state.usersList.selectedItems.contains(userId)) {
                            selectedItems.add(userId)
                        }
                    }
                    MessageCenter.removeFromRequestsWaitingResponses(request_id)
                    appStore.dispatch(UsersListState.Change_items_Action(items))
                    appStore.dispatch(UsersListState.Change_selectedItems_Action(selectedItems))
                }
            }
        }
    }

    /**
     * Action used to delete items from user list
     */
    class DeleteItems: MessageCenterResponseListener {

        // Callback function, which should run after process this action (optional)
        var callback: (()->Unit)? = null
        /**
         * Function used to start action. Prepares request, and sends it to MessageCenter
         *
         * @param callback Callback function, which should run after process this action (optional)
         */
        fun exec(callback:(()->Unit)?) {
            val state = appStore.state as AppState
            val list = state.usersList
            this.callback = callback
            Logger.log(LogLevel.DEBUG,"Starting DeleteItems action.",
                    "UsersListState","DeleteItems.exec")
            if (list.showProgressIndicator) {
                Logger.log(LogLevel.WARNING,"Could not start request, because other request already going",
                        "UsersListsState","DeleteItems.exec")
                return
            }
            if (list.selectedItems.count() == 0) {
                appStore.dispatch(UsersListState.Change_error_Action(
                        UsersListError.RESULT_ERROR_FIELD_IS_EMPTY.getMessage()))
                Logger.log(LogLevel.DEBUG,"No items selected to delete.",
                        "UsersListState","DeleteItems.exec")
                return
            }
            if (!MessageCenter.isConnected) {
                appStore.dispatch(UserDetailState.Change_errors_action(
                        hashMapOf("general" to UserDetailError.RESULT_ERROR_CONNECTION_ERROR)))
                Logger.log(LogLevel.DEBUG,"Server connection error.",
                        "UsersListState","DeleteItems.exec")
                return
            }
            appStore.dispatch(UsersListState.Change_error_Action(UsersListError.RESULT_OK.getMessage()))
            Logger.log(LogLevel.DEBUG,"Validating data and preparing request body.",
                    "UsersListState","DeleteItems.exec")
            val request = hashMapOf(
                    "action" to "admin_remove_users",
                    "sender" to this,
                    "list" to list.selectedItems
            )

            appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(true))
            Logger.log(LogLevel.DEBUG,"Prepared request for MessageCenter. Request body: " +
                    "${stringifyJSON(request)}","UsersListState","DeleteItems.exec")
            val result = MessageCenter.addToPendingRequests(request)
            if (result != null) {
                Logger.log(LogLevel.DEBUG, "Added request to MessageCenter pending requests queue. " +
                        "Added request: ${stringifyJSON(result)}", "UsersListState","DeleteItem.exec")
            } else {
                Logger.log(LogLevel.WARNING, "Could not add request to MessageCenter pending requests queue. " +
                        "Request: ${stringifyJSON(request)}","UsersListState","DeleteItem.exec")
                appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(false))
            }
        }

        /**
         * Function receives response from MessageCenter, after it processes request placed to queue
         *
         * @param request_id: ID of processed request
         * param response: Body of response from server
         *
         * @returns custom result or nothing, depending on sense of this request. In this case, it returns
         * nothing
         */
        override fun handleWebSocketResponse(request_id: String, response: HashMap<String, Any>): Any? {
            appStore.dispatch(UsersListState.Change_showProgressIndicator_Action(false))
            MessageCenter.removeFromRequestsWaitingResponses(request_id)
            var state = appStore.state as AppState
            if (!state.usersList.processResponse(response)) {
                return null
            }
            if (this.callback != null) {
                this.callback!!()
            }
            return null
        }
    }

    /**
     * Function used to implement general process and check for responses, which came
     * from WebSocket server. It checks general response format and response codes
     *
     * @param response: Response body
     * returns True if general fields ok or false otherwise
     */
    fun processResponse(response:HashMap<String,Any>):Boolean {
        if (response["status"] == null) {
            Logger.log(LogLevel.WARNING, "Response does not contain 'status' field",
                    "UsersListState", "processResponse")
            return false
        }
        var response_code = UsersListError.RESULT_ERROR_UNKNOWN_ERROR
        if (response["status"] == "error") {
            var field = "general"
            try {
                response_code = UsersListError.valueOf(response["status_code"].toString())
            } catch (e:Exception) {
                Logger.log(LogLevel.WARNING, "Could not parse result code ${response["status_code"]}",
                        "UsersListState","processResponse")
            }
            appStore.dispatch(UsersListState.Change_error_Action(response_code.getMessage()))
            return false
        }
        if (response["status"] != "ok") {
            Logger.log(LogLevel.WARNING, "Unknown status returned ${response["status"]}",
                    "UsersListState","processResponse")
            appStore.dispatch(UsersListState.Change_error_Action(UsersListError.INTERNAL_ERROR.getMessage()))
            return false
        }
        return true
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
    RESULT_ERROR_UNKNOWN_ERROR("RESULT_ERROR_UNKNOWN_ERROR"),
    RESULT_ERROR_FIELD_IS_EMPTY("RESULT_ERROR_LIST_IS_EMPTY"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR");
    override fun getMessage(): String {
        return when (this) {
            RESULT_OK -> ""
            RESULT_ERROR_CONNECTION_ERROR -> "Server connection error."
            RESULT_ERROR_UNKNOWN_ERROR -> "Unknown error. Please contact support."
            RESULT_ERROR_FIELD_IS_EMPTY -> "No users selected to delete"
            INTERNAL_ERROR -> "System error. Please contact support."
            AUTHENTICATION_ERROR -> "Authentication error. Please contact support."
        }
    }
}