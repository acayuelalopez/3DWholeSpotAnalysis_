# Nextflow Pipeline

This repository contains a Nextflow pipeline for processing and analyzing microscopy images. The pipeline includes steps for converting LIF files to TIFF, segmenting images using Cellpose, merging images, and performing telomere analysis.

## Requirements

- Nextflow
- Fiji
- Cellpose
- Conda

## Parameters

- `--input_dir`: Directory containing input files (required)
- `--output_dir`: Directory to store output files (required)
- `--project_dir`: Directory containing project scripts (required)
- `--perTelomere`: Boolean flag for per telomere analysis (default: false)
- `--perNuclei`: Boolean flag for per nuclei analysis (default: false)
- `--perImage`: Boolean flag for per image analysis (default: false)
- `--perAnimal`: Boolean flag for per animal analysis (default: false)
- `--NucDiam`: Diameter for nuclei segmentation (required)
- `--TelDiam`: Diameter for telomere segmentation (required)

## Directory Structure

- `output_dir/images`: Directory for storing converted TIFF images
- `output_dir/labels`: Directory for storing segmented images
- `output_dir/merge`: Directory for storing merged images
- `output_dir/csv`: Directory for storing analysis results

## Scripts

- `splitFiles.groovy`: Script for converting LIF files to TIFF
- `3DpositiveQuantification_perAnimal.groovy`: Script for 3D analysis per animal
- `3DTelomereAnalysis_perTelomere.groovy`: Script for 3D telomere analysis per telomere
- `3DTelomereAnalysis_perNucleus.groovy`: Script for 3D telomere analysis per nucleus
- `3DTelomereAnalysis_perImage.groovy`: Script for 3D telomere analysis per image
- `generate_plots.R`: R script for generating plots
- `mergeRawSeg.groovy`: Script for merging raw and segmented images
- `plot_analysis.py`: Python script for plotting analysis results

## Processes

### createDirectories

Creates necessary output directories.

### convertLifToTiff

Converts LIF files to TIFF format using Fiji.

### segmentWithCellposeNuc

Segments nuclei in TIFF images using Cellpose.

### segmentWithCellposeTel

Segments telomeres in TIFF images using Cellpose.

### mergeImages

Merges raw and segmented images using Fiji.

### TelomereAnalysis_perTelomere

Performs telomere analysis per telomere using Fiji.

### TelomereAnalysis_perNuclei

Performs telomere analysis per nuclei using Fiji.

### TelomereAnalysis_perImage

Performs telomere analysis per image using Fiji.

### TelomereAnalysis_perAnimal

Performs telomere analysis per animal using Fiji.

### mergeCsv

Merges all generated CSV files into a single file.

### generatePlots

Generates plots from the analysis results using R.

## Workflow

The workflow includes the following steps:

1. **createDirectories**: Creates necessary output directories.
2. **convertLifToTiff**: Converts LIF files to TIFF format using Fiji.
3. **segmentWithCellposeNuc**: Segments nuclei in TIFF images using Cellpose.
4. **segmentWithCellposeTel**: Segments telomeres in TIFF images using Cellpose.
5. **mergeImages**: Merges raw and segmented images using Fiji.
6. **TelomereAnalysis_perTelomere**: Performs telomere analysis per telomere (if `--perTelomere` is true).
7. **TelomereAnalysis_perNuclei**: Performs telomere analysis per nuclei (if `--perNuclei` is true).
8. **TelomereAnalysis_perImage**: Performs telomere analysis per image (if `--perImage` is true).
9. **TelomereAnalysis_perAnimal**: Performs telomere analysis per animal (if `--perAnimal` is true).
10. **mergeCsv**: Merges all generated CSV files into a single file.
11. **generatePlots**: Generates plots from the analysis results using R.

## Usage

To run the pipeline, use the following command:

```bash
nextflow run workflow_nextflow.nf --input_dir <input_directory> --output_dir <output_directory> --project_dir <project_directory> --NucDiam <nuclei_diameter> --TelDiam <telomere_diameter> [--perTelomere] [--perNuclei] [--perImage] [--perAnimal]
