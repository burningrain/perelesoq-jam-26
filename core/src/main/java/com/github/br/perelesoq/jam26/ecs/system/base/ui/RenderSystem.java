package com.github.br.perelesoq.jam26.ecs.system.base.ui;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.ChangeRenderLayerComponent;
import com.github.br.perelesoq.jam26.ecs.component.ui.render.RenderComponent;
import com.github.br.perelesoq.jam26.ecs.component.TransformComponent;
import com.github.br.perelesoq.jam26.ecs.component.singleton.ViewPortSingletonComponent;
import com.github.br.perelesoq.jam26.render.ActorFactory;
import com.github.br.perelesoq.jam26.render.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.perelesoq.jam26.render.TiledUiConstants;
import com.github.br.perelesoq.jam26.render.ui.ObjectLayerObjectRenderInterceptor;

public class RenderSystem extends BaseSystem {

    private final CustomOrthogonalTiledMapRenderer renderer;

    // Подписка на сущности с RenderComponent
    private EntitySubscription renderSubscription;

    private EntitySubscription layerChangeSubscription; // подписка на событие изменения слоев

    // Маппер для быстрого доступа к компонентам
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<RenderComponent> renderMapper;
    protected ComponentMapper<ChangeRenderLayerComponent> changeRenderLayerMapper;

    private final ObjectLayerObjectRenderInterceptorImpl postRenderSupplier = new ObjectLayerObjectRenderInterceptorImpl();

    private class ObjectLayerObjectRenderInterceptorImpl implements ObjectLayerObjectRenderInterceptor {

        private final ObjectMap<String, IntBag> layerEntitiesMap = new ObjectMap<>();

        @Override
        public boolean isIgnoreLayer(String name) {
            return TiledUiConstants.Layers.GAME_OBJECTS_LAYER.equals(name) ||
                TiledUiConstants.Layers.TRIGGERS_LAYER.equals(name);
        }

        @Override
        public void draw(String layerName, Batch batch) {
            IntBag intBag = layerEntitiesMap.get(layerName);
            if (intBag == null) {
                return;
            }

            int size = intBag.size();
            for (int i = 0; i < size; i++) {
                int entityId = intBag.get(i);
                RenderComponent renderComponent = renderMapper.get(entityId);
                TransformComponent transformComponent = transformMapper.get(entityId);

                TextureRegion frame = renderComponent.textureRegion;
                boolean isFlipX = transformComponent.flipX;
                boolean isFlipY = transformComponent.flipY;

                float ox = Float.isNaN(transformComponent.originX) ? frame.getRegionWidth() / 2f : transformComponent.originX;
                float oy = Float.isNaN(transformComponent.originY) ? frame.getRegionHeight() / 2f : transformComponent.originY;

                // Вызываем метод батча, передавая туда параметры региона
                batch.draw(
                    frame.getTexture(),                      // Наша общая текстура-атлас
                    transformComponent.x,                    // Позиция X на экране
                    transformComponent.y,                    // Позиция Y на экране
                    ox,
                    oy,
                    frame.getRegionWidth(),                  // Ширина на экране (например, 32f)
                    frame.getRegionHeight(),                 // Высота на экране (например, 32f)
                    transformComponent.scaleX,
                    transformComponent.scaleY,
                    transformComponent.rotation,
                    frame.getRegionX(),                      // srcX: пиксельный X левого верхнего угла кадра в атласе
                    frame.getRegionY(),                      // srcY: пиксельный Y левого верхнего угла кадра в атласе
                    frame.getRegionWidth(),                  // srcWidth: пиксельная ширина кадра в атласе
                    frame.getRegionHeight(),                 // srcHeight: пиксельная высота кадра в атласе
                    isFlipX,                                 // Тот самый флаг разворота влево/вправо!
                    isFlipY                                  // Флаг разворота вверх/вниз
                );
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

    public CustomOrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    @Override
    protected void initialize() {
        // Инициализируем подписку на сущности, у которых есть RenderComponent
        renderSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(RenderComponent.class));

        // Отлавливаем сущности, у которых есть компонент-команда изменения слоя
        layerChangeSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(ChangeRenderLayerComponent.class));
    }

    @Override
    protected void processSystem() {
        handleChangeLayers();
        handleRendering();
    }

    private void handleChangeLayers() {
        IntBag actives = layerChangeSubscription.getEntities();
        int[] ids = actives.getData();

        for (int i = 0, s = actives.size(); s > i; i++) {
            int entityId = ids[i];
            ChangeRenderLayerComponent changeRenderLayerComponent = changeRenderLayerMapper.get(entityId);
            if (!changeRenderLayerComponent.isDirty) {
                continue;
            }

            String layerName = changeRenderLayerComponent.layerName;
            MapLayer layer = renderer.getLayer(layerName);
            if (layer == null) {
                throw new GdxRuntimeException("layer [" + layerName + "] is not found");
            }

            layer.setVisible(changeRenderLayerComponent.isVisible);
            layer.setOpacity(changeRenderLayerComponent.opacity);
            if (layer instanceof com.badlogic.gdx.maps.MapGroupLayer) {
                renderer.updateOffsetsForGroupLayer(
                    layerName,
                    changeRenderLayerComponent.offsetX,
                    changeRenderLayerComponent.offsetY
                );
            } else {
                layer.setOffsetX(changeRenderLayerComponent.offsetX);
                layer.setOffsetY(changeRenderLayerComponent.offsetY);
            }

            layer.setTintColor(changeRenderLayerComponent.tintColor);
            layer.setParallaxX(changeRenderLayerComponent.parallaxFactor.x);
            layer.setParallaxY(changeRenderLayerComponent.parallaxFactor.y);

            changeRenderLayerComponent.isDirty = false;
        }
    }

    private void handleRendering() {
        IntBag actives = renderSubscription.getEntities();
        int[] ids = actives.getData();

        for (int i = 0, s = actives.size(); s > i; i++) {
            int entityId = ids[i];
            RenderComponent renderComponent = renderMapper.get(entityId);
            if (renderComponent != null) {
                postRenderSupplier.addEntityToLayer(renderComponent.layer, entityId);
            }
        }

        ViewPortSingletonComponent viewPortComponent = ViewPortSingletonComponent.INSTANCE;
        renderer.setView(viewPortComponent.camera);

        ScreenUtils.clear(Color.BLACK);
        renderer.render();

        postRenderSupplier.clearLayersEntities();
    }

}
