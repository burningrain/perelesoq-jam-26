package com.github.br.perelesoq.jam26.ecs.component.singleton;

public class Controller1SingletonComponent {

    public static final Controller1SingletonComponent INSTANCE = new Controller1SingletonComponent();

    public boolean isActivated = false;

}
