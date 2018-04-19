package components.app.users

import core.MessageCenter
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.dom.*
import store.UsersListState
import store.appStore
import kotlin.browser.window
import kotlin.math.ceil

/**
 * Component used to draw User List page
 */
class UsersList : RComponent<UsersListState, UsersListState>() {

    /****************************
     * View rendering functions *
     ****************************/

    /**
     * Function used to draw HTML of component on the screen
     */
    override fun RBuilder.render() {
        var headerClass = "fas fa-users"
        var bodyStyle = js("{opacity:1.0}")
        if (props.showProgressIndicator) {
            headerClass += " fa-pulse"
            bodyStyle = js("{opacity:0.3}")
        }
        div(classes="panel panel-primary") {
            div(classes="panel-heading") {
                h3 {
                    span(classes=headerClass) {}
                    +" "
                    span{+"Users"}
                }
            }
            div(classes="panel-body") {
                attrs["style"] = bodyStyle
                if (!props.error.isEmpty()) {
                    renderErrorMessage()
                }
                renderHeaderNav()
                renderTable()
                renderFooter()
            }
        }
    }

    /**
     * Function used to render and display error image, if error exists in the state
     */
    fun RBuilder.renderErrorMessage() {
        div(classes="col-md-12 alert alert-danger"){
            span(classes="glyphicon glyphicon-exclamation-sign") {
                +" "
            }
            span{

                +props.error
            }
        }
    }

    /**
     * Function used to render and display navigation controls above table
     */
    fun RBuilder.renderHeaderNav() {
        div(classes="pull-left") {
            a(classes="btn btn-success btn-xs", href="#/user") {
                span(classes="glyphicon glyphicon-plus") {}
                span{+"New"}
            }
            +" "
            button(classes="btn btn-info btn-xs") {
                attrs {
                    onClickFunction = {refreshBtnClick()}
                }
                span(classes="glyphicon glyphicon-refresh") {}
                span{+"Refresh"}
            }
        }
        div(classes="pull-right") {
            input(type= InputType.text,classes="form-control") {
                attrs {
                    value = props.filter
                    id = "usersListSearchField"
                    placeholder = "Search ..."
                    onChangeFunction = { setFilter(it) }
                }
            }
        }
    }

    /**
     *
     * Function used to render table with list of users
     */
    fun RBuilder.renderTable() {
        table(classes="table table-bordered") {
            tbody {
                renderTableHeader()
                for (item in props.items) {
                    renderTableRow(item)
                }
            }
        }
    }

    /**
     * Function used to render table header row
     */
    fun RBuilder.renderTableHeader() {
        tr(classes="table-header") {
            th {
                input(type=InputType.checkBox) {
                    attrs {
                        checked = props.isAllItemsSelected
                        onChangeFunction = {toggleAllItems(it)}
                    }
                }
            }
            renderTableHeaderColumn("login","Login")
            renderTableHeaderColumn("email","Email")
            renderTableHeaderColumn("role","Role")
            renderTableHeaderColumn("first_name","First Name")
            renderTableHeaderColumn("last_name","Last Name")
            renderTableHeaderColumn("active","Active")
            renderTableHeaderColumn("default_room","Room")
        }
    }

    /**
     * Function used to render column in table header row
     *
     * @param columnName: column system ID
     * @param columnTitle: title of column
     */
    fun RBuilder.renderTableHeaderColumn(columnName:String,columnTitle:String) {
        th {
            +columnTitle
            if (props.sort.first == columnName) {
                var sortClass = "pull-right yellow glyphicon"
                if (props.sort.second == "ASC")
                    sortClass += " glyphicon-arrow-down"
                else
                    sortClass += " glyphicon-arrow-up"
                span(classes=sortClass) {}
            }
            attrs {
                onClickFunction = {setSort(columnName)}
            }
        }
    }

    /**
     * Function used to render row with provided index
     *
     * @param item: Link to item data row
     */
    fun RBuilder.renderTableRow(item:HashMap<String,String>) {
        tr {
            td {
                if (item["_id"] != MessageCenter.user_id) {
                    input(type = InputType.checkBox) {
                        attrs {
                            checked = props.selectedItems.contains(item["_id"]!!.toString())
                            onChangeFunction = {
                                toggleItem(it, item["_id"].toString())
                            }
                        }
                    }
                }
            }
            renderTableColumn(item, "login")
            renderTableColumn(item, "email")
            renderTableColumn(item, "role")
            renderTableColumn(item, "first_name")
            renderTableColumn(item, "last_name")
            renderTableColumn(item, "active")
            renderTableColumn(item, "default_room")
        }
    }

    /**
     * Function used to render table column
     *
     * @param item: Link to item data row
     * @param columnName: name of column from item data row
     */
    fun RBuilder.renderTableColumn(item:HashMap<String,String>,columnName:String) {
        td {
            a(href="#/user/${item["_id"]!!}") {
                +(item[columnName]!!)
            }
        }
    }

    /**
     * Function used to render footer navigation
     */
    fun RBuilder.renderFooter() {
        var deleteButtondisplayStyle = js("{display:'inline'}")
        if (props.selectedItems.count()==0) {
            deleteButtondisplayStyle = js("{display:'none'}")
        }
        div(classes="pull-left") {
            button(classes="btn btn-danger btn-xs") {
                attrs.onClickFunction = { deleteBtnClick() }
                attrs["style"] = deleteButtondisplayStyle
                span(classes="glyphicon glyphicon-remove"){}
                +" "
                span{+"Delete selected"}
            }
        }
        var options = arrayOf(10,20,50,100,0)
        div(classes="pull-right") {
            +"Show "
            select(classes="form-control inline") {
                attrs {
                    value = props.limit.toString()
                    onChangeFunction = {setLimit(it)}
                }
                for (option in options) {
                    option {
                        attrs {
                            value = option.toString()
                            selected = (option == props.limit)
                            text(if (option!=0) option.toString() else "All")
                        }
                    }
                }
            }
            +" "
            button(classes="btn btn-primary btn-xs") {
                var displayStyle= if (props.offset==0) js("{display:'none'}") else js("{display:'inline'}")
                attrs["style"] = displayStyle
                attrs {
                    onClickFunction = {prevPageBtnClick()}
                }
                span(classes="glyphicon glyphicon-arrow-left") {}
            }
            +" "
            select(classes="form-control inline") {
                var pages = 1
                if (props.limit > 0) {
                    pages = ceil(props.total.toDouble() / props.limit.toDouble()).toString().toInt()
                }
                for (page in 1..pages) {
                    option{
                        attrs {
                            value = ((page-1)*props.limit).toString()
                            selected = (page-1)*props.limit == props.offset
                            text(page)
                        }
                    }
                }
                attrs {
                    onChangeFunction = {setPage(it)}
                }
            }
            +" "
            button(classes="btn btn-primary btn-xs") {
                var displayStyle= if (props.offset>props.total-props.limit || props.limit==0) js("{display:'none'}") else js("{display:'inline'}")
                attrs["style"] = displayStyle
                attrs {
                    onClickFunction = {nextPageBtnClick()}
                }
                span(classes="glyphicon glyphicon-arrow-right") {}
            }
        }
    }

    /***********************
     * Life cycle methods *
     ***********************/

    /**
     * Function runs when component appears on the screen
     */
    override fun componentDidMount() {
        UsersListState.LoadList().exec()
    }

    /******************
     * Event handlers *
     ******************/

    /**
     * "Search ..." input field handler. Used to set filter text for list
     *
     * @param event: Event object, which contains link to input field, which is a source of event
     */
    fun setFilter(event: Event) {
        if (props.showProgressIndicator) {
            return
        }
        val inputItem = event.target as HTMLInputElement
        appStore.dispatch(UsersListState.Change_filter_Action(inputItem.value.trim()))
        appStore.dispatch(UsersListState.Change_offset_Action(0))
        UsersListState.LoadList().exec()
    }

    /**
     * Number of items per page dropdown change handler. Used to set "limit" field
     *
     * @param event: Event object, which contains link to dropdown, which is a source of event
     */
    fun setLimit(event: Event) {
        if (props.showProgressIndicator) {
            return
        }
        val inputItem = event.target as HTMLSelectElement
        appStore.dispatch(UsersListState.Change_limit_Action(inputItem.value.toInt()))
        appStore.dispatch(UsersListState.Change_offset_Action(0))
        UsersListState.LoadList().exec()
    }

    /**
     * Current page dropdown change handler. Used to set current offset of list based on selected page
     *
     * @param event: Event object, which contains link to dropdown, which is a source of event
     */
    fun setPage(event: Event) {
        if (props.showProgressIndicator) {
            return
        }
        val inputItem = event.target as HTMLSelectElement
        appStore.dispatch(UsersListState.Change_offset_Action(inputItem.value.toInt()))
        UsersListState.LoadList().exec()
    }

    /**
     * "Next page" button click handler. Used to set offset based on next page number
     */
    fun nextPageBtnClick() {
        if (props.showProgressIndicator) {
            return
        }
        if (props.offset+props.limit<props.total) {
            appStore.dispatch(UsersListState.Change_offset_Action(props.offset + props.limit))
            UsersListState.LoadList().exec()
        }
    }

    /**
     * "Previous page" button click handler. Used to set offset based on previous page number
     */
    fun prevPageBtnClick() {
        if (props.showProgressIndicator) {
            return
        }
        if (props.offset-props.limit>=0) {
            appStore.dispatch(UsersListState.Change_offset_Action(props.offset-props.limit))
            UsersListState.LoadList().exec()
        }
    }

    /**
     * Table column header click handler. Used to change sort order of items in list
     */
    fun setSort(field:String) {
        if (props.showProgressIndicator) {
            return
        }
        var sort:Pair<String,String> = field to "ASC"
        if (props.sort.first == field) {
            if (props.sort.second == "ASC") {
                sort = field to "DESC"
            } else {
                sort = field to "ASC"
            }
        }
        appStore.dispatch(UsersListState.Change_sort_Action(sort))
        appStore.dispatch(UsersListState.Change_offset_Action(0))
        UsersListState.LoadList().exec()
    }

    /**
     * Select checkbox of table row handler. Used to select items for group actions
     *
     * @param event: Event object which contains link to checkbox object which is a source of event
     * @param id: ID of item, which need select or unselect
     */
    fun toggleItem(event:Event,id:String) {
        if (props.showProgressIndicator) {
            return
        }
        val checkbox = event.target as HTMLInputElement
        val selectedItems = props.selectedItems
        if (checkbox.checked) {
            if (!selectedItems.contains(id)) {
                selectedItems.add(id)
            }
        } else {
            if (selectedItems.contains(id)) {
                selectedItems.remove(id)
            }
        }
        appStore.dispatch(UsersListState.Change_selectedItems_Action(selectedItems))
        if (selectedItems.count() == 0 || selectedItems.count() != props.items.count()) {
            appStore.dispatch(UsersListState.Change_isAllItemsSelected_Action(false))
        } else {
            appStore.dispatch(UsersListState.Change_isAllItemsSelected_Action(true))
        }
    }

    /**
     * Select checkbox of table header handler. Used to select or deselect all items in a table
     *
     * @param event: Event object which contains link to checkbox object which is a source of event
     */
    fun toggleAllItems(event:Event) {
        if (props.showProgressIndicator) {
            return
        }
        val checkbox = event.target as HTMLInputElement
        val selectedItems = props.selectedItems
        if (!checkbox.checked) {
            selectedItems.clear()
        } else {
            for (item in props.items) {
                if (item["_id"]!=null && !selectedItems.contains(item["_id"].toString())
                        && item["_id"] != MessageCenter.user_id) {
                    selectedItems.add(item["_id"].toString())
                }
            }
        }
        appStore.dispatch(UsersListState.Change_selectedItems_Action(selectedItems))
        appStore.dispatch(UsersListState.Change_isAllItemsSelected_Action(checkbox.checked))
    }

    /**
     * "Refresh" button click handler
     */
    fun refreshBtnClick() {
        if (props.showProgressIndicator) {
            return
        }
        UsersListState.LoadList().exec()
    }

    /**
     * "Delete selected" button click handler
     */
    fun deleteBtnClick() {
        if (props.selectedItems.count()>0 && window.confirm("Are you sure?")==true) {
            UsersListState.DeleteItems().exec() {
                UsersListState.LoadList().exec()
            }
        }
    }
}

/**
 * Class constructor. Runs when component initiates and before it redraws itself
 *
 * @param state - object with all properties from Application state, which component can display
 * or use in it functions
 */
fun RBuilder.usersList(state:UsersListState) = child(UsersList::class) {
    attrs.error = state.error
    attrs.showProgressIndicator = state.showProgressIndicator
    attrs.filter = state.filter
    attrs.items = state.items
    attrs.limit = state.limit
    attrs.offset = state.offset
    attrs.selectedItems = state.selectedItems
    attrs.isAllItemsSelected = state.isAllItemsSelected
    attrs.sort = state.sort
    attrs.total = state.total
}