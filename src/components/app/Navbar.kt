package components.app

import react.RBuilder
import react.RComponent
import react.dom.*
import store.*


/**
 * Component used to draw top navigation bar
 */
class NavBar : RComponent<AppState, AppState>() {

    /**
     * Function used to draw HTML of component on the screen
     */
    override fun RBuilder.render() {
        div(classes="col-sm-12") {
            table {
                tr {
                    td {
                        a("#/rooms") {
                            if (props.currentScreen == AppScreen.ROOMS_LIST) {
                                console.log("HERE")
                                attrs["style"] = js("{fontWeight:'bold'}")
                            }
                            +"Rooms"
                        }
                    }
                    td {
                        a("#/users") {
                            if (props.currentScreen == AppScreen.USERS_LIST) {
                                console.log("HERE 2")
                                attrs["style"] = js("{fontWeight:'bold'}")
                            }
                            +"Users"
                        }
                    }
                    td {
                        a("#/sessions") {
                            if (props.currentScreen == AppScreen.SESSIONS_LIST) {
                                console.log("HERE3 ")
                                attrs["style"] = js("{fontWeight:'bold'}")
                            }
                            +"Sessions"
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.navbar(currentScreen:AppScreen) = child(NavBar::class) {
    attrs.currentScreen = currentScreen
}