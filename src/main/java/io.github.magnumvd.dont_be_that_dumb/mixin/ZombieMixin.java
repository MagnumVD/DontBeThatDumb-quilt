package io.github.magnumvd.dont_be_that_dumb.mixin;

import io.github.magnumvd.dont_be_that_dumb.JumpOverGapGoal;
import io.github.magnumvd.dont_be_that_dumb.SeekShelterGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public class ZombieMixin extends HostileEntity {

	protected ZombieMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	protected void initGoalsMixin(CallbackInfo ci) {
		this.goalSelector.add(0, new JumpOverGapGoal(this));
	}
}
