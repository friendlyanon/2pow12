package com.github.friendlyanon.twopowtwelve.util;

import lombok.val;

import java.awt.*;
import java.awt.font.TextLayout;

public class TextDimensions {
    private TextDimensions() {}

    public static int textWidth(String text, Graphics2D graphics) {
        val rect = graphics.getFontMetrics().getStringBounds(text, graphics);
        return (int) rect.getWidth();
    }
    public static int textHeight(String text, Graphics2D graphics, Font font) {
        if (text.length() == 0) {
            return 0;
        }
        val layout = new TextLayout(text, font, graphics.getFontRenderContext());
        return (int) layout.getBounds().getHeight();
    }
}
