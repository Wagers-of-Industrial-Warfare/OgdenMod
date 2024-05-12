_NOTE: this is temporary. A move to a Github wiki is planned for the future._

### Notation
`(A)` - Array \
`(Z)` - Boolean \
`(D)` - Decimal (double/float) \
`(I)` - Integer \
`(O)` - Object \
`(S)` - String \
`(*)` - Varies

---

### Script formatting
__Main script__
```
The firearm action script (script_type: "main")
├── parent (S) - A parent script from which to inherit states.
├── equip_state (S) - The state visited on equipping the item. This must be a valid state in *states*. Child script
│    states are invalid. If *parent* is present, this value is not necessary; if specified, it overrides the parent
│    *equip_state*.
└── states (O) - A key-value map of firearm states. If *parent* is present, any overla
    └── "<state>" (S) - A state object.
        ├── location (S) - A resource location pointing to the script file.
        └── instructions (A) - A list of instruction objects to execute each tick during the state. Each
             instruction listed is executed sequentially.
            └── (O) An instruction object.
```

__State script__
```
The state script
└── instructions (A) - A list of instruction objects to execute each tick during the state. Each
     instruction listed is executed sequentially.
    └── (O) An instruction object.
```

---

### Instruction object format
```
Instruction object
├── instruction (S) - An id referring to a firearm script instruction type. Must be a registered instruction type.
└── args (O) - An object with instruction arguments passed to the instruction constructor.
    └── "<arg>" (*) - A value to be passed to the instruction. Nested instruction return values are also supported.
```

---

### Default instructions

__Mathematical operators__ \
`ogden:add` \
`ogden:subtract` \
`ogden:multiply` \
`ogden:divide` \
`ogden:divide_int` \
`ogden:modulo` \
`ogden:square` \
`ogden:cube` \
`ogden:pow` \
`ogden:square_root` \
`ogden:cube_root` \
`ogden:nth_root` \
`ogden:exp` \
`ogden:ln` \
`ogden:log10` \
`ogden:log_ab` \
`ogden:floor` \
`ogden:ceil` \
`ogden:round` \
`ogden:abs`

__Random operations__ \
`ogden:random_int` \
`ogden:random_float` \
`ogden:random_boolean` \
`ogden:random_gaussian`

__Bitwise operators__ \
`ogden:bitwise_not` \
`ogden:bitwise_or` \
`ogden:bitwise_and` \
`ogden:bitwise_xor` \
`ogden:bitwise_left_shift` \
`ogden:bitwise_right_shift`

__Logical boolean operators__ \
`ogden:not` \
`ogden:or` \
`ogden:and` \
`ogden:equals` \
`ogden:inequals` \
`ogden:less_than` \
`ogden:less_than_or_equal_to` \
`ogden:greater_than` \
`ogden:greater_than_or_equal_to`

__String operations__ \
`ogden:insert_string` \
`ogden:substring`

__List operations__ \
`ogden:create_list` \
`ogden:remove_list_elements` \
`ogden:add_list_element` \
`ogden:add_list_to_list` \
`ogden:sub_list` \
`ogden:list_length`

__Object operations__ \
`ogden:create_object` \
`ogden:set_object_property` \
`ogden:get_object_property` \
`ogden:remove_object_property`

__Item stack operations__ \
`ogden:create_item_stack` \
`ogden:copy_item_stack` \
`ogden:set_item_stack_count` \
`ogden:shrink_item_stack` \
`ogden:grow_item_stack` \
`ogden:split_item_stack` \
`ogden:set_item_stack_damage` \
`ogden:get_item_stack_item` \
`ogden:get_item_stack_count` \
`ogden:get_item_stack_damage` \
`ogden:is_same_item`

__Control structures__ \
`ogden:if` \
`ogden:for` \
`ogden:while`

__Variables and data__ \
`ogden:set_variable` \
`ogden:get_variable` \
`ogden:get_property`

__State actions__ \
`ogden:change_state` \
`ogden:has_action_time_passed` \
`ogden:sync_animation` \
`ogden:update_action_timer`

__Ammunition management__ \
`ogden:count_available_ammo` \
`ogden:get_matching_ammo`

__Input__ \
`ogden:input_down` \
`ogden:input_up` \
`ogden:input_pressed`

__Combat__ \
`ogden:spawn_bullet` \
`ogden:damage_item` \
`ogden:melee_swing`

__Camera manipulation__ \
`ogden:sway_camera` \
`ogden:recoil_camera` \
`ogden:shake_camera`

__NBT modification__ \
`ogden:add_index_to_path` \
`ogden:remove_nbt` \
`ogden:copy_nbt` \
`ogden:merge_nbt` \
`ogden:insert_nbt` \
`ogden:get_nbt_int` \
`ogden:get_nbt_byte` \
`ogden:get_nbt_short` \
`ogden:get_nbt_long` \
`ogden:get_nbt_float` \
`ogden:get_nbt_double` \
`ogden:get_nbt_boolean` \
`ogden:get_nbt_string` \
`ogden:get_nbt_array` \
`ogden:get_nbt_object` \
`ogden:get_nbt_item_stack` \
`ogden:set_nbt_int` \
`ogden:set_nbt_byte` \
`ogden:set_nbt_short` \
`ogden:set_nbt_long` \
`ogden:set_nbt_float` \
`ogden:set_nbt_double` \
`ogden:set_nbt_boolean` \
`ogden:set_nbt_string` \
`ogden:set_nbt_array` \
`ogden:set_nbt_object` \
`ogden:set_nbt_item_stack`
