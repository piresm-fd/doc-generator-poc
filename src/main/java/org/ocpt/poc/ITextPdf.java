package org.ocpt.poc;


import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.ocpt.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ITextPdf {

    public static final Map<String,String> CONFIGS = new HashMap<>();

    public static final List<String> MARKETS = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(ITextPdf.class.getName());
    private static final PdfFont TABLEHEADERFONT;

    static {
        try {
            TABLEHEADERFONT = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String TABLE1TEMPLATEPATH = "src/main/resources/pdf/table/Table1_Template.csv";
    private static final String TABLE2TEMPLATEPATH = "src/main/resources/pdf/table/Table2_Template.csv";
    private static final String PDFOUTPUT = "target/output/tables.pdf";

    public static void createPdf(List<String> markets, List<String> phases) throws IOException {
        Files.createDirectories(Paths.get("target/output"));
        File pdfFile = new File(PDFOUTPUT);
        pdfFile.createNewFile();

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(PDFOUTPUT));
        Document document = new Document(pdfDoc);
        /*
        Document document = new Document(PageSize.A4);
        Files.createDirectories(Paths.get("target/output"));
        File pdfFile = new File(PDFOUTPUT);
        pdfFile.createNewFile();
        PdfWriter.getInstance(document, new FileOutputStream(PDFOUTPUT));
        document.open();
         */
        List<String[]> listTable1 = FileUtils.csvHandler(TABLE1TEMPLATEPATH, ";");
        //Filters phases
        String[] allPhases = listTable1.get(0);
        List<Integer> missingIndexes = findMissingIndexes(allPhases, phases);
        listTable1.replaceAll(r->copyArrayExcludeIndexes(r,missingIndexes));
        //
        Table table1 = createPDFTable(listTable1);
        document.add(table1);
        document.add(new Paragraph("\n"));
        List<String[]> listTable2 = FileUtils.csvHandler(TABLE2TEMPLATEPATH, ";");
        // Filters Markets
        listTable2.subList(1, listTable2.size()).forEach(row -> {
            if (!markets.contains(row[0])) {
                listTable2.remove(row);
            }
        });
        //
        Table table2 = createPDFTable(listTable2);
        document.add(table2);

        document.close();

        LOGGER.info("PDF created successfully: " + PDFOUTPUT);
    }

    private static Table createPDFTable(List<String[]> table) {
        Table pdfTable = new Table(table.get(0).length);
        String[] headers = table.get(0);
        for (String header : headers) {
            pdfTable.addCell(new Cell().add(new Paragraph(header).setFont(TABLEHEADERFONT).setFontSize(15)));
        }
        for (int i = 1; i < table.size(); i++) {
            String[] row = table.get(i);
            for (String cell : row) {
                cell = FileUtils.replacePlaceholder(cell,CONFIGS);
                pdfTable.addCell(cell);
            }
        }
        return pdfTable;
    }

    private static List<Integer> findMissingIndexes(String[] array, List<String> valuesList) {
        List<Integer> missingIndexes = new ArrayList<>();

        for (int i = 0; i < array.length; i++) {
            if (!valuesList.contains(array[i])) {
                missingIndexes.add(i);
            }
        }

        return missingIndexes;
    }

    private static String[] copyArrayExcludeIndexes(String[] sourceArray, List<Integer> indexesToExclude) {
        List<String> resultList = new ArrayList<>();

        for (int i = 0; i < sourceArray.length; i++) {
            if (!indexesToExclude.contains(i)) {
                resultList.add(sourceArray[i]);
            }
        }

        String[] destinationArray = new String[resultList.size()];
        resultList.toArray(destinationArray);

        return destinationArray;
    }
}

/*
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.ocpt.utils.FileUtils;
*/