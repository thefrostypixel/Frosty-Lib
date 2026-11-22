package frostyLib.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.List;

public class Keybind {
    public static InputConstants.Key key(InputWithModifiers event) {
        if (event instanceof KeyEvent keyEvent) {
            return InputConstants.getKey(keyEvent);
        } else if (event instanceof MouseButtonEvent mouseEvent) {
            return InputConstants.Type.MOUSE.getOrCreate(mouseEvent.button());
        } else if (event instanceof MouseButtonInfo buttonInfo) {
            return InputConstants.Type.MOUSE.getOrCreate(buttonInfo.button());
        }
        return InputConstants.UNKNOWN;
    }
    public static InputConstants.Key key(KeyMapping mapping) {
        return InputConstants.getKey(mapping.saveString());
    }

    public static boolean matches(InputConstants.Key key, InputConstants.Key keybind) {
        return key != null && keybind != null && !keybind.equals(InputConstants.UNKNOWN) && key.equals(keybind);
    }
    public static boolean matches(InputWithModifiers event, InputConstants.Key keybind) {
        return matches(key(event), keybind);
    }

    public static boolean matches(InputConstants.Key key, KeyMapping mapping) {
        return matches(key, key(mapping));
    }
    public static boolean matches(InputWithModifiers event, KeyMapping mapping) {
        return matches(event, key(mapping));
    }

    public static boolean matches(InputConstants.Key key, InputConstants.Key... keybinds) {
        if (keybinds != null) {
            for (InputConstants.Key keybind : keybinds) {
                if (matches(key, keybind)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean matches(InputWithModifiers event, InputConstants.Key... keybinds) {
        return matches(key(event), keybinds);
    }

    public static boolean matches(InputConstants.Key key, KeyMapping... mappings) {
        if (mappings != null) {
            for (KeyMapping mapping : mappings) {
                if (matches(key, mapping)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean matches(InputWithModifiers event, KeyMapping... mappings) {
        if (mappings != null) {
            for (KeyMapping mapping : mappings) {
                if (matches(event, mapping)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matches(InputConstants.Key key, List<InputConstants.Key> keybinds) {
        if (keybinds != null) {
            for (InputConstants.Key keybind : keybinds) {
                if (matches(key, keybind)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean matches(InputWithModifiers event, List<InputConstants.Key> keybinds) {
        return matches(key(event), keybinds);
    }

    public static int index(InputConstants.Key key, InputConstants.Key... keybinds) {
        if (keybinds != null) {
            for (int i = 0; i < keybinds.length; i++) {
                if (matches(key, keybinds[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static int index(InputWithModifiers event, InputConstants.Key... keybinds) {
        return index(key(event), keybinds);
    }

    public static int index(InputConstants.Key key, KeyMapping... mappings) {
        if (mappings != null) {
            for (int i = 0; i < mappings.length; i++) {
                if (matches(key, mappings[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static int index(InputWithModifiers event, KeyMapping... mappings) {
        if (mappings != null) {
            for (int i = 0; i < mappings.length; i++) {
                if (matches(event, mappings[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int index(InputConstants.Key key, List<InputConstants.Key> keybinds) {
        if (keybinds != null) {
            for (int i = 0; i < keybinds.size(); i++) {
                if (matches(key, keybinds.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static int index(InputWithModifiers event, List<InputConstants.Key> keybinds) {
        return index(key(event), keybinds);
    }
}
