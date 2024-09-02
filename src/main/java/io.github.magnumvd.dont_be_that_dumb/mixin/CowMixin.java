package io.github.magnumvd.dont_be_that_dumb.mixin;

import io.github.magnumvd.dont_be_that_dumb.JumpOverGapGoal;
import io.github.magnumvd.dont_be_that_dumb.SeekShelterGoal;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CowEntity.class)
public abstract class CowMixin extends AnimalEntity {

	protected CowMixin(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initGoals", at = @At("TAIL"))
	protected void initGoalsMixin(CallbackInfo ci) {
		this.goalSelector.add(4, new SeekShelterGoal(this, 1.0, true, true));
	}
}
