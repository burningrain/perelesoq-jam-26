package com.github.br.perelesoq.jam26.ecs.component.singleton;

public class SirenSingletonComponent {

    public static final SirenSingletonComponent INSTANCE = new SirenSingletonComponent();

    public boolean isActive;

}
