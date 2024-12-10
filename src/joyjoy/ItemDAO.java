package joyjoy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public static List getList(String codeKeyword, String nameKeyword) throws Exception {
        List items = new ArrayList<Item>();

        Statement st = DB.connection().createStatement();
        ResultSet rs = null;

        if (codeKeyword.equals("") && nameKeyword.equals("")) {
            rs = st.executeQuery("SELECT * FROM items ORDER by item_code");
        } else {
            if (!codeKeyword.equals("") && !nameKeyword.equals("")) {
                if (codeKeyword.equals(nameKeyword)) {
                    rs = st.executeQuery("SELECT * FROM items WHERE item_code LIKE '%" + codeKeyword + "%' OR item_name LIKE '%" + nameKeyword + "%' ORDER BY item_code");
                } else {
                    rs = st.executeQuery("SELECT * FROM items WHERE item_code LIKE '%" + codeKeyword + "%' AND item_name LIKE '%" + nameKeyword + "%' ORDER BY item_price");
                }
            } else if (codeKeyword.equals("")) {
                rs = st.executeQuery("SELECT * FROM items WHERE item_name LIKE '%" + nameKeyword + "%' ORDER BY item_price");
            } else if (nameKeyword.equals("")) {
                rs = st.executeQuery("SELECT * FROM items WHERE item_code LIKE '%" + codeKeyword + "%' ORDER BY item_price");
            }
        }

        while (rs.next()) {
            Item item = new Item(
                    rs.getString("item_code"),
                    rs.getString("item_name"),
                    rs.getInt("item_qty"),
                    rs.getDouble("item_retail"),
                    rs.getFloat("item_price")
            );

            items.add(item);
        }
        st.close();
        rs.close();
        return items;
    }

    public static void add(Item item) throws Exception {
        PreparedStatement pst = DB.connection().prepareStatement(
                "INSERT INTO items (item_code, item_name, item_qty, item_retail, item_price)"
                + " VALUES (?,?,?,?,?)");
        pst.setString(1, item.getItemCode());
        pst.setString(2, item.getItemName());
        pst.setInt(3, item.getItemQty());
        pst.setDouble(4, item.getItemRetail());
        pst.setFloat(5, item.getItemPrice());

        pst.executeUpdate();
        pst.close();
    }

    public static void deleteAll() throws Exception {
        Statement st = DB.connection().createStatement();
        st.executeUpdate("DELETE FROM items");
        st.close();
    }

    public static Item getItem(String itemName) throws Exception {
        Item item = null;

        Statement st = DB.connection().createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM items WHERE item_code = '" + itemName + "' OR item_name = '" + itemName + "'");

        while (rs.next()) {
            item = new Item(
                    rs.getString("item_code"),
                    rs.getString("item_name"),
                    rs.getInt("item_qty"),
                    rs.getDouble("item_retail"),
                    rs.getFloat("item_price")
            );
        }
        st.close();
        rs.close();
        return item;
    }

}
