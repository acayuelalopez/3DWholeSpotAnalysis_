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
//def headless = true;
//new ij.ImageJ().setVisible(false);
//def inputFilesDir = new File("./output")
//def outputDir = new File("./output/csv")

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
        def meanTotalTelomerePerNucleusPerImage = new ArrayList<Double>();
        def meanVolOcupPerNucleusPerImage = new ArrayList<Double>();
        def meanNucleiVolumePerImage = new ArrayList<Double>();
        def densityIntListAvgPerImage = new ArrayList<Double>();
        def densityIntListTotalPerImage = new ArrayList<Double>();
        def meanIntListAvgPerImage = new ArrayList<Double>();
        def meanIntListTotalPerImage = new ArrayList<Double>();
        def minIntListAvgPerImage = new ArrayList<Double>();
        def maxIntListAvgPerImage = new ArrayList<Double>();
        def stdIntListAvgPerImage = new ArrayList<Double>();
        def densityIntListAvgPositivePerImage = new ArrayList<Double>();
        def densityIntListTotalPositivePerImage = new ArrayList<Double>();
        def meanIntListAvgPositivePerImage = new ArrayList<Double>();
        def meanIntListTotalPositivePerImage = new ArrayList<Double>();
        def nucleiTotalPositivePerImage = new ArrayList<Double>();
        def zRangePerImage = new ArrayList<Double>();

//        def merge = null
//        if (markerChannel != -1)
//            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomer, chGFP}, false)
//        else
//            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomer}, false)
//        //Save merge image
//        IJ.saveAs(merge, "Tiff", outputDir.getAbsolutePath().replaceAll("csv", "merge") + File.separator + listOfFiles[i].getName())

        // Iterate through nuclei population (nuclei objects)
        for (int n = 0; n < populationNuclei.getNbObjects(); n++) {
            meanTotalTelomerePerNucleusPerImage.add(populationNuclei.getObject(n).getNumbering(imgTelomere)[0].doubleValue())
            meanNucleiVolumePerImage.add(populationNuclei.getObject(n).getVolumeUnit())
            meanVolOcupPerNucleusPerImage.add(populationNuclei.getObject(n).getNumbering(imgTelomere)[1].doubleValue())
            //ZRange per nuclei
            zRangePerImage.add((populationNuclei.getObject(n).zmax.doubleValue() - populationNuclei.getObject(n).zmin.doubleValue()))
            // Declare ArrayList to store values per nuclei

            def densityIntListTemp = new ArrayList<Double>();
            def meanIntListTemp = new ArrayList<Double>();
            def maxIntListTemp = new ArrayList<Double>();
            def minIntListTemp = new ArrayList<Double>();
            def stdIntListTemp = new ArrayList<Double>();

            // Iterate through telomere population (telomere objects) for each nuclei
            for (int t = 0; t < populationTelomere.getNbObjects(); t++) {

                // Check if telomere is included in nuclei population. To keep just telomere per nuclei
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
            densityIntListAvgPerImage.add(densityIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            densityIntListTotalPerImage.add(densityIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .sum())
            // Calculate average values per nuclei for mean intensity
            meanIntListAvgPerImage.add(meanIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            meanIntListTotalPerImage.add(meanIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .sum())

            // Calculate average values per nuclei for max intensity
            maxIntListAvgPerImage.add(maxIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))
            // Calculate average values per nuclei for min intensity
            minIntListAvgPerImage.add(minIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))

            // Calculate average values per nuclei for std intensity
            stdIntListAvgPerImage.add(stdIntListTemp.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0))

        }


        if (markerChannel != -1) {
            /** Process marker channel-DAPI single positive gfp : Get relevant DAPI nuclei to be quantify*/
            def gfpMeanPerObj = new ArrayList<Double>()
            def gfpStdPerObj = new ArrayList<Double>()
            for (def j = 0.intValue(); j < populationNuclei.getNbObjects(); j++) {
                gfpMeanPerObj.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
                gfpStdPerObj.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
            }
            def gfpMean = gfpMeanPerObj.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0).doubleValue()
            def gfpStd = gfpStdPerObj.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0).doubleValue()

            def counterPositive = 0.doubleValue()
            for (def j = 0.intValue(); j < populationNuclei.getNbObjects(); j++) {
                if (populationNuclei.getObject(j).getPixMeanValue(signalGFP) > (gfpMean + gfpStd)) {
                    counterPositive++
                    densityIntListAvgPositivePerImage.add(populationNuclei.getObject(j).getIntegratedDensity(signalGFP))
                    densityIntListTotalPositivePerImage.add(populationNuclei.getObject(j).getIntegratedDensity(signalGFP))
                    meanIntListAvgPositivePerImage.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
                    meanIntListTotalPositivePerImage.add(populationNuclei.getObject(j).getPixMeanValue(signalGFP))
                    nucleiTotalPositivePerImage.add(counterPositive)
                }
            }
        }


        // Iterate through images
        rt.incrementCounter();
        counter++
        rt.setValue("Image Name", counter, listOfFiles[i].getName());
        rt.setValue("ZRange-Avg per Image", counter,  zRangePerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0))
        rt.setValue("N of Nuclei per Image", counter, populationNuclei.getNbObjects())
        rt.setValue("N of Telomere per Image", counter, meanTotalTelomerePerNucleusPerImage.stream()
                .mapToDouble(d -> d)
                .sum())

        rt.setValue("Nucleus Volume (microns3)-Avg per Image", counter, meanNucleiVolumePerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0))

        rt.setValue("Nucleus Volume (microns3)-Sum per Image", counter, meanNucleiVolumePerImage.stream()
                .mapToDouble(d -> d)
                .sum())

        rt.setValue("N of Telomeres per Nucleus-Avg per Image", counter, meanTotalTelomerePerNucleusPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));

        rt.setValue("Nucleus Volume occupied by Telomeres per Nucleus-Avg per Image", counter, meanVolOcupPerNucleusPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));


        rt.setValue("Telomere Sum Intensity-Avg per Nucleus-Avg per Image", counter, densityIntListAvgPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));


        rt.setValue("Telomere Sum Intensity-Sum per Nucleus-Sum per Image", counter, densityIntListTotalPerImage.stream()
                .mapToDouble(d -> d)
                .sum());


        rt.setValue("Telomere Mean Intensity-Avg per Nucleus-Avg per Image", counter, meanIntListAvgPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));

        rt.setValue("Telomere Mean Intensity-Sum per Nucleus-Sum per Image", counter, meanIntListTotalPerImage.stream()
                .mapToDouble(d -> d)
                .sum());

        rt.setValue("Telomere Max Intensity-Avg per Nucleus-Avg per Image", counter, maxIntListAvgPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));

        rt.setValue("Telomere Min Intensity-Avg per Nucleus-Avg per Image", counter, minIntListAvgPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));

        rt.setValue("Telomere Std Intensity-Avg per Nucleus-Avg per Image", counter, stdIntListAvgPerImage.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0));


        if (markerChannel != -1) {
            rt.setValue("Marker Sum Intensity per Nucleus-Avg per Image(+)", counter,  densityIntListAvgPositivePerImage.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0));
            rt.setValue("Marker Sum Intensity per Nucleus-Sum per Image(+)", counter, densityIntListTotalPositivePerImage.stream()
                    .mapToDouble(d -> d)
                    .sum());
            rt.setValue("Marker Mean Intensity per Nucleus-Avg per Image(+)", counter, meanIntListAvgPositivePerImage.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0));
            rt.setValue("Marker Mean Intensity per Nucleus-Sum per Image(+)", counter, meanIntListTotalPositivePerImage.stream()
                    .mapToDouble(d -> d)
                    .sum());

            rt.setValue("N of (+) Nuclei per Image", counter,nucleiTotalPositivePerImage.size());


        }

    }
}
//Save results table per cell or volume as csv file
rt.saveAs(outputDir.getAbsolutePath() +File.separator+ "3DTelomereAnalysis_perImage.csv")

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



