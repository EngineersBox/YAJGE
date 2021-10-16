package com.engineersbox.yajge.logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.io.PrintStream;

public abstract class LoggerCompat {

    public static PrintStream asPrintStream(final Logger logger) {
        return new PrintStream(IoBuilder.forLogger(logger).buildOutputStream());
    }

    public static void registerGLFWLogger(final Logger logger) {
        GLFWErrorCallback.createPrint(LoggerCompat.asPrintStream(logger)).set();
    }

}
