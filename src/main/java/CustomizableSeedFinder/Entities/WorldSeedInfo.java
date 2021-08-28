package CustomizableSeedFinder.Entities;

import static CustomizableSeedFinder.Config.stronghold;


import CustomizableSeedFinder.Config;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.misc.SpawnPoint;
import kaptainwutax.featureutils.structure.generator.structure.ShipwreckGenerator;
import kaptainwutax.mcutils.util.math.DistanceMetric;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;
import nl.jellejurre.seedchecker.SeedChecker;
import nl.jellejurre.seedchecker.SeedCheckerDimension;
import nl.jellejurre.seedchecker.TargetState;
import org.lwjgl.system.CallbackI;

public class WorldSeedInfo{
    public long seed;
    public boolean shouldContinue;
    public CPos[] starts;
    public BPos[] currentStrongHolds = new BPos[4];
    public Boolean[] shipwreckGenerated = new Boolean[]{false,false,false,false};
    private OverworldBiomeSource obs;
    private NetherBiomeSource nbs;
    private OverworldTerrainGenerator otg;
    private SeedChecker checker;
    private SeedChecker netherChecker;
    private BPos spawnPoint;
    public ShipwreckGenerator[] shipwreckGenerator = new ShipwreckGenerator[]{null,null,null,null};
    public Boolean[] doesShipwreckGenerate = new Boolean[]{true,true,true,true};
    public StructureSeedInfo structures;

    public WorldSeedInfo(long seed, StructureSeedInfo structures) {
        this.seed = seed;
        this.structures = structures;
        shouldContinue = true;
    }
    public OverworldBiomeSource getObs() {
        if(obs==null){
            obs = new OverworldBiomeSource(Config.version, seed);
        }
        return obs;
    }

    public NetherBiomeSource getNbs() {
        if(nbs==null){
            nbs = new NetherBiomeSource(Config.version, seed);
        }
        return nbs;
    }

    public OverworldTerrainGenerator getOtg() {
        if(otg==null){
            otg = new OverworldTerrainGenerator(getObs());
        }
        return otg;
    }

    public SeedChecker getChecker() {
        if(checker==null){
            checker = new SeedChecker(seed);
        }
        return checker;
    }

    public SeedChecker getNetherChecker() {
        if(netherChecker==null){
            netherChecker = new SeedChecker(seed, 12, SeedCheckerDimension.NETHER);
        }
        return netherChecker;
    }

    public BPos getSpawnPoint() {
        if (spawnPoint == null) {
            BPos wrongSpawn = SpawnPoint.getSpawn(getOtg());
            spawnPoint = new BPos(wrongSpawn.getX(), 0, wrongSpawn.getZ());
        }
        return spawnPoint;
    }

    public BPos getStronghold(int i) {
        if(this.currentStrongHolds[i]==null){
            if (starts == null)
                starts = stronghold.getStarts(getObs(), 3, structures.chunkRand);

            double minimumDistance = Integer.MAX_VALUE;
            CPos closestStart = null;
            for (CPos start : starts) {
                double distanceSquared =
                    start.toBlockPos().distanceTo(structures.fortressOverworldLocations[i],
                        DistanceMetric.EUCLIDEAN_SQ);
                if (distanceSquared < minimumDistance) {
                    closestStart = start;
                }
            }
            this.currentStrongHolds[i] = closestStart.toBlockPos();
                /*
                if (start.toBlockPos()
                    .distanceTo(structures.fortressOverworldLocations[i],
                        DistanceMetric.EUCLIDEAN_SQ) <
                    Math.pow(Config.strongholdDistance, 2) &&
                    start.toBlockPos()
                        .distanceTo(structures.fortressOverworldLocations[i],
                            DistanceMetric.EUCLIDEAN_SQ) >
                        Math.pow(Config.minStrongholdDistance, 2)) {
                    this.currentStrongHolds[i] = start.toBlockPos();
                }*/
        }
        return this.currentStrongHolds[i];
    }


}
