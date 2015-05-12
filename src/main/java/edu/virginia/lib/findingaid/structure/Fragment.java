package edu.virginia.lib.findingaid.structure;

import java.util.UUID;

public class Fragment {

    public static final String TEXT_TYPE = "text";

    public static final String ITALIC_TYPE = "italic";

    public static final String BOLD_TYPE = "bold";

    public static final String SUPERSCRIPT_TYPE = "superscript";

    public static final String SUBSCRIPT_TYPE = "subscript";

    public static final String CUSTOM_TYPE_PREFIX = "custom_";

    String id;

    String type;

    String content;

    public Fragment(String type, String text) {
        this(UUID.randomUUID().toString(), type, text);
    }

    Fragment(String id, String type, String text) {
        this.id = id;
        this.type = type;
        this.content = text;
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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPlainText() {
        return this.type.equals(TEXT_TYPE);
    }

    public boolean isItalic() {
        return this.type.equals(ITALIC_TYPE);
    }

    public boolean isBoldText() {
        return this.type.equals(BOLD_TYPE);
    }

    public static Fragment textFragment(final String content) {
        return new Fragment(TEXT_TYPE, content);
    }

    public static Fragment italicFragment(String content) {
        return new Fragment(ITALIC_TYPE, content);
    }

    public static Fragment boldFragment(String content) {
        return new Fragment(BOLD_TYPE, content);
    }

    public static Fragment superscriptFragment(String content) {
        return new Fragment(SUPERSCRIPT_TYPE, content);
    }

    public static Fragment subscriptFragment(String content) {
        return new Fragment(SUBSCRIPT_TYPE, content);
    }

}
