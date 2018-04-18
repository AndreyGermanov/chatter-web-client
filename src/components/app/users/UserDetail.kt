package components.app.users

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import lib.jQuery
import lib.moment
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.dom.*
import store.*
import kotlin.browser.document


/**
 * Component used to draw User Detail page
 */
class UserDetail : RComponent<UserDetailState, UserDetailState>() {

    /********************
     * Global variables *
     ********************/

    // Link to Date of Birth datepicker instance
    var birthDatePicker:dynamic = ""

    /****************************
     * View rendering functions *
     ****************************/

    /**
     * Function used to draw HTML of component on the screen
     */
    override fun RBuilder.render() {
        var headerClass = "fas fa-user"
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
                    span{+"${(if (!props.login.isEmpty()) props.login else "User")}"}
                }
            }
            div(classes="panel-body") {
                attrs["style"] = bodyStyle
                if (props.errors["general"] != null && props.errors["general"] is UserDetailError) {
                    renderErrorMessage()
                }
                if (!props.successMessage.isEmpty()) {
                    renderSuccessMessage()
                }
                renderHeaderNav()
                renderForm()
            }
        }
    }

    /**
     * Function used to render and display error message, if error exists in the state
     */
    fun RBuilder.renderErrorMessage() {
        div(classes="col-md-12 alert alert-danger"){
            span(classes="glyphicon glyphicon-exclamation-sign") {
                +" "
            }
            span{
                +props.errors["general"]!!.getMessage()
            }
        }
    }

    /**
     * Function used to render and display success, if user form submitted successfully
     */
    fun RBuilder.renderSuccessMessage() {
        div(classes="col-md-12 alert alert-success"){
            span(classes="glyphicon glyphicon-ok") {
                +" "
            }
            span{
                +props.successMessage
            }
        }
    }


    /**
     * Function used to render and display navigation controls above table
     */
    fun RBuilder.renderHeaderNav() {
        div(classes="pull-left") {
            a(classes="btn btn-primary btn-xs", href="#/users") {
                span(classes="glyphicon glyphicon-arrow-left") {}
                span{+"Back"}
            }
            +" "
            button(classes="btn btn-success btn-xs") {
                attrs {
                    onClickFunction = {saveBtnClick()}
                }
                span(classes="glyphicon glyphicon-save") {}
                span{+"Save"}
            }
        }
        div(classes="clear") {}
    }

    /**
     * Function used to render User detail form
     */
    fun RBuilder.renderForm() {
        form(classes="form-horizontal") {
            attrs {
                onSubmitFunction = {
                    it.stopPropagation()
                    it.preventDefault()
                }
            }
            renderInputField("login","Login",props.login)
            renderInputField("password","Password",props.password,InputType.password)
            renderInputField("confirm_password","Confirm password",props.confirm_password,
                    InputType.password)
            renderInputField("email","Email",props.email,InputType.email)
            div(classes="form-group") {
                div(classes="col-md-1 col-md-offset-2") {
                    input(type=InputType.checkBox) {
                        attrs {
                            id = "activeBox"
                            checked = props.active
                            onChangeFunction = {setActive(it)}
                        }
                    }
                    label(classes="control-label") {
                        +" Active"
                    }
                }
                div(classes="col-md-9") {
                    button(classes="btn btn-primary btn-xs") {
                        attrs {
                            onClickFunction = { sendActivationEmailBtnClick()}
                        }
                        span(classes="glyphicon glyphicon-envelope") {}
                        span {+" Send activation email"}
                    }
                }
            }
            renderDropdownField("default_room","Default room",props.default_room,props.rooms)
            renderDropdownField("role","Role",props.role.toString(),UserRole.getHashMap())
            hr{}
            renderInputField("first_name", "First name",props.first_name)
            renderInputField("last_name","Last name",props.last_name)
            renderDropdownField("gender","Gender",props.gender.value,Gender.getHashMap())
            renderInputField("birthDate","Date of Birth")
        }
    }

    /**
     * Function used to render text field for form
     *
     * @param fieldName: field ID
     * @param fieldLabel: Label text for field (optional)
     * @param fieldValue: Value of field (optional)
     * @param fieldType: Type of text field (optional, e.g. text, password, email, number and so on)
     */
    fun RBuilder.renderInputField(fieldName:String,fieldLabel:String?=null,fieldValue:String?=null,
                                 fieldType:InputType=InputType.text) {
        div(classes="form-group") {
            label(classes="control-label col-md-2") {
                +(if (fieldLabel != null) fieldLabel else fieldName)
            }
            div(classes="col-md-10") {
                input(type=fieldType,classes="form-control ${(if (fieldName=="birthDate") "datepicker"; else "")}") {
                    attrs {
                        id = fieldName
                        onChangeFunction = {changeTextField(it,fieldName)}
                    }
                    if (fieldValue != null) {
                        attrs["value"] = fieldValue
                    }
                }
                if (props.errors[fieldName] != null) {
                    span(classes="error") {
                        +props.errors[fieldName]!!.getMessage()
                    }
                }
            }
        }
    }

    /**
     * Function used to render dropdown field for form
     *
     * @param fieldName: field_id
     * @param fieldLabel: Label text for field (optional)
     * @param fieldValue: Value of field (optional)
     * @param optionList: List of option values for dropdown in format ArrayList<HashMap<String,String>>
     */
    fun RBuilder.renderDropdownField(fieldName:String,fieldLabel:String?=null,fieldValue:String="",
                                     optionsList:HashMap<String,String>) {
        div(classes="form-group") {
            label(classes = "control-label col-md-2") {
                +(if (fieldLabel != null) fieldLabel else fieldName)
            }
            div(classes = "col-md-10") {
                select(classes = "form-control") {
                    attrs {
                        value = fieldValue
                        onChangeFunction = { changeTextField(it, fieldName) }
                    }
                    for ((id, label) in optionsList) {
                        option {
                            attrs {
                                value = id
                                selected = id == fieldValue
                                text(label)
                            }
                        }
                    }
                }
                if (props.errors[fieldName] != null) {
                    span(classes = "error") {
                        +props.errors[fieldName]!!.getMessage()
                    }
                }
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
        this.birthDatePicker = jQuery(".datepicker").asDynamic().datetimepicker(js("{" +
                "format: 'YYYY-MM-DD'}"))
        this.birthDatePicker.on("dp.change",fun(e:dynamic) {
            val birthDate = e.date.utc().unix()
            setBirthDate(birthDate)
        })
        var active_checkbox = document.getElementById("activeBox") as HTMLInputElement
        active_checkbox.defaultChecked = false
        active_checkbox.checked = false
        UserDetailState.GetRoomsList().exec {
            if (props.user_id != null && !props.user_id.toString().isEmpty()) {
                UserDetailState.ClearData().exec()
                UserDetailState.LoadItem().exec(props.user_id.toString())
            } else {
                UserDetailState.ClearData().exec()
            }
        }
    }

    override fun componentDidUpdate(prevProps: UserDetailState, prevState: UserDetailState) {
        if (prevProps.birthDate != props.birthDate) {
            setBirthDate(props.birthDate)
        }
    }

    /******************
     * Event handlers *
     ******************/

    /**
     * "Save" button click handler
     */
    fun saveBtnClick() {
        if (props.showProgressIndicator) {
            return
        }
        UserDetailState.SaveItem().exec()

    }

    /**
     * "Send activation email" button click handler
     */
    fun sendActivationEmailBtnClick() {
        if (props.showProgressIndicator) {
            return
        }
    }

    /**
     * Universal function used as event handler for all text fields in the form
     *
     * @param event: Event object which contains input field - source of new value
     * @param fieldName: Name of field to set
     */
    fun changeTextField(event:Event,fieldName:String) {
        var value = ""
        when (event.target) {
            is HTMLSelectElement -> value = (event.target as HTMLSelectElement).value.toString()
            is HTMLInputElement -> value = (event.target as HTMLInputElement).value.toString()
        }
        when(fieldName) {
            "login" -> appStore.dispatch(UserDetailState.Change_login_action(value))
            "email" -> appStore.dispatch(UserDetailState.Change_email_action(value))
            "password" -> appStore.dispatch(UserDetailState.Change_password_action(value))
            "confirm_password" -> appStore.dispatch(UserDetailState.Change_confirm_password_action(value))
            "default_room" -> appStore.dispatch(UserDetailState.Change_default_room_action(value))
            "role" -> appStore.dispatch(UserDetailState.Change_role_action(value.toIntOrNull() ?: 1))
            "first_name" -> appStore.dispatch(UserDetailState.Change_first_name_action(value))
            "last_name" -> appStore.dispatch(UserDetailState.Change_last_name_action(value))
            "birthDate" -> {
                var value = moment(value).utc().unix()
                setBirthDate(value)
            }
            "gender" -> appStore.dispatch(UserDetailState.Change_gender_action(Gender.valueOf(value)))
        }
    }

    /**
     * Function used to set "Active" user state from the form
     *
     * @param event: Event object which contains checkbox - source of new value
     */
    fun setActive(event:Event) {
        val inputItem = event.target as HTMLInputElement
        appStore.dispatch(UserDetailState.Change_active_action(inputItem.checked))
    }

    /**
     * Function used as a change handler for "Date of Birth" input field. Used to convert
     * date to timestamp and set birthDate variable in applicaiton state.
     *
     * @param value: Timestamp in seconds
     */
    fun setBirthDate(timestamp:dynamic) {
        appStore.dispatch(UserDetailState.Change_birthDate_action(timestamp.toString().toInt()))
        this.birthDatePicker.data("DateTimePicker").date(moment(timestamp*1000))
    }
}

/**
 * Class constructor. Runs when component initiates and before it redraws itself
 *
 * @param state - object with all properties from Application state, which component can display
 * or use in it functions
 */
fun RBuilder.userDetail(state:UserDetailState) = child(UserDetail::class) {
    attrs.user_id = state.user_id
    attrs.successMessage = state.successMessage
    attrs.showProgressIndicator = state.showProgressIndicator
    attrs.role = state.role
    attrs.password = state.password
    attrs.login = state.login
    attrs.gender = state.gender
    attrs.birthDate = state.birthDate
    attrs.last_name = state.last_name
    attrs.first_name = state.first_name
    attrs.errors = state.errors
    attrs.email = state.email
    attrs.default_room = state.default_room
    attrs.confirm_password = state.confirm_password
    attrs.active = state.active
    attrs.rooms = state.rooms
}