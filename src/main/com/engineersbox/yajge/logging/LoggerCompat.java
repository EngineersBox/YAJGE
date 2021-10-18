package com.engineersbox.yajge.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.PrintStream;

public abstract class LoggerCompat {

    public static PrintStream asPrintStream(final Logger logger,
                                            final Level level) {
        return new PrintStream(IoBuilder.forLogger(logger).setLevel(level).buildOutputStream());
    }

    public static void registerGLFWErrorLogger(final Logger logger,
                                               final Level level) {
        GLFWErrorCallback.createPrint(LoggerCompat.asPrintStream(logger, level)).set();
    }

}
