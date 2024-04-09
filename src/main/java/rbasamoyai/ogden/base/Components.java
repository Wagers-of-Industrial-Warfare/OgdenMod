package rbasamoyai.ogden.base;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class Components {

    public static MutableComponent literal(String text) { return new TextComponent(text); }
    public static MutableComponent translatable(String key, Object... objs) { return new TranslatableComponent(key, objs); }

    private Components() {
    }

}
