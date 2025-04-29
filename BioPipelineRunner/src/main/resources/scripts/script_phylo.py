#!/usr/bin/env python

import argparse
import multiprocessing as mp
import os
import sys
from time import gmtime, strftime

from Bio import SeqIO
from Bio.Seq import Seq
from Bio.SeqRecord import SeqRecord

muscle = "muscle"
iqtree = "iqtree"
trimal = "trimal"
raxmlHPC = "raxmlHPC-PTHREADS"
outg = ""

def main():
    parser = argparse.ArgumentParser(description="Perform phylogenomic reconstruction using BUSCOs")

    parser.add_argument("-t", "--threads", type=int, help="Number of threads to use", required=True)
    parser.add_argument("-d", "--directory", type=str, help="Directory containing completed BUSCO runs", required=True)
    parser.add_argument("-o", "--output", type=str, help="Output directory to store results", required=True)
    parser.add_argument("-og", "--outgroup", type=str, help="Name of organism to root the tree", required=False)

    args = parser.parse_args()

    start_directory = os.path.abspath(args.directory)
    working_directory = os.path.abspath(args.output)
    threads = int(args.threads)
    outg = args.outgroup

    if os.path.isdir(start_directory):
        os.chdir(start_directory)
    else:
        print("Error! " + start_directory + " is not a directory!")
        sys.exit(1)

    if os.path.isdir(working_directory):
        print("Error! " + working_directory + " already exists")
        sys.exit(1)
    else:
        os.mkdir(working_directory)

    busco_dirs = [item for item in os.listdir(".") if item.startswith("run_") and os.path.isdir(item)]
    print("Found " + str(len(busco_dirs)) + " BUSCO runs:")

    for directory in busco_dirs:
        print("\t" + directory)

    print("")

    buscos = {}
    all_species = []

    for directory in busco_dirs:
        os.chdir(directory)
        species = directory.split("run_")[1]
        all_species.append(species)

        os.chdir("busco_sequences/single_copy_busco_sequences")

        for busco in os.listdir("."):
            if busco.endswith(".faa"):
                busco_name = busco[:-4]
                record = SeqIO.read(busco, "fasta")
                new_record = SeqRecord(Seq(str(record.seq)), id=species, description="")

                if busco_name not in buscos:
                    buscos[busco_name] = []

                buscos[busco_name].append(new_record)

    for busco in buscos:
        print(busco + " " + str(len(buscos[busco])))

    print("")

    single_copy_buscos = [busco for busco in buscos if len(buscos[busco]) == len(all_species)]

    if len(single_copy_buscos) == 0:
        sys.exit(0)
    else:
        print(str(len(single_copy_buscos)) + " BUSCOs are single copy in all " + str(len(all_species)) + " species")

    os.chdir(working_directory)
    os.makedirs("proteins")
    os.makedirs("alignments")
    os.makedirs("trimmed_alignments")

    for busco in single_copy_buscos:
        busco_seqs = buscos[busco]
        SeqIO.write(busco_seqs, os.path.join(working_directory, "proteins", busco + ".faa"), "fasta")

    mp_commands = [[os.path.join(working_directory, "proteins", busco + ".faa"),
                    os.path.join(working_directory, "alignments", busco + ".aln")] for busco in single_copy_buscos]

    pool = mp.Pool(processes=threads)
    pool.map(run_muscle, mp_commands)

    mp_commands = [[os.path.join(working_directory, "alignments", busco + ".aln"),
                    os.path.join(working_directory, "trimmed_alignments", busco + ".trimmed.aln")] for busco in single_copy_buscos]

    pool.map(run_trimal, mp_commands)

    os.chdir(os.path.join(working_directory, "trimmed_alignments"))
    alignments = {species: "" for species in all_species}

    for alignment in os.listdir("."):
        for record in SeqIO.parse(alignment, "fasta"):
            if str(record.id) in alignments:
                alignments[str(record.id)] += str(record.seq)

    os.chdir(working_directory)
    with open("SUPERMATRIX.aln", "w") as fo:
        for species in alignments:
            fo.write(">" + species + "\n")
            fo.write(alignments[species] + "\n")

    os.system("trimal -in SUPERMATRIX.aln -out SUPERMATRIX.trimmed.aln -automated1")

    os.system("iqtree -s SUPERMATRIX.trimmed.aln -bb 1000 -alrt 1000 -nt AUTO -ntmax " + str(threads) + " -safe > /dev/null")

def run_muscle(io):
    os.system("muscle -in " + io[0] + " -out " + io[1] + " > /dev/null 2>&1")

def run_trimal(io):
    os.system("trimal -in " + io[0] + " -out " + io[1] + " -automated1 ")

if __name__ == "__main__":
    main()