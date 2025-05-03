#!/usr/bin/env python3

import sys
import os
import argparse
import traceback

try:
    from ete3 import TextFace, Tree, faces, AttrFace, TreeStyle, NodeStyle
except ImportError:
    print("Error: ete3 module is not installed. Please install it using 'pip install ete3'.")
    sys.exit(1)

def parse_arguments():
    parser = argparse.ArgumentParser(description='Visualize phylogenetic tree using ETE3.')
    parser.add_argument('--tree', '-t', type=str, default="SUPERMATRIX.trimmed.aln.contree",
                      help='Path to the tree file (default: SUPERMATRIX.trimmed.aln.contree)')
    parser.add_argument('--outgroup', '-o', type=str, default=None,
                      help='Name of the outgroup')
    parser.add_argument('--output', '-p', type=str, default="tree",
                      help='Output filename prefix (default: tree)')
    return parser.parse_args()

def main():
    args = parse_arguments()
    outName = args.output
    tree_file = args.tree
    outgroup = args.outgroup

    # Try to read the tree file
    if not os.path.exists(tree_file):
        print(f"Error: Tree file {tree_file} not found.")
        sys.exit(1)

    try:
        t = Tree(tree_file)
    except Exception as e:
        print(f"Error reading tree file: {e}")
        traceback.print_exc()
        sys.exit(1)

    # Set outgroup if provided
    if outgroup:
        try:
            t.set_outgroup(outgroup)
            print(f"Set outgroup to {outgroup}")
        except Exception as e:
            print(f"Warning: Could not set outgroup {outgroup}: {e}")

    def layout(node):
        if node.is_leaf():
            N = AttrFace("name", fsize=8)
            faces.add_face_to_node(N, node, 0)

    ts = TreeStyle()
    ts.layout_fn = layout
    nstyle = NodeStyle()
    nstyle["size"] = 0
    ts.mode = "r"

    for n in t.traverse():
       n.set_style(nstyle)

    ts.show_leaf_name = False

    # Generate tree visualizations with different options
    try:
        # 1. With branch length and branch support
        ts.show_branch_length = True
        ts.show_branch_support = True
        
        # save the results as pdf
        t.render(outName + ".pdf", tree_style=ts)
        # save the results as png
        t.render(outName + ".png", w=600, units="px", tree_style=ts)
        # save the results as svg
        t.render(outName + ".svg", w=600, units="px", tree_style=ts)
        
        # 2. Without branch length, with branch support
        ts.show_branch_length = False
        ts.show_branch_support = True
        
        # save the results as pdf
        t.render(outName + "_nolength.pdf", tree_style=ts)
        # save the results as png
        t.render(outName + "_nolength.png", w=600, units="px", tree_style=ts)
        # save the results as svg
        t.render(outName + "_nolength.svg", w=600, units="px", tree_style=ts)
        
        # 3. With branch length, without branch support
        ts.show_branch_length = True
        ts.show_branch_support = False
        
        # save the results as pdf
        t.render(outName + "_nonode.pdf", tree_style=ts)
        # save the results as png
        t.render(outName + "_nonode.png", w=600, units="px", tree_style=ts)
        # save the results as svg
        t.render(outName + "_nonode.svg", w=600, units="px", tree_style=ts)
        
        # 4. Without branch length, without branch support
        ts.show_branch_length = False
        ts.show_branch_support = False
        
        # save the results as pdf
        t.render(outName + "_no.pdf", tree_style=ts)
        # save the results as png
        t.render(outName + "_no.png", w=600, units="px", tree_style=ts)
        # save the results as svg
        t.render(outName + "_no.svg", w=600, units="px", tree_style=ts)
        
        print("Tree visualizations generated successfully.")
    except Exception as e:
        print(f"Error generating tree visualizations: {e}")
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()