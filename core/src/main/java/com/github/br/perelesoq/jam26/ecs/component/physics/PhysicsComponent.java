package com.github.br.perelesoq.jam26.ecs.component.physics;

import com.artemis.PooledComponent;
import com.dongbat.jbump.Item;

public class PhysicsComponent extends PooledComponent {

    // Хранит ссылку на объект внутри jbump-мира
    public Item<Integer> item;
    public boolean useGravity = false;
    public Hitbox hitbox;
    public boolean isTrigger;

    @Override
    protected void reset() {
        item = null;
        useGravity = false;
        hitbox = null;
        isTrigger = false;
    }

}
