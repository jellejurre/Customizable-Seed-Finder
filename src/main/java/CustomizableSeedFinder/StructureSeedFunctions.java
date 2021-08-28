package CustomizableSeedFinder;

import static CustomizableSeedFinder.Config.entryDistance;
import static CustomizableSeedFinder.Config.entryMethod;
import static CustomizableSeedFinder.Config.fortress;
import static CustomizableSeedFinder.Config.needStongholdDistance;
import static CustomizableSeedFinder.Config.portal;
import static CustomizableSeedFinder.Config.treasure;
import static CustomizableSeedFinder.Util.Util.isArrayEmpty;
import static CustomizableSeedFinder.Util.Util.isArrayFalse;


import CustomizableSeedFinder.Entities.StructureSeedInfo;
import CustomizableSeedFinder.Util.BastionFeatures;
import CustomizableSeedFinder.Util.SpiralIterator;
import java.util.Arrays;
import java.util.Iterator;
import kaptainwutax.featureutils.loot.LootContext;
import kaptainwutax.featureutils.loot.MCLootTables;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.generator.structure.BuriedTreasureGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

public class StructureSeedFunctions {

    private static BPos[] findNearest(long structureSeed, BPos[] positions,
                                      RegionStructure structure, ChunkRand chunkRand,
                                      long maxDistance) {
        double[] smallestSquares =
            new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        BPos[] outputlist = new BPos[] {null, null, null, null};

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                CPos structurePosition = structure.getInRegion(structureSeed, x, z, chunkRand);
                if (structurePosition == null) {
                    continue;
                }
                for (int i = 0; i < 4; i++) {
                    BPos position = positions[i];
                    if (position == null) {
                        continue;
                    }
                    double distance = structurePosition.toBlockPos()
                        .distanceTo(position, DistanceMetric.EUCLIDEAN_SQ);
                    if (distance < smallestSquares[i] && distance < maxDistance * maxDistance) {
                        smallestSquares[i] = distance;
                        outputlist[i] = structurePosition.toBlockPos();
                    }
                }
            }
        }
        return outputlist;
    }

    public static void generateIronSpawns(StructureSeedInfo seedInfo) {
        BPos[] output = new BPos[4];
        int i = 0;
        switch(Config.ironMethod) {
            case NONE:
                for (int j = 0; j < 4; j++) {
                    output[j] = new BPos(0, 0, 0);
                }
                break;
            case MAPLESS:
                MCLootTables.BURIED_TREASURE_CHEST.apply(Config.version);
                Iterator<CPos> iterator = new SpiralIterator<CPos>(new CPos(0, 0),
                    new CPos(-100, -100), new CPos(100, 100), 1,
                    new SpiralIterator.Builder<CPos>() {
                        @Override
                        public CPos build(int x, int y, int z) {
                            return new CPos(x, z);
                        }
                    }).iterator();
                while(i<4) {
                    CPos next = iterator.next();
                    while(Arrays.stream(output).anyMatch(next::equals)){
                        next = iterator.next();
                    }
                    CPos cPos = Config.ironMethod.getStructure().getInRegion(seedInfo.structureSeed, next.getX(), next.getZ(), seedInfo.chunkRand);
                    if(cPos!=null) {
                        BPos pos = cPos.toBlockPos();
                        seedInfo.chunkRand
                            .setDecoratorSeed(seedInfo.structureSeed, pos.getX(), pos.getZ(),
                                treasure.getDecorationSalt(), Config.version);
                        LootContext a1 =
                            new LootContext(seedInfo.chunkRand.nextLong(), Config.version);
                        int iron_count = MCLootTables.BURIED_TREASURE_CHEST.generate(a1)
                            .stream()
                            .filter(e -> e != null && e.getItem() != null)
                            .filter(e -> e.getItem().getName().equals("iron_ingot"))
                            .mapToInt(ItemStack::getCount).sum();
                        if (iron_count >= entryMethod.getIronCount()) {
                            output[i++] = pos;
                        }
                    }
                }
                break;
            case RUINED_PORTAL:
                MCLootTables.RUINED_PORTAL_CHEST.apply(Config.version);
                for (int x = -1; x <= 0; x++) {
                    for (int z = -1; z <= 0; z++) {
                        BPos pos = Config.ironMethod.getStructure().getInRegion(seedInfo.structureSeed, x, z, seedInfo.chunkRand).toBlockPos();
                        seedInfo.chunkRand
                            .setDecoratorSeed(seedInfo.structureSeed, pos.getX(),
                                pos.getZ(), portal.getDecorationSalt(),
                                Config.version);
                        LootContext a1 = new LootContext(seedInfo.chunkRand.nextLong(), Config.version);
                        int nugget_count = MCLootTables.RUINED_PORTAL_CHEST.generate(a1)
                            .stream()
                            .filter(e -> e != null && e.getItem() != null)
                            .filter(e -> e.getItem().getName().equals("iron_nugget"))
                            .mapToInt(ItemStack::getCount).sum();
                        if (nugget_count >= entryMethod.getIronCount() * 9) {
                            output[i++] = pos;
                        }
                    }
                }
                break;
            default:
                for (int x = -1; x <= 0; x++) {
                    for (int z = -1; z <= 0; z++) {
                        output[i++] = Config.ironMethod.getStructure()
                            .getInRegion(seedInfo.structureSeed, x, z, seedInfo.chunkRand).toBlockPos();
                    }
                }
                break;
        }
        seedInfo.ironLocations = output;
    }

    public static void generateNetherEntries(StructureSeedInfo structureSeedInfo) {
        switch(entryMethod){
            case RUINED_PORTAL:
                structureSeedInfo.entryLocations = findNearest(structureSeedInfo.structureSeed, structureSeedInfo.ironLocations, Config.portal,
                    structureSeedInfo.chunkRand, entryDistance);
                break;
            default:
                structureSeedInfo.entryLocations = structureSeedInfo.ironLocations;
        }
    }

    public static void convertToNetherCoords(StructureSeedInfo structureSeedInfo) {
        for (int i = 0; i < 4; i++) {
            if (structureSeedInfo.entryLocations[i] != null) {
                structureSeedInfo.entryNetherLocations[i] = new BPos(
                    structureSeedInfo.entryLocations[i].getX() / 8, 0,
                    structureSeedInfo.entryLocations[i].getZ() / 8);
            }
        }
    }

    public static void convertToOverworldCoords(StructureSeedInfo structureSeedInfo) {
        structureSeedInfo.fortressOverworldLocations = new BPos[4];
        for (int i = 0; i < 4; i++) {
            if (structureSeedInfo.fortressLocations[i] != null) {
                structureSeedInfo.fortressOverworldLocations[i] =
                    new BPos(
                        structureSeedInfo.fortressLocations[i].getX() * 8, 0,
                        structureSeedInfo.fortressLocations[i].getZ() * 8);
            }
        }
    }

    public static void generateBastions(StructureSeedInfo structureSeedInfo) {
        BPos[] bastions =
            findNearest(structureSeedInfo.structureSeed, structureSeedInfo.entryNetherLocations,
                Config.bastion, structureSeedInfo.chunkRand, Config.bastionDistance);
        if (Config.checkBastionType) {
            for (int i = 0; i < 4; i++) {
                if (bastions[i] == null) {
                    continue;
                }
                String type = BastionFeatures
                    .getType(structureSeedInfo.structureSeed, bastions[i].toChunkPos(),
                        structureSeedInfo.chunkRand);
                if (!Config.allowedBastionTypes.contains(type)) {
                    bastions[i] = null;
                }
            }
        }
        structureSeedInfo.bastionLocations = bastions;
    }

    public static void generateFortresses(StructureSeedInfo structureSeedInfo) {
        structureSeedInfo.fortressLocations =
            findNearest(structureSeedInfo.structureSeed, structureSeedInfo.bastionLocations,
                fortress, structureSeedInfo.chunkRand, Config.fortressDistance);
    }

    private static long l2norm(long x1, long z1, long x2, long z2) {
        return (x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2);
    }

    public static void findClosestStronghold(StructureSeedInfo structureSeedInfo) {
        structureSeedInfo.strongholdsCloseEnough = new boolean[] {false, false, false, false};
        structureSeedInfo.chunkRand.setSeed(structureSeedInfo.structureSeed);

        double angle = structureSeedInfo.chunkRand.nextDouble() * Math.PI * 2.0D;
        double distanceRing = 88D + structureSeedInfo.chunkRand.nextDouble() * 80D;

        double averageDistance = 88D + 0.5 * 80D;

        int chunkX = (int) Math.round(Math.cos(angle) * distanceRing);
        int chunkZ = (int) Math.round(Math.sin(angle) * distanceRing);

        CPos strongholdLocation = new CPos(chunkX, chunkZ);


        for (int i = 0; i < 4; i++) {
            BPos positionOne = structureSeedInfo.fortressOverworldLocations[i];
            if (positionOne == null) {
                continue;
            }
            double newAngle = Math.atan((float) positionOne.getZ() / positionOne.getX());
            double newAngle2 = newAngle + Math.PI * 2 / 3;
            double newAngle3 = newAngle2 + Math.PI * 2 / 3;

            chunkX = (int) Math.round(Math.cos(newAngle2) * averageDistance);
            chunkZ = (int) Math.round(Math.sin(newAngle2) * averageDistance);

            BPos positionTwo = (new CPos(chunkX, chunkZ)).toBlockPos();

            chunkX = (int) Math.round(Math.cos(newAngle3) * averageDistance);
            chunkZ = (int) Math.round(Math.sin(newAngle3) * averageDistance);

            BPos positionThree = (new CPos(chunkX, chunkZ)).toBlockPos();

            long temp1, temp2, temp3;

            BPos strongholdPos = strongholdLocation.toBlockPos();
            temp1 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), positionOne.getX(),
                positionOne.getZ());
            temp2 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), positionTwo.getX(),
                positionTwo.getZ());
            temp3 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), positionThree.getX(),
                positionThree.getZ());


            structureSeedInfo.strongholdsCloseEnough[i] =
                Math.sqrt(Math.min(Math.min(temp1, temp2), temp3)) <
                    Config.strongholdDistance * Config.strongholdDistance;
        }
    }

    public static boolean checkStructureSeed(StructureSeedInfo structureSeedInfo) {
        generateIronSpawns(structureSeedInfo);

        generateNetherEntries(structureSeedInfo);

        if (isArrayEmpty(structureSeedInfo.entryLocations)) {
            return false;
        }

        convertToNetherCoords(structureSeedInfo);

        generateBastions(structureSeedInfo);

        if (isArrayEmpty(structureSeedInfo.bastionLocations)) {
            return false;
        }

        generateFortresses(structureSeedInfo);

        if (isArrayEmpty(structureSeedInfo.fortressLocations)) {
            return false;
        }

        convertToOverworldCoords(structureSeedInfo);

        if (needStongholdDistance) {
            findClosestStronghold(structureSeedInfo);

            if (isArrayFalse(structureSeedInfo.strongholdsCloseEnough)) {
                return false;
            }
        }
        return true;
    }
}
