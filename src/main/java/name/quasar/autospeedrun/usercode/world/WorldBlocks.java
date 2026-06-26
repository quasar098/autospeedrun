package name.quasar.autospeedrun.usercode.world;

import com.mojang.math.Vector3f;
import name.quasar.autospeedrun.AutoSpeedrunAPI;
import name.quasar.autospeedrun.debug.DebugWorldLine;
import name.quasar.autospeedrun.usercode.F3Information;
import name.quasar.autospeedrun.usercode.Util;
import name.quasar.autospeedrun.usercode.geometry.BlockFace;
import name.quasar.autospeedrun.usercode.geometry.BlockLocation;
import name.quasar.autospeedrun.usercode.geometry.DirectedBlockFace;
import name.quasar.autospeedrun.usercode.geometry.Vector3;
import name.quasar.autospeedrun.usercode.pathing.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * collect information about all the blocks seen in the world
 * todo integrate some ml into predicting blocks given nearby blocks
 */
public class WorldBlocks {
    private static WorldBlocks instance = null;

    public static WorldBlocks getInstance() {
        if (instance == null) {
            instance = new WorldBlocks();
        }
        return instance;
    }

    public WorldBlocks() {
        // idk nothing
    }

    public HashMap<BlockLocation, WorldBlock> knownBlocks = new HashMap<>();

    /**
     * write to knownBlocks if the probability of knowledge is greater or equal to
     */
    public void apply(BlockLocation bl, WorldBlock wb) {
        if (!knownBlocks.containsKey(bl) || knownBlocks.get(bl).getProb() <= wb.getProb()) {
            knownBlocks.put(bl, wb);
        }
    }

    public void applyForcefully(BlockLocation bl, WorldBlock wb) {
        knownBlocks.put(bl, wb);
    }

    public WorldBlock get(BlockLocation bl) {
        return knownBlocks.getOrDefault(bl, null);
    }

    public boolean isKnown(BlockLocation bl) {
        return knownBlocks.containsKey(bl);
    }

    public boolean isAirOrUnknown(BlockLocation bl) {
        return !knownBlocks.containsKey(bl) || knownBlocks.get(bl).getBlockType().equals(BlockType.AIR);
    }

    public boolean isAir(BlockLocation bl) {
        return knownBlocks.get(bl).getBlockType().equals(BlockType.AIR);
    }

    public boolean isNonsolidOrUnknown(BlockLocation bl) {
        return !knownBlocks.containsKey(bl) || !knownBlocks.get(bl).getBlockType().isSolid();
    }

    public boolean isNonsolidKnown(BlockLocation bl) {
        return knownBlocks.containsKey(bl) && !knownBlocks.get(bl).getBlockType().isSolid();
    }

    public boolean isSolidKnown(BlockLocation bl) {
        return knownBlocks.containsKey(bl) && knownBlocks.get(bl).getBlockType().isSolid();
    }

    public static void reset() {
        instance = null;
    }

    public ArrayList<BlockFace> rayCollisionDetection(double yaw, double pitch, double px, double py, double pz,
                                                      BlockLocation targetted) {
        double opx = px;
        double opy = py;
        double opz = pz;
        double dx = Math.cos(yaw + Math.PI/2)*Math.cos(-pitch);
        double dy = Math.sin(-pitch);
        double dz = Math.sin(yaw + Math.PI/2)*Math.cos(-pitch);
        double maxIterations = 200;
        int iterations = 0;
        boolean hit = false;
        ArrayList<BlockFace> bfsTotal = new ArrayList<>();
        while (iterations++ < maxIterations) {
            double sx = dx > 0 ? Math.floor(px)+1 : Math.ceil(px)-1;
            double sy = dy > 0 ? Math.floor(py)+1 : Math.ceil(py)-1;
            double sz = dz > 0 ? Math.floor(pz)+1 : Math.ceil(pz)-1;
            double xt = 99999;
            double yt = 99999;
            double zt = 99999;
            if (dx != 0) { xt = (sx - px) / dx; }
            if (dy != 0) { yt = (sy - py) / dy; }
            if (dz != 0) { zt = (sz - pz) / dz; }
            double mintime = Math.min(Math.min(xt, yt), zt);
            px += dx * mintime;
            py += dy * mintime;
            pz += dz * mintime;
            // 20 is distance that eye ray max is, 20 squared is 400
            if ((px-opx)*(px-opx)+(py-opy)*(py-opy)+(pz-opz)*(pz-opz) >= 400) { break; }
            ArrayList<BlockFace> newBFs = new ArrayList<>();
            if (mintime == xt) {
                newBFs.add(new BlockFace(
                        (int) (Math.round(px) - 1), (int) Math.floor(py), (int) Math.floor(pz),
                        BlockFace.Direction.POS_X
                ));
                px = Math.round(px);
            }
            if (mintime == yt) {
                newBFs.add(new BlockFace(
                        (int) Math.floor(px), (int) (Math.round(py) - 1), (int) Math.floor(pz),
                        BlockFace.Direction.POS_Y
                ));
                py = Math.round(py);
            }
            if (mintime == zt) {
                newBFs.add(new BlockFace(
                        (int) Math.floor(px), (int) Math.floor(py), (int) (Math.round(pz) - 1),
                        BlockFace.Direction.POS_Z
                ));
                pz = Math.round(pz);
            }
            for (BlockFace newBF : newBFs) {
                if (newBF.getAdjacentA(F3Information.getDimension()).equals(targetted)) {
                    hit = true;
                    break;
                }
                if (newBF.getAdjacentB(F3Information.getDimension()).equals(targetted)) {
                    hit = true;
                    break;
                }
                bfsTotal.add(newBF);
            }
            if (hit) {
                break;
            }
        }
        if (iterations == maxIterations) {
            AutoSpeedrunAPI.chatMessage("Max iterations reached for eye ray collision detection");
            AutoSpeedrunAPI.emergencyStopUserCode();
            return null;
        }
        return bfsTotal;
    }

    private Double prevTickYaw = null;
    private Double prevTickPitch = null;

    public void collectFacingBlockInformation() {
        // targetted block
        BlockLocation targetted = F3Information.getTargettedBlockLocation();
        if (targetted != null) {
            BlockType bt = new BlockType(
                F3Information.getTargettedBlockName(), F3Information.getBlockProperties()
            );
            // we see it clearly from f3 menu so we know 100% its there
            apply(targetted, new WorldBlock(bt, 1.0));
            Integer solidBlockOffset = bt.solidBlockYOffset();
            if (solidBlockOffset != null) {
                apply(targetted.offsetY(solidBlockOffset), new WorldBlock(BlockType.UNKNOWN_SOLID, 0.8));
            }
        }
        if (prevTickYaw == null || prevTickPitch == null) {
            prevTickYaw = F3Information.getYaw();
            prevTickPitch = F3Information.getPitch();
        }
        // use angles to find all air in the way, 3D DDA algorithm that finds all intersected faces
        // todo change 0.05 for more precision. idk what to do about manual mouse movement during debugging tho
        double yawTL = Math.toRadians(F3Information.getYaw()-0.05);
        double pitchTL = Math.toRadians(F3Information.getPitch()-0.05);
        double yawBR = Math.toRadians(F3Information.getYaw()+0.05);
        double pitchBR = Math.toRadians(F3Information.getPitch()+0.05);
        Vector3 prevTickPlayerPos = Navigation.getInstance().prevPosition();
        if (prevTickPlayerPos == null) { prevTickPlayerPos = F3Information.getPosition(); }
        double px = prevTickPlayerPos.getX();
        double py = prevTickPlayerPos.getY() + Util.getEyeOffset();
        double pz = prevTickPlayerPos.getZ();
        ArrayList<BlockFace> bfsMaxTL = rayCollisionDetection(yawTL, pitchTL, px, py, pz, targetted);
        ArrayList<BlockFace> bfsMaxBR = rayCollisionDetection(yawBR, pitchBR, px, py, pz, targetted);
        // check for discrepancies to make sure we don't make errors due to slightly bad angles
        for (int i = 0; i < Math.min(bfsMaxTL.size(), bfsMaxBR.size()); i++) {
            BlockFace faceTL = bfsMaxTL.get(i);
            BlockFace faceBR = bfsMaxBR.get(i);
            if (!faceTL.equals(faceBR)) {
//                AutoSpeedrunAPI.chatMessage(String.format("BAD %.2fs %d", Util.tickCount / 20.0, i));
                faceTL.debugDraw();
                faceBR.debugDraw();
//                for (BlockFace f : bfsMaxTL) {
//                    f.debugDraw(0.04f, 0.9f, 0.1f, 0.5f);
//                    System.out.println("TL:" + f);
//                }
//                for (BlockFace f : bfsMaxBR) {
//                    f.debugDraw(0.02f, 0.5f, 0.9f, 0.3f);
//                    System.out.println("BR:" + f);
//                }
                return;
            }
            BlockFace mutualFace = bfsMaxTL.get(i);
            BlockLocation blockA = mutualFace.getAdjacentA(F3Information.getDimension());
            BlockLocation blockB = mutualFace.getAdjacentB(F3Information.getDimension());
//            if (knownBlocks.containsKey(blockA) && !knownBlocks.get(blockA).getValue().equals("air")) {
//                double dx1 = Math.cos(yawTL + Math.PI/2)*Math.cos(-pitchTL);
//                double dy1 = Math.sin(-pitchTL);
//                double dz1 = Math.sin(yawTL + Math.PI/2)*Math.cos(-pitchTL);
//                double dx2 = Math.cos(yawBR + Math.PI/2)*Math.cos(-pitchBR);
//                double dy2 = Math.sin(-pitchBR);
//                double dz2 = Math.sin(yawBR + Math.PI/2)*Math.cos(-pitchBR);
//                AutoSpeedrunApi.renderLine(new DebugRenderLine(
//                        new Vector3(px, py, pz).toVector3f(),
//                        new Vector3(px, py, pz).add(new Vector3(dx1, dy1, dz1).mult(20.0)).toVector3f(),
//                        1.0f, 1.0f, 0.0f
//                ));
//                AutoSpeedrunApi.renderLine(new DebugRenderLine(
//                        new Vector3(px, py, pz).toVector3f(),
//                        new Vector3(px, py, pz).add(new Vector3(dx2, dy2, dz2).mult(20.0)).toVector3f(),
//                        1.0f, 1.0f, 0.2f
//                ));
//                AutoSpeedrunApi.emergencyStopUserCode();
//                AutoSpeedrunApi.chatMessage("WTF " + targetted);
//                return;
//            }
            WorldBlocks.getInstance().apply(blockA, new WorldBlock(BlockType.AIR, 1.0));
            WorldBlocks.getInstance().apply(blockB, new WorldBlock(BlockType.AIR, 1.0));
        }
//        AutoSpeedrunApi.chatMessage("Success writing " + Math.min(bfsMaxTL.size(), bfsMaxBR.size()));
        prevTickYaw = F3Information.getYaw();
        prevTickPitch = F3Information.getPitch();
    }

    /**
     * returns null if there is no visible block face from where the camera point is
     */
    public DirectedBlockFace getVisibleFace(BlockLocation desired) {
        // todo predict next tick (eye) position and use instead of current tick eye position !!
        Vector3 camera = Util.getEyePosition();
        DirectedBlockFace[] dbfaces = desired.getDirectedFaces();
        for (DirectedBlockFace dbface : dbfaces) {
            if (dbface.getNormal().dot((dbface.getCenter().sub(camera)).normalized()) > -0.04) {
                continue;
            }
            double[] yawAndPitch = (dbface.getCenter().sub(camera)).toYawAndPitchRadians();
            ArrayList<BlockFace> detect = rayCollisionDetection(yawAndPitch[0], yawAndPitch[1],
                camera.getX(), camera.getY(), camera.getZ(), desired);
            boolean blockInTheWay = false;
            for (BlockFace bf : detect) {
                BlockLocation blockA = bf.getAdjacentA(F3Information.getDimension());
                BlockLocation blockB = bf.getAdjacentB(F3Information.getDimension());
                if (!blockA.equals(desired) && !WorldBlocks.getInstance().isAirOrUnknown(blockA)) {
                    blockInTheWay = true;
                    break;
                }
                if (!blockB.equals(desired) && !WorldBlocks.getInstance().isAirOrUnknown(blockB)) {
                    blockInTheWay = true;
                    break;
                }
            }
            if (!blockInTheWay) {
                return dbface;
            }
        }
        return null;
    }

    public void debugDraw() {
        for (Map.Entry<BlockLocation, WorldBlock> entry : knownBlocks.entrySet()) {
            BlockLocation loc = entry.getKey();
            if (loc.getDimension() != F3Information.getDimension()) {
                continue;
            }
            BlockType blockType = entry.getValue().getBlockType();
            if (blockType.getValue().equals("air")) {
                // draw black X through air blocks
                if (Util.toggleDebugAir) {
                    AutoSpeedrunAPI.render(new DebugWorldLine(
                            new Vector3f(loc.getX() + 0.4f, loc.getY() + 0.5f, loc.getZ() + 0.4f),
                            new Vector3f(loc.getX() + 0.6f, loc.getY() + 0.5f, loc.getZ() + 0.6f),
                            0f, 0f, 0f
                    ));
                    AutoSpeedrunAPI.render(new DebugWorldLine(
                            new Vector3f(loc.getX() + 0.6f, loc.getY() + 0.5f, loc.getZ() + 0.4f),
                            new Vector3f(loc.getX() + 0.4f, loc.getY() + 0.5f, loc.getZ() + 0.6f),
                            0f, 0f, 0f
                    ));
                }
            } else {
                if (blockType.isSolid()) {
                    // draw green / on top of all known solid blocks
                    AutoSpeedrunAPI.render(new DebugWorldLine(
                        new Vector3f(loc.getX(), loc.getY() + 1.02f, loc.getZ()),
                        new Vector3f(loc.getX() + 1f, loc.getY() + 1.02f, loc.getZ() + 1f),
                        0f, 1f, 0f
                    ));
                } else {
                    // draw pink / on top of all known nonsolid blocks
                    AutoSpeedrunAPI.render(new DebugWorldLine(
                        new Vector3f(loc.getX(), loc.getY() + 1.02f, loc.getZ()),
                        new Vector3f(loc.getX() + 1f, loc.getY() + 1.02f, loc.getZ() + 1f),
                        1f, 0f, 1f
                    ));
                }
            }
        }
    }
}
