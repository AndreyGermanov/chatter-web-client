package store

import lib.Action
import lib.State

/**
 * Base interface, which all enum classes should implement
 */
interface SmartEnum {
    /**
     * Function which returns text message for specified enum member
     */
    fun getMessage():String
}

/**
 * Class which holds whole application state.
 * Contain substates for a;; parts of application
 */
class AppState: State {
    // holds current screen on which user stands
    var currentScreen: AppScreen? = null
    // holds state of Login form
    var loginForm = LoginFormState()
    // holds state of Navigation bar
    var navbar = NavbarState()
    // holds state of User
    var user = UserState()

    /**
     * Function returns string representation of Application state
     */
    override fun toString(): String {
        return "Current screen: $currentScreen,Login form: $loginForm,NavBar: ${navbar}."
    }

    /**
     * Action used to change current screen of application
     */
    data class changeCurrentScreenAction(val currentScreen:AppScreen): AppAction

    /**
     * Action which hides all dropdowns
     */
     class hideDropdowns() {
        fun exec() {
            appStore.dispatch(NavbarState.changeUserMenuDropdownClass("dropdown"))
        }
    }
}

/**
 * Abstract interface for all actions of AppState state
 */
interface AppAction: Action {}


enum class AppScreen(val value:String): SmartEnum {
    LOGIN_FORM("LOGIN_FORM"),
    USERS_LIST("USERS_LIST"),
    ROOMS_LIST("ROOMS_LIST"),
    SESSIONS_LIST("SESSIONS_LIST"),
    MESSAGES_LIST("MESSAGES_LIST");
    override fun getMessage():String {
        var result = ""
        when(this) {
            LOGIN_FORM -> result = "Login"
            USERS_LIST -> result = "Users"
            ROOMS_LIST -> result = "Rooms"
            SESSIONS_LIST -> result = "Sessions"
            MESSAGES_LIST -> result = "Messages"
        }
        return result
    }

}