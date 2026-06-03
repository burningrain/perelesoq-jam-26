package com.github.br.perelesoq.jam26.ecs.system.base.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import de.golfgl.gdx.controllers.mapping.ConfiguredInput;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;

public class MyControllerMapping extends ControllerMappings {

    public static final int BUTTON_A = 0;
    public static final int BUTTON_B = 1;
    public static final int BUTTON_X = 2;
    public static final int BUTTON_Y = 3;
    public static final int BUTTON_START = 4;
    public static final int BUTTON_BACK = 5;
    public static final int AXIS_LEFT_Y = 6;
    public static final int AXIS_LEFT_X = 7;

    // Новые константы для крестовины (D-Pad)
    public static final int DPAD_UP = 8;
    public static final int DPAD_DOWN = 9;
    public static final int DPAD_LEFT = 10;
    public static final int DPAD_RIGHT = 11;

    public MyControllerMapping() {
        super();

        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_A));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_B));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_X));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_Y));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_START));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_BACK));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_LEFT_Y));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_LEFT_X));

        // Регистрируем направления крестовины как кнопки (цифровой ввод)
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, DPAD_UP));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, DPAD_DOWN));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, DPAD_LEFT));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, DPAD_RIGHT));

        commitConfig();
    }

    @Override
    public boolean getDefaultMapping(MappedInputs defaultMapping, Controller controller) {
        ControllerMapping gdxMapping = controller.getMapping();

        defaultMapping.putMapping(new MappedInput(AXIS_LEFT_Y, new ControllerAxis(gdxMapping.axisLeftY)));
        defaultMapping.putMapping(new MappedInput(AXIS_LEFT_X, new ControllerAxis(gdxMapping.axisLeftX)));

        defaultMapping.putMapping(new MappedInput(BUTTON_A, new ControllerButton(gdxMapping.buttonA)));
        defaultMapping.putMapping(new MappedInput(BUTTON_B, new ControllerButton(gdxMapping.buttonB)));
        defaultMapping.putMapping(new MappedInput(BUTTON_X, new ControllerButton(gdxMapping.buttonX)));
        defaultMapping.putMapping(new MappedInput(BUTTON_Y, new ControllerButton(gdxMapping.buttonY)));

        defaultMapping.putMapping(new MappedInput(BUTTON_START, new ControllerButton(gdxMapping.buttonStart)));
        defaultMapping.putMapping(new MappedInput(BUTTON_BACK, new ControllerButton(gdxMapping.buttonBack)));

        // Связываем наши внутренние константы с системными ID крестовины геймпада Xbox
        defaultMapping.putMapping(new MappedInput(DPAD_UP, new ControllerButton(gdxMapping.buttonDpadUp)));
        defaultMapping.putMapping(new MappedInput(DPAD_DOWN, new ControllerButton(gdxMapping.buttonDpadDown)));
        defaultMapping.putMapping(new MappedInput(DPAD_LEFT, new ControllerButton(gdxMapping.buttonDpadLeft)));
        defaultMapping.putMapping(new MappedInput(DPAD_RIGHT, new ControllerButton(gdxMapping.buttonDpadRight)));

        return true;
    }
}
