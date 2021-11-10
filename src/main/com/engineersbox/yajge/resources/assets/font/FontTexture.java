package com.engineersbox.yajge.resources.assets.font;

import com.engineersbox.yajge.resources.assets.material.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

public class FontTexture {

    private static final Logger LOGGER = LogManager.getLogger(FontTexture.class);
    private static final String IMAGE_FORMAT = "png";
    private static final int CHAR_PADDING = 2;

    private final Font font;
    private final Charset charset;
    private final Map<Character, CharInfo> charMap;
    private Texture texture;
    private int height;
    private int width;

    public FontTexture(final Font font,
                       final Charset charset) {
        this.font = font;
        this.charset = charset;
        this.charMap = new HashMap<>();
        buildTexture();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public CharInfo getCharInfo(final char c) {
        return this.charMap.get(c);
    }

    private String getAllAvailableChars(final Charset charset) {
        final CharsetEncoder charsetEncoder = charset.newEncoder();
        final StringBuilder result = new StringBuilder();
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            if (charsetEncoder.canEncode(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void buildTexture() {
        final String allChars = getAllCharsetChars();
        final BufferedImage img = createCharsetImage(allChars);

        ByteBuffer buf = null;
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, IMAGE_FORMAT, out);
            out.flush();
            final byte[] data = out.toByteArray();
            buf = ByteBuffer.allocateDirect(data.length);
            buf.put(data, 0, data.length);
            buf.flip();
        } catch (final IOException e) {
            LOGGER.error(e);
        }
        this.texture = new Texture(buf);
    }

    private String getAllCharsetChars() {
        final BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2D = img.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setFont(this.font);
        final FontMetrics fontMetrics = g2D.getFontMetrics();

        final String allChars = getAllAvailableChars(this.charset);
        this.width = 0;
        this.height = fontMetrics.getHeight();
        for (final char c : allChars.toCharArray()) {
            final CharInfo charInfo = new CharInfo(this.width, fontMetrics.charWidth(c));
            this.charMap.put(c, charInfo);
            this.width += charInfo.width() + CHAR_PADDING;
        }
        g2D.dispose();
        return allChars;
    }

    private BufferedImage createCharsetImage(final String allChars) {
        final BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2D = img.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setFont(this.font);
        final FontMetrics fontMetrics = g2D.getFontMetrics();
        g2D.setColor(Color.WHITE);
        int startX = 0;
        for (final char c : allChars.toCharArray()) {
            final CharInfo charInfo = this.charMap.get(c);
            g2D.drawString("" + c, startX, fontMetrics.getAscent());
            startX += charInfo.width() + CHAR_PADDING;
        }
        g2D.dispose();
        return img;
    }
}
