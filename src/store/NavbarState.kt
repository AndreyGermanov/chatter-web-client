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
    // Errors
    var errors = HashMap<String,SmartEnum>()
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
    /**
     * Action used to change errors
     */
    data class changeErrorsAction(val errors:HashMap<String,SmartEnum>): NavbarAction
}

/**
 * Abstract interface for all actions of this state
 */
interface NavbarAction: Action {}

