import ij.IJ
import ij.ImagePlus
import ij.plugin.ChannelSplitter
import ij.plugin.RGBStackMerge
import mcib3d.image3d.ImageInt

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

#@File(label = " Input Raw Files Directory", style = "directory") inputFilesRaw
#@File(label = " Input Seg Files Directory", style = "directory") inputFilesSeg
#@File(label = "output directory", style = "directory") outputDir
#@Integer(label = "Nuclei channel", value = 0) nucleiChannel
#@Integer(label = "Telomere channel", value = 2) telomereChannel
#@Integer(label = "Marker channel", value = 1) markerChannel

//def inputFilesRaw= new File('/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output/images')
//def inputFilesSeg = new File('/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output/labels')
//def outputDir = new File('/run/user/752424298/gvfs/smb-share:server=imgserver,share=images/CONFOCAL/IA/generalimageanalysisgui/Projects/2025_02_25_dhernandez_Telomeres/Output')
//def nucleiChannel = 0
//def telomereChannel = 2
//def markerChannel = 1

def listOfFiles = inputFilesRaw.listFiles();

for (def i = 0.intValue(); i < listOfFiles.size(); i++) {
    if (!listOfFiles[i].getName().contains("nuclei") && !listOfFiles[i].getName().contains("telomere") && !listOfFiles[i].getName().contains("marker")) {
        /** Create image for each file in the input directory */
        def imps = new ImagePlus(inputFilesRaw.getAbsolutePath() + File.separator + listOfFiles[i].getName())

        /** Split channels */
        def channels = ChannelSplitter.split(imps)
        def labelTelomere, labelNuclei, chTelomere, chNuclei = null

        /** Get telomer channel */
        labelTelomere = new ImagePlus(inputFilesSeg.getAbsolutePath() + File.separator + listOfFiles[i].getName().replaceAll(".tif", ".tif_telomere_cp_masks.tif"))
        chTelomere = channels[telomereChannel.intValue()]

        /** Get nuclei channel */
        labelNuclei = new ImagePlus(inputFilesSeg.getAbsolutePath() + File.separator + listOfFiles[i].getName().replaceAll(".tif", ".tif_nuclei_cp_masks.tif"))
        chNuclei = channels[nucleiChannel.intValue()]

        def chGFP = null
        if (markerChannel != -1)
            chGFP = channels[markerChannel.intValue()]


        def merge = null
        if (markerChannel != -1)
            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomere, chGFP}, false)
        else
            merge = RGBStackMerge.mergeChannels(new ImagePlus[]{labelNuclei, chNuclei, labelTelomere, chTelomere}, false)
        //Save merge image
        //IJ.saveAs(merge, "Tiff", outputDir.getAbsolutePath().replaceAll("csv", "merge") + File.separator + listOfFiles[i].getName())

        /** Merge channels and save the result */
        IJ.saveAs(merge,"Tiff", outputDir.getAbsolutePath() + File.separator + listOfFiles[i].getName())
    }
}


// Function to get file titles from a directory
        def getFileTitles(String directoryPath) {
            def titles = []
            Files.walk(Paths.get(directoryPath)).each { path ->
                if (Files.isRegularFile(path)) {
                    def title = path.fileName.toString()
                    titles << title
                }
            }
            return titles
        }

        static ArrayList<String> getUniqueTitles(ArrayList<String> titles) {
            def seriesTitles = titles.stream()
                    .map { title ->
                        def seriesIndex = title.indexOf("Series")
                        if (seriesIndex != -1) {
                            return title.substring(0, seriesIndex + "Series".length()).trim()
                        } else {
                            return title
                        }
                    }
                    .distinct()
                    .collect(Collectors.toList())
            return seriesTitles
        }

private static boolean containsAny(String fileName, String[] words) {
    for (String word : words) {
        if (fileName.contains(word)) {
            return true;
        }
    }
    return false;
}
