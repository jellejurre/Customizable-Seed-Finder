package CustomizableSeedFinder;

import static CustomizableSeedFinder.Config.fortressDistance;
import static CustomizableSeedFinder.Config.ironDistance;
import static CustomizableSeedFinder.Config.shipwreck;
import static CustomizableSeedFinder.Config.stronghold;
import static CustomizableSeedFinder.Util.Util.posToBox;


import CustomizableSeedFinder.Entities.FilterFunction;
import CustomizableSeedFinder.Util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.featureutils.loot.ChestContent;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.featureutils.structure.generator.structure.ShipwreckGenerator;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nl.jellejurre.seedchecker.ReflectionUtils;

public class FunctionRepo {

    public static final HashMap<String, FilterFunction> misc = new HashMap<>();

    /*
        Guarantees villages spawning
     */
    public static final FilterFunction villageSpawns = new FilterFunction("villageSpawns", ((i, info) ->
        Config.village.canSpawn(info.structures.ironLocations[i].toChunkPos(), info.getObs())
    ));

    /*
        Guarantees iron in the village blacksmith chest
     */
    public static final FilterFunction villageIron = new FilterFunction("villageIron", ((i, info) -> {
        int ironNeeded = Config.entryMethod.getIronCount();
        if(!Config.village.isZombieVillage(info.structures.structureSeed, info.structures.ironLocations[i].toChunkPos(), info.structures.chunkRand)){
            ironNeeded-=3;
        }
        Box villageBox = posToBox(info.structures.ironLocations[i], 200);
        boolean found = false;
        boolean villagespawned = false;
        Map<BlockPos, BlockEntity> itemMap =
            info.getChecker().getBlockEntitiesInBox(BlockEntityType.CHEST, villageBox);
        for (Map.Entry<BlockPos, BlockEntity> entry : itemMap.entrySet()) {
            ChestBlockEntity chest = (ChestBlockEntity) entry.getValue();
            if (chest != null) {
                Identifier id =
                    (Identifier) ReflectionUtils
                        .getValueFromField(chest, "lootTableId");
                if (id != null) {
                    String path =
                        (String) ReflectionUtils.getValueFromField(id, "path");
                    if(path.contains("village")){
                        villagespawned = true;
                    }
                    if (path.contains("village_weaponsmith")) {
                        List<ItemStack> items =
                            info.getChecker().generateChestLoot(entry.getKey());
                        int iron =
                            items.stream().filter(x -> x.getItem() == Items.IRON_INGOT)
                                .mapToInt(x -> x.getCount()).sum();
                        if (iron >= ironNeeded) {
                            found = true;
                        }
                    }
                }
            }
        }
        if (!found) {
            if (villagespawned) {
                info.shouldContinue = false;
                return false;
            } else {
                return false;
            }
        }
        return true;
    }));

    /*
        Guarantees the portal spawning
     */
    public static final FilterFunction portalSpawns = new FilterFunction("portalSpawns", (i, info) -> {
        return Config.portal.canSpawn(info.structures.ironLocations[i].toChunkPos(), info.getObs());
    });


    /*
        Guarantees no forest/taiga between spawnpoint and ruined portal
     */
    public static final FilterFunction portalBiomes = new FilterFunction("portalBiomes", (i, info) -> {
        BlockPos spawnPos =
            new BlockPos(info.getSpawnPoint().getX(), info.getSpawnPoint().getY(),
                info.getSpawnPoint().getZ());
        BlockPos entry = new BlockPos(info.structures.entryLocations[i].getX(),
            info.structures.entryLocations[i].getY(), info.structures.entryLocations[i].getZ());
        Box entryBox = new Box(spawnPos, entry);
        boolean goodBiomes = true;
        for (int x = (int) entryBox.minX >> 4; x <= (int) entryBox.maxX >> 4; x++) {
            for (int z = (int) entryBox.minZ >> 4; z <= (int) entryBox.maxZ >> 4;
                 z++) {
                if (info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                    .getName()
                    .equals("forest") ||
                    info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                        .getName()
                        .equals("taiga")) {
                    goodBiomes = false;
                }
            }
        }
        if(goodBiomes){
            return true;
        }
        if(!Config.ironMethod.toString().equals(Config.entryMethod.toString())){
            BlockPos iron = new BlockPos(info.structures.ironLocations[i].getX(),
                info.structures.ironLocations[i].getY(), info.structures.ironLocations[i].getZ());
            entry = new BlockPos(info.structures.entryLocations[i].getX(),
                info.structures.entryLocations[i].getY(), info.structures.entryLocations[i].getZ());
            entryBox = new Box(iron, entry);
            goodBiomes = true;
            for (int x = (int) entryBox.minX >> 4; x <= (int) entryBox.maxX >> 4; x++) {
                for (int z = (int) entryBox.minZ >> 4; z <= (int) entryBox.maxZ >> 4;
                     z++) {
                    if (info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                        .getName()
                        .equals("forest") ||
                        info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                            .getName()
                            .equals("taiga")) {
                        goodBiomes = false;
                    }
                }
            }
            return goodBiomes;
        }
        return false;
    });


    /*
        Guarantees the portal generating and also the ruined portal being completable
     */
    public static final FilterFunction portalGenerates = new FilterFunction("portalGenerates", (i, info) -> {
        RuinedPortalGenerator portalGenerator = new RuinedPortalGenerator(
            Config.version);
        if(!portalGenerator.generate(info.getOtg(),
            info.structures.entryLocations[i].toChunkPos().getX(),
            info.structures.entryLocations[i].toChunkPos().getZ(), info.structures.chunkRand)) return false;
        if (!(portalGenerator.getLocation() == RuinedPortalGenerator.Location.ON_LAND_SURFACE)) {
            return false;
        }
        List<Pair<Block, BPos>> buildlist = portalGenerator.getMinimalPortal();
        if (buildlist.stream().map(x -> x.getFirst()).filter(x -> x.equals(
            kaptainwutax.mcutils.block.Blocks.CRYING_OBSIDIAN)).count() > 0) {
            return false;
        }
        if (ReflectionUtils.getValueFromField(portalGenerator, "cPos") == null) {
            return false;
        }
        int required = 10 - buildlist.size();
        if (required > 0) {
            if (Config.portal
                .getLoot(info.structures.structureSeed, portalGenerator, info.structures.chunkRand,
                    false).stream().mapToInt(x -> x.getCount(y -> y.equals(
                    kaptainwutax.featureutils.loot.item.Items.OBSIDIAN))).sum() < required) {
                return false;
            }
        }
        return true;
    });

    /*
        Guarantees the jungle pyramid spawning
     */
    public static final FilterFunction pyramidSpawns = new FilterFunction("pyramidSpawns", ((i, info) ->
        Config.junglePyramid.canSpawn(info.structures.ironLocations[i].toChunkPos(), info.getObs())
    ));

    /*
        Guarantees the jungle pyramid having iron
     */
    public static final FilterFunction pyramidIron = new FilterFunction("pyramidIron", (i, info) -> {
        Box pyramidBox = posToBox(info.structures.ironLocations[i], ironDistance * 2);
        boolean found = false;
        boolean spawned = false;
        Map<BlockPos, BlockEntity> itemMap =
            info.getChecker().getBlockEntitiesInBox(BlockEntityType.CHEST, pyramidBox);
        for (Map.Entry<BlockPos, BlockEntity> entry : itemMap.entrySet()) {
            ChestBlockEntity chest = (ChestBlockEntity) entry.getValue();
            if (chest != null) {
                Identifier id =
                    (Identifier) ReflectionUtils
                        .getValueFromField(chest, "lootTableId");
                if (id != null) {
                    String path = (String) ReflectionUtils.getValueFromField(id, "path");
                    if (path.contains("jungle_temple")) {
                        spawned = true;
                        List<ItemStack> items = info.getChecker().generateChestLoot(entry.getKey());
                        int iron =
                            items.stream().filter(x -> x.getItem() == Items.IRON_INGOT)
                                .mapToInt(x -> x.getCount()).sum();
                        if (iron >= Config.entryMethod.getIronCount()) {
                            found = true;
                        }
                    }
                }
            }
        }
        if (!found) {
            if (spawned) {
                info.shouldContinue = false;
            } else {
                return false;
            }
        }
        return true;
    });

    /*
        Guarantees the shipwreck spawning
     */
    public static final FilterFunction shipwreckSpawns = new FilterFunction("shipwreckSpawns", ((i, info) ->
        Config.shipwreck.canSpawn(info.structures.ironLocations[i].toChunkPos(), info.getObs())
    ));

    /*
        Guarantees the shipwreck having iron
     */
    public static final FilterFunction shipwreckIron = new FilterFunction("shipwreckIron", ((i, info)->{
        if(info.shipwreckGenerator[i] == null) {
            info.shipwreckGenerator[i] = new ShipwreckGenerator(Config.version);
            info.doesShipwreckGenerate[i] = info.shipwreckGenerator[i].generate(info.getOtg(),info.structures.ironLocations[i].toChunkPos());
        }

        if(!info.doesShipwreckGenerate[i]) return false;

        List<ChestContent> loot = shipwreck.getLoot(info.seed, info.shipwreckGenerator[i], info.structures.chunkRand, false);
        int totalIronNuggets = 0;
        int totalIron = 0;
        for(ChestContent cc : loot){
            totalIronNuggets += cc.getCount(x -> x.getName().equals("iron_nugget"));
            totalIron += cc.getCount(x -> x.getName().equals("iron_ingot"));
        }
        return totalIron*9+totalIronNuggets >= Config.entryMethod.getIronCount()*9;

    }));

    /*
    Guarantees the treasure spawning
    */
    public static final FilterFunction treasureSpawns = new FilterFunction("treasureSpawns", ((i, info) ->
        Config.treasure.canSpawn(info.structures.ironLocations[i].toChunkPos(), info.getObs())
    ));

    /*
    Guarantees treasure mapless entry possibility
     */
    public static final FilterFunction treasureAlone = new FilterFunction("treasureAlone", ((i, info) -> {
        Box treasureBox = posToBox(info.structures.ironLocations[i], 64);
        Map<BlockPos, BlockEntity> chestMap = info.getChecker().getBlockEntitiesInBox(BlockEntityType.CHEST, treasureBox);
        if(chestMap.size()!=1){
            return false;
        }
        if(chestMap.keySet().iterator().next().getY()<49){
            return false;
        }
        ChestBlockEntity chest = (ChestBlockEntity) chestMap.values().iterator().next();
        if (chest != null) {
            Identifier id =
                (Identifier) ReflectionUtils
                    .getValueFromField(chest, "lootTableId");
            if (id != null) {
                String path =
                    (String) ReflectionUtils.getValueFromField(id, "path");
                if (path.contains("buried")) {
                    return true;
                }
            }
        }
        return false;
    }));

    /*
        Guarantees the shipwreck having carrots
     */
    public static final FilterFunction shipwreckCarrots = new FilterFunction("shipwreckCarrots", ((i, info)->{
        if(info.shipwreckGenerator[i] == null) {
            info.shipwreckGenerator[i] = new ShipwreckGenerator(Config.version);
            info.doesShipwreckGenerate[i] = info.shipwreckGenerator[i].generate(info.getOtg(),info.structures.ironLocations[i].toChunkPos());
        }

        if(!info.doesShipwreckGenerate[i]) return false;

        List<ChestContent> loot = shipwreck.getLoot(info.seed, info.shipwreckGenerator[i], info.structures.chunkRand, false);
        for(ChestContent cc : loot){
            if(cc.getCount(x -> x.getName().equals("carrot"))>0){
                return true;
            }
        }
        return false;

    }));

    /*
        Guarantees the shipwreck being a mast type
     */
    private static final HashSet<String> allowedTypes = new HashSet<>(Arrays.asList("with_mast", "with_mast_degraded"));

    public static final FilterFunction shipwreckMast = new FilterFunction("shipwreckMast", ((i, info)->{
        if(info.shipwreckGenerator[i] == null) {
            info.shipwreckGenerator[i] = new ShipwreckGenerator(Config.version);
            info.doesShipwreckGenerate[i] = info.shipwreckGenerator[i].generate(info.getOtg(),info.structures.ironLocations[i].toChunkPos());
        }

        if(!info.doesShipwreckGenerate[i]) return false;

        return allowedTypes.contains(info.shipwreckGenerator[i].getType());
    }));

    /*
        Guarantees the shipwreck not being beached
     */

    public static final FilterFunction shipwreckNotBeached = new FilterFunction("shipwreckNotBeached", ((i, info)->{
        if(info.shipwreckGenerator[i] == null) {
            info.shipwreckGenerator[i] = new ShipwreckGenerator(Config.version);
            info.doesShipwreckGenerate[i] = info.shipwreckGenerator[i].generate(info.getOtg(),info.structures.ironLocations[i].toChunkPos());
        }

        if(!info.doesShipwreckGenerate[i]) return false;

        return !info.shipwreckGenerator[i].isBeached();
    }));


    enum State{
        NO_WATER_YET,
        WATER_FOUND,
        GRAVEL_FOUND,
        KELP_FOUND;
    }
    /*
        Makes a relatively accurate floating kelp check for the magma ravine.
        Doesn't work 100% of the time, but at least more than guessing
     */
    public static final FilterFunction kelpCheck = new FilterFunction("kelpCheck", ((i, info)->{
        Box locationBox = posToBox(info.structures.ironLocations[i], Config.entryDistance);
        Box lavaSpawnBox = posToBox(info.getSpawnPoint(), Config.entryDistance);
        int villageLava =
            Util.getTopBlockCount(info.getChecker(), Blocks.MAGMA_BLOCK, locationBox);
        int spawnLava =
            Util.getTopBlockCount(info.getChecker(), Blocks.MAGMA_BLOCK, lavaSpawnBox);
        if (villageLava < 5 && spawnLava < 5) {
            return false;
        }

        BlockPos magmaBPos = Util.locateBlockFromTop(info.getChecker(), Blocks.MAGMA_BLOCK, locationBox);
        if(magmaBPos.getX()==0 && magmaBPos.getY()==0 && magmaBPos.getZ()==0) {
            magmaBPos = Util.locateBlockFromTop(info.getChecker(), Blocks.MAGMA_BLOCK, locationBox);
            if (magmaBPos.getX() == 0 && magmaBPos.getY() == 0 && magmaBPos.getZ() == 0) {
                return false;
            }
        }

        int totalFloatingKelp = 0;
        for(int xo=-20;xo<20;xo++){
            for(int zo=-20;zo<20;zo++) {
                State state = State.NO_WATER_YET;
                int temporaryKelp = 0;
                for (int y = 0; y < 100; y++) {
                    net.minecraft.block.Block block = info.getChecker().getBlock(magmaBPos.getX()+xo, y, magmaBPos.getZ()+zo);
                    if(state==State.NO_WATER_YET && block.equals(Blocks.WATER)){
                        state = State.WATER_FOUND;
                    }
                    if(state==State.WATER_FOUND && !(block.equals(Blocks.WATER) || block.equals(Blocks.GRAVEL))){
                        state = State.NO_WATER_YET;
                    }
                    if(state==State.WATER_FOUND && block.equals(Blocks.GRAVEL)){
                        state = State.GRAVEL_FOUND;
                    }
                    if(state==State.GRAVEL_FOUND && !(block.equals(Blocks.GRAVEL) || block.equals(Blocks.KELP) || block.equals(Blocks.KELP_PLANT))){
                        //I'm pretty confident we wont get water->gravel->(not gravel or kelp) and still have something eligable
                        break;
                    }
                    if(state == State.GRAVEL_FOUND && (block.equals(Blocks.KELP) || block.equals(Blocks.KELP_PLANT))){
                        state = State.KELP_FOUND;
                    }
                    if(state == State.KELP_FOUND && (block.equals(Blocks.KELP) || block.equals(Blocks.KELP_PLANT))){
                        temporaryKelp++;
                    }
                    if(state == State.KELP_FOUND && block.equals(Blocks.WATER)){
                        totalFloatingKelp+=temporaryKelp;
                        break;
                    }
                }
            }
        }
        return totalFloatingKelp>15;

    }));

    /*
        Guarantees a lava pool, also no taiga / forest obstructing vision
     */
    public static final FilterFunction lavaCheck = new FilterFunction("lavaCheck", (i, info) -> {
        Box locationBox = posToBox(info.structures.ironLocations[i], Config.entryDistance);
        Box lavaSpawnBox = posToBox(info.getSpawnPoint(), Config.entryDistance);
        int villageLava =
            Util.getTopBlockCount(info.getChecker(), Blocks.LAVA, locationBox, new ArrayList<>(
                Arrays.asList(Blocks.AIR, Blocks.CAVE_AIR)));
        int spawnLava =
            Util.getTopBlockCount(info.getChecker(), Blocks.LAVA, lavaSpawnBox, new ArrayList<>(
                Arrays.asList(Blocks.AIR, Blocks.CAVE_AIR)));
        if (villageLava < 5 && spawnLava < 5) {
            return false;
        }
        boolean goodBiomesTotal = false;
        if (villageLava > 4) {
            BlockPos lavaPos =
                Util.locateBlockFromTop(info.getChecker(), Blocks.LAVA, locationBox,
                    new ArrayList<>(
                        Arrays.asList(Blocks.AIR, Blocks.CAVE_AIR)));
            BlockPos villagePos =
                new BlockPos(info.structures.ironLocations[i].getX(),
                    info.structures.ironLocations[i].getY(),
                    info.structures.ironLocations[i].getZ());
            Box lavaBox = new Box(lavaPos, villagePos);
            boolean goodBiomes = true;
            for (int x = (int) lavaBox.minX << 4; x < (int) lavaBox.maxX << 4; x++) {
                for (int z = (int) lavaBox.minZ << 4; z < (int) lavaBox.maxZ << 4;
                     z++) {
                    if (info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                        .getName()
                        .equals("forest") ||
                        info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                            .getName()
                            .equals("taiga")) {
                        goodBiomes = false;
                    }
                }
            }
            if (goodBiomes) {
                goodBiomesTotal = true;
            }
        }
        if (spawnLava > 4) {
            BlockPos lavaPos =
                Util.locateBlockFromTop(info.getChecker(), Blocks.LAVA, lavaSpawnBox,
                    new ArrayList<>(
                        Arrays.asList(Blocks.AIR, Blocks.CAVE_AIR)));
            BlockPos spawnPos =
                new BlockPos(info.getSpawnPoint().getX(), info.getSpawnPoint().getY(),
                    info.getSpawnPoint().getZ());
            Box lavaBox = new Box(lavaPos, spawnPos);
            boolean goodBiomes = true;
            for (int x = (int) lavaBox.minX >> 4; x <= (int) lavaBox.maxX >> 4; x++) {
                for (int z = (int) lavaBox.minZ >> 4; z <= (int) lavaBox.maxZ >> 4;
                     z++) {
                    if (info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                        .getName()
                        .equals("forest") ||
                        info.getObs().getBiome(new CPos(x, z).toBlockPos()).getCategory()
                            .getName()
                            .equals("taiga")) {
                        goodBiomes = false;
                    }
                }
            }
            if (goodBiomes) {
                goodBiomesTotal = true;
            }
        }
        if (!goodBiomesTotal) {
            return false;
        }
        return true;
    });

    /*
        Guarantees the ruined portal chest spawning
     */
    public static final FilterFunction portalCheck = new FilterFunction("portalCheck", (i, info) -> {
        Box portalBox = posToBox(info.structures.entryLocations[i], 32, 50, 100);
        int blocks = info.getChecker().getBlockCountInBox(Blocks.CHEST, portalBox);
        return blocks != 0;
    });

    /*
        Guarantees a magma ravine
     */
    public static final FilterFunction magmaCheck = new FilterFunction("magmaCheck", (i, info) -> {
        Box locationBox = posToBox(info.structures.ironLocations[i], Config.entryDistance);
        Box lavaSpawnBox = posToBox(info.getSpawnPoint(), Config.entryDistance);
        int locationMagma =
            Util.getTopBlockCount(info.getChecker(), Blocks.MAGMA_BLOCK, locationBox, new ArrayList<>(
                Arrays.asList(Blocks.WATER)));
        int spawnMagma =
            Util.getTopBlockCount(info.getChecker(), Blocks.MAGMA_BLOCK, lavaSpawnBox, new ArrayList<>(
                Arrays.asList(Blocks.WATER)));
        return locationMagma >= 5 || spawnMagma >= 5;
    });

    /*
        Generic check to make sure the current index [0-4] is even worth checking worldseeds for
     */
    public static final FilterFunction hasStructures = new FilterFunction("hasStructures", ((i, info) ->
        !(info.structures.fortressLocations[i] == null ||
            info.structures.bastionLocations[i] == null ||
            info.structures.ironLocations[i] == null || info.structures.entryLocations[i] == null)
    ));

    /*
        Check for distance from spawn to iron
     */
    public static final FilterFunction spawnToIronDistance = new FilterFunction("spawnToIronDistance", ((i, info) -> {
        return !(
            info.getSpawnPoint()
                .distanceTo(info.structures.ironLocations[i], DistanceMetric.EUCLIDEAN_SQ) >
                Math.pow(Config.ironDistance, 2));

    }));

    /*
        Check to make sure the nether structures can spawn
     */
    public static final FilterFunction netherStructuresSpawn = new FilterFunction("netherStructuresSpawn", ((i, info) ->
        Config.bastion.canSpawn(info.structures.bastionLocations[i].toChunkPos(), info.getNbs()) &&
            Config.fortress.canSpawn(info.structures.fortressLocations[i].toChunkPos(), info.getNbs())
    ));

    /*
        Check to make sure the stronghold is close enough
     */
    public static final FilterFunction strongholdDistance = new FilterFunction("strongholdDistance", ((i, info) -> {
        if (info.starts == null)
            info.starts = stronghold.getStarts(info.getObs(), 3, info.structures.chunkRand);
        boolean found = false;
        for (CPos start : info.starts) {
            if (start.toBlockPos()
                .distanceTo(info.structures.fortressOverworldLocations[i],
                    DistanceMetric.EUCLIDEAN_SQ) <
                Math.pow(Config.strongholdDistance, 2) &&
                start.toBlockPos()
                .distanceTo(info.structures.fortressOverworldLocations[i],
                    DistanceMetric.EUCLIDEAN_SQ) >
                Math.pow(Config.minStrongholdDistance, 2)) {
                found = true;
                info.currentStrongHolds[i] = start.toBlockPos();
            }
        }
        return found;
    }));

    /*
        Check for distance between fortress spawners and bastion
     */
    public static final FilterFunction bastionToSpawnerDistance = new FilterFunction("bastionToSpawnerDistance", ((i, info) -> {
            int fortressRadius = fortressDistance - (int) info.structures.fortressLocations[i].distanceTo(info.structures.bastionLocations[i], DistanceMetric.EUCLIDEAN);
            if(fortressRadius>150){
                return true;
            }
            Box bastionBox = posToBox(info.structures.fortressLocations[i], fortressRadius);
            Map<BlockPos, BlockEntity> spawners = info.getNetherChecker().getBlockEntitiesInBox(BlockEntityType.MOB_SPAWNER, bastionBox);
            if (spawners.size() < 2) {
                return false;
            }

            for (Map.Entry<BlockPos, BlockEntity> entry : spawners.entrySet()) {
                BPos pos = new BPos(entry.getKey().getX(), 0, entry.getKey().getZ());
                if (pos
                    .distanceTo(info.structures.bastionLocations[i], DistanceMetric.EUCLIDEAN_SQ) >
                    Math.pow(Config.fortressDistance, 2)) {
                    return false;
                }
            }
            return true;
        }));

    public static final FilterFunction bastionToStartDistance = new FilterFunction("bastionToStartDistance", ((i, info) -> {
        return info.structures.fortressLocations[i].distanceTo(info.structures.bastionLocations[i], DistanceMetric.EUCLIDEAN_SQ) < Math.pow(Config.fortressDistance, 2);
    }));

    /*
        Check to see if the stronghold is exposed
     */
    public static final FilterFunction strongholdExposed = new FilterFunction("strongholdExposed", (i, info) -> {
        BPos currentStronghold = info.getStronghold(i);
        if(currentStronghold==null) return false;
        Box portalBox = posToBox(currentStronghold, 100);
        int exposedcount = Util.getTopBlockCount(info.getChecker(), Blocks.STONE_BRICKS, portalBox);
        return exposedcount >= 5;
    });

    /*
        Check to see if there's atleast 2 eyes in the stronghold
     */
    public static final FilterFunction strongholdEyeCount = new FilterFunction("strongholdEyeCount", (i, info) -> {
        BPos currentStronghold = info.getStronghold(i);
        if(currentStronghold==null) return false;
        Box portalBox = posToBox(currentStronghold, 100);
        int eyes = Util.goodEyeCount(info.getChecker(), portalBox);
        return eyes >= 2;
    });

    /*
        Check to see if there's gravel nearby
     */
    public static final FilterFunction gravelCheck = new FilterFunction("gravelCheck", (i, info) -> {
        Box locationBox = posToBox(info.structures.ironLocations[i], Config.entryDistance);
        Box lavaSpawnBox = posToBox(info.getSpawnPoint(), Config.entryDistance);
        int villageGravel =
            Util.getTopBlockCount(info.getChecker(), Blocks.GRAVEL, locationBox, new ArrayList<>(
                Arrays.asList(Blocks.WATER)));
        int spawnGravel =
            Util.getTopBlockCount(info.getChecker(), Blocks.GRAVEL, lavaSpawnBox, new ArrayList<>(
                Arrays.asList(Blocks.WATER)));
        if (villageGravel < 4 && spawnGravel < 4) {
            return false;
        }
        return true;
    });

    /*
        Check to see if you spawn on an island
     */
    public static final FilterFunction isIslandSpawn = new FilterFunction("isIslandSpawn", ((i, info) -> {
        int totalOcean = 0;
        //Checks [(-4,-4),(-4,0),(-4,4),
        //        (0,-4),        (0,4),
        //        (4,-4), (4,0), (4,4)]
        for (int x = -4; x <= 4; x += 4) {
            for (int z = -4; z <= 4; z += 4) {
                if (x != 0 && z != 0) {
                    if (info.getObs().getBiome(new BPos(info.getSpawnPoint().getX() + x * 16,
                        0,
                        info.getSpawnPoint().getZ() + z * 16)).getCategory() ==
                        Biome.Category.OCEAN) {
                        totalOcean++;
                    }
                }
            }
        }
        return totalOcean > 6;
    }
    ));

    /*
        Check to see if there's at least 12 logs near spawn
     */
    public static final FilterFunction logsNearSpawn = new FilterFunction("logsNearSpawn", ((i, info) -> {
        Box box = posToBox(info.getSpawnPoint(), 50);
        int logs = info.getChecker().getBlockCountInBox(Blocks.OAK_LOG, box)+info.getChecker().getBlockCountInBox(Blocks.SPRUCE_LOG, box)+
            info.getChecker().getBlockCountInBox(Blocks.JUNGLE_LOG, box)+info.getChecker().getBlockCountInBox(Blocks.BIRCH_LOG, box)+
            info.getChecker().getBlockCountInBox(Blocks.DARK_OAK_LOG, box)+info.getChecker().getBlockCountInBox(Blocks.ACACIA_LOG, box);
        return logs>=12;
    }));

    static {
        misc.put("gravelCheck", gravelCheck);
        misc.put("islandSpawn", isIslandSpawn);
    }

}
