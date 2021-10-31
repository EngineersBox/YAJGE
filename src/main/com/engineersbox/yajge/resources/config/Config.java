package com.engineersbox.yajge.resources.config;

import java.io.Serial;

public class Config {
  public final Config.Engine engine;
  public final Config.Render render;
  public final Config.Video video;
  // NOTE: incomplete #62 implementation
  public enum GraphicsAPIType {
    OPENGL,
    VULKAN;
  }

  public static class Engine {
    public final Engine.Features features;
    public final Engine.Resources resources;

    public static class Features {
      public final GraphicsAPIType graphicsAPI;

      public Features(
              final com.typesafe.config.Config c,
              final java.lang.String parentPath,
              final $TsCfgValidator $tsCfgValidator) {
        this.graphicsAPI = GraphicsAPIType.valueOf(c.getString("graphicsAPI"));
      }
    }

    public static class Resources {
      public final java.lang.String shaders;
      public final java.lang.String textures;

      public Resources(
              final com.typesafe.config.Config c,
              final java.lang.String parentPath,
              final $TsCfgValidator $tsCfgValidator) {
        this.shaders = $_reqStr(parentPath, c, "shaders", $tsCfgValidator);
        this.textures = $_reqStr(parentPath, c, "textures", $tsCfgValidator);
      }

      private static java.lang.String $_reqStr(
              final java.lang.String parentPath,
              final com.typesafe.config.Config c,
              final java.lang.String path,
              final $TsCfgValidator $tsCfgValidator) {
        if (c == null) {
          return null;
        }
        try {
          return c.getString(path);
        } catch (final com.typesafe.config.ConfigException e) {
          $tsCfgValidator.addBadPath(parentPath + path, e);
          return null;
        }
      }
    }

    public Engine(
            final com.typesafe.config.Config c,
            final java.lang.String parentPath,
            final $TsCfgValidator $tsCfgValidator) {
      this.features =
          c.hasPathOrNull("features")
              ? new Engine.Features(
                  c.getConfig("features"), parentPath + "features.", $tsCfgValidator)
              : new Engine.Features(
                  com.typesafe.config.ConfigFactory.parseString("features{}"),
                  parentPath + "features.",
                  $tsCfgValidator);
      this.resources =
          c.hasPathOrNull("resources")
              ? new Engine.Resources(
                  c.getConfig("resources"), parentPath + "resources.", $tsCfgValidator)
              : new Engine.Resources(
                  com.typesafe.config.ConfigFactory.parseString("resources{}"),
                  parentPath + "resources.",
                  $tsCfgValidator);
    }
  }

  public static class Render {
    public final Render.Camera camera;
    public final Render.Lighting lighting;

    public static class Camera {
      public final double fov;
      public final double zFar;
      public final double zNear;

      public Camera(
              final com.typesafe.config.Config c,
              final java.lang.String parentPath,
              final $TsCfgValidator $tsCfgValidator) {
        this.fov = c.hasPathOrNull("fov") ? c.getDouble("fov") : 60.0;
        this.zFar = c.hasPathOrNull("zFar") ? c.getDouble("zFar") : 1000.0;
        this.zNear = c.hasPathOrNull("zNear") ? c.getDouble("zNear") : 0.01;
      }
    }

    public static class Lighting {
      public final int maxPointLights;
      public final int maxSpotLights;
      public final int shadowMapHeight;
      public final int shadowMapWidth;

      public Lighting(
              final com.typesafe.config.Config c,
              final java.lang.String parentPath,
              final $TsCfgValidator $tsCfgValidator) {
        this.maxPointLights = c.hasPathOrNull("maxPointLights") ? c.getInt("maxPointLights") : 5;
        this.maxSpotLights = c.hasPathOrNull("maxSpotLights") ? c.getInt("maxSpotLights") : 5;
        this.shadowMapHeight =
            c.hasPathOrNull("shadowMapHeight") ? c.getInt("shadowMapHeight") : 1024;
        this.shadowMapWidth = c.hasPathOrNull("shadowMapWidth") ? c.getInt("shadowMapWidth") : 1024;
      }
    }

    public Render(
            final com.typesafe.config.Config c,
            final java.lang.String parentPath,
            final $TsCfgValidator $tsCfgValidator) {
      this.camera =
          c.hasPathOrNull("camera")
              ? new Render.Camera(c.getConfig("camera"), parentPath + "camera.", $tsCfgValidator)
              : new Render.Camera(
                  com.typesafe.config.ConfigFactory.parseString("camera{}"),
                  parentPath + "camera.",
                  $tsCfgValidator);
      this.lighting =
          c.hasPathOrNull("lighting")
              ? new Render.Lighting(
                  c.getConfig("lighting"), parentPath + "lighting.", $tsCfgValidator)
              : new Render.Lighting(
                  com.typesafe.config.ConfigFactory.parseString("lighting{}"),
                  parentPath + "lighting.",
                  $tsCfgValidator);
    }
  }

  public static class Video {
    public final int fps;
    public final int monitor;
    public final int ups;

    public Video(
            final com.typesafe.config.Config c,
            final java.lang.String parentPath,
            final $TsCfgValidator $tsCfgValidator) {
      this.fps = c.hasPathOrNull("fps") ? c.getInt("fps") : 60;
      this.monitor = c.hasPathOrNull("monitor") ? c.getInt("monitor") : 1;
      this.ups = c.hasPathOrNull("ups") ? c.getInt("ups") : 30;
    }
  }

  public Config(final com.typesafe.config.Config c) {
    final $TsCfgValidator $tsCfgValidator = new $TsCfgValidator();
    final java.lang.String parentPath = "";
    this.engine =
        c.hasPathOrNull("engine")
            ? new Config.Engine(c.getConfig("engine"), parentPath + "engine.", $tsCfgValidator)
            : new Config.Engine(
                com.typesafe.config.ConfigFactory.parseString("engine{}"),
                parentPath + "engine.",
                $tsCfgValidator);
    this.render =
        c.hasPathOrNull("render")
            ? new Config.Render(c.getConfig("render"), parentPath + "render.", $tsCfgValidator)
            : new Config.Render(
                com.typesafe.config.ConfigFactory.parseString("render{}"),
                parentPath + "render.",
                $tsCfgValidator);
    this.video =
        c.hasPathOrNull("video")
            ? new Config.Video(c.getConfig("video"), parentPath + "video.", $tsCfgValidator)
            : new Config.Video(
                com.typesafe.config.ConfigFactory.parseString("video{}"),
                parentPath + "video.",
                $tsCfgValidator);
    $tsCfgValidator.validate();
  }

  private static final class $TsCfgValidator {
    private final java.util.List<java.lang.String> badPaths = new java.util.ArrayList<>();

    void addBadPath(final java.lang.String path, final com.typesafe.config.ConfigException e) {
      this.badPaths.add("'" + path + "': " + e.getClass().getName() + "(" + e.getMessage() + ")");
    }

    void validate() {
      if (!this.badPaths.isEmpty()) {
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("Invalid configuration:");
        for (final java.lang.String path : this.badPaths) {
          sb.append("\n    ").append(path);
        }
        throw new com.typesafe.config.ConfigException(sb.toString()) {
          @Serial
          private static final long serialVersionUID = 1169357720903533991L;
        };
      }
    }
  }
}