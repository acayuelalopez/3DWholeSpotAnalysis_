#!/usr/bin/env nextflow

// General Params
params.input_dir = params.input_dir ?: error('Debe especificarse el parámetro --input_dir')
params.output_dir = params.output_dir ?: error('Debe especificarse el parámetro --output_dir')
params.project_dir = params.project_dir ?: error('Debe especificarse el parámetro --project_dir')



// Analysis Type: these are boolean parameters expected as true/false
params.perTelomere = params.perTelomere ?: false
params.perNuclei = params.perNuclei ?: false
params.perImage = params.perImage ?: false
params.perAnimal = params.perAnimal ?: false


params.output_dir_images = "${params.output_dir}/images"
params.output_dir_seg = "${params.output_dir}/labels"
params.output_dir_merge = "${params.output_dir}/merge"
params.output_dir_csv = "${params.output_dir}/csv"
//params.nucleiCh = 0
//params.telomereCh = 2
//params.markerCh = 1
params.nucleiCh = params.nucleiCh ?: 0

params.telomereCh = params.telomereCh ?: 2
params.markerCh = params.markerCh ?: 1

// **Paths using the project_dir**
params.fijiLifToTif = "${params.project_dir}/Scripts/groovy/src/main/splitFiles.groovy"


params.fiji3DAnalysis_perAnimal = "${params.project_dir}/Scripts/groovy/src/main/3DpositiveQuantification_perAnimal.groovy"
params.fiji3DAnalysis_perTelomere = "${params.project_dir}/Scripts/groovy/src/main/3DTelomereAnalysis_perTelomere.groovy"
params.fiji3DAnalysis_perNuclei = "${params.project_dir}/Scripts/groovy/src/main/3DTelomereAnalysis_perNucleus.groovy"
params.fiji3DAnalysis_perImage = "${params.project_dir}/Scripts/groovy/src/main/3DTelomereAnalysis_perImage.groovy"

params.generatePlots = "${params.project_dir}/Scripts/R/generate_plots.R"


params.fiji_path = '/home/analysisuser/Fiji/Fiji.app/ImageJ-linux64'  


//params.fiji_path = '/home/confocal/Fiji.app/Fiji.app/ImageJ-linux64'  



params.fijiMerge = "${params.project_dir}/Scripts/groovy/src/main/mergeRawSeg.groovy"


// Cellpose Params
params.cellpose_model_nuclei = "${params.project_dir}/Scripts/cellpose/models/model_nuclei"
params.cellpose_model_telomere = "${params.project_dir}/Scripts/cellpose/models/model_telomer.901241"
//params.NucDiam = 60
//params.TelDiam = 6.20

params.NucDiam = params.NucDiam ?: error('Debe especificarse el parámetro NucDiam')
params.TelDiam = params.TelDiam ?: error('Debe especificarse el parámetro TelDiam')

// Python params
params.pythonPlots = "${params.project_dir}/Scripts/python/plot_analysis.py"

// Process to create the directories (if needed) 
process createDirectories {
    """
    echo "Creating directories..."
    mkdir -p ${params.output_dir_images}
    mkdir -p ${params.output_dir}/merge
    mkdir -p ${params.output_dir_csv}
    mkdir -p ${params.output_dir_seg}
    """
}

process convertLifToTiff {
    publishDir "${params.output_dir_images}", mode: 'copy', overwrite: true

    input:
    path lif_file

    output:
    path "*.tif"

    shell:
    """
    echo "Running convertLifToTiff with input: ${lif_file}"
    ${params.fiji_path} --ij2 --headless --console --run "${params.fijiLifToTif}" "input='${lif_file}', output='./',nucleiCh=${params.nucleiCh},telCh=${params.telomereCh},markerCh=${params.markerCh}"
    echo "Finished convertLifToTiff"
    """
}

process segmentWithCellposeNuc {
    input:
    path tiff_files

    output:
    path "*_cp_masks.tif"

    publishDir "${params.output_dir_seg}", mode: 'copy', overwrite: true

    shell:
    """
    source /home/analysisuser/miniforge3/etc/profile.d/conda.sh && conda activate cellpose && \
    python -m cellpose --use_gpu --dir ./ --img_filter nuclei --pretrained_model ${params.cellpose_model_nuclei} --chan 1 --diameter ${params.NucDiam} --save_tif --savedir ./ --verbose --no_npy --stitch_threshold 0.1
    """
}

process segmentWithCellposeTel {
    input:
    path tiff_files

    output:
    path "*_cp_masks.tif"

    publishDir "${params.output_dir_seg}", mode: 'copy', overwrite: true

    shell:
    """
    source /home/analysisuser/miniforge3/etc/profile.d/conda.sh && conda activate cellpose && \
    python -m cellpose --use_gpu --dir ./ --img_filter telomere --pretrained_model ${params.cellpose_model_telomere} --chan 1 --diameter ${params.TelDiam} --save_tif --savedir ./ --verbose --no_npy --stitch_threshold 0.1
    """
}

process mergeImages {
    input:
    path tiff_files

    output:
    path "*.tif"

    publishDir "${params.output_dir_merge}", mode: 'copy', overwrite: true

    shell:
    """
    ${params.fiji_path} --ij2 --headless --console --run "${params.fijiMerge}" "inputFilesRaw='${params.output_dir_images}',inputFilesSeg='${params.output_dir_seg}',outputDir='./',nucleiChannel=${params.nucleiCh},telomereChannel=${params.telomereCh},markerChannel=${params.markerCh}"
    """
}


process TelomereAnalysis_perTelomere {   
    input:
    path tiff_files

    output:
    path "*.csv"

    publishDir "${params.output_dir_csv}", mode: 'copy', overwrite: true

    shell:
    """
    echo "Running TelomereAnalysis_perTelomere with the following paths:"
    echo "inputFilesRaw = ${params.input_dir}"
    echo "inputFilesSeg = ${params.output_dir_seg}"
    echo "outputDir = ${params.output_dir_csv}" 
    echo "nucleiChannel = ${params.nucleiCh}, telomereChannel = ${params.telomereCh}, markerChannel = ${params.markerCh}"
	
    
    ${params.fiji_path} --ij2 --headless --console --run "${params.fiji3DAnalysis_perTelomere}" "inputFilesRaw='${params.output_dir_images}',inputFilesSeg='${params.output_dir_seg}',outputDir='./',nucleiChannel=${params.nucleiCh},telomereChannel=${params.telomereCh}"
    """
}


process TelomereAnalysis_perNuclei {   
    input:
    path tiff_files

    output:
    path "*.csv"

    publishDir "${params.output_dir_csv}", mode: 'copy', overwrite: true

    shell:
    """
    echo "Running TelomereAnalysis_perNuclei with the following paths:"
    echo "inputFilesRaw = ${params.input_dir}"
    echo "inputFilesSeg = ${params.output_dir_seg}"
    echo "outputDir = ${params.output_dir_csv}" 
    echo "nucleiChannel = ${params.nucleiCh}, telomereChannel = ${params.telomereCh}, markerChannel = ${params.markerCh}"
    
    
    
    ${params.fiji_path} --ij2 --headless --console --run "${params.fiji3DAnalysis_perNuclei}" "inputFilesRaw='${params.output_dir_images}',inputFilesSeg='${params.output_dir_seg}',outputDir='./',nucleiChannel=${params.nucleiCh},telomereChannel=${params.telomereCh},markerChannel=${params.markerCh}"
    """
}


process TelomereAnalysis_perImage {   
    input:
    path tiff_files

    output:
    path "*.csv"

    publishDir "${params.output_dir_csv}", mode: 'copy', overwrite: true

    shell:
    """
    echo "Running TelomereAnalysis_perImage with the following paths:"
    echo "inputFilesRaw = ${params.input_dir}"
    echo "inputFilesSeg = ${params.output_dir_seg}"
    echo "outputDir = ${params.output_dir_csv}" 
    echo "nucleiChannel = ${params.nucleiCh}, telomereChannel = ${params.telomereCh}, markerChannel = ${params.markerCh}"
    
    
    
    ${params.fiji_path} --ij2 --headless --console --run "${params.fiji3DAnalysis_perImage}" "inputFilesRaw='${params.output_dir_images}',inputFilesSeg='${params.output_dir_seg}',outputDir='./',nucleiChannel=${params.nucleiCh},telomereChannel=${params.telomereCh},markerChannel=${params.markerCh}"
    """
}

process TelomereAnalysis_perAnimal {
    input:
    path tiff_files

    output:
    path "*.csv"

    publishDir "${params.output_dir_csv}", mode: 'copy', overwrite: true

    shell:
    """
    echo "Running TelomereAnalysis_perAnimal with the following paths:"
    echo "inputFilesRaw = ${params.input_dir}"
    echo "inputFilesSeg = ${params.output_dir_seg}"
    echo "outputDir = ${params.output_dir_csv}" 
    echo "nucleiChannel = ${params.nucleiCh}, telomereChannel = ${params.telomereCh}, markerChannel = ${params.markerCh}"

    
    ${params.fiji_path} --ij2 --headless --console --run "${params.fiji3DAnalysis_perAnimal}" "inputFilesRaw='${params.output_dir_images}',inputFilesSeg='${params.output_dir_seg}',outputDir='./',nucleiChannel=${params.nucleiCh},telomereChannel=${params.telomereCh},markerChannel=${params.markerCh}"
    """
}

process mergeCsv {
    input:
    path analysis_files 

    output:
    path "merged_analysis.csv"

    publishDir "${params.output_dir}", mode: 'copy', overwrite: true

    shell:
    """
    csvstack ${analysis_files.join(' ')} > merged_analysis.csv
    """
}

process generatePlots {
    input:
    val output_dir



    publishDir "${params.output_dir}/plots", mode: 'copy', overwrite: true

    script:
    """
    Rscript ${params.generatePlots} "${output_dir}"
    """
}

workflow {
    createDirectories()

    images = Channel.fromPath("${params.input_dir}", type: 'dir')

    tiff_files = convertLifToTiff(images)

    segmented_nuc_files = segmentWithCellposeNuc(tiff_files)

    segmented_tel_files = segmentWithCellposeTel(tiff_files)

    merged_images = mergeImages(segmented_tel_files)

    // Create a channel to collect all CSV files generated
    analysis_files = Channel.empty()

    if (params.perTelomere) {
        telomere_csv = TelomereAnalysis_perTelomere(segmented_tel_files)
        analysis_files = analysis_files.mix(telomere_csv)
    }

    if (params.perNuclei) {
        nuclei_csv = TelomereAnalysis_perNuclei(segmented_tel_files)
        analysis_files = analysis_files.mix(nuclei_csv)
    }

    if (params.perImage) {
        image_csv = TelomereAnalysis_perImage(segmented_tel_files)
        analysis_files = analysis_files.mix(image_csv)
    }

    if (params.perAnimal) {
        animal_csv = TelomereAnalysis_perAnimal(segmented_tel_files)
        analysis_files = analysis_files.mix(animal_csv)
    }

    // Wait till all CSV files are generated before funning generatePlots
    generatePlots(analysis_files.collect().map { params.output_dir })
}
