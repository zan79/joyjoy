package joyjoy;

import java.awt.HeadlessException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class Excel {

    public ArrayList<ArrayList> DataList;

    public String FileDateStamp;
    public String DateNow;
    public int itemCount;

    public void importExcel(String excelFile) throws Exception {

        try {
            ItemDAO.deleteAll();

            DataList = new ArrayList<ArrayList>();

            FileInputStream file = new FileInputStream(new File(excelFile));

            //date
            Path f = Paths.get(excelFile);
            BasicFileAttributes attr
                    = Files.readAttributes(f, BasicFileAttributes.class);

            LocalDateTime ExcelFile = attr.lastModifiedTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            FileDateStamp = ExcelFile.format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM/dd/yyyy - HH:mm a"));
            DateNow = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM/dd/yyyy"));
            //
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            int offset = 0;

            while (rowIterator.hasNext()) {
                offset++;

                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                if (offset > 3) {
                    ArrayList ro = new ArrayList<>();
                    while (cellIterator.hasNext()) {

                        //
                        Cell nextCell = cellIterator.next();
                        int columnIndex = nextCell.getColumnIndex();

                        switch (columnIndex) {
                            case 0:
                                ro.add(nextCell.getStringCellValue());
                                break;
                            case 1:
                                ro.add(nextCell.getStringCellValue());
                                break;
                            case 2:
                                ro.add(nextCell.getNumericCellValue());
                                break;
                            case 3:
                                ro.add(nextCell.getNumericCellValue());
                                break;
                            case 4:
                                ro.add(nextCell.getNumericCellValue());
                                break;
                        }
                    }
                    if (!ro.get(1).toString().isEmpty()) {
                        int qty = (int) Double.parseDouble(ro.get(2).toString());
                        Item item = new Item(
                                ro.get(0).toString(),
                                ro.get(1).toString(),
                                qty,
                                (double) Double.parseDouble(ro.get(3).toString()),
                                Float.parseFloat(ro.get(4).toString())
                        );
                        itemCount += qty;
                        ItemDAO.add(item);
                    }
                }
            }

            file.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid File Type" + e, "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

}
