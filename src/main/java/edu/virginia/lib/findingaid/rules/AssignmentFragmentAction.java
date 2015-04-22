package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Path;

import java.util.Map;
import java.util.Set;

public class AssignmentFragmentAction implements FragmentAction {

    private Path blockPath;

    private Map<String, String> matchToPath;

    private Set<String> omit;

    public AssignmentFragmentAction(String blockPath, Map<String, String> matchToPath, Set<String> omit) {
        this.blockPath = blockPath == null || blockPath.length() == 0 ? null : new Path(blockPath);
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
            } else if (matchToPath.containsKey(m.getId())) {
                if (blockPlacement == null && blockPath != null) {
                    Path parentPath = blockPath.getParentPath();
                    blockPlacement = new Element(e.getProfile().getNodeType(blockPath.getLastPathElement()));
                    if (parentPath != null) {
                        e.getParent().locateOrCreatePath(parentPath, -1).addChild(blockPlacement, e.getIndexWithinParent());
                    } else {
                        e.getParent().addChild(blockPlacement, e.getIndexWithinParent());
                    }
                }
                final Path path = new Path(matchToPath.get(m.getId()));
                if (blockPlacement != null) {
                    e.moveElement(blockPlacement, blockPlacement.getChildren().size());
                }
                e.assignPath(path);
            } else {
                // this element was part of the match, but not to be mapped...
            }
        }
    }
}
