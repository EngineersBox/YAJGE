package com.engineersbox.yajge.scene.element.particles;

import com.engineersbox.yajge.scene.element.SceneElement;

import java.util.List;

public interface IParticleEmitter {
    void cleanup();
    Particle getBaseParticle();
    List<SceneElement> getParticles();
}
