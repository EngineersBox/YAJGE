package com.engineersbox.yajge.scene.element.animation;

import com.engineersbox.yajge.animation.Animation;
import com.engineersbox.yajge.scene.element.SceneElement;
import com.engineersbox.yajge.scene.element.object.composite.Mesh;

import java.util.Map;
import java.util.Optional;

public class AnimatedSceneElement extends SceneElement {

    private final Map<String, Animation> animations;

    private Animation currentAnimation;

    public AnimatedSceneElement(final Mesh[] meshes,
                                final Map<String, Animation> animations) {
        super(meshes);
        this.animations = animations;
        final Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();
        this.currentAnimation = entry.map(Map.Entry::getValue).orElse(null);
    }

    public Animation getAnimation(final String name) {
        return this.animations.get(name);
    }

    public Animation getCurrentAnimation() {
        return this.currentAnimation;
    }

    public void setCurrentAnimation(final Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
    }
}
