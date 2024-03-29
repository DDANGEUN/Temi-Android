package com.robotemi.sdk.listeners

import com.robotemi.sdk.calls.CallState

abstract class OnTelepresenceStatusChangedListener(var sessionId: String) {

    /**
     * Called when Telepresence status was changed.
     *
     */
    abstract fun onTelepresenceStatusChanged(callState: CallState)
}
