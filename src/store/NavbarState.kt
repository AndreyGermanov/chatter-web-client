package store

import lib.Action
import lib.State

/**
 * Class which holds Navigation bar state for Redux
 */
class NavbarState: State {
    // Holds HTML class for right dropdown menu of NavBar
    var userMenuDropdownClass:String = "dropdown"
    // Holds current screen on which user stands
    var currentScreen: AppScreen? = null
    // Holds name of logged user
    var  username:String = ""
    /**
     * Function returns string representation of Application state
     */
    override fun toString(): String {
        return "Current screen: $currentScreen,userMenuDropDownClass:$userMenuDropdownClass"
    }

    /**
     * Action used to change current screen of application
     */
    data class changeUserMenuDropdownClass(val dropdownClass:String): NavbarAction
}

/**
 * Abstract interface for all actions of this state
 */
interface NavbarAction: Action {}

