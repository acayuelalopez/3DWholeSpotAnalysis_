import ij.IJ
import ij.ImagePlus
import ij.measure.ResultsTable
import ij.plugin.ChannelSplitter
import ij.plugin.Duplicator
import inra.ijpb.measure.ResultsBuilder
import inra.ijpb.plugins.AnalyzeRegions
import inra.ijpb.plugins.AnalyzeRegions3D
import inra.ijpb.measure.IntensityMeasures
import mcib3d.geom.Object3D
import mcib3d.geom.Objects3DPopulation
import mcib3d.image3d.ImageInt


import java.io.File


// INPUT UI
//

#@File(label = " Input Raw Files Directory", style = "directory") inputFilesRaw
#@File(label = " Input Seg Files Directory", style = "directory") inputFilesSeg
#@File(label = "output directory", style = "directory") outputDir
#@Integer(label = "Nuclei channel", value = 0) nucleiChannel
#@Integer(label = "Telomere channel", value = 2) telomereChannel

//def inputFilesRaw = new File("/mnt/imgserver/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output/images")
//def inputFilesSeg = new File("/mnt/imgserver/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output/labels")
//def outputDir = new File("/mnt/imgserver/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output/csv")
//def nucleiChannel = 0
//def telomereChannel = 2

// IDE
//
//
//def headless = true;
//new ij.ImageJ().setVisible(false);
//def inputFilesDir = new File("./output")
//def outputDir = new File("./output/csv")

IJ.log("-Parameters selected: ")
IJ.log("    -inputFilesNuclei: " +inputFilesSeg.toString())
IJ.log("    -inputRawFiles: " + inputFilesRaw.toString())
IJ.log("    -outputDir: " + outputDir.toString())
IJ.log("                                                           ");

/** Get 3D label (images) from input directory to evaluate telomer and nuclei intensity on both markers */
def listOfFiles = inputFilesRaw.listFiles();

// Define table (ResultsTable) to store the results per volume (per embryo or cell)
def rt = ResultsTable.getResultsTable();
if (rt == null) {
    rt = new ResultsTable();
}
def counter = 0.intValue()
for (def i = 0.intValue(); i < listOfFiles.length; i++) {


    if ((!listOfFiles[i].getName().contains("nuclei") && !listOfFiles[i].getName().contains("telomere") && !listOfFiles[i].getName().contains(".DS") && !listOfFiles[i].getName().contains("marker")) ) {
        /** Get telomere and nuclei labels */
        def labelTelomere = new ImagePlus(inputFilesSeg.getAbsolutePath()+File.separator+ replaceLast(listOfFiles[i].getName(), ".tif", ".tif_telomere_cp_masks.tif"))
        def labelNuclei = new ImagePlus(inputFilesSeg.getAbsolutePath()+File.separator+ replaceLast(listOfFiles[i].getName(), ".tif", ".tif_nuclei_cp_masks.tif"))
        //IJ.log(inputFilesSeg.getAbsolutePath()+File.separator+ replaceLast(listOfFiles[i].getName(), ".tif", ".tif_telomere_cp_masks.tif"))
        /** Get telomere and nuclei channels */
        def imp = new ImagePlus(inputFilesRaw.getAbsolutePath() + File.separator + listOfFiles[i].getName())
        def channels = ChannelSplitter.split(imp)
        def chNuclei = channels[nucleiChannel.intValue()]
        def chTelomere = channels[telomereChannel.intValue()]


        // Get Nuclei objects population
        def imgNuclei = ImageInt.wrap(extractCurrentStack(labelNuclei));
        def populationNuclei = new Objects3DPopulation(imgNuclei);
        // Get Nuclei signal
        def signalNuclei = ImageInt.wrap(extractCurrentStack(chNuclei));
        // Get Telomere objects population
        def imgTelomere = ImageInt.wrap(extractCurrentStack(labelTelomere));
        def populationTelomere = new Objects3DPopulation(imgTelomere)
        // Get Telomere signal
        def signalTelomere = ImageInt.wrap(extractCurrentStack(chTelomere));

        // Declare ArrayList to store mean values per nuclei
        def totalPopulationTelomereList = new ArrayList<Object3D>();
        def areaUnitList = new ArrayList<Double>();
        def areaPixList = new ArrayList<Double>();
        def volUnitList = new ArrayList<Double>();
        def volPixList = new ArrayList<Double>();
        def xCoordList = new ArrayList<Double>();
        def yCoordList = new ArrayList<Double>();
        def zCoordList = new ArrayList<Double>();
        def xCMList = new ArrayList<Double>();
        def yCMList = new ArrayList<Double>();
        def zCMList = new ArrayList<Double>();
        def maxIntList = new ArrayList<Double>();
        def minIntList = new ArrayList<Double>();
        def meanIntList = new ArrayList<Double>();
        def densityIntList = new ArrayList<Double>();

        // Iterate through nuclei population (nuclei objects)
        for (int n = 0; n < populationNuclei.getNbObjects(); n++) {
            // Declare ArrayList to store values per nuclei
            def areaUnitListTemp = new ArrayList<Double>();
            def areaPixListTemp = new ArrayList<Double>();
            def volUnitListTemp = new ArrayList<Double>();
            def volPixListTemp = new ArrayList<Double>();
            def xCoordListTemp = new ArrayList<Double>();
            def yCoordListTemp = new ArrayList<Double>();
            def zCoordListTemp = new ArrayList<Double>();
            def xCMListTemp = new ArrayList<Double>();
            def yCMListTemp = new ArrayList<Double>();
            def zCMListTemp = new ArrayList<Double>();
            def maxIntListTemp = new ArrayList<Double>();
            def minIntListTemp = new ArrayList<Double>();
            def meanIntListTemp = new ArrayList<Double>();
            def densityIntListTemp = new ArrayList<Double>();

            // Iterate through telomere population (telomere objects) for each nuclei
            for (int t = 0; t < populationTelomere.getNbObjects(); t++) {

                // Check if telomere is included in nuclei population. To keep just telomere per nuclei
                if (populationNuclei.getObject(n).includes(populationTelomere.getObject(t))) {
                    // Store telomere values per nuclei to calculate further the mean values per nuclei
                    totalPopulationTelomereList.add(populationTelomere.getObject(t))
                    areaUnitListTemp.add(populationTelomere.getObject(t).getAreaUnit())
                    areaPixListTemp.add(populationTelomere.getObject(t).getAreaPixels())
                    volUnitListTemp.add(populationTelomere.getObject(t).getVolumeUnit())
                    volPixListTemp.add(populationTelomere.getObject(t).getVolumePixels().doubleValue())
                    xCoordListTemp.add(populationTelomere.getObject(t).getCenterX())
                    yCoordListTemp.add(populationTelomere.getObject(t).getCenterY())
                    zCoordListTemp.add(populationTelomere.getObject(t).getCenterZ())
                    xCMListTemp.add(populationTelomere.getObject(t).getMassCenterX(imgTelomere))
                    yCMListTemp.add(populationTelomere.getObject(t).getMassCenterY(imgTelomere))
                    zCMListTemp.add(populationTelomere.getObject(t).getMassCenterZ(imgTelomere))
                    maxIntListTemp.add(populationTelomere.getObject(t).getPixMaxValue(signalTelomere))
                    minIntListTemp.add(populationTelomere.getObject(t).getPixMinValue(signalTelomere))
                    meanIntListTemp.add(populationTelomere.getObject(t).getPixMeanValue(signalTelomere))
                    densityIntListTemp.add(populationTelomere.getObject(t).getIntegratedDensity(signalTelomere))
                }
            }
            // Calculate average values per nuclei
            areaUnitList.add(areaUnitListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            areaPixList.add(areaPixListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            volUnitList.add(volUnitListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            volPixList.add(volPixListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            xCoordList.add(xCoordListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            yCoordList.add(yCoordListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            zCoordList.add(zCoordListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            xCMList.add(xCMListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            yCMList.add(xCMListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            zCMList.add(xCMListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            maxIntList.add(maxIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            minIntList.add(maxIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            meanIntList.add(meanIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            densityIntList.add(densityIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))


        }

        def totalPopulationTelomere = new Objects3DPopulation(totalPopulationTelomereList)


        // Iterate through nuclei population 3D objects
        for (int r = 0.intValue(); r < populationNuclei.getNbObjects(); r++) {

            int[] numbers = populationNuclei.getObject(r).getNumbering(imgTelomere)
            def zRange = (populationNuclei.getObject(r).zmax.doubleValue() - populationNuclei.getObject(r).zmin.doubleValue())
            // Iterate through each telomere population per nuclei
            for (int t = 0; t <populationTelomere.getNbObjects(); t++) {
                if (populationNuclei.getObject(r).includes(populationTelomere.getObject(t))) {
                    rt.incrementCounter();
                    counter++

                    //Telomere Calculations
                    //Add values per Telomere
                    rt.setValue("Image Name", counter, listOfFiles[i].getName());
                    rt.setValue("Telomere Label", counter, populationTelomere.getObject(t).getValue());
                    rt.setValue("Nuclei ZRange", counter, zRange.toString())
                    rt.setValue("Nuclei Label", counter, populationNuclei.getObject(r).getValue());
                    rt.setValue("N of Telomeres per Nucleus", counter, numbers[0]);
                    rt.setValue("Nucleus Volume occupied by Telomeres", counter, numbers[1]);
                    rt.setValue("Telomere Area (units) per Telomere", counter, populationTelomere.getObject(t).getAreaUnit());
                    rt.setValue("Telomere Area (pix) per Telomere", counter, populationTelomere.getObject(t).getAreaPixels());
                    rt.setValue("Telomere Volume (units) per Telomere", counter, populationTelomere.getObject(t).getVolumeUnit());
                    rt.setValue("Telomere Volume (pix) per Telomere", counter, populationTelomere.getObject(t).getVolumePixels());
                    rt.setValue("Telomere X Coord per Telomere", counter, populationTelomere.getObject(t).getCenterX());
                    rt.setValue("Telomere Y Coord per Telomere", counter, populationTelomere.getObject(t).getCenterY());
                    rt.setValue("Telomere Z Coord per Telomere", counter, populationTelomere.getObject(t).getCenterZ());
                    rt.setValue("Telomere Max Intensity per Telomere", counter, populationTelomere.getObject(t).getPixMaxValue(signalTelomere))
                    rt.setValue("Telomere Min Intensity per Telomere", counter, populationTelomere.getObject(t).getPixMinValue(signalTelomere))
                    rt.setValue("Telomere Mean Intensity per Telomere", counter, populationTelomere.getObject(t).getPixMeanValue(signalTelomere))
                    rt.setValue("Telomere Sum Intensity per Telomere", counter, populationTelomere.getObject(t).getIntegratedDensity(signalTelomere));
                    //Nucleus Calculations
                    //Add values per Nucleus
                    rt.setValue("Nucleus Area (units) per Nucleus", counter, populationNuclei.getObject(r).getAreaUnit());
                    rt.setValue("Nucleus Area (pix) per Nucleus", counter, populationNuclei.getObject(r).getAreaPixels());
                    rt.setValue("Nucleus Volume (units) per Nucleus", counter, populationNuclei.getObject(r).getVolumeUnit());
                    rt.setValue("Nucleus Volume (pix) per Nucleus", counter, populationNuclei.getObject(r).getVolumePixels());
                    rt.setValue("Nucleus X Coord per Nucleus", counter, populationNuclei.getObject(r).getCenterX());
                    rt.setValue("Nucleus Y Coord per Nucleus", counter, populationNuclei.getObject(r).getCenterY());
                    rt.setValue("Nucleus Z Coord per Nucleus", counter, populationNuclei.getObject(r).getCenterZ());
                    rt.setValue("Nucleus Min Intensity per Nucleus", counter, populationNuclei.getObject(r).getPixMinValue(signalNuclei));
                    rt.setValue("Nucleus Max Intensity per Nucleus", counter, populationNuclei.getObject(r).getPixMaxValue(signalNuclei))
                    rt.setValue("Nucleus Mean Intensity per Nucleus", counter, populationNuclei.getObject(r).getPixMeanValue(signalNuclei));
                    rt.setValue("Nucleus Sum Intensity per Nucleus", counter, populationNuclei.getObject(r).getIntegratedDensity(signalNuclei));
                    //Average per nucleus
                    //Add average values per Nucleus
                    rt.setValue("Telomere Area (units)-Avg per Nucleus", counter, areaUnitList.get(r));
                    rt.setValue("Telomere Area (pix)-Avg per Nucleus", counter, areaPixList.get(r));
                    rt.setValue("Telomere Volume (units)-Avg per Nucleus", counter, volUnitList.get(r));
                    rt.setValue("Telomere Volume (pix)-Avg per Nucleus", counter, volPixList.get(r));
                    rt.setValue("Telomere Max Intensity-Avg per Nucleus", counter, maxIntList.get(r))
                    rt.setValue("Telomere Min Intensity-Avg per Nucleus", counter, minIntList.get(r))
                    rt.setValue("Telomere Mean Intensity-Avg per Nucleus", counter, meanIntList.get(r))
                    rt.setValue("Telomere Sum Intensity-Avg per Nucleus", counter, densityIntList.get(r));




                }
            }
        }


    }
}

//Save results table per cell or volume as csv file
rt.saveAs(outputDir.getAbsolutePath() + File.separator + "3DTelomereAnalysis_perTelomere.csv")

ImagePlus extractCurrentStack(ImagePlus plus) {
    // check dimensions
    int[] dims = plus.getDimensions();//XYCZT
    int channel = plus.getChannel();
    int frame = plus.getFrame();
    ImagePlus stack;
    // crop actual frame
    if ((dims[2] > 1) || (dims[4] > 1)) {
        IJ.log("hyperstack found, extracting current channel " + channel + " and frame " + frame);
        def duplicator = new Duplicator();
        stack = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
    } else stack = plus.duplicate();

    return stack;
}

IJ.log("Done!!!")


 public static String replaceLast(String text, String searchString, String replacement) {
        if (text == null || searchString == null || replacement == null) {
            return text;
        }
        
        // Find the last occurrence of the searchString
        int lastIndex = text.lastIndexOf(searchString);
        
        // If the searchString is not found, return the original text
        if (lastIndex == -1) {
            return text;
        }
        
        // Build the new string by concatenating parts of the original text
        String beginning = text.substring(0, lastIndex);
        String ending = text.substring(lastIndex + searchString.length());
        return beginning + replacement + ending;
    }
