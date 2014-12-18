package edu.virginia.lib.findingaid;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.FindingAidBuilder;

public interface MarkupAssigner {

    public static enum BlockType {
        PARAGRAPH,    /* A paragraph whose entire text is formatted the same and will be included in the text */
        NESTED_BOLD,
        NESTED_ITALIC,
        NESTED_TEXT;
    }

    public void markupText(String text, FindingAidBuilder ead, BlockType type);

}
