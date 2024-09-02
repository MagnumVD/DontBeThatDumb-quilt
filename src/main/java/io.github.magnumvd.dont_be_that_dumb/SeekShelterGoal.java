package io.github.magnumvd.dont_be_that_dumb;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class SeekShelterGoal extends Goal {

	protected final PathAwareEntity mob;
	private final double speed;
	private final boolean seekAtNight;
	private final boolean seekAtRain;
	private double targetX;
	private double targetY;
	private double targetZ;
	private boolean nextNodeIsOutdoors;
	private final World world;


	public SeekShelterGoal(PathAwareEntity mob, double speed, boolean seekAtNight, boolean seekAtRain) {
		this.mob = mob;
		this.speed = speed;
		this.world = mob.getWorld();
		this.seekAtNight = seekAtNight;
		this.seekAtRain = seekAtRain;
		this.setControls(EnumSet.of(Goal.Control.MOVE));
	}

	@Override
	public boolean canStart() {
		// will seek shelter at night or when it's raining
		if ((!this.world.isDay() && this.seekAtNight) || (this.world.isRaining() && this.seekAtRain)) {

			nextNodeIsOutdoors = false;
			if (this.mob.isNavigating()) {
				Path path = this.mob.getNavigation().getCurrentPath();
				if (path != null) {
					nextNodeIsOutdoors = this.world.isSkyVisible(path.getCurrentNodePos());
				}
			}

			// start when the mob is outdoors or walking outdoors and a shaded position has been found
			return (this.world.isSkyVisible(this.mob.getBlockPos()) || nextNodeIsOutdoors) && this.targetShadedPos();
		} else {
			return false;
		}
	}

	protected boolean targetShadedPos() {
		Vec3d vec3d = this.locateShadedPos();
		if (vec3d == null) {
			return false;
		} else {
			this.targetX = vec3d.x;
			this.targetY = vec3d.y;
			this.targetZ = vec3d.z;
			return true;
		}
	}

	@Override
	public boolean shouldContinue() {
		return !this.mob.getNavigation().isIdle();
	}

	@Override
	public void start() {
		this.mob.setCustomName(Text.of(String.valueOf(nextNodeIsOutdoors)));
		this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
	}

	@Nullable
	protected Vec3d locateShadedPos() {
		if (nextNodeIsOutdoors && !this.world.isSkyVisible(this.mob.getBlockPos())) {
			return this.mob.getPos();
		}

		RandomGenerator randomGenerator = this.mob.getRandom();
		BlockPos blockPos = this.mob.getBlockPos();

		for (int i = 0; i < 10; i++) {
			BlockPos blockPos2 = blockPos.add((randomGenerator.nextInt(20) - 10), randomGenerator.nextInt(6) - 3, randomGenerator.nextInt(20) - 10);
			if (this.world.getBlockState(blockPos2).isAir() && !this.world.isSkyVisible(blockPos2) && this.mob.getPathfindingFavor(blockPos2) < 0.0F) {
				return Vec3d.ofBottomCenter(blockPos2);
			}
		}

		return null;
	}
}
