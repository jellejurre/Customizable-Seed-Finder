package CustomizableSeedFinder;


import CustomizableSeedFinder.Entities.FilterFunction;
import CustomizableSeedFinder.Entities.StructureSeedInfo;
import CustomizableSeedFinder.Entities.WorldSeedInfo;
import CustomizableSeedFinder.Util.Util;
import CustomizableSeedFinder.Util.WBADThreadPool;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import kaptainwutax.mcutils.rand.seed.StructureSeed;

public class WorldSeedFunctions {
    public static ArrayList<FilterFunction> OBSchecks = new ArrayList<>();
    public static ArrayList<FilterFunction> NBSchecks = new ArrayList<>();
    public static ArrayList<FilterFunction> OTGchecks = new ArrayList<>();
    public static ArrayList<FilterFunction> CheckerChecks = new ArrayList<>();
    public static ArrayList<FilterFunction> NetherCheckerChecks = new ArrayList<>();
    public static ArrayList<FilterFunction> allChecks = new ArrayList<>();
    public static boolean setup = false;

    public static boolean contains(FilterFunction function){
        return OBSchecks.contains(function)||NBSchecks.contains(function)||OTGchecks.contains(function)||CheckerChecks.contains(function)||NetherCheckerChecks.contains(function);
    }

    public static void addCheck(FilterFunction function){
        long startTime = System.nanoTime();
        List<Integer> entries = Collections.synchronizedList(new ArrayList<>());
        List<Long> times = Collections.synchronizedList(new ArrayList<>());
        System.out.println("Setting up check: "+function.name);
        for (int i = 0; i < 100000; i++) {
            Main.pool.execute(new TestTask(entries, times, function));
        }
        while(entries.size()<100&&(System.nanoTime()-startTime<60000000000L)){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Main.pool.getExecutor().shutdownNow();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Main.pool = new WBADThreadPool(Config.threadCount);
        entries.sort(null);
        if(entries.size()==0){
            System.out.println("Check too rare to assign values: "+function.name);
            System.out.println("Assigning arbitrary values cutoff 5 and average time 1000000000000 ns");
            function.cutoff = 5;
            function.averageTime = Math.pow(10, 12);
        } else {
            function.cutoff = entries.get(entries.size() * 3 / 4) ;
            function.averageTime = times.stream().mapToDouble(d -> d).average().getAsDouble();
        }
        System.out.println("Cutoff for check " +
            function.name + ": " + function.cutoff +
            ", average time: " + ((long)function.averageTime) + " ns");
        allChecks.add(function);
    }

    public static void finishSetup(){
        Comparator<FilterFunction> comparator = new Comparator<FilterFunction>() {
            @Override
            public int compare(FilterFunction o1, FilterFunction o2) {
                return (int) (o1.averageTime - o2.averageTime);
            }
        };
        allChecks.sort(comparator);
    }

    public static void checkSeed(StructureSeedInfo structureInfo){
        int biomeseed = 0;
        int[][] checkCounts = new int[allChecks.size()][4];
        WorldSeedInfo info = null;
        while(shouldContinue(checkCounts, allChecks, biomeseed)&&(info==null||info.shouldContinue)){
            info = new WorldSeedInfo(StructureSeed.toWorldSeed(structureInfo.structureSeed, biomeseed), structureInfo);
            nextbiomeseed:
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < allChecks.size(); j++) {
                    FilterFunction function = allChecks.get(j);
                    if(function.predicate.test(i, info)){
                       checkCounts[j][i]++;
                    } else {
                        break nextbiomeseed;
                    }
                }
                System.out.println("Seed: "+info.seed);
                return;
            }
            biomeseed++;
        }
    }

    public static boolean shouldContinue(int[][] checkCounts, ArrayList<FilterFunction> allChecks, int biomeSeed){
        if(biomeSeed>(1L<<16))
            return false;
        int currentCheckLevel = 0;
        for (int i = 0; i < allChecks.size(); i++) {
            if(Util.getArrayMax(checkCounts[i])>0){
                currentCheckLevel = i;
            }
        }
        int[] currentCheck = checkCounts[currentCheckLevel];
        return Util.getArrayMax(currentCheck) <= allChecks.get(currentCheckLevel).cutoff;
    }


}
