package rbasamoyai.ogden.entities;

import javax.annotation.Nullable;

import rbasamoyai.ogden.ammunition.AmmunitionProperties;

public interface AmmunitionPropertiesEntity<T extends AmmunitionProperties> {

    @Nullable T getAmmunitionProperties();

}
