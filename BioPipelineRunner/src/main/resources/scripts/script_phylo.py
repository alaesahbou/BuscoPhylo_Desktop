#!/usr/bin/env python3

import argparse
import multiprocessing as mp
import os
import sys
import traceback
from time import gmtime, strftime
import subprocess
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

try:
    from Bio import SeqIO
    from Bio.Seq import Seq
    from Bio.SeqRecord import SeqRecord
except ImportError:
    logger.error("Error: Biopython module is not installed. Please install it using 'pip install biopython'.")
    sys.exit(1)

def check_dependency(command):
    """Check if a dependency is available in the system."""
    try:
        subprocess.run(["which", command], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except subprocess.CalledProcessError:
        return False

# Define tools and default values
muscle = "muscle"
iqtree = "iqtree"
trimal = "trimal"
raxmlHPC = "raxmlHPC-PTHREADS"
outg = ""

def run_muscle(io):
    """Run MUSCLE alignment on a pair of input and output files."""
    try:
        cmd = f"{muscle} -in {io[0]} -out {io[1]}"
        logger.info(f"Running: {cmd}")
        result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if result.returncode != 0:
            logger.error(f"MUSCLE error: {result.stderr}")
            return False
        return True
    except Exception as e:
        logger.error(f"Error running MUSCLE: {str(e)}")
        return False

def run_trimal(io):
    """Run TrimAl on a pair of input and output files."""
    try:
        cmd = f"{trimal} -in {io[0]} -out {io[1]} -automated1"
        logger.info(f"Running: {cmd}")
        result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if result.returncode != 0:
            logger.error(f"TrimAl error: {result.stderr}")
            return False
        return True
    except Exception as e:
        logger.error(f"Error running TrimAl: {str(e)}")
        return False

def main():
    parser = argparse.ArgumentParser(description="Perform phylogenomic reconstruction using BUSCOs")

    parser.add_argument("-t", "--threads", type=int, help="Number of threads to use", required=True)
    parser.add_argument("-d", "--directory", type=str, help="Directory containing completed BUSCO runs", required=True)
    parser.add_argument("-o", "--output", type=str, help="Output directory to store results", required=True)
    parser.add_argument("-og", "--outgroup", type=str, help="Name of organism to root the tree", required=False)
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable verbose output")

    args = parser.parse_args()

    if args.verbose:
        logger.setLevel(logging.DEBUG)

    # Check dependencies
    dependencies = [muscle, trimal, iqtree]
    missing_deps = [dep for dep in dependencies if not check_dependency(dep)]
    if missing_deps:
        logger.error(f"Missing dependencies: {', '.join(missing_deps)}")
        logger.error("Please install all required dependencies before running this script.")
        sys.exit(1)

    start_directory = os.path.abspath(args.directory)
    working_directory = os.path.abspath(args.output)
    threads = int(args.threads)
    outg = args.outgroup

    logger.info(f"Starting directory: {start_directory}")
    logger.info(f"Output directory: {working_directory}")
    logger.info(f"Threads: {threads}")
    if outg:
        logger.info(f"Outgroup: {outg}")

    if os.path.isdir(start_directory):
        os.chdir(start_directory)
    else:
        logger.error(f"Error! {start_directory} is not a directory!")
        sys.exit(1)

    if os.path.isdir(working_directory):
        logger.error(f"Error! {working_directory} already exists")
        sys.exit(1)
    else:
        os.mkdir(working_directory)
        logger.info(f"Created output directory: {working_directory}")

    busco_dirs = [item for item in os.listdir(".") if item.startswith("run_") and os.path.isdir(item)]
    logger.info(f"Found {len(busco_dirs)} BUSCO runs:")

    for directory in busco_dirs:
        logger.info(f"\t{directory}")

    buscos = {}
    all_species = []

    for directory in busco_dirs:
        try:
            os.chdir(os.path.join(start_directory, directory))
            species = directory.split("run_")[1]
            all_species.append(species)
            logger.info(f"Processing {species}")

            busco_seq_dir = "busco_sequences/single_copy_busco_sequences"
            if not os.path.exists(busco_seq_dir):
                logger.warning(f"Directory {busco_seq_dir} not found in {directory}")
                continue

            os.chdir(busco_seq_dir)

            for busco in os.listdir("."):
                if busco.endswith(".faa"):
                    busco_name = busco[:-4]
                    try:
                        record = SeqIO.read(busco, "fasta")
                        new_record = SeqRecord(Seq(str(record.seq)), id=species, description="")

                        if busco_name not in buscos:
                            buscos[busco_name] = []

                        buscos[busco_name].append(new_record)
                    except Exception as e:
                        logger.error(f"Error reading {busco}: {str(e)}")
        except Exception as e:
            logger.error(f"Error processing directory {directory}: {str(e)}")
            traceback.print_exc()
        finally:
            os.chdir(start_directory)

    for busco in buscos:
        logger.info(f"{busco} {len(buscos[busco])}")

    logger.info("")

    single_copy_buscos = [busco for busco in buscos if len(buscos[busco]) == len(all_species)]

    if len(single_copy_buscos) == 0:
        logger.error("No single-copy BUSCOs found in all species! Cannot create phylogeny.")
        sys.exit(0)
    else:
        logger.info(f"{len(single_copy_buscos)} BUSCOs are single copy in all {len(all_species)} species")

    os.chdir(working_directory)
    os.makedirs("proteins", exist_ok=True)
    os.makedirs("alignments", exist_ok=True)
    os.makedirs("trimmed_alignments", exist_ok=True)

    for busco in single_copy_buscos:
        busco_seqs = buscos[busco]
        SeqIO.write(busco_seqs, os.path.join(working_directory, "proteins", busco + ".faa"), "fasta")

    mp_commands = [[os.path.join(working_directory, "proteins", busco + ".faa"),
                    os.path.join(working_directory, "alignments", busco + ".aln")] for busco in single_copy_buscos]

    logger.info(f"Running MUSCLE alignments for {len(mp_commands)} sequences...")
    
    pool = mp.Pool(processes=threads)
    results = pool.map(run_muscle, mp_commands)
    pool.close()
    pool.join()
    
    if not all(results):
        logger.error("Some MUSCLE alignments failed!")
    else:
        logger.info("All MUSCLE alignments completed successfully.")

    mp_commands = [[os.path.join(working_directory, "alignments", busco + ".aln"),
                    os.path.join(working_directory, "trimmed_alignments", busco + ".trimmed.aln")] for busco in single_copy_buscos]

    logger.info(f"Running TrimAl on {len(mp_commands)} alignments...")
    
    pool = mp.Pool(processes=threads)
    results = pool.map(run_trimal, mp_commands)
    pool.close()
    pool.join()
    
    if not all(results):
        logger.error("Some TrimAl operations failed!")
    else:
        logger.info("All TrimAl operations completed successfully.")

    logger.info("Creating supermatrix...")
    os.chdir(os.path.join(working_directory, "trimmed_alignments"))
    alignments = {species: "" for species in all_species}

    for alignment in os.listdir("."):
        try:
            for record in SeqIO.parse(alignment, "fasta"):
                if str(record.id) in alignments:
                    alignments[str(record.id)] += str(record.seq)
        except Exception as e:
            logger.error(f"Error processing alignment {alignment}: {str(e)}")

    os.chdir(working_directory)
    with open("SUPERMATRIX.aln", "w") as fo:
        for species in alignments:
            fo.write(">" + species + "\n")
            fo.write(alignments[species] + "\n")

    logger.info("Running final trimming on supermatrix...")
    trimal_cmd = f"{trimal} -in SUPERMATRIX.aln -out SUPERMATRIX.trimmed.aln -automated1"
    try:
        subprocess.run(trimal_cmd, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        logger.error(f"Error running TrimAl on supermatrix: {str(e)}")
        sys.exit(1)

    logger.info("Running IQ-TREE on trimmed supermatrix...")
    iqtree_cmd = f"{iqtree} -s SUPERMATRIX.trimmed.aln -bb 1000 -alrt 1000 -nt AUTO -ntmax {threads} -safe"
    try:
        subprocess.run(iqtree_cmd, shell=True, check=True)
        logger.info("IQ-TREE completed successfully")
    except subprocess.CalledProcessError as e:
        logger.error(f"Error running IQ-TREE: {str(e)}")
        sys.exit(1)

    logger.info("Phylogenetic analysis completed successfully!")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.error("Process interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}")
        traceback.print_exc()
        sys.exit(1)