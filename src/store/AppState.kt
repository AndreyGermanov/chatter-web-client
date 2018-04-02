package store

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
    // holds state of Login form
    var loginForm = LoginFormState()
}