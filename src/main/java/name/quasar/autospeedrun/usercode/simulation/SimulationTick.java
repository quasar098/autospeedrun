package name.quasar.autospeedrun.usercode.simulation;

import name.quasar.autospeedrun.usercode.Dimension;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.geometry.AABB;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.world.BlockType;
import name.quasar.autospeedrun.usercode.world.World;

import java.util.HashMap;

/** movement/input simulation stuff */
public class SimulationTick {
    private Vector3 playerPos;
    private Vector3 playerVelo;
    private float playerYaw;  // in degrees
    private float playerPitch;  // in degrees
    private Dimension dimension;
    private boolean isInBoat;

    // next tick calculation stuff
    private SimulationInput input = null;
    private World world = null;
    private boolean isSwimming = false;
    private boolean crouching = false;
    private boolean sprintingFlag = false;
    private boolean shiftKeyDownFlag = false;  // notably distinct from input.shiftKeyDown
    private boolean wasTouchingWater = false;
    private boolean wasUnderwater = false;
    private boolean wasEyeInWater = false;
    private boolean isTouchingLava = false;
    private boolean horizontalCollision = false;
    private boolean verticalCollision = false;
    private boolean onGround = true;
    private boolean jumping = false;
    private int waterVisionTime = 0;
    private int sprintTriggerTime = 0;
    private int noJumpDelay = 0;
    private float speed = (float)0.10000000149011612;
    private float flyingSpeed = 0.02F;
    private float xxa = 0.0f;
    private float yya = 0.0f;
    private float zza = 0.0f;
    private Vector3 stuckSpeedMultiplier = Vector3.ZERO;
    private String fluidOnEyes = null;
    private HashMap<String, Double> fluidHeights = new HashMap<>();
    private BlockLocation blockPosition = null;

    private final int JUMP_BOOST_LVL = 0;  // todo make configurable
    private final float maxUpStep = 0.6F;

    public SimulationTick(Dimension dimension) {
        this.dimension = dimension;
        this.isInBoat = false;
    }

    /** return a copy of the simulation tick advanced by one tick. the kbm is reset btw */
    public SimulationTick getNext(FakeKBMInputs kbm, World world) {
        if (getNextCache != null) {
            return getNextCache;
        }
        SimulationTick next = new SimulationTick(dimension);
        next.playerPos = this.playerPos;
        next.playerVelo = this.playerVelo;
        next.playerYaw = this.playerYaw;
        next.playerPitch = this.playerPitch;
        next.dimension = this.dimension;
        next.isInBoat = this.isInBoat;

        next.input = new SimulationInput(kbm);
        next.world = world;
        next.isSwimming = this.isSwimming;
        next.crouching = this.crouching;
        next.sprintingFlag = this.sprintingFlag;
        next.shiftKeyDownFlag = this.shiftKeyDownFlag;
        next.wasTouchingWater = this.wasTouchingWater;
        next.wasUnderwater = this.wasUnderwater;
        next.wasEyeInWater = this.wasEyeInWater;
        next.isTouchingLava = this.isTouchingLava;
        next.horizontalCollision = this.horizontalCollision;
        next.verticalCollision = this.verticalCollision;
        next.onGround = this.onGround;
        next.jumping = this.jumping;
        next.sprintTriggerTime = this.sprintTriggerTime;
        next.waterVisionTime = this.waterVisionTime;
        next.noJumpDelay = this.noJumpDelay;
        next.speed = this.speed;
        next.flyingSpeed = this.flyingSpeed;
        next.xxa = this.xxa;
        next.yya = this.yya;
        next.zza = this.zza;
        next.stuckSpeedMultiplier = this.stuckSpeedMultiplier;
        next.fluidOnEyes = this.fluidOnEyes;
        next.fluidHeights = this.fluidHeights;
        next.tick_LocalPlayer();
        getNextCache = next;
        return next;
    }

    private SimulationTick getNextCache = null;
    
    private AABB getPlayerBoundingBox() {
        Vector3 aaa = playerPos.offsetX(-0.3).offsetZ(-0.3);
        // beware: entity height stored as a float !! (1.8f != 1.8)
        Vector3 bbb = playerPos.offsetX(0.3).offsetZ(0.3).offsetY(1.8f);
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

        BlockLocation blockLoc = BlockLocation.fromVector3(dimension, waterEyeCheckPos);
        BlockType blockType = world.getBlockState(blockLoc);

        if (blockType.isFluid()) {
            double e = blockLoc.getY() + blockType.getFluidHeight();
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
                    BlockType blockType = world.getBlockState(blockLoc);
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

        this.flyingSpeed = 0.02F;
        if (this.sprintingFlag) {
            this.flyingSpeed = (float)(this.flyingSpeed + 0.005999999865889549);
        }

        // lets assume we are always alive
        // im ignoring the this.touch() stuff because its only for edge cases like item pickup, pufferfish, etc.
    }

    private void aiStep_LivingEntity() {
        if (this.noJumpDelay > 0) {
            this.noJumpDelay--;
        }

        // i am super confused whether isEffectiveAi is supposed to be true or false
//        playerVelo = playerVelo.mult(0.98);

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
        serverAiStep_LocalPlayer();

        if (this.jumping) {
            double inFluidDepth;
            if (this.isTouchingLava) {
                inFluidDepth = fluidHeights.getOrDefault("lava", 0.0);
            } else {
                inFluidDepth = fluidHeights.getOrDefault("water", 0.0);
            }

            boolean actuallyInWater = this.wasTouchingWater && inFluidDepth > 0.0;
            if (!actuallyInWater || this.onGround && inFluidDepth <= 0.4) {
                if (!this.isTouchingLava || this.onGround && inFluidDepth <= 0.4) {
                    if (this.onGround && this.noJumpDelay == 0) {
                        this.jumpFromGround_LivingEntity();
                        this.noJumpDelay = 10;
                    }
                } else {
                    playerVelo = playerVelo.add(new Vector3(0.0, 0.04F, 0.0));
                }
            } else {
                playerVelo = playerVelo.add(new Vector3(0.0, 0.04F, 0.0));
            }
        } else {
            this.noJumpDelay = 0;
        }

        this.xxa *= 0.98F;
        this.zza *= 0.98F;
        travel_Player(new Vector3(this.xxa, this.yya, this.zza));

        // we assume no entity pushing happens
//        this.pushEntities_LivingEntity();

        // lets assume we dont take drowning damage
    }

    private void serverAiStep_LocalPlayer() {
        this.xxa = this.input.leftImpulse;
        this.zza = this.input.forwardImpulse;
        this.jumping = this.input.jumping;
    }

    private void travel_Player(Vector3 vec3) {
        if (this.isSwimming() && !isInBoat) {
            // dy = -Util.sin_Mth(playerPitch * Math.PI / 180.0); is equivalent
            double dy = getLookAngle_Entity().getY();
            double divingMult = dy < -0.2 ? 0.085 : 0.06;
            // im omitting " || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 0.9, this.getZ())).getFluidState().isEmpty()" because idk what it does
            if (dy <= 0.0 || this.jumping) {
                playerVelo = playerVelo.add(new Vector3(0.0, (dy - playerVelo.getY()) * divingMult, 0.0));
            }
        }

        travel_LivingEntity(vec3);
    }

    private void travel_LivingEntity(Vector3 vec3) {
        boolean goingDownwards = playerVelo.getY() <= 0.0;

        // todo fluid movement
//        FluidState fluidState = this.level.getFluidState(this.blockPosition());
        if (wasTouchingWater) {
            double e = playerPos.getY();
            float waterSpeedMult = sprintingFlag ? 0.9F : 0.8F;
            float g = 0.02F;
            float depthStriderLevel = 0.0F;  // todo
            if (depthStriderLevel > 3.0F) {
                depthStriderLevel = 3.0F;
            }

            if (!this.onGround) {
                depthStriderLevel *= 0.5F;
            }

            if (depthStriderLevel > 0.0F) {
                waterSpeedMult += (0.54600006F - waterSpeedMult) * depthStriderLevel / 3.0F;
                g += (this.speed - g) * depthStriderLevel / 3.0F;
            }

            // assume no dolphins grace
//            if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
//                waterSpeedMult = 0.96F;
//            }

            moveRelative_Entity(g, vec3);
            move_LocalPlayer(MoverType.SELF, getPlayerVelo());
            Vector3 vec32newVelo = this.getPlayerVelo();
            if (this.horizontalCollision && this.onClimbable_LivingEntity()) {
                vec32newVelo = new Vector3(vec32newVelo.getX(), 0.2, vec32newVelo.getZ());
            }

            this.setPlayerVelo(vec32newVelo.multComponentwise(new Vector3(waterSpeedMult, 0.8F, waterSpeedMult)));
            Vector3 adjustedVelo = this.getFluidFallingAdjustedMovement_LivingEntity(goingDownwards, this.getPlayerVelo());
            this.setPlayerVelo(adjustedVelo);
            if (this.horizontalCollision && this.isFree_Entity(
                adjustedVelo.getX(),
                adjustedVelo.getY() + 0.6F - this.getPlayerPos().getY() + e,
                adjustedVelo.getZ()
            )) {
                this.setPlayerVelo(new Vector3(adjustedVelo.getX(), 0.3F, adjustedVelo.getZ()));
            }
        } else if (isTouchingLava) {
            double ex = this.getPlayerPos().getY();
            this.moveRelative_Entity(0.02F, vec3);
            this.move_LocalPlayer(MoverType.SELF, getPlayerVelo());
            if (this.fluidHeights.getOrDefault("lava", 0.0) <= 0.4) {
                this.setPlayerVelo(this.getPlayerVelo().multComponentwise(new Vector3(0.5, 0.8F, 0.5)));
                Vector3 adjustedVelo = this.getFluidFallingAdjustedMovement_LivingEntity(goingDownwards, this.getPlayerVelo());
                this.setPlayerVelo(adjustedVelo);
            } else {
                this.setPlayerVelo(this.getPlayerVelo().mult(0.5));
            }

            this.setPlayerVelo(this.getPlayerVelo().add(new Vector3(0.0, -0.02, 0.0)));

            Vector3 vec34 = this.getPlayerPos();
            if (this.horizontalCollision && this.isFree_Entity(vec34.getX(), vec34.getY() + 0.6F - this.getPlayerPos().getY() + ex, vec34.getZ())) {
                this.setPlayerVelo(new Vector3(vec34.getX(), 0.3F, vec34.getZ()));
            }
//      } else if (this.isFallFlying()) {  // assume no fall flying
//          ...
        } else {
            BlockLocation blockLoc = new BlockLocation(
                getDimension(),
                Util.floor_Mth(getPlayerPos().getX()),
                Util.floor_Mth(getPlayerPos().getY() - 0.5000001),
                Util.floor_Mth(getPlayerPos().getZ())
            );
            float friction = world.getBlockState(blockLoc).getFriction();
            float frictionAndDrag = this.onGround ? friction * 0.91F : 0.91F;
            Vector3 vec37 = handleRelativeFrictionAndCalculateMovement_LivingEntity(vec3, friction);
            double newYVelo = vec37.getY();
            newYVelo -= 0.08;

            this.setPlayerVelo(new Vector3(vec37.getX() * frictionAndDrag, newYVelo * 0.98F, vec37.getZ() * frictionAndDrag));
        }
    }

    private Vector3 handleRelativeFrictionAndCalculateMovement_LivingEntity(Vector3 vec3, float friction) {
        moveRelative_Entity(this.getFrictionInfluencedSpeed_LivingEntity(friction), vec3);
        this.setPlayerVelo(this.handleOnClimbable_LivingEntity(this.getPlayerVelo()));
        this.move_LocalPlayer(MoverType.SELF, this.getPlayerVelo());
        Vector3 vec32 = this.getPlayerVelo();
        if ((this.horizontalCollision || this.jumping) && this.onClimbable_LivingEntity()) {
            vec32 = new Vector3(vec32.getX(), 0.2, vec32.getZ());
        }

        return vec32;
    }

    private Vector3 handleOnClimbable_LivingEntity(Vector3 vec3) {
        if (this.onClimbable_LivingEntity()) {
            double newX = Util.clamp_Mth(vec3.getX(), -0.15F, 0.15F);
            double newY = Math.max(vec3.getY(), -0.15F);
            double newZ = Util.clamp_Mth(vec3.getZ(), -0.15F, 0.15F);
            if (newY < 0.0 && !world.getBlockState(blockPosition).getValue().equals("scaffolding")
                && shiftKeyDownFlag) {
                newY = 0.0;
            }

            vec3 = new Vector3(newX, newY, newZ);
        }

        return vec3;
    }

    private float getFrictionInfluencedSpeed_LivingEntity(float f) {
        return this.onGround ? this.speed * (0.21600002F / (f * f * f)) : this.flyingSpeed;
    }

    private boolean isFree_Entity(double dx, double dy, double dz) {
        return this.isFree_Entity(this.getPlayerBoundingBox().move(dx, dy, dz));
    }

    private boolean isFree_Entity(AABB aabb) {
        return world.noCollision(aabb) && !world.containsAnyLiquid(aabb);
    }

    public Vector3 getFluidFallingAdjustedMovement_LivingEntity(boolean goingDown, Vector3 ogVelo) {
        if (!this.sprintingFlag) {
            return new Vector3(
                ogVelo.getX(),
                goingDown && Math.abs(ogVelo.getY() - 0.005) >= 0.003 && Math.abs(ogVelo.getY() - 0.005) < 0.003
                    ? -0.003 : ogVelo.getY() - 0.005,
                ogVelo.getZ()
            );
        } else {
            return ogVelo;
        }
    }

    private boolean onClimbable_LivingEntity() {
        BlockLocation blockPos = this.blockPosition;
        BlockType blockState = world.getBlockState(blockPosition);
        if (blockState.isClimbable()) {
            return true;
//        } else if (block instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockPos, blockState)) {
//            return true;
        } else {
            return false;
        }
    }

    private void move_LocalPlayer(MoverType moverType, Vector3 playerVelo) {
        // kind of useless since i removed the autojump relevant code but whatever
        move_Entity(moverType, playerVelo);
    }

    private void move_Entity(MoverType moverType, Vector3 moveVec) {
        // assuming no piston pushing
//        if (Objects.equals(moverType, "piston")) {
//            moveVec = this.limitPistonMovement(moveVec);
//            if (moveVec.equals(Vector3.ZERO)) {
//                return;
//            }
//        }

        if (this.stuckSpeedMultiplier.lengthSquared() > 1.0E-7) {
            moveVec = moveVec.multComponentwise(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vector3.ZERO;
            this.setPlayerVelo(Vector3.ZERO);
        }

        moveVec = maybeBackOffFromEdge_Player(moveVec, moverType);
        Vector3 moveVec2 = collide_Entity(moveVec);
        if (moveVec2.lengthSquared() > 1.0E-7) {
            // i hope nothing bad happens by me simplifying all the bounding box code ...
            setPlayerPos(getPlayerPos().add(moveVec2));
        }

        this.horizontalCollision = !Util.equal_Mth(moveVec.getX(), moveVec2.getX()) || !Util.equal_Mth(moveVec.getZ(), moveVec2.getZ());
        this.verticalCollision = moveVec.getY() != moveVec2.getY();
        this.onGround = this.verticalCollision && moveVec.getY() < 0.0;
        BlockLocation blockLoc = getOnPos_Entity();
        BlockType blockState = world.getBlockState(blockLoc);
        // assume no fall damage
//        this.checkFallDamage(moveVec2.getY(), this.onGround, blockState, blockLoc);
        Vector3 newPlayerVelo = this.getPlayerVelo();
        if (moveVec.getX() != moveVec2.getX()) {
            this.setPlayerVelo(new Vector3(0.0, newPlayerVelo.getY(), newPlayerVelo.getZ()));
        }

        if (moveVec.getZ() != moveVec2.getZ()) {
            this.setPlayerVelo(new Vector3(newPlayerVelo.getX(), newPlayerVelo.getY(), 0.0));
        }

        if (moveVec.getY() != moveVec2.getY()) {
            setPlayerVelo(getPlayerVelo().withY(0.0));
        }

        // assume no weird stuffs here (stairs, magma, slime, redstone ore)
//        if (this.onGround && !this.isSteppingCarefully()) {
//            block.stepOn(this.level, blockLoc, this);
//        }

        // todo check inside lava
        try {
            this.isTouchingLava = false;
            this.checkInsideBlocks_Entity();
        } catch (Throwable var18) {
            throw new UnsupportedOperationException("ummm touching blocks issue (?)");
        }

        float speedFactor = world.getBlockState(this.blockPosition).getBlockSpeedFactor();
        if (speedFactor == 1.0F) {
            speedFactor = world.getBlockState(new BlockLocation(
                getDimension(),
                Util.floor_Mth(getPlayerPos().getX()),
                Util.floor_Mth(getPlayerPos().getY() - 0.5000001),
                Util.floor_Mth(getPlayerPos().getZ())
            )).getBlockSpeedFactor();
        }
        this.setPlayerVelo(this.getPlayerVelo().multComponentwise(new Vector3(speedFactor, 1.0, speedFactor)));

        // assume no fire i guess
//        if (world.getBlockStatesIfLoaded(getPlayerBoundingBox().deflate(0.001))
//            .noneMatch(blockType -> blockType.getValue().equals("fire") || blockType.getValue().equals("lava"))
//            && this.remainingFireTicks <= 0) {
//            this.setRemainingFireTicks(-this.getFireImmuneTicks());
//        }

        // assume no fire
//        if (this.isInWaterRainOrBubble() && this.isOnFire()) {
//            this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
//            this.setRemainingFireTicks(-this.getFireImmuneTicks());
//        }
    }

    private void checkInsideBlocks_Entity() {
        AABB aABB = getPlayerBoundingBox();
        BlockLocation blockLoc = new BlockLocation(
            getDimension(),
            Util.floor_Mth(aABB.getMinX() + 0.001),
            Util.floor_Mth(aABB.getMinY() + 0.001),
            Util.floor_Mth(aABB.getMinZ() + 0.001)
        );
        BlockLocation blockLoc2 = new BlockLocation(
            getDimension(),
            Util.floor_Mth(aABB.getMaxX() - 0.001),
            Util.floor_Mth(aABB.getMaxY() - 0.001),
            Util.floor_Mth(aABB.getMaxZ() - 0.001)
        );
        for (long i = blockLoc.getX(); i <= blockLoc2.getX(); i++) {
            for (long j = blockLoc.getY(); j <= blockLoc2.getY(); j++) {
                for (long k = blockLoc.getZ(); k <= blockLoc2.getZ(); k++) {
                    BlockLocation collideLocation = new BlockLocation(getDimension(), i, j, k);
                    BlockType blockType = this.world.getBlockState(collideLocation);

                    try {
                        if (blockType.getValue().equals("lava")) {
                            float f = collideLocation.getY() + blockType.getFluidHeight();
                            if (aABB.getMinY() < f || f > aABB.getMaxY()) {
                                isTouchingLava = true;
                            }
                        }
                    } catch (Throwable var12) {
                        throw new UnsupportedOperationException("ummm entity inside block issue (?)");
                    }
                }
            }
        }
    }

    private BlockLocation getOnPos_Entity() {
        int i = Util.floor_Mth(getPlayerPos().getX());
        int j = Util.floor_Mth(getPlayerPos().getY() - 0.2F);
        int k = Util.floor_Mth(getPlayerPos().getZ());
        BlockLocation blockLoc = new BlockLocation(getDimension(), i, j, k);
        if (!world.getBlockState(blockLoc).isSolid()) {  // originally it is .isAir() but idk if that means nonsolids or just air
            BlockType blockState = world.getBlockState(blockLoc.below());
            // what are the chances we walk on top of walls/fences ... :clueless:
            // todo do this
//            Block block = blockState.getBlock();
//            if (block.is(BlockTags.FENCES) || block.is(BlockTags.WALLS) || block instanceof FenceGateBlock) {
//                return blockLoc.below();
//            }
        }
        return blockLoc;
    }

    // todo
    private Vector3 collide_Entity(Vector3 moveVec) {
        AABB playerAABB = getPlayerBoundingBox();
        // assume world border is irrelevant
//        VoxelShape voxelShape = this.level.getWorldBorder().getCollisionShape();
        Vector3 vec32 = moveVec.lengthSquared() == 0.0
            ? moveVec : collideBoundingBoxHeuristically_Entity(moveVec, playerAABB);
        boolean bl = moveVec.getX() != vec32.getX();
        boolean bl2 = moveVec.getY() != vec32.getY();
        boolean bl3 = moveVec.getZ() != vec32.getZ();
        boolean bl4 = this.onGround || bl2 && moveVec.getY() < 0.0;
        if (bl4 && (bl || bl3)) {
            Vector3 vec33 = collideBoundingBoxHeuristically_Entity(new Vector3(moveVec.getX(), this.maxUpStep, moveVec.getZ()), playerAABB);
            Vector3 vec34 = collideBoundingBoxHeuristically_Entity(
                new Vector3(0.0, this.maxUpStep, 0.0), playerAABB.expandTowards(moveVec.getX(), 0.0, moveVec.getZ())
            );
            if (vec34.getY() < this.maxUpStep) {
                Vector3 vec35 = collideBoundingBoxHeuristically_Entity(new Vector3(moveVec.getX(), 0.0, moveVec.getZ()), playerAABB.move(vec34))
                    .add(vec34);
                if (vec35.getX()*vec35.getX()+vec35.getZ()+vec35.getZ() > vec33.getX()*vec33.getX()+vec33.getZ()*vec33.getZ()) {
                    vec33 = vec35;
                }
            }

            if (vec33.getX()*vec33.getX()+vec33.getZ()*vec33.getZ() > vec32.getX()*vec32.getX()+vec32.getZ()*vec32.getZ()) {
                return vec33.add(
                    collideBoundingBoxHeuristically_Entity(new Vector3(0.0, -vec33.getY() + moveVec.getY(), 0.0), playerAABB.move(vec33))
                );
            }
        }

        return vec32;
    }

    private Vector3 collideBoundingBoxHeuristically_Entity(
        Vector3 delta, AABB aabb
    ) {
        boolean dxZero = delta.getX() == 0.0;
        boolean dyZero = delta.getY() == 0.0;
        boolean dzZero = delta.getZ() == 0.0;
        if ((!dxZero || !dyZero) && (!dxZero || !dzZero) && (!dyZero || !dzZero)) {
            // legacy path if 2+ directions are nonzero
            return collideBoundingBoxLegacy_Entity(delta, aabb);
        } else {
            // new path if 0-1 directions are nonzero
            return collideBoundingBox_Entity(delta, aabb);
        }
    }

    private Vector3 collideBoundingBoxLegacy_Entity(Vector3 delta, AABB aabb) {
        double dx = delta.getX();
        double dy = delta.getY();
        double dz = delta.getZ();
        if (dy != 0.0) {
            dy = VoxelShape_collide(Direction.Axis.Y, aabb, dy);
            if (dy != 0.0) {
                aabb = aabb.move(0.0, dy, 0.0);
            }
        }

        boolean zFirst = Math.abs(dx) < Math.abs(dz);
        if (zFirst && dz != 0.0) {
            dz = VoxelShape_collide(Direction.Axis.Z, aabb, dz);
            if (dz != 0.0) {
                aabb = aabb.move(0.0, 0.0, dz);
            }
        }

        if (dx != 0.0) {
            dx = VoxelShape_collide(Direction.Axis.X, aabb, dx);
            if (!zFirst && dx != 0.0) {
                aabb = aabb.move(dx, 0.0, 0.0);
            }
        }

        if (!zFirst && dz != 0.0) {
            dz = VoxelShape_collide(Direction.Axis.Z, aabb, dz);
        }

        return new Vector3(dx, dy, dz);
    }

    // not perfectly accurate (copy pasted method body from Shapes_collide)
    private double VoxelShape_collide(Direction.Axis axis, AABB aabb, double d) {
        if (aabb.getXSize() < 1.0E-6 || aabb.getYSize() < 1.0E-6 || aabb.getZSize() < 1.0E-6) {
            return d;
        } else if (Math.abs(d) < 1.0E-7) {
            return 0.0;
        } else {
            int minPerp1 = Util.floor_Mth(aabb.min(axis.perp1()) - 1.0E-7) - 1;
            int maxPerp1 = Util.floor_Mth(aabb.max(axis.perp1()) + 1.0E-7) + 1;
            int minPerp2 = Util.floor_Mth(aabb.min(axis.perp2()) - 1.0E-7) - 1;
            int maxPerp2 = Util.floor_Mth(aabb.max(axis.perp2()) + 1.0E-7) + 1;
            double minMain1 = aabb.min(axis) - 1.0E-7;
            double maxMain1 = aabb.max(axis) + 1.0E-7;
            int start = d > 0.0 ? Util.floor_Mth(aabb.max(axis) - 1.0E-7) - 1 : Util.floor_Mth(aabb.min(axis) + 1.0E-7) + 1;
            int end = d > 0.0 ? Util.floor_Mth(maxMain1 + d) + 1 : Util.floor_Mth(minMain1 + d) - 1;
            int inc = d > 0.0 ? 1 : -1;
            for (long iMain = start; d > 0.0 ? iMain <= end : iMain >= end; iMain += inc) {
                for (long iPerp1 = minPerp1; iPerp1 <= maxPerp1; iPerp1++) {
                    for (long iPerp2 = minPerp2; iPerp2 <= maxPerp2; iPerp2++) {
                        int s = 0;
                        if (iPerp1 == minPerp1 || iPerp1 == maxPerp1) {
                            s++;
                        }

                        if (iPerp2 == minPerp2 || iPerp2 == maxPerp2) {
                            s++;
                        }

                        if (iMain == start || iMain == end) {
                            s++;
                        }

                        if (s < 3) {
                            BlockLocation bl = Direction.Axis.makeBL(axis, getDimension(), iMain, iPerp1, iPerp2);
                            BlockType blockType = world.getBlockState(bl);
                            // todo fix when fences or pistons maybe
//                            if ((s != 1 || blockType.hasLargeCollisionShape()) && (s != 2 || blockType.is(Blocks.MOVING_PISTON))) {
                            if ((s != 1) && (s != 2)) {
                                for (AABB aabbOther : blockType.getCompositeCollisionBoxes(bl)) {
                                    d = aabbOther.distanceUntilCollision(axis, aabb, d);
                                    if (Math.abs(d) < 1.0E-7) {
                                        return 0.0;
                                    }
                                }

                                end = d > 0.0 ? Util.floor_Mth(maxMain1 + d) + 1 : Util.floor_Mth(minMain1 + d) - 1;
                            }
                        }
                    }
                }
            }

            return d;
        }
    }

    private Vector3 collideBoundingBox_Entity(Vector3 delta, AABB aabb) {
        double dx = delta.getX();
        double dy = delta.getY();
        double dz = delta.getZ();
        if (dy != 0.0) {
            dy = Shapes_collide(Direction.Axis.Y, aabb, dy);
            if (dy != 0.0) {
                aabb = aabb.move(0.0, dy, 0.0);
            }
        }

        boolean bl = Math.abs(dx) < Math.abs(dz);
        if (bl && dz != 0.0) {
            dz = Shapes_collide(Direction.Axis.Z, aabb, dz);
            if (dz != 0.0) {
                aabb = aabb.move(0.0, 0.0, dz);
            }
        }

        if (dx != 0.0) {
            dx = Shapes_collide(Direction.Axis.X, aabb, dx);
            if (!bl && dx != 0.0) {
                aabb = aabb.move(dx, 0.0, 0.0);
            }
        }

        if (!bl && dz != 0.0) {
            dz = Shapes_collide(Direction.Axis.Z, aabb, dz);
        }

        return new Vector3(dx, dy, dz);
    }

    // not perfectly accurate
    private double Shapes_collide(Direction.Axis axis, AABB aabb, double d) {
        if (aabb.getXSize() < 1.0E-6 || aabb.getYSize() < 1.0E-6 || aabb.getZSize() < 1.0E-6) {
            return d;
        } else if (Math.abs(d) < 1.0E-7) {
            return 0.0;
        } else {
            int minPerp1 = Util.floor_Mth(aabb.min(axis.perp1()) - 1.0E-7) - 1;
            int maxPerp1 = Util.floor_Mth(aabb.max(axis.perp1()) + 1.0E-7) + 1;
            int minPerp2 = Util.floor_Mth(aabb.min(axis.perp2()) - 1.0E-7) - 1;
            int maxPerp2 = Util.floor_Mth(aabb.max(axis.perp2()) + 1.0E-7) + 1;
            double minMain1 = aabb.min(axis) - 1.0E-7;
            double maxMain1 = aabb.max(axis) + 1.0E-7;
            int start = d > 0.0 ? Util.floor_Mth(aabb.max(axis) - 1.0E-7) - 1 : Util.floor_Mth(aabb.min(axis) + 1.0E-7) + 1;
            int end = d > 0.0 ? Util.floor_Mth(maxMain1 + d) + 1 : Util.floor_Mth(minMain1 + d) - 1;
            int inc = d > 0.0 ? 1 : -1;
            for (long iMain = start; d > 0.0 ? iMain <= end : iMain >= end; iMain += inc) {
                for (long iPerp1 = minPerp1; iPerp1 <= maxPerp1; iPerp1++) {
                    for (long iPerp2 = minPerp2; iPerp2 <= maxPerp2; iPerp2++) {
                        int s = 0;
                        if (iPerp1 == minPerp1 || iPerp1 == maxPerp1) {
                            s++;
                        }

                        if (iPerp2 == minPerp2 || iPerp2 == maxPerp2) {
                            s++;
                        }

                        if (iMain == start || iMain == end) {
                            s++;
                        }

                        if (s < 3) {
                            BlockLocation bl = Direction.Axis.makeBL(axis, getDimension(), iMain, iPerp1, iPerp2);
                            BlockType blockType = world.getBlockState(bl);
                            // todo fix when fences or pistons maybe
//                            if ((s != 1 || blockType.hasLargeCollisionShape()) && (s != 2 || blockType.is(Blocks.MOVING_PISTON))) {
                            if ((s != 1) && (s != 2)) {
                                for (AABB aabbOther : blockType.getCompositeCollisionBoxes(bl)) {
                                    d = aabbOther.distanceUntilCollision(axis, aabb, d);
                                    if (Math.abs(d) < 1.0E-7) {
                                        return 0.0;
                                    }
                                }

                                end = d > 0.0 ? Util.floor_Mth(maxMain1 + d) + 1 : Util.floor_Mth(minMain1 + d) - 1;
                            }
                        }
                    }
                }
            }

            return d;
        }
    }

    private Vector3 maybeBackOffFromEdge_Player(Vector3 moveVec, MoverType moverType) {
        if ((moverType == MoverType.SELF || moverType == MoverType.PLAYER) && this.onGround && this.shiftKeyDownFlag) {
            double moveX = moveVec.getX();
            double moveZ = moveVec.getZ();

            // todo actually impl collision stuff to prevent shift sliding off or something
//            while (moveX != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(moveX, -this.maxUpStep, 0.0))) {
//                if (moveX < 0.05 && moveX >= -0.05) {
//                    moveX = 0.0;
//                } else if (moveX > 0.0) {
//                    moveX -= 0.05;
//                } else {
//                    moveX += 0.05;
//                }
//            }
//
//            while (moveZ != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(0.0, -this.maxUpStep, moveZ))) {
//                if (moveZ < 0.05 && moveZ >= -0.05) {
//                    moveZ = 0.0;
//                } else if (moveZ > 0.0) {
//                    moveZ -= 0.05;
//                } else {
//                    moveZ += 0.05;
//                }
//            }
//
//            while (moveX != 0.0 && moveZ != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(moveX, -this.maxUpStep, moveZ))) {
//                if (moveX < 0.05 && moveX >= -0.05) {
//                    moveX = 0.0;
//                } else if (moveX > 0.0) {
//                    moveX -= 0.05;
//                } else {
//                    moveX += 0.05;
//                }
//
//                if (moveZ < 0.05 && moveZ >= -0.05) {
//                    moveZ = 0.0;
//                } else if (moveZ > 0.0) {
//                    moveZ -= 0.05;
//                } else {
//                    moveZ += 0.05;
//                }
//            }

            moveVec = new Vector3(moveX, moveVec.getY(), moveZ);
        }

        return moveVec;
    }

    private void moveRelative_Entity(float f, Vector3 vec3) {
        Vector3 inputVector = getInputVector_Entity(vec3, f, playerYaw);
        setPlayerVelo(getPlayerVelo().add(inputVector));
    }

    private Vector3 getInputVector_Entity(Vector3 vec3, float f, float yawAngle) {
        double d = vec3.lengthSquared();
		if (d < 1.0E-7) {
			return Vector3.ZERO;
		} else {
			Vector3 adjustedVector = (d > 1.0 ? vec3.normalized() : vec3).mult(f);
			float h = Util.sin_Mth(yawAngle * (float) (Math.PI / 180.0));
			float i = Util.cos_Mth(yawAngle * (float) (Math.PI / 180.0));
			return new Vector3(adjustedVector.getX() * i - adjustedVector.getZ() * h, adjustedVector.getY(), adjustedVector.getZ() * i + adjustedVector.getX() * h);
		}
    }

    private Vector3 getLookAngle_Entity() {
        float pitchRad = playerPitch * (float) (Math.PI / 180.0);
        float yawRad = -playerYaw * (float) (Math.PI / 180.0);
        return new Vector3(
            Util.sin_Mth(yawRad) * Util.cos_Mth(pitchRad),
            -Util.sin_Mth(pitchRad), Util.cos_Mth(yawRad) * Util.cos_Mth(pitchRad)
        );
    }

    private void jumpFromGround_LivingEntity() {
        float newYVelo = this.getJumpPower_LivingEntity();
        if (JUMP_BOOST_LVL != 0) {
            newYVelo += 0.1F * JUMP_BOOST_LVL;
        }

        this.setPlayerVelo(this.getPlayerVelo().withY(newYVelo));
        if (this.sprintingFlag) {
            float g = this.playerYaw * (float) (Math.PI / 180.0);
            this.setPlayerVelo(this.getPlayerVelo().add(new Vector3(-Util.sin_Mth(g) * 0.2F, 0.0, Util.cos_Mth(g) * 0.2F)));
        }
    }

    private float getJumpPower_LivingEntity() {
        return 0.42F * getBlockJumpFactor_Entity();
    }

    private float getBlockJumpFactor_Entity() {
        float f = world.getBlockState(this.blockPosition).getJumpFactor();
        Vector3 playerPos = getPlayerPos();
        BlockLocation blockLocBelow = new BlockLocation(
            getDimension(),
            Util.floor_Mth(playerPos.getX()),
            Util.floor_Mth(playerPos.getY() - 0.5000001),
            Util.floor_Mth(playerPos.getZ())
        );
        float g = world.getBlockState(blockLocBelow).getJumpFactor();
        return f == 1.0 ? g : f;
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
        BlockLocation blockLoc = new BlockLocation(getDimension(), Util.floor_Mth(x), Util.floor_Mth(y), Util.floor_Mth(z));
        if (this.blocked_LocalPlayer(blockLoc)) {
            double x2 = x - blockLoc.getX();
            double z2 = z - blockLoc.getZ();
            Direction direction = null;
            double i = 9999.0;
            if (!this.blocked_LocalPlayer(blockLoc.offsetX(-1)) && x2 < i) {
                i = x2;
                direction = Direction.WEST;
            }

            if (!this.blocked_LocalPlayer(blockLoc.offsetX(1)) && 1.0 - x2 < i) {
                i = 1.0 - x2;
                direction = Direction.EAST;
            }

            if (!this.blocked_LocalPlayer(blockLoc.offsetZ(-1)) && z2 < i) {
                i = z2;
                direction = Direction.NORTH;
            }

            if (!this.blocked_LocalPlayer(blockLoc.offsetZ(1)) && 1.0 - z2 < i) {
                i = 1.0 - z2;
                direction = Direction.SOUTH;
            }

            if (direction != null) {
                Vector3 velo = this.getPlayerVelo();
                switch (direction) {
                    case WEST:
                        this.setPlayerVelo(new Vector3(-0.1, velo.getY(), velo.getZ()));
                        break;
                    case EAST:
                        this.setPlayerVelo(new Vector3(0.1, velo.getY(), velo.getZ()));
                        break;
                    case NORTH:
                        this.setPlayerVelo(new Vector3(velo.getX(), velo.getY(), -0.1));
                        break;
                    case SOUTH:
                        this.setPlayerVelo(new Vector3(velo.getX(), velo.getY(), 0.1));
                }
            }
        }
    }

    private boolean blocked_LocalPlayer(BlockLocation blockLoc) {
        AABB playerAABB = this.getPlayerBoundingBox();

        for (int i = Util.floor_Mth(playerAABB.getMinY()); i < Util.ceil_Mth(playerAABB.getMaxY()); i++) {
            BlockLocation newBL = new BlockLocation(
                getDimension(),
                blockLoc.getX(),
                i,
                blockLoc.getZ()
            );
            if (!this.freeAt_Player(newBL)) {
                return true;
            }
        }

        return false;
    }

    private boolean freeAt_Player(BlockLocation blockLoc) {
        return !this.world.getBlockState(blockLoc).isSuffocating();
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

    /** in degrees */
    public float getPlayerYaw() {
        return playerYaw;
    }

    /** in degrees */
    public float getPlayerPitch() {
        return playerPitch;
    }

    public SimulationTick setPlayerPos(Vector3 playerPos) {
        this.playerPos = playerPos;
        this.blockPosition = new BlockLocation(getDimension(), Util.floor_Mth(playerPos.getX()), Util.floor_Mth(playerPos.getY()), Util.floor_Mth(playerPos.getZ()));
        return this;
    }

    public SimulationTick setPlayerVelo(Vector3 playerVelo) {
        this.playerVelo = playerVelo;
        return this;
    }

    /** in degrees */
    public SimulationTick setPlayerYaw(float playerYaw) {
        this.playerYaw = playerYaw;
        return this;
    }

    /** in degrees */
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
}
