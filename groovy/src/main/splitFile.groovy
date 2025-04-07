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
#@File(label = "Input File", style = "file") input_file
#@File(label = "Output directory", style = "directory") output
//def inputFilesDir = new File("C:/Users/acayuela/Desktop/users/buyuma/image")
//def outputDir = new File("C:/Users/acayuela/Desktop/users/buyuma/images")
// IDE
//
//
def headless = true;
//new ij.ImageJ().setVisible(true);
//def inputFilesDir = new File("/mnt/imgserver/CONFOCAL/IA/Projects/2024/2024_09_06_sburgaz/images")
//def outputDir = new File("/mnt/imgserver/CONFOCAL/IA/Projects/2024/2024_09_06_sburgaz/output/images")
IJ.log("-Parameters selected: ")
IJ.log("    -input File: " + input_file)
IJ.log("    -outputDir: " + output)
IJ.log("                                                           ");
/** Get files (images) from input directory */

IJ.log("Analyzing file: " + input_file.getName());
/** Define output directory per file */

def options = new ImporterOptions();
options.setId(input_file.getAbsolutePath());
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
    IJ.log("saved:    "+output.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", ""))
//            def channels = ChannelSplitter.split(imp)
//            IJ.saveAs(channels[0], "Tiff", outputDir.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_nuclei")
//            IJ.saveAs(channels[2], "Tiff", outputDir.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_telomere")
//            IJ.saveAs(channels[1], "Tiff", outputDir.getAbsolutePath() + File.separator + imps[j].getTitle().replaceAll("/", "") + "_marker")


}



