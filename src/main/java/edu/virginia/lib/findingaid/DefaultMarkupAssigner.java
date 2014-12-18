package edu.virginia.lib.findingaid;

import edu.virginia.lib.findingaid.structure.FindingAidBuilder;

public class DefaultMarkupAssigner implements MarkupAssigner {

    @Override
    public void markupText(String text, FindingAidBuilder ead, BlockType type) {
        if (text == null || text.trim().equals("")) {
            // skip empty paragraphs.
        } else {
            ead.addChild(ead.getSchema().getUnassignedType(), text, false);
        }
    }
}
