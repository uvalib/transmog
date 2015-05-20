package edu.virginia.lib.findingaid.structure;

import java.util.UUID;

/**
 * A section of text within a paragraph that, as far as Transmog knows,
 * has consistent formatting.
 */
public class Fragment {

    public static final String ITALIC = "italic";

    public static final String BOLD = "bold";

    public static final String STRIKETHROUGH = "strikethrough";

    public static final String SUPERSCRIPT = "superscript";

    public static final String SUBSCRIPT = "subscript";

    String id;

    String[] styles;

    String content;

    public Fragment(final String text) {
        this(UUID.randomUUID().toString(), new String[0], text);
    }

    public Fragment(final String text, final String[] styles) {
        this(UUID.randomUUID().toString(), styles, text);
    }

    Fragment(String id, String[] styles, String text) {
        this.id = id;
        this.styles = styles;
        this.content = text;
        for (String style : styles) {
            if (style.contains(" ")) {
                throw new IllegalArgumentException("Style types may not contain spaces (\"" + style + "\")!");
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.content;
    }

    public void setText(String text) {
        this.content = text;
    }

    public String[] getStyles() {
        return this.styles;
    }

    public void setStyles(String[] styles) {
        this.styles = styles;
    }

    public String getStylesAsSpaceDelimitedString() {
        StringBuffer result = new StringBuffer();
        for (String s : styles) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(s);
        }
        return result.toString();
    }
}
