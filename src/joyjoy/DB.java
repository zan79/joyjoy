package joyjoy;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    
    public static String excel_path = "C:\\Users\\SON\\Documents\\JB DB.xlsx";
    public static String db_path = "C:\\Users\\SON\\Documents\\JOYJOY\\items_db.db";
    
    public static Connection connection() throws Exception {
        //Class.forName("com.mysql.cj.jdbc.Driver");
        //return DriverManager.getConnection("jdbc:mysql://localhost:3306/enrollment", "root", "");
        
        return DriverManager.getConnection("jdbc:sqlite:"+db_path);
    }
    
}
