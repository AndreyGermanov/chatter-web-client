package components.app

import components.app.login.loginForm
import components.app.users.userDetail
import components.app.users.usersList
import core.MessageCenter
import kotlinx.html.js.onClickFunction
import lib.State
import lib.StoreSubscriber
import react.*
import react.dom.div
import react.dom.span
import react.router.dom.*
import store.*

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
        if (state.currentScreen != AppScreen.LOGIN_FORM) {
            navbar(state.currentScreen!!,state.user.login,state.navbar.userMenuDropdownClass,
                    state.loginForm.errors as HashMap<String,SmartEnum>)
        }
        div(classes="row") {
            attrs {
                onClickFunction = {
                    AppState.hideDropdowns().exec()
                }
            }
            div(classes="col-md-12") {
                hashRouter {
                    switch {
                        if ((!state.user.isLogin || MessageCenter.user_id.isEmpty()) && state.currentScreen != AppScreen.LOGIN_FORM) {
                            redirect("", "/login")
                            appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                        }
                        if (state.user.isLogin && state.currentScreen == AppScreen.LOGIN_FORM) {
                            redirect("", "/users")
                            appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.USERS_LIST))
                        }
                        route("/login", strict = true) {
                            if (state.currentScreen != AppScreen.LOGIN_FORM) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.LOGIN_FORM))
                            }
                            loginForm(
                                    login = state.loginForm.login,
                                    password = state.loginForm.password,
                                    errors = state.loginForm.errors
                            )
                        }
                        route("/users",strict= true) {
                            if (state.currentScreen != AppScreen.USERS_LIST) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.USERS_LIST))
                            }
                            usersList(state.usersList)
                        }
                        route("/sessions",strict= true) {
                            if (state.currentScreen != AppScreen.SESSIONS_LIST) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.SESSIONS_LIST))
                            }
                            span {}
                        }
                        route("/rooms",strict= true) {
                            if (state.currentScreen != AppScreen.ROOMS_LIST) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.ROOMS_LIST))
                            }
                            span {}
                        }
                        route<idProps>( "/user/:id") { props ->
                            if (state.currentScreen != AppScreen.USER_DETAIL) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.USER_DETAIL))
                            }
                            if (props.match.params.id!=null && props.match.params.id != state.userDetail.user_id) {
                                appStore.dispatch(UserDetailState.Change_user_id_action(props.match.params.id.toString()))
                            } else if (props.match.params.id == null) {
                                appStore.dispatch(UserDetailState.Change_user_id_action(""))
                            }
                            userDetail(state.userDetail)
                        }
                        route("/user") {
                            if (state.currentScreen != AppScreen.USER_DETAIL) {
                                appStore.dispatch(AppState.changeCurrentScreenAction(AppScreen.USER_DETAIL))
                            }
                            if (state.userDetail.user_id != null && !state.userDetail.user_id!!.isEmpty()) {
                                appStore.dispatch(UserDetailState.Change_user_id_action(""))
                            }
                            userDetail(state.userDetail)

                        }
                        route("/",strict=true) {
                            redirect("", "/users")
                        }
                    }
                }
            }
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
