package components.app

import components.app.login.loginForm
import core.MessageCenter
import lib.State
import lib.StoreSubscriber
import react.*
import store.AppScreen
import store.AppState
import store.LoginFormState
import store.appStore


class App : RComponent<RProps, AppState>(), StoreSubscriber {

    /**
     * Function runs right after component appeared on the screen. Used to init
     * anything in application, which should be done after it
     */
    override fun componentDidMount() {
        MessageCenter.setup("192.168.0.214",8080,"websocket")
        MessageCenter.run()
        appStore.subscribe(this)
        val state = appStore.state as AppState
        this.setState(state)
        if (!MessageCenter.user_id.isEmpty() && !MessageCenter.session_id.isEmpty()) {
            LoginFormState.doLogin().exec(MessageCenter.user_id,MessageCenter.session_id)
        } else {
            appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
        }
    }

    /**
     * Function runs when need to generate HTML of root component
     */
    override fun RBuilder.render() {
        if (state.loginForm==null) {
            return
        }
        if (state.currentScreen == null) {
            return
        }
        if (!state.user.isLogin || MessageCenter.user_id.isEmpty()) {
            loginForm(
                    login = state.loginForm.login,
                    password = state.loginForm.password,
                    errors = state.loginForm.errors
            )
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
