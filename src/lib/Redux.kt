package lib

import react.RState
import kotlin.reflect.KFunction2

/**
 * Simple implementation of Redux state management pattern
 *
 * @property state State : Initial state for store
 * @property reducer Function : Function, which applies action to state
 */
class Store(state:State, reducer: KFunction2<@ParameterName(name = "state") State, @ParameterName(name = "action") Action, State>) {

    // Current application state
    var state:State

    // Reducer which will apply actions to state
    var reducer:KFunction2<@ParameterName(name = "state") State, @ParameterName(name = "action") Action, State>

    // List of subscriber objects, which will be notified after any state change
    var subscribers = ArrayList<StoreSubscriber>()

    /**
     * Class constructor, sets initial application state and reducer
     */
    init {
        this.state = state
        this.reducer = reducer
    }

    /** Method applies provided [action] to state, using reducer function
     *  and notifies all subscribers about this, providing them updated state
     *
     *  @param action Action, which need to apply to state
     */
    fun dispatch(action:Action) {

        this.state = this.reducer.invoke(this.state,action)
        for (subscriber in this.subscribers) {
            subscriber.newState(this.state)
        }
    }

    /**
     * Method adds subscriber object to subscribers list
     *
     * @param subscriber : Subcriber object
     */
    fun subscribe(subscriber:StoreSubscriber) {
        if (!this.subscribers.contains(subscriber)) {
            this.subscribers.add(subscriber)
        }
    }

    /**
     * Removes object with provided index from subscribers list
     *
     * @param subscriber : Object to remove
     */
    fun unsubscribe(subscriber: StoreSubscriber) {
        if (this.subscribers.contains(subscriber)) {
            this.subscribers.remove(subscriber)
        }
    }
}

// Abstract interface, which should implement application state object. Can be struct, class or anything else
open interface State: RState {}

// Abstract interface, which should implement action. Can be struct, class or anything else
interface Action

// Interface, which object should implement to be able to subscribe to application state changes and receive
// notifications
interface StoreSubscriber {
    /** Function called on subscribed every time when Application state changed
     *
     * @param state State : new updated state
     */
    fun newState(state:State)
}
