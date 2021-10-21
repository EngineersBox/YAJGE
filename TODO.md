# TODO

* Create custom exceptions
* Throw exceptions to be caught in engine try-catch rather than progressing (when applicable)
* Fix duplicate vertices between faces, ensure vertices are shared to avoid gaps between faces
* Implement mapping:
  * Normal
  * Height
* Create shadow casting extending on Phong shading model
* Cascaded shadow mapping
* Centralise scene components under Scene object
* Created enum declarations for uniform names and update magic string references
* Refactor to Assimp library for handling more model formats
* Create deferred shading pipeline
* Implement fog
* Implement particle system
* Skybox
* Audio system
* Physics system
* PBR and IBL

## Assimp Structure

![](docs/images/assimp_structure.png)