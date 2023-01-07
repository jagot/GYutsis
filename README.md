# GYutsis

A library and graphical interface for reducing a Yutsis graph to a
recoupling formula, optimized using heuristics for selecting the best
way to reduce the graph at every step, as described in

- Dyck, D. V., & Fack, V. (2003). New heuristic approach to the
  calculation of general recoupling coefficients. Computer Physics
  Communications, 151(3), 354–368. DOI:
  [10.1016/s0010-4655(02)00733-6](http://dx.doi.org/10.1016/s0010-4655(02)00733-6)

- Dyck, D. V., & Fack, V. (2003). GYutsis: heuristic based calculation
  of general recoupling coefficients. Computer Physics Communications,
  154(3), 219–232. DOI:
  [10.1016/s0010-4655(03)00280-7](http://dx.doi.org/10.1016/s0010-4655(03)00280-7)

- Dyck, D. V., & Fack, V. (2007). On the reduction of Yutsis
  graphs. Discrete Mathematics, 307(11-12), 1506–1515. DOI:
  [10.1016/j.disc.2005.11.088](http://dx.doi.org/10.1016/j.disc.2005.11.088)

## Compilation instructions

Simply run `make` in the root directory, and if the build process is
successful, `bin/GYutsis.jar` will be created.

## Usage

The main interface is launched by typing

```sh
java -jar bin/GYutsis.jar
```

Other programs of interest can be run by typing

- `java -class CycleCostAlgorithm`
- `java -class PathGenerator`
- `java -class CycleGenerator`
- `java -class YutsisGraph`

# Copyright notice

Copyright remains with the original authors. See LICENSE.md for
details.
