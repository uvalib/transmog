package edu.virginia.lib.findingaid;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.FindingAidBuilder;
import edu.virginia.lib.findingaid.structure.Schema;
import org.apache.poi.POIXMLException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WordParser {

    private MarkupAssigner markupAssigner;

    public void setMarkupAssigner(MarkupAssigner m) {
        markupAssigner = m;
    }

    public Element processDocument(File f, Schema s) {
        try {
            try {
                XWPFDocument d = new XWPFDocument(new FileInputStream(f));
                return processXMLDoc(s, d, f, false);
            } catch (POIXMLException e) {
                HWPFDocument wd = new HWPFDocument(new FileInputStream(f));
                return processWORDDoc(s, wd, f, false);
            }
        } catch (Exception e) {
            System.out.println("Could not parse " + f.getName());
            e.printStackTrace();
            return null;
        }
    }

    private Element processWORDDoc(Schema schema, HWPFDocument doc, File orig, boolean identifyItalics) throws IOException, XMLStreamException {
        FindingAidBuilder b = new FindingAidBuilder(schema);
        WordExtractor w = new WordExtractor(doc);
        for (String p : w.getParagraphText()) {
            markupAssigner.markupText(p, b, MarkupAssigner.BlockType.PARAGRAPH);
        }
        return b.getDocument();
    }

    private Element processXMLDoc(Schema schema, XWPFDocument doc, File orig, boolean identifyItalics) throws IOException, XMLStreamException {
        FindingAidBuilder b = new FindingAidBuilder(schema);
        for (XWPFParagraph p : doc.getParagraphs()) {
            boolean internalFormatting = false;
            for (IRunElement i : p.getIRuns()) {
                XWPFRun r = (XWPFRun) i;
                if (r.isItalic() || r.isBold()) {
                    internalFormatting = true;
                    break;
                }
            }
            if (internalFormatting && identifyItalics) {
                StringBuffer plainText = new StringBuffer();
                for (IRunElement i : p.getIRuns()) {
                    XWPFRun r = (XWPFRun) i;
                    if (r.isItalic()) {
                        if (plainText.length() > 0) {
                            markupAssigner.markupText(plainText.toString(), b, MarkupAssigner.BlockType.NESTED_TEXT);
                            plainText = new StringBuffer();
                        }
                        markupAssigner.markupText(r.toString(), b, MarkupAssigner.BlockType.NESTED_ITALIC);
                    } else if (r.isBold()) {
                        if (plainText.length() > 0) {
                            markupAssigner.markupText(plainText.toString(), b, MarkupAssigner.BlockType.NESTED_TEXT);
                            plainText = new StringBuffer();
                        }
                        markupAssigner.markupText(r.toString(), b, MarkupAssigner.BlockType.NESTED_BOLD);
                    } else {
                        plainText.append(r.toString());
                    }
                }
                if (plainText.length() > 0) {
                    markupAssigner.markupText(plainText.toString(), b, MarkupAssigner.BlockType.NESTED_TEXT);
                }
            } else {
                markupAssigner.markupText(p.getText(), b, MarkupAssigner.BlockType.PARAGRAPH);
            }
        }

        return b.getDocument();
    }

}
