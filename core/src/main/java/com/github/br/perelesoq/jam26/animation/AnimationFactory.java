package com.github.br.perelesoq.jam26.animation;

import com.github.ashvard.gdx.simple.animation.component.AnimatorDynamicPart;
import com.github.ashvard.gdx.simple.animation.component.SimpleAnimationComponent;
import com.github.ashvard.gdx.simple.animation.fsm.FsmContext;
import com.github.br.perelesoq.jam26.Resources;

public class AnimationFactory {

    public static SimpleAnimationComponent createHero() {
        FsmContext fsmContext = new FsmContext();
        resetHeroAnimationContext(fsmContext);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_IDLE, true);

        AnimatorDynamicPart animatorDynamicPart = new AnimatorDynamicPart(/*animatorIdle*/);
        return new SimpleAnimationComponent(Resources.Animations.HERO, fsmContext, animatorDynamicPart);
    }

    public static void resetHeroAnimationContext(FsmContext fsmContext) {
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_WALKING, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_PRE_JUMP, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_ATTACK_UP, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_DEATH, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_IDLE, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_PRE_JUMP, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_TRAINING_LEGS, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_JUMP, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WALKING, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_ATTACK_DOWN, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_WEAPON_IDLE, false);
        fsmContext.insert(HeroAnimationType.TransitionPredicate.IS_JUMP, false);
    }

    public static SimpleAnimationComponent createDoor() {
        FsmContext fsmContext = new FsmContext();
        resetDoorAnimationContext(fsmContext);

        AnimatorDynamicPart animatorDynamicPart = new AnimatorDynamicPart(/*animatorIdle*/);
        return new SimpleAnimationComponent(Resources.Animations.DOOR, fsmContext, animatorDynamicPart);
    }

    private static void resetDoorAnimationContext(FsmContext fsmContext) {
        fsmContext.insert(DoorAnimationType.TransitionPredicate.IS_CLOSING, false);
        fsmContext.insert(DoorAnimationType.TransitionPredicate.IS_OPENING, false);
    }

}
