package com.engineersbox.yajge.scene.element.particles;

import com.engineersbox.yajge.scene.element.SceneElement;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlowParticleEmitter implements IParticleEmitter {

    private int maxParticles;
    private boolean active;
    private final List<SceneElement> particles;
    private final Particle baseParticle;
    private long creationPeriodMillis;
    private long lastCreationTime;
    private float speedRndRange;
    private float positionRndRange;
    private float scaleRndRange;
    private long animRange;

    public FlowParticleEmitter(final Particle baseParticle,
                               final int maxParticles,
                               final long creationPeriodMillis) {
        this.particles = new ArrayList<>();
        this.baseParticle = baseParticle;
        this.maxParticles = maxParticles;
        this.active = false;
        this.lastCreationTime = 0;
        this.creationPeriodMillis = creationPeriodMillis;
    }

    @Override
    public Particle getBaseParticle() {
        return this.baseParticle;
    }

    public long getCreationPeriodMillis() {
        return this.creationPeriodMillis;
    }

    public int getMaxParticles() {
        return this.maxParticles;
    }

    @Override
    public List<SceneElement> getParticles() {
        return this.particles;
    }

    public float getPositionRndRange() {
        return this.positionRndRange;
    }

    public float getScaleRndRange() {
        return this.scaleRndRange;
    }

    public float getSpeedRndRange() {
        return this.speedRndRange;
    }

    public void setAnimRange(final long animRange) {
        this.animRange = animRange;
    }

    public void setCreationPeriodMillis(final long creationPeriodMillis) {
        this.creationPeriodMillis = creationPeriodMillis;
    }

    public void setMaxParticles(final int maxParticles) {
        this.maxParticles = maxParticles;
    }

    public void setPositionRndRange(final float positionRndRange) {
        this.positionRndRange = positionRndRange;
    }

    public void setScaleRndRange(final float scaleRndRange) {
        this.scaleRndRange = scaleRndRange;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setSpeedRndRange(final float speedRndRange) {
        this.speedRndRange = speedRndRange;
    }

    public void update(final long elapsedTime) {
        final long now = System.currentTimeMillis();
        if (this.lastCreationTime == 0) {
            this.lastCreationTime = now;
        }
        final Iterator<? extends SceneElement> it = this.particles.iterator();
        while (it.hasNext()) {
            final Particle particle = (Particle) it.next();
            if (particle.updateTTL(elapsedTime) < 0) {
                it.remove();
            } else {
                updatePosition(particle, elapsedTime);
            }
        }

        final int length = this.getParticles().size();
        if (now - this.lastCreationTime >= this.creationPeriodMillis && length < this.maxParticles) {
            createParticle();
            this.lastCreationTime = now;
        }
    }

    private void createParticle() {
        final Particle particle = new Particle(this.getBaseParticle());
        final float sign = Math.random() > 0.5d ? -1.0f : 1.0f;
        final float speedInc = sign * (float)Math.random() * this.speedRndRange;
        final float posInc = sign * (float)Math.random() * this.positionRndRange;
        final float scaleInc = sign * (float)Math.random() * this.scaleRndRange;
        final long updateAnimInc = (long)sign *(long)(Math.random() * (float)this.animRange);
        particle.getPosition().add(posInc, posInc, posInc);
        particle.getSpeed().add(speedInc, speedInc, speedInc);
        particle.setScale(particle.getScale() + scaleInc);
        particle.setUpdateTextureMills(particle.getUpdateTextureMillis() + updateAnimInc);
        this.particles.add(particle);
    }

    public void updatePosition(final Particle particle, final long elapsedTime) {
        final Vector3f speed = particle.getSpeed();
        final float delta = elapsedTime / 1000.0f;
        final float dx = speed.x * delta;
        final float dy = speed.y * delta;
        final float dz = speed.z * delta;
        final Vector3f pos = particle.getPosition();
        particle.setPosition(pos.x + dx, pos.y + dy, pos.z + dz);
    }

    @Override
    public void cleanup() {
        getParticles().forEach(SceneElement::cleanup);
    }
}
