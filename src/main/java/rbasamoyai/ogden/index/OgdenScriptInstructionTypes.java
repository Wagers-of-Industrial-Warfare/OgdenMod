package rbasamoyai.ogden.index;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.instructions.ammunition.CountAvailableAmmoInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.ammunition.GetMatchingAmmoInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseAndInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseLeftShiftInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseNotInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseOrInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseRightShiftInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.bitwise.BitwiseXorInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.combat.SpawnBulletInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.control.ForInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.control.IfInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.control.WhileInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.CopyItemStackInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.CreateItemStackInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.GetItemStackCountInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.GetItemStackDamageInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.GetItemStackItemInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.GrowItemStackInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.IsSameItemInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.SetItemStackCountInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.SetItemStackDamageInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.ShrinkItemStackInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.item_stack.SplitItemStackInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.AddListElementInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.AddListToListInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.CreateListInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.ListLengthInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.RemoveListElementsInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.list.SublistInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.AndOperatorInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.EqualsInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.GreaterThanInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.GreaterThanOrEqualToInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.LessThanInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.LessThanOrEqualToInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.NotInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.OrInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.logical.UnequalsInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.AbsInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.AddInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.CeilInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.CubeInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.CubeRootInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.DivideInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.DivideIntInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.ExpInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.FloorInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.LnInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.Log10Instruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.LogABInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.ModuloInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.MultiplyInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.NthRootInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.PowInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.RoundInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.SquareInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.SquareRootInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.math.SubtractInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.AddIndexToPathInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.CopyNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.GetNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.InsertNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.MergeNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.RemoveNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.nbt.SetNbtInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.object.CreateObjectInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.object.GetObjectPropertyInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.object.RemoveObjectPropertyInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.object.SetObjectPropertyInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.random.RandomBooleanInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.random.RandomFloatInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.random.RandomGaussianInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.random.RandomIntInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.state_actions.ChangeStateInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.state_actions.HasActionTimePassedInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.state_actions.SyncAnimationInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.state_actions.UpdateActionTimerInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.string.InsertStringInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.string.SubstringInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.variables.GetPropertyInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.variables.GetVariableInstruction;
import rbasamoyai.ogden.firearms.scripting.instructions.variables.SetVariableInstruction;

public class OgdenScriptInstructionTypes {

    //////// Mathematical operator instructions ////////
    public static final ScriptInstructionType
        ADD = register("add", new AddInstruction.Serializer()),
        SUBTRACT = register("subtract", new SubtractInstruction.Serializer()),
        MULTIPLY = register("multiply", new MultiplyInstruction.Serializer()),
        DIVIDE = register("divide", new DivideInstruction.Serializer()),
        DIVIDE_INT = register("divide_int", new DivideIntInstruction.Serializer()),
        MODULO = register("modulo", new ModuloInstruction.Serializer()),
        SQUARE = register("square", new SquareInstruction.Serializer()),
        CUBE = register("cube", new CubeInstruction.Serializer()),
        POW = register("pow", new PowInstruction.Serializer()),
        SQUARE_ROOT = register("square_root", new SquareRootInstruction.Serializer()),
        CUBE_ROOT = register("cube_root", new CubeRootInstruction.Serializer()),
        NTH_ROOT = register("nth_root", new NthRootInstruction.Serializer()),
        EXP = register("exp", new ExpInstruction.Serializer()),
        LN = register("ln", new LnInstruction.Serializer()),
        LOG10 = register("log10", new Log10Instruction.Serializer()),
        LOG_AB = register("log_ab", new LogABInstruction.Serializer()),
        FLOOR = register("floor", new FloorInstruction.Serializer()),
        CEIL = register("ceil", new CeilInstruction.Serializer()),
        ROUND = register("round", new RoundInstruction.Serializer()),
        ABS = register("abs", new AbsInstruction.Serializer());

    //////// Random generation instructions ////////
    public static final ScriptInstructionType
        RANDOM_INT = register("random_int", new RandomIntInstruction.Serializer()),
        RANDOM_FLOAT = register("random_float", new RandomFloatInstruction.Serializer()),
        RANDOM_BOOLEAN = register("random_boolean", new RandomBooleanInstruction.Serializer()),
        RANDOM_GAUSSIAN = register("random_gaussian", new RandomGaussianInstruction.Serializer());

    //////// Bitwise operator instructions ////////
    public static final ScriptInstructionType
        BITWISE_NOT = register("bitwise_not", new BitwiseNotInstruction.Serializer()),
        BITWISE_OR = register("bitwise_or", new BitwiseOrInstruction.Serializer()),
        BITWISE_AND = register("bitwise_and", new BitwiseAndInstruction.Serializer()),
        BITWISE_XOR = register("bitwise_xor", new BitwiseXorInstruction.Serializer()),
        BITWISE_LEFT_SHIFT = register("bitwise_left_shift", new BitwiseLeftShiftInstruction.Serializer()),
        BITWISE_RIGHT_SHIFT = register("bitwise_right_shift", new BitwiseRightShiftInstruction.Serializer());

    //////// Logical boolean operator instructions ////////
    public static final ScriptInstructionType
        NOT = register("not", new NotInstruction.Serializer()),
        OR = register("or", new OrInstruction.Serializer()),
        AND = register("and", new AndOperatorInstruction.Serializer()),
        EQUALS = register("equals", new EqualsInstruction.Serializer()),
        UNEQUAL = register("unequal", new UnequalsInstruction.Serializer()),
        LESS_THAN = register("less_than", new LessThanInstruction.Serializer()),
        LESS_THAN_OR_EQUAL_TO = register("less_than_or_equal_to", new LessThanOrEqualToInstruction.Serializer()),
        GREATER_THAN = register("greater_than", new GreaterThanInstruction.Serializer()),
        GREATER_THAN_OR_EQUAL_TO = register("greater_than_or_equal_to", new GreaterThanOrEqualToInstruction.Serializer());

    //////// String operation instructions ////////
    public static final ScriptInstructionType
        INSERT_STRING = register("insert_string", new InsertStringInstruction.Serializer()),
        SUBSTRING = register("substring", new SubstringInstruction.Serializer());

    //////// List operation instructions ////////
    public static final ScriptInstructionType
        CREATE_LIST = register("create_list", new CreateListInstruction.Serializer()),
        REMOVE_LIST_ELEMENTS = register("remove_list_elements", new RemoveListElementsInstruction.Serializer()),
        ADD_LIST_ELEMENT = register("add_list_element", new AddListElementInstruction.Serializer()),
        ADD_LIST_TO_LIST = register("add_list_to_list", new AddListToListInstruction.Serializer()),
        SUBLIST = register("sublist", new SublistInstruction.Serializer()),
        LIST_LENGTH = register("list_length", new ListLengthInstruction.Serializer());

    //////// Object operation instructions ////////
    public static final ScriptInstructionType
        CREATE_OBJECT = register("create_object", new CreateObjectInstruction.Serializer()),
        SET_OBJECT_PROPERTY = register("set_object_property", new SetObjectPropertyInstruction.Serializer()),
        GET_OBJECT_PROPERTY = register("get_object_property", new GetObjectPropertyInstruction.Serializer()),
        REMOVE_OBJECT_PROPERTY = register("remove_object_property", new RemoveObjectPropertyInstruction.Serializer());

    //////// Item stack operation instructions ////////
    public static final ScriptInstructionType
        CREATE_ITEM_STACK = register("create_item_stack", new CreateItemStackInstruction.Serializer()),
        COPY_ITEM_STACK = register("copy_item_stack", new CopyItemStackInstruction.Serializer()),
        SET_ITEM_STACK_COUNT = register("set_item_stack_count", new SetItemStackCountInstruction.Serializer()),
        SHRINK_ITEM_STACK = register("shrink_item_stack", new ShrinkItemStackInstruction.Serializer()),
        GROW_ITEM_STACK = register("grow_item_stack", new GrowItemStackInstruction.Serializer()),
        SPLIT_ITEM_STACK = register("split_item_stack", new SplitItemStackInstruction.Serializer()),
        SET_ITEM_STACK_DAMAGE = register("set_item_stack_damage", new SetItemStackDamageInstruction.Serializer()),
        GET_ITEM_STACK_ITEM = register("get_item_stack_item", new GetItemStackItemInstruction.Serializer()),
        GET_ITEM_STACK_COUNT = register("get_item_stack_count", new GetItemStackCountInstruction.Serializer()),
        GET_ITEM_STACK_DAMAGE = register("get_item_stack_damage", new GetItemStackDamageInstruction.Serializer()),
        IS_SAME_ITEM = register("is_same_item", new IsSameItemInstruction.Serializer());

    //////// Control structure instructions ////////
    public static final ScriptInstructionType
        IF = register("if", new IfInstruction.Serializer()),
        FOR = register("for", new ForInstruction.Serializer()),
        WHILE = register("while", new WhileInstruction.Serializer());

    //////// Variables and data instructions ////////
    public static final ScriptInstructionType
        SET_VARIABLE = register("set_variable", new SetVariableInstruction.Serializer()),
        GET_VARIABLE = register("get_variable", new GetVariableInstruction.Serializer()),
        GET_PROPERTY = register("get_property", new GetPropertyInstruction.Serializer());

    //////// Firearm state instructions ////////
    public static final ScriptInstructionType
        CHANGE_STATE = register("change_state", new ChangeStateInstruction.Serializer()),
        HAS_ACTION_TIME_PASSED = register("has_action_time_passed", new HasActionTimePassedInstruction.Serializer()),
        UPDATE_ACTION_TIMER = register("update_action_timer", new UpdateActionTimerInstruction.Serializer()),
        SYNC_ANIMATION = register("sync_animation", new SyncAnimationInstruction.Serializer());

    //////// Ammunition management instructions ////////
    public static final ScriptInstructionType
        COUNT_AVAILABLE_AMMO = register("count_available_ammo", new CountAvailableAmmoInstruction.Serializer()),
        GET_MATCHING_AMMO = register("get_matching_ammo", new GetMatchingAmmoInstruction.Serializer());

    //////// Combat instructions ////////
    public static final ScriptInstructionType
        SPAWN_BULLET = register("spawn_bullet", new SpawnBulletInstruction.Serializer());

    //////// Camera manipulation instructions ////////

    //////// NBT instructions ////////
    public static final ScriptInstructionType
        ADD_INDEX_TO_PATH = register("add_index_to_path", new AddIndexToPathInstruction.Serializer()),
        REMOVE_NBT = register("remove_nbt", new RemoveNbtInstruction.Serializer()),
        COPY_NBT = register("copy_nbt", new CopyNbtInstruction.Serializer()),
        MERGE_NBT = register("merge_nbt", new MergeNbtInstruction.Serializer()),
        INSERT_NBT = register("insert_nbt", new InsertNbtInstruction.Serializer()),
        GET_NBT_INT = register("get_nbt_int", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_INT, ScriptValue.ZERO)),
        GET_NBT_BYTE = register("get_nbt_byte", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_BYTE, ScriptValue.ZERO)),
        GET_NBT_SHORT = register("get_nbt_short", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_SHORT, ScriptValue.ZERO)),
        GET_NBT_LONG = register("get_nbt_long", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_LONG, ScriptValue.ZERO)),
        GET_NBT_FLOAT = register("get_nbt_float", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_FLOAT, ScriptValue.ZERO)),
        GET_NBT_DOUBLE = register("get_nbt_double", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_DOUBLE, ScriptValue.ZERO)),
        GET_NBT_BOOLEAN = register("get_nbt_boolean", GetNbtInstruction.Serializer.provider(tag -> ScriptValue.bool(((ByteTag) tag).getAsByte() != (byte) 0), Tag.TAG_BYTE, ScriptValue.FALSE)),
        GET_NBT_STRING = register("get_nbt_string", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_STRING, ScriptValue.EMPTY_STRING)),
        GET_NBT_LIST = register("get_nbt_list", GetNbtInstruction.Serializer.provider(GetNbtInstruction::getScriptValueList)),
        GET_NBT_OBJECT = register("get_nbt_object", GetNbtInstruction.Serializer.provider(ScriptValue::fromNbt, Tag.TAG_COMPOUND, ScriptValue.EMPTY_OBJECT)),
        GET_NBT_ITEM_STACK = register("get_nbt_item_stack", GetNbtInstruction.Serializer.provider(tag -> ScriptValue.itemStack(ItemStack.of((CompoundTag) tag)), Tag.TAG_COMPOUND, ScriptValue.EMPTY_ITEM_STACK)),
        SET_NBT_INT = register("set_nbt_int", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::intValue, IntTag::valueOf))),
        SET_NBT_BYTE = register("set_nbt_byte", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::byteValue, ByteTag::valueOf))),
        SET_NBT_SHORT = register("set_nbt_short", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::shortValue, ShortTag::valueOf))),
        SET_NBT_LONG = register("set_nbt_long", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::longValue, LongTag::valueOf))),
        SET_NBT_FLOAT = register("set_nbt_float", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::floatValue, FloatTag::valueOf))),
        SET_NBT_DOUBLE = register("set_nbt_double", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::num, Number::doubleValue, DoubleTag::valueOf))),
        SET_NBT_BOOLEAN = register("set_nbt_boolean", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::bool, ByteTag::valueOf))),
        SET_NBT_STRING = register("set_nbt_string", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::str, StringTag::valueOf))),
        SET_NBT_LIST = register("set_nbt_list", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::list, SetNbtInstruction::getListTag))),
        SET_NBT_OBJECT = register("set_nbt_object", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::map, SetNbtInstruction::getObjectTag))),
        SET_NBT_ITEM_STACK = register("set_nbt_item_stack", SetNbtInstruction.Serializer.provider(SetNbtInstruction.Getter.fromFunctions(ScriptValue::stack, SetNbtInstruction::getItemStackTag)));

    private static ScriptInstructionType register(String id, ScriptInstructionSerializer ser) {
        return Registry.register(OgdenRegistries.SCRIPT_INSTRUCTIONS, OgdenMod.resource(id), new ScriptInstructionType(ser));
    }

    private static ScriptInstructionType register(String id, Function<ScriptInstructionType, ScriptInstructionSerializer> serProv) {
        return Registry.register(OgdenRegistries.SCRIPT_INSTRUCTIONS, OgdenMod.resource(id), new ScriptInstructionType(serProv));
    }

    public static void register() {
    }

}
