package components.app

import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Attr
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.dom.*
import store.*
import kotlin.browser.document


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
                            a(href="#/rooms") {+"Rooms"}
                        }
                        li(classes=(if (props.currentScreen == AppScreen.USERS_LIST) "active" else "")) {
                            a(href="#/users") {+"Users"}
                        }
                        li(classes=(if (props.currentScreen == AppScreen.SESSIONS_LIST) "active" else "")) {
                            a(href="#/sessions") {+"Sessions"}
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
                                        appStore.dispatch(NavbarState.changeUserMenuDropdownClass(
                                                if (props.userMenuDropdownClass == "dropdown")
                                                    "dropdown open"
                                                else
                                                    "dropdown")
                                        )
                                    }

                                }
                                +props.username
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
}

fun RBuilder.navbar(currentScreen:AppScreen,username:String,dropdownClass:String) = child(NavBar::class) {
    attrs.currentScreen = currentScreen
    attrs.username = username
    attrs.userMenuDropdownClass = dropdownClass
}