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


class App : RComponent<RProps, AppState>(), StoreSubscriber {

    /**
     * Function runs after Application component appears on the screen.
     * Here, we start Message center
     */
    override fun componentDidMount() {
        MessageCenter.setup("192.168.0.184",8080,"websocket")
        MessageCenter.run()
        appStore.subscribe(this)
    }

    /**
     * Function runs when need to generate HTML of root component
     */
    override fun RBuilder.render() {
        if (MessageCenter.user_id.count()==0 || MessageCenter.session_id.count()==0) {
            loginForm()
        } else {
            +"Admin panel"
        }
    }

    /**
     * Function runs every time when component receives application state update notification
     * from Root reducer. Here component can redraw itself
     *
     * @param state: New state
     */
    override fun newState(state: State) {
        val state = state as AppState
        this.setState(state)
    }
}

fun RBuilder.app() = child(App::class) {}
