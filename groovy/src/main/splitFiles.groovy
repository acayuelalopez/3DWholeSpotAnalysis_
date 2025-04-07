import ij.IJ
import ij.ImagePlus
import ij.gui.PolygonRoi
import ij.gui.Roi
import ij.gui.ShapeRoi
import ij.measure.ResultsTable
import ij.plugin.ChannelSplitter
import ij.plugin.ZProjector
import ij.plugin.frame.RoiManager
import loci.plugins.BF
import loci.plugins.in.ImporterOptions
import org.apache.commons.compress.utils.FileNameUtils


// INPUT UI
//
#@File(label = "Input File Directory", style = "directory") input
#@File(label = "Output directory", style = "directory") output
#@Integer(label = "Nuclei Ch", default = 0) nucleiCh
#@Integer(label = "Telomere Ch", default = 1) telCh
#@Integer(label = "Marker Ch", default = 2) markerCh
//def inputFilesDir = new File("C:/Users/acayuela/Desktop/users/buyuma/image")
//def outputDir = new File("C:/Users/acayuela/Desktop/users/buyuma/images")
// IDE
//
//
//def headless = true;
//new ij.ImageJ().setVisible(true);
//def inputFilesDir = new File("/mnt/imgserver/CONFOCAL/IA/Projects/2024/2024_09_06_sburgaz/images")
//def outputDir = new File("/mnt/imgserver/CONFOCAL/IA/Projects/2024/2024_09_06_sburgaz/output/images")
IJ.log("-Parameters selected: ")
IJ.log("    -inputFileDir: " + input)
IJ.log("    -outputDir: " + output)
IJ.log("                                                           ");
/** Get files (images) from input directory */
def listOfFiles = input.listFiles();

for (def i = 0; i < listOfFiles.length; i++) {

    if (!listOfFiles[i].getName().contains("DS")) {
        IJ.log("Analyzing file: " + listOfFiles[i].getName());
        /** Define output directory per file */

        def options = new ImporterOptions();
        options.setId(input.getAbsolutePath() + File.separator + listOfFiles[i].getName());
        options.setSplitChannels(false);
        options.setSplitTimepoints(false);
        options.setSplitFocalPlanes(false);
        options.setAutoscale(true);
        options.setStackFormat(ImporterOptions.VIEW_HYPERSTACK);
        options.setStackOrder(ImporterOptions.ORDER_XYCZT);
        options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
        options.setCrop(false);
        options.setOpenAllSeries(true);
        def imps = BF.openImagePlus(options);


        for (int j = 0; j < imps.length; j++) {
            //if(!imps[j].getTitle().contains("Series") || !imps[j].getTitle().contains("settings") ) {
            def imp = imps[j]
            //imp = ZProjector.run(imp, "max")
            IJ.saveAs(imp, "Tiff", output.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", ""))
            def channels = ChannelSplitter.split(imp)
	        if(nucleiCh != -1)
                IJ.saveAs(channels[nucleiCh.intValue()], "Tiff", output.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_nuclei")
            if(telCh != -1)  
                IJ.saveAs(channels[telCh.intValue()], "Tiff", output.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_telomere")
            if(markerCh != -1) 
                IJ.saveAs(channels[markerCh.intValue()], "Tiff", output.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_marker")


        }

    }
}