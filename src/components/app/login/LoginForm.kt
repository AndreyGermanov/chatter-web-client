package components.app.login

import core.MessageCenterResponseListener
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import lib.State
import lib.StoreSubscriber
import react.RBuilder
import react.RComponent
import react.RProps
import react.dom.*
import store.AppState
import store.LoginFormAction
import store.LoginFormState
import store.appStore

class LoginForm : RComponent<RProps, LoginFormState>(), MessageCenterResponseListener, StoreSubscriber {

    override fun componentDidMount() {
        appStore.subscribe(this)
    }

    override fun RBuilder.render() {
        div {
            attrs["class"] = "panel panel-primary col-sm-4"
            div {
                attrs["class"] = "panel-heading"
                h3 {
                    +"Login"
                }
            }
            div {
                attrs["class"] = "panel-body"
               form(classes="form-horizontal") {
                   div(classes="form-group") {
                       label(classes="control-label col-sm-2") {
                           +"Login"
                       }
                       div(classes="col-sm-10") {
                           input(classes="form-control") {
                               attrs {
                                   value = state.login
                                   onChangeFunction = {
                                       appStore.dispatch(LoginFormState.changeLoginField(value))
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
                                   value = state.password
                                   onChangeFunction = {
                                       appStore.dispatch(LoginFormState.changePasswordField(value))
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

    override fun handleWebSocketResponse(request_id:String, response: HashMap<String, Any>) {
        console.log("$request_id handled")
    }

    override fun newState(state: State) {
        val state = state as AppState
        console.log(state)
        this.setState(state.loginForm)
    }

}

fun RBuilder.loginForm() = child(LoginForm::class) {}