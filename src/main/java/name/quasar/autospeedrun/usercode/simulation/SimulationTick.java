package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.world.BlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;

/** movement/input simulation stuff */
public class SimulationTick {
    private Vector3 playerPos;
    private Vector3 playerVelo;
    private float playerYaw;
    private float playerPitch;
    private Dimension dimension;
    private HashMap<BlockLocation, BlockType> blocks;
    private boolean isInBoat;

    // next tick calculation stuff
    private SimulationInput input = null;
    private boolean isSwimming = false;
    private boolean crouching = false;
    private boolean sprintingFlag = false;
    private boolean wasTouchingWater = false;
    private boolean wasUnderwater = false;
    private boolean wasEyeInWater = false;
    private boolean horizontalCollision = false;
    private boolean onGround = true;
    private int waterVisionTime = 0;
    private int sprintTriggerTime = 0;
    private int noJumpDelay = 0;
    private float speed = (float)0.10000000149011612;
    private String fluidOnEyes = null;
    private HashMap<String, Double> fluidHeights = new HashMap<>();

    public SimulationTick(Dimension dimension) {
        this.dimension = dimension;
        this.blocks = new HashMap<>();
        this.isInBoat = false;
    }

    /** return a copy of the simulation tick advanced by one tick. the kbm is reset btw */
    public SimulationTick getNext(FakeKBMInputs kbm) {
        if (getNextCache != null) {
            return getNextCache;
        }
        SimulationTick next = new SimulationTick(dimension);
        next.playerPos = this.playerPos;
        next.playerVelo = this.playerVelo;
        next.playerYaw = this.playerYaw;
        next.playerPitch = this.playerPitch;
        next.dimension = this.dimension;
        next.blocks = this.blocks;
        next.isInBoat = this.isInBoat;

        next.input = new SimulationInput(kbm);
        next.isSwimming = this.isSwimming;
        next.crouching = this.crouching;
        next.sprintingFlag = this.sprintingFlag;
        next.wasTouchingWater = this.wasTouchingWater;
        next.wasUnderwater = this.wasUnderwater;
        next.wasEyeInWater = this.wasEyeInWater;
        next.horizontalCollision = this.horizontalCollision;
        next.onGround = this.onGround;
        next.sprintTriggerTime = this.sprintTriggerTime;
        next.waterVisionTime = this.waterVisionTime;
        next.noJumpDelay = noJumpDelay;
        next.speed = this.speed;
        next.fluidOnEyes = this.fluidOnEyes;
        next.fluidHeights = this.fluidHeights;
        next.tick_LocalPlayer();
        getNextCache = next;
        return next;
    }

    private SimulationTick getNextCache = null;
    
    private AABB getPlayerBoundingBox() {
        Vector3 aaa = playerPos.offsetX(-0.3).offsetZ(-0.3);
        Vector3 bbb = playerPos.offsetX(0.3).offsetZ(0.3).offsetY(1.8);
        return new AABB(aaa, bbb);
    }

    private void tick_LocalPlayer() {
        tick_Player();
    }

    private void tick_Player() {
        updateIsUnderwater_LocalPlayer();
        tick_LivingEntity();
    }

    private void updateIsUnderwater_LocalPlayer() {
        updateIsUnderwater_Player();
    }

    private void updateIsUnderwater_Player() {
        wasUnderwater = "water".equals(fluidOnEyes);
    }

    private void tick_LivingEntity() {
        tick_Entity();
        aiStep_LocalPlayer();
    }

    private void tick_Entity() {
        baseTick_LivingEntity();
    }

    private void baseTick_LivingEntity() {
        baseTick_Entity();
        // hopefully there is nothing important in livingentity after the baseTick_Entity stuff ...
    }

    private void baseTick_Entity() {
        updateInWaterStateAndDoFluidPushing_Entity();
        updateFluidOnEyes_Entity();
        updateSwimming_Entity();
    }

    private void updateFluidOnEyes_Entity() {
        wasEyeInWater = "water".equals(fluidOnEyes);
        fluidOnEyes = null;
        Vector3 waterEyeCheckPos = getPlayerEyeY().add(new Vector3(0, -0.11111111F, 0));
        if (isInBoat) {  // imperfect check since boat may sink and submerge player. we assume that never happens
            return;
        }

        BlockLocation blockPos = BlockLocation.fromVector3(dimension, waterEyeCheckPos);
        BlockType blockType = blocks.getOrDefault(blockPos, BlockType.AIR);

        if (blockType.isFluid()) {
            double e = blockPos.getY() + blockType.getFluidHeight();
            if (e > waterEyeCheckPos.getY()) {
                fluidOnEyes = blockType.getValue();
            }
        }
    }

    private void updateSwimming_Entity() {
        if (this.isSwimming()) {
            this.setSwimming(this.sprintingFlag && this.wasTouchingWater && !isInBoat);
        } else {
            this.setSwimming(this.sprintingFlag && this.wasEyeInWater && this.wasTouchingWater && !isInBoat);
        }
    }

    // done
    private boolean updateInWaterStateAndDoFluidPushing_Entity() {
        updateInWaterStateAndDoWaterCurrentPushing_Entity();
        if (this.wasTouchingWater) {
            return true;
        } else {
            double scale = dimension.equals(Dimension.NETHER) ? 0.007 : 0.0023333333333333335;
            return updateFluidHeightAndDoFluidPushing_Entity("lava", scale);
        }
    }

    // done
    private void updateInWaterStateAndDoWaterCurrentPushing_Entity() {
        if (isInBoat) {
            this.wasTouchingWater = false;
        } else if (updateFluidHeightAndDoFluidPushing_Entity("water", 0.014)) {
//            this.fallDistance = 0.0F;
            this.wasTouchingWater = true;
//            this.clearFire();
        } else {
            this.wasTouchingWater = false;
        }
    }

    private boolean updateFluidHeightAndDoFluidPushing_Entity(String fluidType, double scale) {
        AABB playerAABB = getPlayerBoundingBox().deflate(0.001);
        int minX = Util.floor_Mth(playerAABB.getMinX());
        int maxX = Util.ceil_Mth(playerAABB.getMaxX());
        int minY = Util.floor_Mth(playerAABB.getMinY());
        int maxY = Util.ceil_Mth(playerAABB.getMaxY());
        int minZ = Util.floor_Mth(playerAABB.getMinZ());
        int maxZ = Util.ceil_Mth(playerAABB.getMaxZ());
        double newFluidHeight = 0.0;
        boolean touchingWater = false;
        Vector3 acceleration = Vector3.ZERO;
        int numFluidBlocksTouched = 0;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockLocation blockLoc = new BlockLocation(dimension, x, y, z);
                    BlockType blockType = blocks.getOrDefault(blockLoc, BlockType.AIR);
                    if (blockType.getValue().equals(fluidType)) {
                        double fluidTopY = y + blockType.getFluidHeight();
                        if (fluidTopY >= playerAABB.getMinY()) {
                            touchingWater = true;
                            newFluidHeight = Math.max(fluidTopY - playerAABB.getMinY(), newFluidHeight);
                            Vector3 accelPiece = blockType.getFluidFlow();  // btw check impl for problematic thing (?)
                            if (newFluidHeight < 0.4) {
                                accelPiece = accelPiece.mult(newFluidHeight);
                            }

                            acceleration = acceleration.add(accelPiece);
                            numFluidBlocksTouched++;
                        }
                    }
                }
            }
        }

        if (acceleration.length() > 0.0) {
            if (numFluidBlocksTouched > 0) {
                acceleration = acceleration.mult(1.0 / numFluidBlocksTouched);
            }

            Vector3 velocity = getPlayerVelo();
            acceleration = acceleration.mult(scale);
            if (Math.abs(velocity.getX()) < 0.003 && Math.abs(velocity.getZ()) < 0.003
                && acceleration.length() < 0.0045000000000000005) {
                acceleration = acceleration.normalized().mult(0.0045000000000000005);
            }

            this.setPlayerVelo(velocity.add(acceleration));
        }

        this.fluidHeights.put(fluidType, newFluidHeight);
        return touchingWater;
    }

    private void aiStep_LocalPlayer() {
        if (this.sprintTriggerTime > 0) {
            this.sprintTriggerTime--;
        }

        boolean prevInputShiftKeyDown = this.input.shiftKeyDown;
        boolean prevHasEnoughImpulseSprinting = this.hasEnoughImpulseToStartSprinting_LocalPlayer();
        this.crouching = !this.isSwimming() && this.canEnterPose_Entity("crouching")
            && (this.input.shiftKeyDown || !this.canEnterPose_Entity("standing"));
        this.input.tick(crouching);  // todo this should be crawlingOrCrouching, should handle crawling properly
        // todo handle item usage
//        if (this.isUsingItem()) {
//            this.input.leftImpulse *= 0.2F;
//            this.input.forwardImpulse *= 0.2F;
//            this.sprintTriggerTime = 0;
//        }

        // push players out of blocks if they are inside of the block? todo verify
        float bbWidth = 0.6f;  // todo idk if this is actually correct
        checkInBlock_LocalPlayer(playerPos.add(new Vector3(-bbWidth*0.35, 0.5, bbWidth*0.35)));
        checkInBlock_LocalPlayer(playerPos.add(new Vector3(-bbWidth*0.35, 0.5, -bbWidth*0.35)));
        checkInBlock_LocalPlayer(playerPos.add(new Vector3(bbWidth*0.35, 0.5, -bbWidth*0.35)));
        checkInBlock_LocalPlayer(playerPos.add(new Vector3(bbWidth*0.35, 0.5, bbWidth*0.35)));

        if (prevInputShiftKeyDown) {
            // double-tap w timer reset
            this.sprintTriggerTime = 0;
        }

        // lets assume we have >3 hunger bars and no blindness effect and not using item
        if ((this.onGround || wasUnderwater)
            && !prevInputShiftKeyDown
            && !prevHasEnoughImpulseSprinting
            && this.hasEnoughImpulseToStartSprinting_LocalPlayer()
            && !this.sprintingFlag) {
            if (this.sprintTriggerTime <= 0 && !input.getOptions().keySprint) {
                // will start sprinting next tick due to double-tap of w
                this.sprintTriggerTime = 7;
            } else {
                // start sprinting as a result of double tap w
                sprintingFlag = true;
            }
        }

        // lets assume we have >3 hunger bars and no blindness effect and not using item
        if (!this.sprintingFlag
            && (!this.wasTouchingWater || this.wasUnderwater)
            && this.hasEnoughImpulseToStartSprinting_LocalPlayer()
            && this.input.getOptions().keySprint) {
            // start sprinting as a result of sprint key being pressed
            this.sprintingFlag = true;
        }

        if (sprintingFlag) {
            boolean forwardAtAll = !this.input.hasForwardImpulse();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.shiftKeyDown && forwardAtAll || !this.wasTouchingWater) {
                    // stop sprinting when swimming at water surface level
                    this.sprintingFlag = false;
                }
            } else if (forwardAtAll || this.horizontalCollision || this.wasTouchingWater && !this.wasUnderwater) {
                // stop sprinting when snagging on a block horizontally
                this.sprintingFlag = false;
            }
        }

        if (this.wasTouchingWater && this.input.shiftKeyDown) {
            // sneak = down in water
            this.playerVelo.add(new Vector3(0.0, -0.04F, 0.0));
        }

        if ("water".equals(fluidOnEyes)) {
            this.waterVisionTime = Util.clamp_Mth(this.waterVisionTime + 1, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.waterVisionTime = Util.clamp_Mth(this.waterVisionTime - 10, 0, 600);
        }

        // also lets assume we never ride a horse (the code for riding a horse is omitted)

        aiStep_Player();
    }

    private void aiStep_Player() {
        // lets assume the difficulty is never peaceful (code for peaceful natural regen is omitted)

        // hopefully ignoring inventory ticking doesnt have any consequences
//        this.inventory.tick();
        aiStep_LivingEntity();

        this.speed = (float)0.10000000149011612;

        // lets assume we are always alive
        // im ignoring the this.touch() stuff because its only for edge cases like item pickup, pufferfish, etc.
    }

    private void aiStep_LivingEntity() {
        if (this.noJumpDelay > 0) {
            this.noJumpDelay--;
        }

        playerVelo = playerVelo.mult(0.98);

        Vector3 oldPlayerVelo = playerVelo;
        double dx = oldPlayerVelo.getX();
        double dy = oldPlayerVelo.getY();
        double dz = oldPlayerVelo.getZ();
        if (Math.abs(oldPlayerVelo.getX()) < 0.003) {
            dx = 0.0;
        }
        if (Math.abs(oldPlayerVelo.getY()) < 0.003) {
            dy = 0.0;
        }
        if (Math.abs(oldPlayerVelo.getZ()) < 0.003) {
            dz = 0.0;
        }
        playerVelo = new Vector3(dx, dy, dz);

        if (this.jumping && this.isAffectedByFluids()) {
            double k;
            if (this.isInLava()) {
                k = this.getFluidHeight(FluidTags.LAVA);
            } else {
                k = this.getFluidHeight(FluidTags.WATER);
            }

            boolean bl = this.isInWater() && k > 0.0;
            double l = this.getFluidJumpThreshold();
            if (!bl || this.onGround && !(k > l)) {
                if (!this.isInLava() || this.onGround && !(k > l)) {
                    if ((this.onGround || bl && k <= l) && this.noJumpDelay == 0) {
                        this.jumpFromGround();
                        this.noJumpDelay = 10;
                    }
                } else {
                    this.jumpInLiquid(FluidTags.LAVA);
                }
            } else {
                this.jumpInLiquid(FluidTags.WATER);
            }
        } else {
            this.noJumpDelay = 0;
        }

        this.level.getProfiler().pop();
        this.level.getProfiler().push("travel");
        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        this.updateFallFlying();
        net.minecraft.world.phys.AABB aABB = this.getBoundingBox();
        this.travel(new Vec3(this.xxa, this.yya, this.zza));
        this.level.getProfiler().pop();
        this.level.getProfiler().push("push");
        if (this.autoSpinAttackTicks > 0) {
            this.autoSpinAttackTicks--;
            this.checkAutoSpinAttack(aABB, this.getBoundingBox());
        }

        this.pushEntities();
        this.level.getProfiler().pop();
        if (!this.level.isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }
    }

    private boolean hasEnoughImpulseToStartSprinting_LocalPlayer() {
        return wasUnderwater ? this.input.hasForwardImpulse() : this.input.forwardImpulse >= 0.8;
    }

    private boolean canEnterPose_Entity(String pose) {
        // todo put implementation
        return true;
    }

    private void checkInBlock_LocalPlayer(Vector3 v3) {
        checkInBlock_LocalPlayer(v3.getX(), v3.getY(), v3.getZ());
    }

    private void checkInBlock_LocalPlayer(double x, double y, double z) {
        BlockPos blockPos = new BlockPos(x, y, z);
        if (this.blocked(blockPos)) {
            double g = x - blockPos.getX();
            double h = z - blockPos.getZ();
            Direction direction = null;
            double i = 9999.0;
            if (!this.blocked(blockPos.west()) && g < i) {
                i = g;
                direction = Direction.WEST;
            }

            if (!this.blocked(blockPos.east()) && 1.0 - g < i) {
                i = 1.0 - g;
                direction = Direction.EAST;
            }

            if (!this.blocked(blockPos.north()) && h < i) {
                i = h;
                direction = Direction.NORTH;
            }

            if (!this.blocked(blockPos.south()) && 1.0 - h < i) {
                i = 1.0 - h;
                direction = Direction.SOUTH;
            }

            if (direction != null) {
                Vec3 vec3 = this.getDeltaMovement();
                switch (direction) {
                    case WEST:
                        this.setDeltaMovement(-0.1, vec3.y, vec3.z);
                        break;
                    case EAST:
                        this.setDeltaMovement(0.1, vec3.y, vec3.z);
                        break;
                    case NORTH:
                        this.setDeltaMovement(vec3.x, vec3.y, -0.1);
                        break;
                    case SOUTH:
                        this.setDeltaMovement(vec3.x, vec3.y, 0.1);
                }
            }
        }
    }

    /* misc, getters, setters */

    private Dimension getDimension() {
        return dimension;
    }

    public Vector3 getPlayerPos() {
        return playerPos;
    }

    public Vector3 getPlayerEyeY() {
        // todo maybe inaccurate since crouching eye level change is 2 ticks delayed or something
        return playerPos.add(new Vector3(0, crouching ? 1.27 : 1.62, 0));
    }

    public Vector3 getPlayerVelo() {
        return playerVelo;
    }

    public float getPlayerYaw() {
        return playerYaw;
    }

    public float getPlayerPitch() {
        return playerPitch;
    }

    public SimulationTick setPlayerPos(Vector3 playerPos) {
        this.playerPos = playerPos;
        return this;
    }

    public SimulationTick setPlayerVelo(Vector3 playerVelo) {
        this.playerVelo = playerVelo;
        return this;
    }
    
    public SimulationTick setPlayerYaw(float playerYaw) {
        this.playerYaw = playerYaw;
        return this;
    }
    
    public SimulationTick setPlayerPitch(float playerPitch) {
        this.playerPitch = playerPitch;
        return this;
    }

    public boolean isInBoat() {
        return isInBoat;
    }

    public SimulationTick setInBoat(boolean isInBoat) {
        this.isInBoat = isInBoat;
        return this;
    }

    private boolean isSwimming() {
        return isSwimming;
    }

    private SimulationTick setSwimming(boolean isSwimming) {
        this.isSwimming = isSwimming;
        return this;
    }

    private SimulationInput getSimulationInput() {
        return input;
    }

    public HashMap<BlockLocation, BlockType> getBlocks() {
        return blocks;
    }
}
