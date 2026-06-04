package com.github.br.perelesoq.jam26.animation;

public interface BossAnimationType {

    interface State {

        String IDLE = "idle";
        String PREPARED = "prepare";
        String ATTACK = "attack";
        String DEAD = "dead";

    }

    interface TransitionPredicate {

        String TO_ATTACK = "to_attack";
        String TO_IDLE = "to_idle";
        String TO_DEAD = "to_dead";
        String TO_PREPARE = "to_prepare";

    }

}
