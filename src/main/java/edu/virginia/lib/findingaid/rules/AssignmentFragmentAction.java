package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;

import java.util.Map;
import java.util.Set;

public class AssignmentFragmentAction implements FragmentAction {

    private String blockPath;

    private Map<String, String> matchToPath;

    private Set<String> omit;

    public AssignmentFragmentAction(String blockPath, Map<String, String> matchToPath, Set<String> omit) {
        this.blockPath = blockPath;
        this.matchToPath = matchToPath;
        this.omit = omit;
    }

    @Override
    public void apply(BlockMatch match) {
        Element blockPlacement = null;
        for (ElementMatch m : match) {
            Element e = m.getElement();
            if (omit.contains(m.getId())) {
                e.removeFromParent();
            } else {
                if (blockPlacement == null && (blockPath != null && blockPath.length() > 0)) {
                    blockPlacement = e.getParent().locateOrCreatePath(blockPath, e.getIndexWithinParent());
                }
                final String path = matchToPath.get(m.getId());
                if (blockPlacement != null) {
                    e.moveElement(blockPlacement, blockPlacement.getChildren().size());
                }
                e.assignPath(path);
            }
        }
    }
}
