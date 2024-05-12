package rbasamoyai.ogden.firearms.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;

import net.minecraft.Util;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class ScriptValue implements ScriptValueSupplier {

    @Nullable private final Boolean bool;
    @Nullable private final Number num;
    @Nullable private final String str;
    @Nullable private final ItemStack stack;
    @Nullable private final List<ScriptValue> list;
    @Nullable private final Map<String, ScriptValue> map;

    public static final ScriptValue VOID = new ScriptValue(null, null, null, null, null, null);
    public static final ScriptValue TRUE = new ScriptValue(true);
    public static final ScriptValue FALSE = new ScriptValue(false);
    public static final ScriptValue ZERO = new ScriptValue(0);
    public static final ScriptValue EMPTY_STRING = new ScriptValue("");
    public static final ScriptValue EMPTY_LIST = new ScriptValue(new ArrayList<>());
    public static final ScriptValue EMPTY_OBJECT = new ScriptValue(new HashMap<>());
    public static final ScriptValue EMPTY_ITEM_STACK = new ScriptValue(ItemStack.EMPTY);

    public static final ScriptValue[] INT_CACHE = Util.make(new ScriptValue[1152], c -> {
       for (int i = -128; i < 1024; ++i)
           c[i + 128] = new ScriptValue(i);
    });

    public static final ScriptValue[] BYTE_CACHE = Util.make(new ScriptValue[256], c -> {
       for (int b = -128; b < 128; ++b)
           c[b + 128] = new ScriptValue((byte) b);
    });

    private ScriptValue(@Nullable Boolean bool, @Nullable Number num, @Nullable String str, @Nullable ItemStack stack,
                        @Nullable List<ScriptValue> list, @Nullable Map<String, ScriptValue> map) {
        this.bool = bool;
        this.num = num;
        this.str = str;
        this.stack = stack;
        this.list = list;
        this.map = map;
    }

    private ScriptValue(boolean bool) { this(bool, null, null, null, null, null); }
    private ScriptValue(String str) { this(null, null, str, null, null, null); }
    private ScriptValue(ItemStack stack) { this(null, null, null, stack, null, null); }
    private ScriptValue(Number num) { this(null, num, null, null, null, null); }

    public ScriptValue(List<ScriptValue> list) { this(null, null, null, null, list, null); }
    public ScriptValue(Map<String, ScriptValue> map) { this(null, null, null, null, null, map); }

    public static ScriptValue bool(boolean value) { return value ? TRUE : FALSE; }
    public static ScriptValue string(String str) { return str.isEmpty() ? EMPTY_STRING : new ScriptValue(str); }
    public static ScriptValue itemStack(ItemStack stack) { return stack.isEmpty() ? EMPTY_ITEM_STACK : new ScriptValue(stack); }

    public static ScriptValue ofInt(int i) { return -128 <= i && i < 1024 ? INT_CACHE[i + 128] : new ScriptValue(i); }
    public static ScriptValue ofByte(byte b) { return BYTE_CACHE[b + 128]; }
    public static ScriptValue ofShort(short s) { return -128 <= s && s < 1024 ? INT_CACHE[s + 128] : new ScriptValue(s); }
    public static ScriptValue ofLong(long l) { return -128 <= l && l < 1024 ? INT_CACHE[(int) l + 128] : new ScriptValue(l); }

    public static ScriptValue ofFloat(float f) { return f == 0.0f ? ZERO : new ScriptValue(f); }
    public static ScriptValue ofDouble(double d) { return d == 0.0d ? ZERO : new ScriptValue(d); }

    @Nullable public Boolean bool() { return this.bool; }
    @Nullable public Number num() { return this.num; }
    @Nullable public String str() { return this.str; }
    @Nullable public ItemStack stack() { return this.stack; }
    @Nullable public List<ScriptValue> list() { return this.list; }
    @Nullable public Map<String, ScriptValue> map() { return this.map; }

    public boolean isVoid() {
        if (this == VOID)
            return true;
        return this.bool == null && this.num == null && this.str == null && this.stack == null && this.list == null && this.map == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ScriptValue other))
            return false;

        if (!Objects.equals(this.bool, other.bool))
            return false;
        if (!Objects.equals(this.num, other.num))
            return false;
        if (!Objects.equals(this.str, other.str))
            return false;

        if (this.stack != other.stack) {
            if (this.stack == null || other.stack == null)
                return false;
            if (!ItemStack.isSameItemSameTags(this.stack, other.stack))
                return false;
        }

        if (this.list != other.list) {
            if (this.list == null || other.list == null)
                return false;
            if (!this.list.equals(other.list))
                return false;
        }

        if (this.map == other.map)
            return true;
        if (this.map == null || other.map == null)
            return false;
        return this.map.equals(other.map);
    }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) { return this; }

    public Tag toNbt() {
        if (this.bool != null) {
            return ByteTag.valueOf(this.bool);
        } else if (this.num instanceof Integer i) {
            return IntTag.valueOf(i);
        } else if (this.num instanceof Byte b) {
            return ByteTag.valueOf(b);
        } else if (this.num instanceof Short s) {
            return ShortTag.valueOf(s);
        } else if (this.num instanceof Long l) {
            return LongTag.valueOf(l);
        } else if (this.num instanceof Float f) {
            return FloatTag.valueOf(f);
        } else if (this.num instanceof Double d) {
            return DoubleTag.valueOf(d);
        } else if (this.num != null) {
            return DoubleTag.valueOf(this.num.doubleValue());
        } else if (this.str != null) {
            return StringTag.valueOf(this.str);
        } else if (this.stack != null) {
            return this.stack.isEmpty() ? ItemStack.EMPTY.save(new CompoundTag()) : this.stack.save(new CompoundTag());
        } else if (this.list != null) {
            ListTag list = new ListTag();
            for (int i = 0; i < this.list.size(); ++i)
                list.add(i, this.list.get(i).toNbt());
            return list;
        } else if (this.map != null) {
            CompoundTag tag = new CompoundTag();
            for (Map.Entry<String, ScriptValue> entry : this.map.entrySet())
                tag.put(entry.getKey(), entry.getValue().toNbt());
            return tag;
        } else {
            return new CompoundTag();
        }
    }

    public static ScriptValue fromNbt(Tag tag) {
        if (tag instanceof NumericTag ntag) {
            return new ScriptValue(ntag.getAsNumber());
        } else if (tag instanceof CompoundTag ctag) {
            Map<String, ScriptValue> obj = new HashMap<>();
            for (String key : ctag.getAllKeys()) {
                Tag tag1 = ctag.get(key);
                if (tag1 == null) // Should not happen
                    continue;
                obj.put(key, fromNbt(tag1));
            }
            return new ScriptValue(obj);
        } else if (tag instanceof StringTag stag) {
            return string(stag.getAsString());
        } else if (tag instanceof CollectionTag<?> cltag) {
            int sz = cltag.size();
            List<ScriptValue> list = new ArrayList<>(sz);
            for (Tag tag1 : cltag)
                list.add(fromNbt(tag1));
            return new ScriptValue(list);
        } else { // If EndTag, should not reach here
            return VOID;
        }
    }

    public static ScriptValue fromJson(JsonElement el) {
        if (GsonHelper.isNumberValue(el)) {
            return new ScriptValue(el.getAsNumber());
        } else if (GsonHelper.isStringValue(el)) {
            return string(el.getAsString());
        } else if (GsonHelper.isBooleanValue(el)) {
            return bool(el.getAsBoolean());
        } else {
            return ScriptValue.VOID;
        }
    }

}
