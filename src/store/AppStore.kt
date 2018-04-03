package store

import lib.Store
import reducers.RootReducer

var appStore = Store(AppState(), ::RootReducer)