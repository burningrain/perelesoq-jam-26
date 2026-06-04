package com.github.br.perelesoq.jam26.animation;

public interface HeroAnimationType {

    interface State {
        String PRE_JUMP = "pre_jump";
        String JUMP = "jump";
        String TRAINING_LEGS = "training_legs";
        String ATTACK_DOWN = "attack_down";
        String WEAPON_PRE_JUMP = "weapon_pre_jump";
        String WEAPON_WALK = "weapon_walk";
        String WEAPON_IDLE = "weapon_idle";
        String WEAPON_JUMP = "weapon_jump";
        String IDLE = "idle";
        String DEATH = "death";
        String ATTACK_UP = "attack_up";
        String WALK = "walk";
    }

    interface TransitionPredicate {

        String IS_WEAPON_WALKING = "is_weapon_walking";
        String IS_PRE_JUMP = "is_pre_jump";
        String IS_ATTACK_UP = "is_attack_up";
        String IS_DEATH = "is_death";
        String IS_IDLE = "is_idle";
        String IS_WEAPON_PRE_JUMP = "is_weapon_pre_jump";
        String IS_TRAINING_LEGS = "is_training_legs";
        String IS_WEAPON_JUMP = "is_weapon_jump";
        String IS_WALKING = "is_walking";
        String IS_ATTACK_DOWN = "is_attack_down";
        String IS_WEAPON_IDLE = "is_weapon_idle";
        String IS_JUMP = "is_jump";

    }

}
