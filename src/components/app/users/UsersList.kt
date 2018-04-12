package components.app.users

import kotlinx.html.InputType
import kotlinx.html.id
import react.RBuilder
import react.RComponent
import react.dom.*
import store.UsersListState
import kotlin.math.ceil


/**
 * Component used to draw top navigation bar
 */
class UsersList : RComponent<UsersListState, UsersListState>() {

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
                +"&nbsp;"
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
            button(classes="btn btn-success btn-xs") {
                span(classes="glyphicon glyphicon-plus") {}
                span{+"New"}
            }
            +" "
            button(classes="btn btn-info btn-xs") {
                span(classes="glyphicon glyphicon-refresh") {}
                span{+"Refresh"}
            }
        }
        div(classes="pull-right") {
            input(type= InputType.text,classes="form-control") {
                attrs {
                    id = "usersListSearchField"
                    placeholder = "Search ..."
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
                        id = "usersListCheckAllitems"
                    }
                }
            }
            renderTableHeaderColumn("login","Login")
            renderTableHeaderColumn("email","Email")
            renderTableHeaderColumn("role","Role")
            renderTableHeaderColumn("first_name","First Name")
            renderTableHeaderColumn("last_name","Last Name")
            renderTableHeaderColumn("active","Active")
            renderTableHeaderColumn("room","Room")
        }
    }

    /**
     * Function used to render column in table header row
     */
    fun RBuilder.renderTableHeaderColumn(columnName:String,columnTitle:String) {
        th {
            +columnTitle
            if (props.sort.first == columnName) {
                var sortClass = "pull-right yellow glyphicon"
                if (props.sort.second == "ASC")
                    sortClass += " glyphicon-arrow-down"
                else
                    sortClass = " glyphicon-arrow-up"
                span(classes=sortClass) {}
            }
        }
    }

    /**
     * Function used to render row with provided index
     */
    fun RBuilder.renderTableRow(item:HashMap<String,String>) {
        td {
            input(type=InputType.checkBox) {
                attrs {
                    checked = props.selectedItems.contains(item["user_id"]!!)
                }
            }
        }
        renderTableColumn(item,"login")
        renderTableColumn(item,"email")
        renderTableColumn(item,"role")
        renderTableColumn(item,"first_name")
        renderTableColumn(item,"last_name")
        renderTableColumn(item,"active")
        renderTableColumn(item,"room")
    }

    /**
     * Function used to render table column
     */
    fun RBuilder.renderTableColumn(item:HashMap<String,String>,columnName:String) {
        td {
            a(href="#/user/${item["user_id"]!!}") {
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
                span(classes="glyphicon glyphicon-arrow-left") {}
            }
            +" "
            select(classes="form-control inline") {
                var pages = ceil(props.total.toDouble()/props.limit.toDouble()).toString().toInt()
                for (page in 1..pages) {
                    option{
                        attrs {
                            value = ((page-1)*props.limit).toString()
                            selected = (page-1)*props.limit == props.offset
                            text(page)
                        }
                    }
                }
            }
            +" "
            button(classes="btn btn-primary btn-xs") {
                var displayStyle= if (props.offset>props.total-props.limit) js("{display:'none'}") else js("{display:'inline'}")
                attrs["style"] = displayStyle
                span(classes="glyphicon glyphicon-arrow-right") {}
            }
        }
    }
}

fun RBuilder.usersList(state:UsersListState) = child(UsersList::class) {
    attrs.error = state.error
    attrs.showProgressIndicator = state.showProgressIndicator
    attrs.filter = state.filter
    attrs.items = state.items
    attrs.limit = state.limit
    attrs.offset = state.offset
    attrs.selectedItems = state.selectedItems
    attrs.sort = state.sort
    attrs.total = state.total
}