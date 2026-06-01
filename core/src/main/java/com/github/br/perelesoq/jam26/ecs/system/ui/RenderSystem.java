package com.github.br.perelesoq.jam26.ecs.system.ui;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.perelesoq.jam26.ecs.component.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;
import com.github.br.perelesoq.jam26.render.ActorFactory;
import com.github.br.perelesoq.jam26.render.CustomOrthogonalTiledMapRenderer;
import com.github.br.perelesoq.jam26.render.ui.ObjectLayerPostRenderSupplier;

public class RenderSystem extends BaseSystem {

    private final CustomOrthogonalTiledMapRenderer renderer;

    // Подписка на сущности с RenderComponent
    private EntitySubscription renderSubscription;

    // Маппер для быстрого доступа к компонентам
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<RenderComponent> renderMapper;

    private final ObjectLayerPostRenderSupplierImpl postRenderSupplier = new ObjectLayerPostRenderSupplierImpl();

    private class ObjectLayerPostRenderSupplierImpl implements ObjectLayerPostRenderSupplier {

        private final ObjectMap<String, IntBag> layerEntitiesMap = new ObjectMap<>();

        @Override
        public void draw(String layerName, Batch batch) {
            IntBag intBag = layerEntitiesMap.get(layerName);
            if (intBag == null) {
                return;
            }

            int size = intBag.size();
            for (int i = 0; i < size; i++) {
                RenderComponent renderComponent = renderMapper.get(i);
                TransformComponent transformComponent = transformMapper.get(i);
                batch.draw(renderComponent.textureRegion, transformComponent.x, transformComponent.y);
            }
        }

        public void addEntityToLayer(String layer, int entityId) {
            IntBag intBag = layerEntitiesMap.get(layer);
            if (intBag == null) {
                intBag = new IntBag();
                layerEntitiesMap.put(layer, intBag);
            }

            intBag.add(entityId);
        }

        public void clearLayersEntities() {
            for (ObjectMap.Entry<String, IntBag> bagEntry : layerEntitiesMap) {
                bagEntry.value.clear();
            }
        }

    }

    public RenderSystem(ActorFactory actorFactory, Viewport viewport, TiledMap map, float unitScale) {
        this.renderer = new CustomOrthogonalTiledMapRenderer(actorFactory, viewport, map, unitScale, postRenderSupplier);
    }

    @Override
    protected void initialize() {
        // Инициализируем подписку на сущности, у которых есть RenderComponent
        renderSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(RenderComponent.class));
    }

    @Override
    protected void processSystem() {
        IntBag actives = renderSubscription.getEntities();
        int[] ids = actives.getData();

        for (int i = 0, s = actives.size(); s > i; i++) {
            int entityId = ids[i];
            RenderComponent renderComponent = renderMapper.get(entityId);
            postRenderSupplier.addEntityToLayer(renderComponent.layer, entityId);
        }

        ViewPortSingletonComponent viewPortComponent = ViewPortSingletonComponent.INSTANCE;
        renderer.setView(viewPortComponent.camera);

        ScreenUtils.clear(Color.BLACK);
        renderer.render();

        postRenderSupplier.clearLayersEntities();
    }

}
