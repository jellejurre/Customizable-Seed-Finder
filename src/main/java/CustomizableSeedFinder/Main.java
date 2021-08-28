package CustomizableSeedFinder;

import static CustomizableSeedFinder.StructureSeedFunctions.checkStructureSeed;
import static CustomizableSeedFinder.WorldSeedFunctions.checkSeed;

import CustomizableSeedFinder.Entities.StructureSeedInfo;
import CustomizableSeedFinder.Util.WBADThreadPool;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.WorldSeed;

public class Main {
    static long StructureSeed = 0;
    static int index = 0;
    static ArrayList<Long> StructureSeeds;
    static WBADThreadPool pool;
    static Object lock = new Object();
    static boolean setup = false;

    public static void main(String[] args){
        Config.setup();
        System.out.println("Iron method: "+Config.ironMethod);
        System.out.println("Entry method: "+Config.entryMethod);
        System.out.println("Starting seedfinding");
//        StructureSeeds = new ArrayList<>(Arrays.asList(
//
//        ));
//        runFromList();
        runRandom();
    }

    public static void startNext() {
        startNextFromRandom();
    }

    public static void runRandom() {
        StructureSeed =
            WorldSeed.toStructureSeed(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
        for (int i = 0; i < 1000; i++) {
            startNextFromRandom();
        }
        setup = true;
    }

    public static void runFromList() {
        for (int i = 0; i < 1000; i++) {
            startNextFromList();
        }
    }

    public static void startNextFromRandom() {
        synchronized (lock) {
            if(setup&&pool.getActiveCount()<500){
                System.out.println("THREADS BELOW 500, PLEASE SEND ALL THE OUTPUT TO @jellejurre#8585 ON DISCORD");
                System.out.println(pool.getActiveCount());
            }
            pool.execute(()->{
                    StructureSeedInfo structureSeedInfo = new StructureSeedInfo(StructureSeed, new ChunkRand());
                    if(!checkStructureSeed(structureSeedInfo)){
                        Main.startNext();
                        return;
                    }
                    checkSeed(structureSeedInfo);
                    Main.startNext();
            });
            StructureSeed = StructureSeed + 1;
        }
    }

    public static void startNextFromList() {
        synchronized (lock) {
            if (index != StructureSeeds.size()) {
                pool.execute(()->{
                    StructureSeedInfo structureSeedInfo = new StructureSeedInfo(StructureSeeds.get(index), new ChunkRand());
                    if(!checkStructureSeed(structureSeedInfo)){
                        Main.startNext();
                        return;
                    }
                    checkSeed(structureSeedInfo);
                    Main.startNext();
                });
                index++;
            } else {
                if(pool.getThreadCount()==1){
                    pool.shutdown();
                    System.out.println("done");
                }
            }
        }
    }

    public static void print(String s) {
        synchronized (lock) {
            try {
                FileWriter fw = new FileWriter(new File("output.txt"), true);
                fw.append(s).append(String.valueOf('\n'));
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
