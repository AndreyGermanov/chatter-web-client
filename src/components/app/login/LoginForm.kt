package components.app.login

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import react.dom.*
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import store.AppState
import store.LoginFormError
import store.LoginFormState
import store.appStore
import utils.LogLevel
import utils.Logger
import kotlin.browser.document
import kotlin.browser.window

class LoginForm : RComponent<LoginFormState, LoginFormState>() {

    /**
     * Function used to draw HTML of component on the screen
     */
    override fun RBuilder.render() {
        div(classes="panel panel-primary col-sm-6 screen_center") {
            div {
                attrs["class"] = "panel-heading"
                h3 {
                    +"Login"
                }
            }
            div {
                attrs["class"] = "panel-body"
               form(classes="form-horizontal") {
                   attrs {
                       onSubmitFunction = {
                           it.stopPropagation()
                           it.preventDefault()
                       }
                   }
                   div(classes="form-group") {
                       label(classes="control-label col-sm-2") {
                           +"Login"
                       }
                       div(classes="col-sm-10") {

                           input(classes="form-control") {
                               attrs {
                                   id = "loginField"
                                   value = state.login
                                   onChangeFunction = {
                                        var element = document.getElementById("loginField") as HTMLInputElement
                                        appStore.dispatch(LoginFormState.changeLoginField(element?.value))
                                   }
                               }
                           }
                           if (props.errors["login"] != null) {
                               div(classes="error") {
                                   span(classes="error") {
                                       attrs {
                                           id = "loginError"
                                       }
                                       +props.errors["login"]!!.getMessage()
                                   }
                               }
                           }
                       }
                   }
                   div(classes="form-group") {
                       label(classes="control-label col-sm-2") {
                           +"Password"
                       }
                       div(classes="col-sm-10") {
                           input(classes="form-control",type= InputType.password) {
                               attrs {
                                   id = "passwordField"
                                   value = props.password
                                   this.onChangeFunction = {
                                       var element = document.getElementById("passwordField") as HTMLInputElement
                                       appStore.dispatch(LoginFormState.changePasswordField(element?.value))
                                   }
                               }
                           }
                           if (props.errors["password"] != null) {
                               div(classes="error") {
                                   span(classes="error") {
                                       attrs {
                                           id = "passwordError"
                                       }
                                       +props.errors["password"]!!.getMessage()
                                   }
                               }
                           }
                       }
                   }
                   div(classes="form-group") {
                       button(classes="btn btn-primary") {
                           attrs {
                               onClickFunction = {
                                   LoginFormState.doLogin().exec()
                               }
                           }
                           +"Login"
                       }
                   }
               }
            }
        }
    }

    /**
     * Function runs every time when component receives notification about changing application state from reducer
     * or properties from uplevel object. Here component can control what to do right after UI updated
     *
     * @param prevProps: Previous property values, to compare current properties with
     * @param prevState: Previous state, to compare with current state values
     */
    override fun componentDidUpdate(prevProps: LoginFormState, prevState: LoginFormState) {
        Logger.log(LogLevel.DEBUG_REDUX, "Applying new state to LoginForm. State: $props",
                "LoginForm", "newState")
        if (props.errors["general"] != null) {
            window.alert(props.errors["general"]!!.getMessage())
            val errors = props.errors
            errors.remove("general")
            appStore.dispatch(LoginFormState.changeErrorsField(errors))
        }
    }

}

fun RBuilder.loginForm(login:String="", password:String="",
                       errors:HashMap<String, LoginFormError> = HashMap()) = child(LoginForm::class) {
    attrs.login = login
    attrs.password = password
    attrs.errors = errors
}