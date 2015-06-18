package edu.virginia.lib.findingaid.rules;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssignmentFragmentAction implements FragmentAction {

    private Path blockPath;

    /**
     * A map from a part name to a map from a group number (0-x) to the Path
     * to which that group should be mapped.
     * For example:
     * header --> { 1 --> Title, 2 --> Date }
     * would assign the first matching group of the "header" pattern to "Title"
     * and the second matching group to "Date".
     */
    private Map<String, Map<Integer, String>> matchToGroupToPath;

    private Set<String> omit;

    public AssignmentFragmentAction(String blockPath,
                                    Map<String, Map<Integer, String>> matchToGroupToPath, Set<String> omit) {
        this.blockPath = blockPath == null || blockPath.length() == 0 ? null : new Path(blockPath);
        this.matchToGroupToPath = matchToGroupToPath;
        this.omit = omit;
    }

    @Override
    public void apply(BlockMatch match) {
        Element blockPlacement = null;
        for (ElementMatch m : match) {
            Element e = m.getElement();
            if (omit.contains(m.getId())) {
                e.removeFromParent();
            } else if (matchToGroupToPath.containsKey(m.getId())) {
                if (blockPlacement == null && blockPath != null) {
                    Path parentPath = blockPath.getParentPath();
                    blockPlacement = new Element(e.getProfile().getNodeType(blockPath.getLastPathElement()));
                    if (parentPath != null) {
                        e.getParent().locateOrCreatePath(parentPath, -1).addChild(blockPlacement, e.getIndexWithinParent());
                    } else {
                        e.getParent().addChild(blockPlacement, e.getIndexWithinParent());
                    }
                }
                final Map<Integer, String> groupToPath = matchToGroupToPath.get(m.getId());
                List<Integer> groups = new ArrayList<Integer>(groupToPath.keySet());
                Collections.sort(groups);
                for (Integer group : groups)  {
                    final Path path = new Path(groupToPath.get(group));
                    if (group.intValue() == 0) {
                        if (blockPlacement != null) {
                            e.moveElement(blockPlacement, blockPlacement.getChildren().size());
                        }
                        e.assignPath(path);
                    } else {
                        final Element newElement = m.createElement(group.intValue());
                        e.getParent().addChild(newElement, e.getIndexWithinParent());
                        if (blockPlacement != null) {
                            newElement.moveElement(blockPlacement, blockPlacement.getChildren().size());
                        }
                        newElement.assignPath(path);
                    }
                }
                if (!groups.contains(0)) {
                    e.removeFromParent();
                }
            } else {
                // this element was part of the match, but not to be mapped...
            }
        }
    }
}
