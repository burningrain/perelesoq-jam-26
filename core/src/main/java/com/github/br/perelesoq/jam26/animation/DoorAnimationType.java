package com.github.br.perelesoq.jam26.animation;

public interface DoorAnimationType {

    interface State {
        String CLOSED = "closed";
        String OPENED = "opened";
        String IS_OPENING = "is_opening";
        String IS_CLOSING = "is_closing";
    }

    interface TransitionPredicate {
        String IS_CLOSING = "isClosing";
        String IS_OPENING = "isOpening";
    }

}
