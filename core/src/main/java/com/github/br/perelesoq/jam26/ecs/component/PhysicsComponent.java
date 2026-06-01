package com.github.br.perelesoq.jam26.ecs.component;

import com.artemis.Component;
import com.dongbat.jbump.Item;

public class PhysicsComponent extends Component {

    // Хранит ссылку на объект внутри jbump-мира
    public Item<Integer> item;
    public float width;
    public float height;
    public boolean useGravity = false;

}
