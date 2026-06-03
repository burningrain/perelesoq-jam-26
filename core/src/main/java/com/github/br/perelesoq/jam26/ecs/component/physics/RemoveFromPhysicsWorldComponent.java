package com.github.br.perelesoq.jam26.ecs.component.physics;

import com.artemis.PooledComponent;
import com.dongbat.jbump.Item;

public class RemoveFromPhysicsWorldComponent extends PooledComponent {

    public Item<Integer> itemToRemove;

    @Override
    protected void reset() {
        itemToRemove = null;
    }

}
