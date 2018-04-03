package components.app.login

import core.MessageCenterResponseListener
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import react.dom.*
import lib.State
import lib.StoreSubscriber
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import store.AppState
import store.LoginFormState
import store.appStore
import utils.LogLevel
import utils.Logger
import kotlin.browser.document
import kotlin.browser.window

class LoginForm : RComponent<RProps, LoginFormState>(), StoreSubscriber {

    /**
     * Function runs right after component appears on the screen
     */
    override fun componentDidMount() {
        appStore.subscribe(this)
        var state = appStore.state as AppState
        this.setState(state.loginForm)
    }

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
                           if (state.errors != null && state.errors["login"] != null) {
                               div(classes="error") {
                                   span(classes="error") {
                                       attrs {
                                           id = "loginError"
                                       }
                                       +state.errors["login"]!!.getMessage()
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
                                   value = state.password
                                   this.onChangeFunction = {
                                       var element = document.getElementById("passwordField") as HTMLInputElement
                                       appStore.dispatch(LoginFormState.changePasswordField(element?.value))
                                   }
                               }
                           }
                           if (state.errors!=null && state.errors["password"] != null) {
                               div(classes="error") {
                                   span(classes="error") {
                                       attrs {
                                           id = "passwordError"
                                       }
                                       +state.errors["password"]!!.getMessage()
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
     * Here component can redraw itself, if something changed
     *
     * @param state: Changed state
     */
    override fun newState(state: State) {
        val state = state as AppState
        Logger.log(LogLevel.DEBUG_REDUX,"Applying new state to LoginForm. State: $state",
                "LoginForm","newState")
        if (state.loginForm.errors["general"] != null) {
            window.alert(state.loginForm.errors["general"]!!.getMessage())
            state.loginForm.errors.remove("general")
            appStore.dispatch(LoginFormState.changeErrorsField(state.loginForm.errors))
        }
        this.setState(state.loginForm)
    }

}

fun RBuilder.loginForm() = child(LoginForm::class) {}