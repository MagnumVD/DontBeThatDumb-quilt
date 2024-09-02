package io.github.magnumvd.dont_be_that_dumb;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.EnumSet;

public class JumpOverGapGoal extends Goal {
	protected final PathAwareEntity mob;
	private final World world;
	private Vec3d startPos = null;
	private Vec3d targetPos = null;
	private BlockPos OriginalTargetPos = null;
	private JumpPhases jumpPhase = JumpPhases.None;
	public enum JumpPhases {
		None,
		PreRun,
		Run,
		Jump,
		End
	}

	public JumpOverGapGoal(PathAwareEntity mob) {
		this.mob = mob;
		this.world = mob.getWorld();
		this.setControls(EnumSet.of(Goal.Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (this.mob.isNavigating()) {
			//test if Mob is stuck while navigating,help it reach its destination if possible
			Path path = this.mob.getNavigation().getCurrentPath();
			if (path != null) {
				this.OriginalTargetPos = path.getTarget();
				if (!path.reachesTarget() && (path.getLength() == 1 ? true : path.getCurrentNode() == path.getEnd())) {
					this.startPos = path.getEnd().getBlockPos().ofBottomCenter();
					return locateTargetPos(this.OriginalTargetPos.ofCenter().subtract(this.startPos));
				}
			}
		}
		return false;
	}

	@Override
	public void start() {
		this.jumpPhase = JumpPhases.PreRun;
	}

	@Override
	public boolean shouldContinue() {
		return this.jumpPhase != JumpPhases.None;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		switch (this.jumpPhase) {
		case PreRun:
			this.mob.getMoveControl().moveTo(this.startPos.x, this.startPos.y, this.startPos.z, 0.7);
			if (this.startPos.withinRange(this.mob.getPos(), 0.1)) {
				this.jumpPhase = JumpPhases.Run;
			}
			break;

		case Run:
			this.mob.getMoveControl().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, speed());
			if (!this.world.isTopSolid(this.mob.getBlockPos().down(), this.mob) && !this.startPos.withinRange(this.mob.getPos(), 0.8)) {
				this.jumpPhase = JumpPhases.Jump;
				this.mob.getJumpControl().setActive();
			}
			break;

		case Jump:
			this.mob.getMoveControl().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, speed());
			if (this.mob.isOnGround()) {
				this.jumpPhase = JumpPhases.End;
				this.mob.getNavigation().startMovingTo(this.OriginalTargetPos.getX(), this.OriginalTargetPos.getY(), this.OriginalTargetPos.getZ(), 1.0);
			}
			break;

		case End:
			if (canStart()) {
				this.jumpPhase = JumpPhases.PreRun;
			} else {
				this.jumpPhase = JumpPhases.None;
			}
			break;
		}
		if (!this.mob.isOnGround()) {
			this.jumpPhase = JumpPhases.Jump;
		}
	}

	protected double jumpHeight() {
		/* Calculate the max jump height based off of
		a formula from the minecraft wiki relative to
		any leaping effects that might be applied */
		double baseJumpHeight = 1.2;
		double leapingStrength = this.mob.getJumpBoostVelocityModifier()*10;
		return baseJumpHeight*Math.pow(1.5, leapingStrength);
	}

	protected double speed() {
		//very crude estimation of the needed speed for the jump
		double squaredDistance = this.startPos.squaredDistanceTo(this.targetPos);
		double speed = (this.mob.getGravity()*squaredDistance*0.8)/(this.mob.getMovementSpeed()*jumpHeight());
		return speed;
	}

	protected boolean locateTargetPos(Vec3d searchDirection) {
		int searchFoV = 70; //Degrees
		int searchIncrementSize = 20; //Degrees

		searchDirection = searchDirection.multiply(1,0,1).normalize();
		int searchIterations = (searchFoV/searchIncrementSize)*2;
		int searchAngle = 0;
		for (int i = 0; i <= searchIterations; i++) {
			searchAngle = -searchAngle;
			if (Math.floorMod(i, 2) == 1) {
				searchAngle += searchIncrementSize;
			}
			Vec3d testDirection = searchDirection.rotateY((float) Math.toRadians(searchAngle));
			this.targetPos = this.startPos.add(testDirection.multiply(2.4));
			BlockPos jumpPos = BlockPos.fromPosition(this.startPos.add(testDirection.multiply(1.2))).down();
			// use the targetPos if it's solid and the mob would actually jump over a non-solid block
			for (int j = 0; j < 3; j++) {
				//make sure that the block tested is solid, there is a non-solid block the mob would jump over and the jump has clearance
				if (this.world.isTopSolid(BlockPos.fromPosition(this.targetPos).down(j), this.mob) && !this.world.isTopSolid(jumpPos, this.mob) && hasClearance()) {
					// move the startpos back by 0.3 blocks so the mob will have time to catch up to speed a bit more
					this.startPos = this.startPos.subtract(testDirection.multiply(0.3));
					return true;
				}
			}

		}
		return false;
	}

	protected boolean hasClearance() {
		double requiredHeight = this.mob.getHeight()-0.3F + jumpHeight();
		double testHeight = 0.5;
		while(testHeight<requiredHeight) {
			testHeight += 0.5;

			Vec3d start = this.startPos.add(0, requiredHeight, 0);
			Vec3d end = this.targetPos.add(0, requiredHeight, 0);
			BlockHitResult result = this.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, this.mob));
			if (result.getType() != HitResult.Type.MISS) {
				return false;
			}
		}
		return true;
	}
}
