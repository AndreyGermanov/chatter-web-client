package components.app

import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.dom.*
import store.*
import utils.LogLevel
import utils.Logger
import kotlin.browser.window

/**
 * Component used to draw top navigation bar
 */
class NavBar : RComponent<NavbarState, NavbarState>() {

    /**
     * Function used to draw HTML of component on the screen
     */
    override fun RBuilder.render() {
        div(classes="navbar navbar-default") {
            attrs {
                role = "navigation"
            }
            div(classes="container-fluid") {
                div(classes="navbar-header") {
                    button(classes = "navbar-toggle collapsed") {
                        attrs["data-toggle"] = "collapse"
                        attrs["data-target"] = "bs-navbar-collapse-1"
                        attrs["aria-expanded"] = "false"
                        span(classes = "sr-only") {
                            +"Toggle navigation"
                        }
                        span(classes = "icon-bar"){}
                        span(classes = "icon-bar"){}
                        span(classes = "icon-bar"){}
                    }
                    a(href = "#", classes = "navbar-brand") {
                        +"CHATTER ADMIN"
                    }
                }
                div(classes="collapse navbar-collapse") {
                    attrs["id"] = "bs-navbar-collapse-1"
                    ul(classes="nav navbar-nav") {
                        li(classes=(if (props.currentScreen == AppScreen.ROOMS_LIST) "active" else "")) {
                            a(href="#/rooms") {
                                span(classes="glyphicon glyphicon-map-marker") {
                                    + " "
                                }
                                +" Rooms"
                            }
                        }
                        li(classes=(if (props.currentScreen == AppScreen.USERS_LIST) "active" else "")) {
                            a(href="#/users") {
                                span(classes="fas fa-users") {
                                    + " "
                                }
                                +" Users"
                            }
                        }
                        li(classes=(if (props.currentScreen == AppScreen.SESSIONS_LIST) "active" else "")) {
                            a(href="#/sessions") {
                                span(classes="fas fa-plug") {
                                    + " "
                                }
                                +" Sessions"
                            }
                        }
                    }
                    ul(classes="nav navbar-nav navbar-right") {
                        li(classes=props.userMenuDropdownClass) {
                            attrs["id"] = "rightDropdown"
                            a(classes="dropdown-toggle") {
                                attrs["role"] = "button"
                                attrs["aria-haspopup"] = "true"
                                attrs["aria-expanded"] = "false"
                                attrs {
                                    onClickFunction  = {
                                        it.stopPropagation()
                                        appStore.dispatch(NavbarState.changeUserMenuDropdownClass(
                                                if (props.userMenuDropdownClass == "dropdown")
                                                    "dropdown open"
                                                else
                                                    "dropdown")
                                        )
                                    }

                                }
                                span(classes="fas fa-user") {
                                    + " "
                                }
                                +" ${props.username}"
                                span(classes="caret") {}
                            }
                            ul(classes="dropdown-menu") {
                                li {
                                    a("#") {
                                        +"Profile"
                                    }
                                }
                                li(classes="divider") {
                                    attrs["role"] = "separator"
                                }
                                li {
                                    a("#") {
                                        attrs.onClickFunction = { logoutClick() }
                                        +"Logout"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**********************
     * Life cycle methods *
     *********************/

    /**
     * Function runs every time when component receives notification about changing application state from reducer
     * or properties from uplevel object. Here component can control what to do right after UI updated
     *
     * @param prevProps: Previous property values, to compare current properties with
     * @param prevState: Previous state, to compare with current state values
     */
    override fun componentDidUpdate(prevProps: NavbarState, prevState: NavbarState) {
        Logger.log(LogLevel.DEBUG_REDUX, "Applying new state to LoginForm. State: $props",
                "LoginForm", "newState")
        if (props.errors["general"] != null) {
            window.alert(props.errors["general"]!!.getMessage())
            val errors = props.errors
            errors.remove("general")
            appStore.dispatch(LoginFormState.changeErrorsField(errors as HashMap<String,LoginFormError>))
        }
    }

    /******************
     * Event handlers *
     *****************/

    /**
     * "Logout" menu item click handler
     */
    fun logoutClick() {
        LoginFormState.doLogout().exec {
            window.location.reload()
        }
    }
}

fun RBuilder.navbar(currentScreen:AppScreen,username:String,dropdownClass:String,errors:HashMap<String,SmartEnum>) = child(NavBar::class) {
    attrs.currentScreen = currentScreen
    attrs.username = username
    attrs.userMenuDropdownClass = dropdownClass
    attrs.errors = errors
}