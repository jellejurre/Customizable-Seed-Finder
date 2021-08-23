package CustomizableSeedFinder.Entities;

import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.misc.SpawnPoint;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.terrainutils.terrain.OverworldTerrainGenerator;
import nl.jellejurre.seedchecker.SeedChecker;

public class WorldSeedInfo{
    public long seed;
    public boolean shouldContinue;
    public CPos[] starts;
    public BPos currentStrongHold;
    public OverworldBiomeSource obs;
    public NetherBiomeSource nbs;
    public OverworldTerrainGenerator otg;
    public SeedChecker checker;
    public SeedChecker netherChecker;
    private BPos spawnPoint;
    public StructureSeedInfo structures;

    public WorldSeedInfo(long seed, StructureSeedInfo structures) {
        this.seed = seed;
        this.structures = structures;
        shouldContinue = true;
    }

    public BPos getSpawnPoint() {
        if (spawnPoint == null) {
            BPos wrongSpawn = SpawnPoint.getSpawn(otg);
            spawnPoint = new BPos(wrongSpawn.getX(), 0, wrongSpawn.getZ());
        }
        return spawnPoint;
    }
}
