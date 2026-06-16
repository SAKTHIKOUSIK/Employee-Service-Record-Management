package com.employee.backend.service;

import com.employee.backend.entity.Employee;
import com.employee.backend.exception.EmployeeNotFoundException;
import com.employee.backend.repository.EmployeeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;

    private static final float MARGIN      = 50f;
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
    private static final float COL1_WIDTH  = 200f;
    private static final float COL2_WIDTH  = TABLE_WIDTH - COL1_WIDTH;
    private static final float ROW_HEIGHT  = 22f;
    private static final float CELL_PAD    = 5f;
    private static final int   NUM_ROWS    = 9;

    public EmployeeReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public byte[] generateEmployeeReport(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // NotoSansTamil — heavier strokes suit the heading; used only for the title
            Font awtTamil14 = loadAwtFont("fonts/NotoSansTamil-Regular.ttf", 14f);

            // Latha — lighter, traditional Tamil strokes that match Helvetica body-text weight
            Font awtLatha11 = loadAwtFont("fonts/LATHA.TTF", 11f);

            String joiningDate = employee.getDateOfJoining() != null
                    ? employee.getDateOfJoining().toString() : "";

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = PAGE_HEIGHT - MARGIN;

                // English title
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Employee Service Record Report");
                cs.endText();
                y -= 26f;

                // Tamil title — NotoSansTamil shaped via TextLayout → vector paths
                drawTamil(cs, "பணியாளர் சேவை விவர அறிக்கை", awtTamil14, MARGIN, y);
                y -= 30f;

                // Draw the entire table grid in one pass so every line is drawn exactly once.
                // Drawing per-cell rectangles causes shared edges (row separators, column divider)
                // to be stroked twice, making them appear darker than the outer border.
                drawTableGrid(cs, y);

                // Text content — no border drawing here
                String[][] rows = {
                    {"Employee Code",           safe(employee.getEmployeeCode()),        "en"},
                    {"Employee Name (English)", safe(employee.getEmployeeNameEnglish()), "en"},
                    {"Employee Name (Tamil)",   safe(employee.getEmployeeNameTamil()),   "ta"},
                    {"Designation",             safe(employee.getDesignation()),         "en"},
                    {"Department",              safe(employee.getDepartment()),          "en"},
                    {"Date of Joining",         joiningDate,                            "en"},
                    {"Mobile Number",           safe(employee.getMobileNumber()),        "en"},
                    {"Email",                   safe(employee.getEmail()),               "en"},
                    {"Remarks",                 safe(employee.getRemarks()),             "en"},
                };

                for (String[] r : rows) {
                    rowText(cs, r[0], r[1], "ta".equals(r[2]), awtLatha11, y);
                    y -= ROW_HEIGHT;
                }
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate employee report PDF", ex);
        }
    }

    private Font loadAwtFont(String classpathPath, float size) throws Exception {
        ClassPathResource res = new ClassPathResource(classpathPath);
        try (InputStream is = res.getInputStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
        }
    }

    /**
     * Draws the complete table grid (all horizontals + 3 verticals) as a single stroked path.
     * y is the bottom of the first row (same coordinate passed to the first rowText call).
     */
    private void drawTableGrid(PDPageContentStream cs, float firstRowBottom) throws IOException {
        float tableTop    = firstRowBottom + ROW_HEIGHT;
        float tableBottom = firstRowBottom - (NUM_ROWS - 1) * ROW_HEIGHT;
        float left        = MARGIN;
        float right       = MARGIN + TABLE_WIDTH;
        float divider     = MARGIN + COL1_WIDTH;

        cs.setLineWidth(0.5f);

        // Horizontal lines: one for each row top + the final bottom line
        for (int i = 0; i <= NUM_ROWS; i++) {
            float lineY = tableTop - i * ROW_HEIGHT;
            cs.moveTo(left,  lineY);
            cs.lineTo(right, lineY);
        }

        // Vertical lines: left edge, column divider, right edge
        cs.moveTo(left,     tableTop); cs.lineTo(left,     tableBottom);
        cs.moveTo(divider,  tableTop); cs.lineTo(divider,  tableBottom);
        cs.moveTo(right,    tableTop); cs.lineTo(right,    tableBottom);

        cs.stroke();
    }

    private void rowText(PDPageContentStream cs, String label, String value,
                         boolean valueTamil, Font tamilFont, float y) throws IOException {
        float textY = y + CELL_PAD + 2f;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(MARGIN + CELL_PAD, textY);
        cs.showText(label);
        cs.endText();

        if (valueTamil) {
            drawTamil(cs, value, tamilFont, MARGIN + COL1_WIDTH + CELL_PAD, textY);
        } else {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN + COL1_WIDTH + CELL_PAD, textY);
            cs.showText(value);
            cs.endText();
        }
    }

    /**
     * Renders Tamil text as vector glyph paths.
     * TextLayout applies OpenType shaping (GSUB/GPOS: vowel reordering, conjuncts) via the JVM
     * text engine. getOutline() extracts those shaped paths so they embed correctly in every
     * PDF viewer without relying on PDF font encoding.
     */
    private void drawTamil(PDPageContentStream cs, String text,
                            Font awtFont, float pdfX, float pdfY) throws IOException {
        if (text == null || text.isEmpty()) return;

        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        Shape outline = new TextLayout(text, awtFont, frc).getOutline(null);

        cs.setNonStrokingColor(0, 0, 0);

        PathIterator pi = outline.getPathIterator(null);
        float[] c = new float[6];
        float cx = 0, cy = 0;
        boolean hasPath = false;

        while (!pi.isDone()) {
            int type = pi.currentSegment(c);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    // AWT Y increases downward from baseline; PDF Y increases upward — flip Y
                    cs.moveTo(pdfX + c[0], pdfY - c[1]);
                    cx = c[0]; cy = c[1];
                    hasPath = true;
                    break;
                case PathIterator.SEG_LINETO:
                    cs.lineTo(pdfX + c[0], pdfY - c[1]);
                    cx = c[0]; cy = c[1];
                    break;
                case PathIterator.SEG_QUADTO: {
                    // TrueType fonts use quadratic beziers; convert to cubic for PDF
                    float qx = c[0], qy = c[1], ex = c[2], ey = c[3];
                    float bx1 = cx + 2f/3f*(qx-cx), by1 = cy + 2f/3f*(qy-cy);
                    float bx2 = ex + 2f/3f*(qx-ex), by2 = ey + 2f/3f*(qy-ey);
                    cs.curveTo(pdfX+bx1, pdfY-by1, pdfX+bx2, pdfY-by2, pdfX+ex, pdfY-ey);
                    cx = ex; cy = ey;
                    break;
                }
                case PathIterator.SEG_CUBICTO:
                    cs.curveTo(pdfX+c[0], pdfY-c[1], pdfX+c[2], pdfY-c[3], pdfX+c[4], pdfY-c[5]);
                    cx = c[4]; cy = c[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    cs.closePath();
                    break;
            }
            pi.next();
        }

        if (hasPath) cs.fill();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
