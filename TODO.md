# TODO

* Create custom exceptions
* Throw exceptions to be caught in engine try-catch rather than progressing (when applicable)
* Variable shadow map resolutions
* Cascaded shadow mapping
  * Create tangent space transform to ensure object space or adapt shader view matrix to account for it
* Created enum declarations for uniform names and update magic string references
* Refactor to Assimp library for handling more model formats
* Create deferred shading pipeline
* Audio system
* Physics system
* PBR and IBL
* Scalable LOD
* Fixed borked shadow mapping with directional light
* Implement shadows for point and spot lights
* Dynamic light instance counts (non-fixed limit of 5)

## Assimp Structure

![](docs/images/assimp_structure.png)