# TODO

* Create custom exceptions
* Create tangent space transform to ensure object space or adapt shader view matrix to account for it
* Created enum declarations for uniform names and update magic string references
* Core Physics system
* PBR and IBL
* Scalable LOD
* Implement shadows for point lights and spotlights
* Fix particle texture not masked by shadow or fog
* Implement animation lerp for linear, non-linear and custom curves. Standard set:
  * Linear
  * Sigmoid
  * Step
* Auto create visibility box for frustum culling for meshes based on best fit to cube, sphere, etc
* Enable early fragment testing to increase performance with shaders
* Refactor from log4j to slf4j
* Refactor to Nuklear GUI lib
* Create project file structure for save/import
* Create shader lib for basic effects
* Implement external bindings for custom shaders and document:
  * Uniforms
  * Ins
  * Outs
  * Deferred shader processing order
* Devise system for interaction bounding boxes on SceneElements