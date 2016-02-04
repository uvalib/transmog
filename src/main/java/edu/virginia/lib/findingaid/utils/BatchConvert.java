package edu.virginia.lib.findingaid.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import edu.virginia.lib.findingaid.service.DocumentConverter;
import edu.virginia.lib.findingaid.service.ProfileStore;
import edu.virginia.lib.findingaid.structure.Document;

public class BatchConvert {
    
    public static void main(String [] args) throws IOException, XMLStreamException {
        System.out.println("BatchConvert -- A tool to convert all the Modern Library Bibliography Word      ");
        System.out.println("    documents into structured Transmog XML files.");
        if (args.length != 2) {
            System.out.println("\nThis program requires two arguments.  Usage: BatchConvert [source dir] [output dir]");
            System.exit(1);
        }
        File sourceDir = new File(args[0]);
        File outputDir = new File(args[1]);
        
        Pattern filenamePattern = Pattern.compile("(\\d{4}).*(-\\d{6})?\\.docx");
        DocumentConverter c = new DocumentConverter();
        boolean overwrite = true;
        
        for (File f : sourceDir.listFiles()) {
            System.out.println(f.getName());
            Matcher m = filenamePattern.matcher(f.getName());
            if (m.matches()) {
                final String year = m.group(1);
                File output = new File(outputDir, year + ".xml");
                if (output.exists() && !overwrite) {
                    System.out.println("  -- converted (prior to this invocation)");
                } else {
                    Document doc = c.convertWordDoc(f, ProfileStore.getProfileStore().getDefaultProfile(), f.getName());
                    DocumentConverter.applyRules(doc);
                    if (!doc.isAssignedAfterType("HEAD", 2)) {
                        System.out.println("  -- skipped; " + doc.getUnassignedElementCount() + " elements couldn't be assigned automatically");
                    } else {
                        FileOutputStream fos = new FileOutputStream(output);
                        try {
                            doc.getRootElement().writeOutXML(fos);
                            System.out.println("  -- converted (" + doc.getUnassignedElementCount() + " unassigned elements)");
                        } finally {
                            fos.close();
                        }
                    }
                }
            } else {
                System.out.println("  -- skipped; doesn't appear to be a source file");
            }
        }
    }
}
