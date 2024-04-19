# PATcopier
Tool for copying visual effects from French Bread game's .pat files.

Usage:
java -jar PATcopier.jar "path/to/receiving.pat" "path/to/source.pat" "path/to/output.pat" startno endno [-k optional]

As many ranges of patterns to copy as desired can be entered; ie, '1 20 30 50' will give you 1->20, and 30->50.

By default, copied patterns are remapped to the first available unused pattern IDs in the receiving pat.
-k will attempt to preserve the original IDs of the patterns being copied, assuming no overlaps.

Known problems:
- Default shape 0 is copied despite not actually being used after copying (<100 bytes wasted)
- Copier does not check if copies are duplicates of existing objects; try not to rerun copies between the same files
- Still squashing the occasional effect that won't copy correctly


Original .pat parsing code sourced from LegendaryBlueShirt's UnielViewer tool:
https://github.com/LegendaryBlueShirt/UnielViewer
