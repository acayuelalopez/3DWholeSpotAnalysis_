import ij.IJ
import ij.ImagePlus
import ij.measure.ResultsTable
import ij.plugin.ChannelSplitter
import ij.plugin.Duplicator
import ij.plugin.RGBStackMerge
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
#@File(label = " Input Seg Files Directory", style = "directory") inputFilesSeg
#@File(label = "Input Raw Files Directory", style = "directory") inputFilesRaw
#@File(label = "Output directory", style = "directory") outputDir
#@Integer(label = "Nuclei channel", value = 0) nucleiChannel
#@Integer(label = "Telomere channel", value = 2) telomereChannel
#@Integer(label = "Marker channel", value = 1) markerChannel
//def inputFilesNuclei = new File("/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/Projects/2024/2024_03_13_buyuma/output/nuclei")
//def inputFilesTelomere = new File("/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/Projects/2024/2024_03_13_buyuma/output/telomere")
//def inputFiles = new File("/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/Projects/2024/2024_03_13_buyuma/output/images")
//def outputDir = new File("/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/Projects/2024/2024_03_13_buyuma/output/csv")

// IDE
//
//


IJ.log("-Parameters selected: ")
IJ.log("    -inputFilesNuclei: " + inputFilesSeg.toString())
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

    if ((!listOfFiles[i].getName().contains("nuclei") && !listOfFiles[i].getName().contains("telomere") && !listOfFiles[i].getName().contains(".DS") && !listOfFiles[i].getName().contains("marker"))) {
        /** Get telomere and nuclei labels */
        def labelTelomere = new ImagePlus(inputFilesSeg.getAbsolutePath() + File.separator + replaceLast(listOfFiles[i].getName(), ".tif", ".tif_telomere_cp_masks.tif"))
        def labelNuclei = new ImagePlus(inputFilesSeg.getAbsolutePath() + File.separator + replaceLast(listOfFiles[i].getName(), ".tif", ".tif_nuclei_cp_masks.tif"))
        /** Get telomere and nuclei channels */
        def imp = new ImagePlus(inputFilesRaw.getAbsolutePath() + File.separator + listOfFiles[i].getName())
        def channels = ChannelSplitter.split(imp)
        def chNuclei = channels[nucleiChannel.intValue()]
        def chTelomer = channels[telomereChannel.intValue()]


        def chGFP, signalGFP = null

        if (markerChannel != -1) {

            chGFP = channels[markerChannel.intValue()]

            // Get GFP signal
            signalGFP = ImageInt.wrap(extractCurrentStack(chGFP));
        }
        // Get Nuclei objects population
        def imgNuclei = ImageInt.wrap(extractCurrentStack(labelNuclei));
        def populationNuclei = new Objects3DPopulation(imgNuclei);
        // Get Nuclei signal
        def signalNuclei = ImageInt.wrap(extractCurrentStack(chNuclei));
        // Get Telomere objects population
        def imgTelomere = ImageInt.wrap(extractCurrentStack(labelTelomere));
        def populationTelomere = new Objects3DPopulation(imgTelomere)
        // Get Telomere signal
        def signalTelomere = ImageInt.wrap(extractCurrentStack(chTelomer));

        // Declare ArrayList to store mean,max,min and std values per nuclei
        def densityIntListAvgPerNucleus = new ArrayList<Double>();
        def densityIntListTotalPerNucleus = new ArrayList<Double>();
        def meanIntListAvgPerNucleus = new ArrayList<Double>();
        def meanIntListTotalPerNucleus = new ArrayList<Double>();
        def minIntListAvgPerNucleus = new ArrayList<Double>();
        def maxIntListAvgPerNucleus = new ArrayList<Double>();
        def stdIntListAvgPerNucleus = new ArrayList<Double>();

//        def merge = null
//        if (markerChannel != -1)
//            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomer, chGFP}, false)
//        else
//            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomer}, false)
//        //Save merge image
//        IJ.saveAs(merge, "Tiff", outputDir.getAbsolutePath().replaceAll("csv", "merge") + File.separator + listOfFiles[i].getName())

        // Iterate through nuclei population (nuclei objects)
        for (int n = 0; n < populationNuclei.getNbObjects(); n++) {
            // Declare ArrayList to store values per nuclei

            def densityIntListTemp = new ArrayList<Double>();
            def meanIntListTemp = new ArrayList<Double>();
            def maxIntListTemp = new ArrayList<Double>();
            def minIntListTemp = new ArrayList<Double>();
            def stdIntListTemp = new ArrayList<Double>();

            // Iterate through telomere population (telomere objects) for each nuclei
            for (int t = 0; t < populationTelomere.getNbObjects(); t++) {

                if (populationNuclei.getObject(n).inside(populationTelomere.getObject(t).getCenterAsPoint())) {
                    // Store telomere values per nuclei to calculate further the mean values per nuclei
                    densityIntListTemp.add(populationTelomere.getObject(t).getIntegratedDensity(signalTelomere))
                    meanIntListTemp.add(populationTelomere.getObject(t).getPixMeanValue(signalTelomere))
                    maxIntListTemp.add(populationTelomere.getObject(t).getPixMaxValue(signalTelomere))
                    minIntListTemp.add(populationTelomere.getObject(t).getPixMinValue(signalTelomere))
                    stdIntListTemp.add(populationTelomere.getObject(t).getPixStdDevValue(signalTelomere))
                }
                //}
            }
            // Calculate average values per nuclei for integrated density
            densityIntListAvgPerNucleus.add(densityIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            densityIntListTotalPerNucleus.add(densityIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .sum())
            // Calculate average values per nuclei for mean intensity
            meanIntListAvgPerNucleus.add(meanIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            meanIntListTotalPerNucleus.add(meanIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .sum())

            // Calculate average values per nuclei for max intensity
            maxIntListAvgPerNucleus.add(maxIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            // Calculate average values per nuclei for min intensity
            minIntListAvgPerNucleus.add(minIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))

            // Calculate average values per nuclei for std intensity
            stdIntListAvgPerNucleus.add(stdIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))

        }

        def gfpMean, gfpStd = null
        if (markerChannel != -1) {
            /** Process marker channel-DAPI single positive gfp : Get relevant DAPI nuclei to be quantify*/
            def gfpMeanPerObj = new ArrayList<Double>()
            def gfpStdPerObj = new ArrayList<Double>()
            for (def j = 0.intValue(); j < populationNuclei.getNbObjects(); j++) {
                gfpMeanPerObj.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
                gfpStdPerObj.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
            }
            gfpMean = gfpMeanPerObj.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0).doubleValue()
            gfpStd = gfpStdPerObj.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0).doubleValue()
        }

        int r0 = rt.getCounter();
        // Iterate through nuclei population 3D objects
        for (int r = 0; r < populationNuclei.getNbObjects(); r++) {

            def zRange = (populationNuclei.getObject(r).zmax.doubleValue() - populationNuclei.getObject(r).zmin.doubleValue())

            counter++
            int[] numbers = populationNuclei.getObject(r).getNumbering(imgTelomere)
            // Iterate through each telomere population per nuclei
            rt.incrementCounter();
            //Telomere Calculations
            //Add values per Telomere
            rt.setValue("Image Name", counter, listOfFiles[i].getName());
            rt.setValue("Nuclei ZRange", counter, zRange.toString())
            rt.setValue("N of Nuclei per Image", counter, populationNuclei.getNbObjects())
            rt.setValue("Nuclei Label", counter, populationNuclei.getObject(r).getValue());
            rt.setValue("Nucleus Volume (microns3)", counter, populationNuclei.getObject(r).getVolumeUnit())
            rt.setValue("N of Telomeres per Nucleus", counter, numbers[0]);
            rt.setValue("Nucleus Volume occupied by Telomeres", counter, numbers[1]);
            //Average per nucleus
            //Add average for sum values per Nucleus
            rt.setValue("Telomere Sum Intensity-Avg per Nucleus", counter, densityIntListAvgPerNucleus.get(r));
            //Add sum values per Nucleus
            rt.setValue("Telomere Sum Intensity-Sum per Nucleus", counter, densityIntListTotalPerNucleus.get(r));
            //Add average for mean values per Nucleus
            rt.setValue("Telomere Mean Intensity-Avg per Nucleus", counter, meanIntListAvgPerNucleus.get(r));
            //Add mean values per Nucleus
            rt.setValue("Telomere Mean Intensity-Sum per Nucleus", counter, meanIntListTotalPerNucleus.get(r));
            //Add max values per Nucleus
            rt.setValue("Telomere Max Intensity-Avg per Nucleus", counter, maxIntListAvgPerNucleus.get(r));
            //Add min values per Nucleus
            rt.setValue("Telomere Min Intensity-Avg per Nucleus", counter, minIntListAvgPerNucleus.get(r));
            //Add std values per Nucleus
            rt.setValue("Telomere Std Intensity-Avg per Nucleus", counter, stdIntListAvgPerNucleus.get(r));

            if (markerChannel != -1) {
                rt.setValue("Marker Sum Intensity per Nucleus", counter, populationNuclei.getObject(r).getIntegratedDensity(signalGFP));
                rt.setValue("Marker Mean Intensity per Nucleus", counter, populationNuclei.getObject(r).getPixMeanValue(signalGFP));

                if (populationNuclei.getObject(r).getPixMeanValue(signalGFP) > (gfpMean + gfpStd))
                    rt.setValue("Nuclei Status", counter, "Positive")
                else
                    rt.setValue("Nuclei Status", counter, "Negative")
            }
        }


    }
}
//Save results table per cell or volume as csv file
rt.saveAs(outputDir.getAbsolutePath() +File.separator+ "3DTelomereAnalysis_perNucleus.csv")

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


