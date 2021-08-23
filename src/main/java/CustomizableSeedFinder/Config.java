package CustomizableSeedFinder;

import CustomizableSeedFinder.Entities.FilterFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import kaptainwutax.featureutils.structure.BastionRemnant;
import kaptainwutax.featureutils.structure.Fortress;
import kaptainwutax.featureutils.structure.JunglePyramid;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.featureutils.structure.Shipwreck;
import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.Village;
import kaptainwutax.featureutils.structure.generator.structure.ShipwreckGenerator;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import nl.jellejurre.seedchecker.SeedCheckerSettings;

public class Config {

    public static Config.IRON_METHODS ironMethod = null;
    public static Config.ENTRY_METHODS entryMethod = null;
    public static Boolean checkStrongholdDistance = false;
    public static Boolean checkBastionType = false;
    public static int ironDistance = 0;
    public static int entryDistance = 0;
    public static int bastionDistance = 0;
    public static int fortressDistance = 0;
    public static int strongholdDistance = 0;
    public static int minStrongholdDistance = 0;
    public static MCVersion version;
    public static Village village;
    public static Shipwreck shipwreck;
    public static JunglePyramid junglePyramid;
    public static RuinedPortal rp;
    public static BastionRemnant bastion;
    public static Fortress fortress;
    public static Stronghold stronghold;
    public static ShipwreckGenerator shipwreckGenerator;
    public static List<String> allowedBastionTypes = new ArrayList<>();
    public static Scanner in = new Scanner(System.in);

    public static void setup() {
        SeedCheckerSettings.initialise();
        ArrayList<FilterFunction> checks = new ArrayList<>();
        while (version == null) {
            System.out.println(
                "Enter MCVersion (default: 1.16.1, other versions might not work optimally)");
            String s = in.nextLine();
            if (s.equals("")) {
                version = MCVersion.v1_16_1;
            } else {
                version = MCVersion.fromString(s);
            }
        }
        village = new Village(version);
        shipwreck = new Shipwreck(version);
        junglePyramid = new JunglePyramid(version);
        rp = new RuinedPortal(Dimension.OVERWORLD, version);
        bastion = new BastionRemnant(version);
        fortress = new Fortress(version);
        stronghold = new Stronghold(version);
        shipwreckGenerator = new ShipwreckGenerator(version);
        while (ironMethod == null) {
            System.out.println("Enter iron method");
            System.out.print("Possible options: ");
            System.out.println(
                Arrays.stream(IRON_METHODS.values())
                    .map(IRON_METHODS::toString)
                    .collect(Collectors.joining(", ")));
            String s = in.nextLine();
            for (Config.IRON_METHODS method : Config.IRON_METHODS.values()) {
                if (method.toString().equals(s)) {
                    ironMethod = method;
                    break;
                }
            }
        }
        for (FilterFunction function : ironMethod.getFunctions()) {
            if (!checks.contains(function)) {
                checks.add(function);
            }
        }
        while (entryMethod == null) {
            System.out.println("Enter entry method");
            System.out.print("Possible options: ");
            System.out.println(
                Arrays.stream(ENTRY_METHODS.values())
                    .map(ENTRY_METHODS::toString)
                    .collect(Collectors.joining(", ")));
            String s = in.nextLine();
            for (Config.ENTRY_METHODS method : Config.ENTRY_METHODS.values()) {
                if (method.toString().equals(s)) {
                    entryMethod = method;
                    break;
                }
            }
        }
        for (FilterFunction function : entryMethod.getFunctions()) {
            if (!checks.contains(function)) {
                checks.add(function);
            }
        }
        ironDistance = addIntegerCheck("Maximum iron location distance from spawn:", 150);
        checks.add(FunctionRepo.spawnToIronDistance);
        entryDistance = addIntegerCheck("Maximum entry location distance from spawn/iron location:", 100);
        bastionDistance = addIntegerCheck("Maximum bastion distance from entry location:", 100);
        fortressDistance = addIntegerCheck("Maximum fortress spawner distance from bastion:", 200);
        checks.add(FunctionRepo.bastionToFortressDistance);
        if (addBooleanCheck("Do you want to toggle one of the extra settings?")) {
            if(addBooleanCheck("Should bastion type be checked?")){
                checkBastionType = true;
                if(addBooleanCheck("Allow housing bastion?", "yes"))
                    allowedBastionTypes.add("housing");
                if(addBooleanCheck("Allow bridge bastion?", "yes"))
                    allowedBastionTypes.add("bridge");
                if(addBooleanCheck("Allow stables bastion?", "yes"))
                    allowedBastionTypes.add("stables");
                if(addBooleanCheck("Allow treasure bastion?", "yes"))
                    allowedBastionTypes.add("treasure");
            }
            if(addBooleanCheck("Should stronghold distance be checked?")){
                checks.add(FunctionRepo.strongholdDistance);
                checkStrongholdDistance = true;
                strongholdDistance = addIntegerCheck("Maximum stronghold distance from fortress:", 500);
                minStrongholdDistance = addIntegerCheck("Minimum stronghold distance from fortress:", 0);
            }
            if(addBooleanCheck("Should stronghold exposed be checked?")){
                checks.add(FunctionRepo.strongholdExposed);
            }
            if(addBooleanCheck("Should stronghold eye count greater than two be checked?")){
                checks.add(FunctionRepo.strongholdEyeCount);
            }
            if(addBooleanCheck("Should there be a different amount of iron than the default ("+entryMethod.getIronCount()+")?")){
                entryMethod.setIronCount(addIntegerCheck("Minimum iron count:", entryMethod.getIronCount()));
            }
            if(ironMethod==IRON_METHODS.SHIPWRECK) {
                if(addBooleanCheck("Should carrots in shipwreck chest be checked?")){
                    checks.add(FunctionRepo.shipwreckCarrots);
                }
            }
            if(entryMethod==ENTRY_METHODS.MAGMA_RAVINE) {
                if(addBooleanCheck("Should floating kelp near magma ravine be checked?")){
                    checks.add(FunctionRepo.kelpCheck);
                }
            }
            if(addBooleanCheck("Should logs near spawn be checked?")){
                checks.add(FunctionRepo.logsNearSpawn);
            }
        }
        checks.add(FunctionRepo.hasStructures);
        checks.add(FunctionRepo.netherStructuresSpawn);
        for (FilterFunction check : checks) {
            WorldSeedFunctions.addCheck(check);
            System.out.println();
        }
        WorldSeedFunctions.finishSetup();
    }

    public enum IRON_METHODS{
        VILLAGE("village", new ArrayList<>(Arrays.asList(FunctionRepo.villageSpawns, FunctionRepo.villageIron)), Config.village),
        RUINED_PORTAL("ruined portal", new ArrayList<>(Arrays.asList(FunctionRepo.portalSpawns, FunctionRepo.portalBiomes, FunctionRepo.portalGenerates)), Config.rp),
        JUNGLE_TEMPLE("jungle temple", new ArrayList<>(Arrays.asList(FunctionRepo.pyramidSpawns, FunctionRepo.pyramidIron)), Config.junglePyramid),
        SHIPWRECK("shipwreck", new ArrayList<>(Arrays.asList(FunctionRepo.shipwreckSpawns, FunctionRepo.shipwreckIron)), Config.shipwreck)
        ;

        private final String text;
        private final List<FilterFunction> functions;
        private final RegionStructure structure;
        IRON_METHODS(String text, List<FilterFunction> functions, RegionStructure structure){
            this.text = text;
            this.functions = functions;
            this.structure = structure;
        }
        public String toString(){
            return text;
        }

        public List<FilterFunction> getFunctions(){
            return functions;
        }

        public RegionStructure getStructure(){
            return structure;
        }
    }

    public enum ENTRY_METHODS {
        LAVA_LAKE("lava lake", 7, new ArrayList<>(Arrays.asList(FunctionRepo.lavaCheck)), null),
        RUINED_PORTAL("ruined portal", 4, new ArrayList<>(Arrays.asList(FunctionRepo.portalSpawns, FunctionRepo.portalBiomes, FunctionRepo.portalGenerates, FunctionRepo.portalCheck)), Config.rp),
        MAGMA_RAVINE("magma ravine", 7, new ArrayList<>(Arrays.asList(FunctionRepo.magmaCheck)), null);
        private final String text;
        private int ironCount;
        private final List<FilterFunction> functions;
        private final RegionStructure structure;

        ENTRY_METHODS(String text, int ironCount, List<FilterFunction> functions, RegionStructure structure) {
            this.text = text;
            this.ironCount = ironCount;
            this.functions = functions;
            this.structure = structure;
        }

        public String toString() {
            return text;
        }

        public void setIronCount(int ironCount) {
            this.ironCount = ironCount;
        }

        public int getIronCount() {
            return ironCount;
        }

        public List<FilterFunction> getFunctions(){
            return functions;
        }

        public RegionStructure getStructure(){
            return structure;
        }
    }

    public static int addIntegerCheck(String action, int defaultint){
        String s;
        while(true){
            System.out.println(action+" (default: "+defaultint+")");
            s = in.nextLine();
            if(s.equals("")){
                return defaultint;
            }
            try{
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        }
    }

    public static boolean addBooleanCheck(String action) {
        return addBooleanCheck(action, "no");
    }

    public static boolean addBooleanCheck(String action, String defaultOption){
        String s;
        while(true){
            System.out.println(action+" (default: "+defaultOption+")");
            System.out.println("Possible options: yes, no");
            s = in.nextLine();
            if(defaultOption.equals("no")) {
                if (s.equals("yes")) {
                    return true;
                }
                if (s.equals("no") || s.equals("")) {
                    return false;
                }
            } else if(defaultOption.equals("yes")){
                if (s.equals("yes") || s.equals("")) {
                    return true;
                }
                if (s.equals("no")) {
                    return false;
                }
            }
        }
    }
}
