package components.app

import components.app.login.loginForm
import core.MessageCenter
import core.MessageCenterResponseListener
import lib.State
import lib.StoreSubscriber
import react.*
import react.dom.*
import store.AppState
import store.LoginFormState
import store.appStore


class App : RComponent<RProps, AppState>(), MessageCenterResponseListener,StoreSubscriber {

    override fun componentDidMount() {
        appStore.subscribe(this)
    }

    override fun RBuilder.render() {
        if (MessageCenter.user_id.count()==0 || MessageCenter.session_id.count()==0) {
            loginForm()
        } else {
            +"Admin panel"
        }
    }

    override fun handleWebSocketResponse(request_id:String, response: HashMap<String, Any>) {
        console.log("$request_id handled")
    }

    override fun newState(state: State) {
        val state = state as AppState
        this.setState(state)
    }
}

fun RBuilder.app() = child(App::class) {}
