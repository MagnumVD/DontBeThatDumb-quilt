package io.github.magnumvd.dont_be_that_dumb.mixin;

import io.github.magnumvd.dont_be_that_dumb.SeekShelterGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SheepEntity.class)
public abstract class SheepMixin extends AnimalEntity {

	protected SheepMixin(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	protected void initGoals(CallbackInfo ci) {
		this.goalSelector.add(5, new SeekShelterGoal(this, 1.0, true, true));
	}
}

