package com.github.br.perelesoq.jam26.ecs.component.trigger;

import com.artemis.PooledComponent;

// Флаг вешается на триггер ТОЛЬКО в том кадре, когда в него наступили/в нем стоят
public class OverlappedThisFrameComponent extends PooledComponent {
    @Override
    protected void reset() {

    }
}
