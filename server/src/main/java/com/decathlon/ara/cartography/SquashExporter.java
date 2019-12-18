package com.decathlon.ara.cartography;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SquashExporter is an Exporter which serialize the functionalities in order to make them importable in SquashTM.
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
@Slf4j
public class SquashExporter extends Exporter {

    private static final String PROJECT_NAME = "squash_project_name";
    private static final String USER = "squash_user";
    private static final String SEVERITY_CRITICAL = "squash_sev_critical";
    private static final String SEVERITY_MAJOR = "squash_sev_major";
    private static final String SEVERITY_MINOR = "squash_sev_minor";

    @Override
    public String getName() {
        return "SquashTM";
    }

    @Override
    public String getDescription() {
        return "Export this cartography to import it as requirements in SquashTM";
    }

    @Override
    public String getFormat() {
        return "xls";
    }

    @Override
    public byte[] generate(List<FunctionalityDTO> functionalities, Map<String, String> requiredInfos) {
        String projectNameInSquash = requiredInfos.get(PROJECT_NAME);
        String userWhichMakesTheImport = requiredInfos.get(USER);
        String severityMappedToCritical = requiredInfos.get(SEVERITY_CRITICAL);
        String severityMappedToMajor = requiredInfos.get(SEVERITY_MAJOR);
        String severityMappedToMinor = requiredInfos.get(SEVERITY_MINOR);

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("REQUIREMENTS");
        this.createHeaderRow(sheet);
        Date importDate = new Date();
        int idx = 1;
        for (FunctionalityDTO functionality: functionalities) {
            if (FunctionalityType.FUNCTIONALITY.name().equals(functionality.getType())) {
                String criticity = "";
                if (functionality.getSeverity().equals(severityMappedToCritical)) {
                    criticity = "CRITICAL";
                } else if (functionality.getSeverity().equals(severityMappedToMajor)) {
                    criticity = "MAJOR";
                } else if (functionality.getSeverity().equals(severityMappedToMinor)) {
                    criticity = "MINOR";
                } // No default case, will let SquashTM handles unmapped or invalid severities.

                this.addRowToSheet(sheet, idx, projectNameInSquash,
                        this.getFunctionalityPath(functionality, functionalities), functionality.getName(),
                        criticity,
                        importDate, userWhichMakesTheImport);
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Unable to write the Squash export : {}", ex.getMessage());
            log.debug("Full exception of the Squash export failure", ex);
            return new byte[0];
        }
    }

    @Override
    public List<ExportField> listRequiredFields() {
        List<ExportField> fields = new ArrayList<>();
        fields.add(new ExportField(PROJECT_NAME, "Targeted project's name", "string",
                "The name of targeted project in SquashTM. Note that the project must be created before doing the import."));
        fields.add(new ExportField(USER, "Targeted User's name", "string",
                "The name of the user whom will act as the creator of the requirements in SquashTM. Note that the user must exist before doing the import."));
        fields.add(new ExportField(SEVERITY_CRITICAL, "Critical severity", "string",
                "The severity in this ARA project, which correspond to a Critical severity in SquashTM."));
        fields.add(new ExportField(SEVERITY_MAJOR, "Major severity", "string",
                "The severity in this ARA project, which correspond to a Major severity in SquashTM."));
        fields.add(new ExportField(SEVERITY_MINOR, "Minor severity", "string",
                "The severity in this ARA project, which correspond to a Minor severity in SquashTM."));
        return fields;
    }

    private Row createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        int rowIdx = 0;
        this.addCellToRow(header, rowIdx++, "ACTION");
        this.addCellToRow(header, rowIdx++, "PROJECT_ID");
        this.addCellToRow(header, rowIdx++, "PROJECT_NAME");
        this.addCellToRow(header, rowIdx++, "REQ_PATH");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_NUM");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_NAME");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_CRITICALITY");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_CATEGORY");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_STATUS");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_DESCRIPTION");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_#_TC");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_#_ATTACHEMENT");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_CREATED_ON");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_CREATED_BY");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_LAST_MODIFIED_ON");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_LAST_MODIFIED_BY");
        this.addCellToRow(header, rowIdx++, "REQ_VERSION_MILESTONE");
        this.addCellToRow(header, rowIdx, "REQ_VERSION_CUF_<code du cuf>");
        return header;
    }

    private void addRowToSheet(Sheet sheet, int index, String projectName, String path, String functionality, String criticity, Date creationDate, String username) {
        Row row = sheet.createRow(index);
        this.addCellToRow(row, 0, "C");
        this.addCellToRow(row, 2, projectName);
        this.addCellToRow(row, 3, path);
        this.addCellToRow(row, 4, "1");
        this.addCellToRow(row, 7, criticity);
        this.addCellToRow(row, 8, "CAT_USE_CASE");
        this.addCellToRow(row, 9, "APPROVED");
        this.addCellToRow(row, 10, functionality);
        this.addCellToRow(row, 13, creationDate);
        this.addCellToRow(row, 14, username);
        this.addCellToRow(row, 15, creationDate);
        this.addCellToRow(row, 16, username);
    }

    private void addCellToRow(Row row, int position, String value) {
        Cell result = row.createCell(position);
        result.setCellValue(value);
    }

    private void addCellToRow(Row row, int position, Date value) {
        Cell result = row.createCell(position);
        Workbook workbook = row.getSheet().getWorkbook();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));
        result.setCellStyle(dateStyle);
        result.setCellValue(value);
    }

    private String getFunctionalityPath(FunctionalityDTO functionality, List<FunctionalityDTO> functionalities) {
        if (null == functionality.getParentId()) {
            return "";
        }
        Long parentId = functionality.getParentId();
        FunctionalityDTO parent = functionalities.stream()
                .filter(f -> f.getId().equals(parentId))
                .findFirst()
                .orElse(null);
        if (parent == null) {
            return "";
        }
        return this.getFunctionalityPath(parent, functionalities) + "/" + parent.getName();
    }
}
