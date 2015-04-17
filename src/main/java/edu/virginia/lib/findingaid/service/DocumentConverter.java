package edu.virginia.lib.findingaid.service;

import edu.virginia.lib.findingaid.WordParser;
import edu.virginia.lib.findingaid.rules.BlockMatch;
import edu.virginia.lib.findingaid.rules.Rule;
import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Schema;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentConverter {

    private WordParser p;

    public DocumentConverter() {
        p = new WordParser();
    }

    public Element convertWordDoc(InputStream wordDoc, Schema s) throws IOException {
        File f = File.createTempFile("temporary", ".doc");
        FileOutputStream fos = new FileOutputStream(f);
        try {
            IOUtils.copy(wordDoc, fos);
        } finally {
            fos.close();
        }
        try {
            return convertWordDoc(f, s);
        } finally {
            f.delete();
        }
    }

    public Element convertWordDoc(File doc, Schema s) throws IOException {
        final Element root = p.processDocument(doc, s);

        annotateTables(root);

        applyRules(root, s);

        return root;
    }

    /**
     * Identifies tab-separated values and guesses at table structure, adding annotations
     * to the elements.
     */
    private void annotateTables(Element el) {
        TableBuilder b = new TableBuilder();
        for (Element child : new ArrayList<Element>(el.getChildren())) {
            b.addRow(child);
        }
        b.finish();
    }

    private void applyRules(Element el, Schema s) {
        for (Rule r : s.getRules()) {
            for (BlockMatch b : r.getWhenClause().findMatches(el)) {
                r.getAction().apply(b);
            }
        }
    }

    private static class TableBuilder {

        private String delimiter = "\\s*\\t+\\s*";

        private int minColumns = 2;

        private int minRows = 2;

        private List<Element> currentTable = new ArrayList<Element>();

        public void addRow(Element el) {
            if (el.isUnassigned() && el.getContent() != null) {
                String[] columns = el.getContentAsString().trim().split(delimiter);
                if (columns.length < minColumns) {
                    // this row is ineligible because it has too few columns
                    closeOutTable();
                    return;
                } else {
                    if (currentTable.size() == 0) {
                        // this row is eligible and the first of a new table
                        currentTable.add(el);
                        return;
                    } else if (currentTableColumnCount() == columns.length) {
                        // this row is eligible as part of the existing table
                        currentTable.add(el);
                        return;
                    } else {
                        // this row is eligible, but only as a new table following
                        // the previous one
                        closeOutTable();
                        currentTable.add(el);
                        return;
                    }
                }
            } else {
                // this row in ineligible because it has no content or is already assigned a type
                closeOutTable();
            }
        }

        private int currentTableColumnCount() {
            return currentTable.get(0).getContentAsString().trim().split(delimiter).length;
        }

        public void finish() {
            closeOutTable();
        }


        private void closeOutTable() {
            if (!currentTable.isEmpty()) {
                if (currentTable.size() >= minRows) {
                    annotateTable();
                }
                currentTable.clear();
            }
        }

        private void annotateTable() {
            Element tableEl = null;
            for (Element oldRow : currentTable) {
                if (tableEl == null) {
                    final Element parent = oldRow.getParent();
                    int index = oldRow.getIndexWithinParent();
                    tableEl = new Element(parent.getType().getSchema().getTableType());
                    parent.addChild(tableEl, index);
                }
                Element rowEl = new Element(tableEl.getType().getSchema().getRowType());
                tableEl.addChild(rowEl);
                oldRow.removeFromParent();
                for (String columnText : oldRow.getContentAsString().trim().split(delimiter)) {
                    final Element column = new Element(oldRow.getType(), columnText);
                    rowEl.addChild(column);
                }
            }
        }

    }
}
