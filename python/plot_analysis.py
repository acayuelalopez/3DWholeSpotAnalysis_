import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import argparse
import os

def generate_plots(df, output_dir):
    # Plot 1: Distribution of Nuclei Sizes
    plt.figure(figsize=(10, 6))
    sns.histplot(df['Nuclei_Size'], bins=30, kde=True)
    plt.title('Distribution of Nuclei Sizes')
    plt.xlabel('Nuclei Size')
    plt.ylabel('Frequency')
    plt.savefig(os.path.join(output_dir, 'nuclei_size_distribution.png'))

    # Plot 2: Distribution of Telomere Sizes
    plt.figure(figsize=(10, 6))
    sns.histplot(df['Telomere_Size'], bins=30, kde=True)
    plt.title('Distribution of Telomere Sizes')
    plt.xlabel('Telomere Size')
    plt.ylabel('Frequency')
    plt.savefig(os.path.join(output_dir, 'telomere_size_distribution.png'))

    # Plot 3: Scatter Plot of Nuclei vs Telomere Sizes
    plt.figure(figsize=(10, 6))
    sns.scatterplot(x='Nuclei_Size', y='Telomere_Size', data=df)
    plt.title('Nuclei Size vs Telomere Size')
    plt.xlabel('Nuclei Size')
    plt.ylabel('Telomere Size')
    plt.savefig(os.path.join(output_dir, 'nuclei_vs_telomere_size.png'))

def main(csv_file, output_dir):
    # Load the CSV file into a DataFrame
    df = pd.read_csv(csv_file)
    generate_plots(df, output_dir)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Process CSV file for plotting.')
    parser.add_argument('csv_file', type=str, help='Path to the CSV file')
    parser.add_argument('output_dir', type=str, help='Path to the output directory')
    args = parser.parse_args()
    main(args.csv_file, args.output_dir)
