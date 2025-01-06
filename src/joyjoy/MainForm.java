package joyjoy;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class MainForm extends javax.swing.JFrame {

    Excel exc;
    List<List> QouteList;
    int itemCount;

    public MainForm() throws Exception {
        initComponents();
        exc = new Excel();
        QouteList = new ArrayList<>();

        //loadDB();
        loadQueryTable("", "");

        initTab();
        displayStatus();

        this.setFocusable(true);
        this.requestFocus();

        ItemNameTF.requestFocus();
    }

    private void displayStatus() {
        FileDateStampLBL.setText(exc.FileDateStamp);
        DateNowLBL.setText(exc.DateNow);
    }

    private void initTab() {
        qouteTab.setVisible(false);

        queryTabBtn.setBackground(Color.BLUE);
        qouteTabBtn.setBackground(Color.GRAY);
    }

    private void loadDB() throws Exception {
        exc.importExcel(DB.excel_path);
    }

    private void loadQueryTable(String codeKeyword, String nameKeyword) {
        try {
            List<Item> list = ItemDAO.getList(codeKeyword, nameKeyword);
            DefaultTableModel model = (DefaultTableModel) DataTable.getModel();
            model.setRowCount(0);
            itemCount = 0;
            for (int i = 0; i < list.size(); i++) {
                List l = new ArrayList();

                l.add(list.get(i).getItemCode());
                l.add(list.get(i).getItemName());
                l.add(list.get(i).getItemQty());
                itemCount += list.get(i).getItemQty();
                l.add(list.get(i).getItemRetail());
                l.add(list.get(i).getItemPrice());

                model.addRow(l.toArray());
            }

            itemCountLabel.setText(itemCount + " ITEMS FOUND");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQouteTable() {
        DefaultTableModel model = (DefaultTableModel) QouteTable.getModel();
        model.setRowCount(0);

        for (int i = 0; i < QouteList.size(); i++) {
            List row = new ArrayList();

            row.add(QouteList.get(i).get(0));
            row.add(QouteList.get(i).get(1));
            row.add(QouteList.get(i).get(2));
            //row.add(QouteList.get(i).get(PriceTypeCB.getSelectedIndex() == 0 ? 3 : 4));
            row.add(QouteList.get(i).get(6));
            row.add(QouteList.get(i).get(5));

            model.addRow(row.toArray());
        }

        calculateQoute();
    }

    private void calculateQoute() {
        int originalPrice = 0;
        int totalPrice = 0;

        for (List item : QouteList) {
            originalPrice += Float.parseFloat(item.get(3).toString()) * Integer.parseInt(item.get(2).toString());
            totalPrice += Float.parseFloat(item.get(5).toString());
        }
        OriginalPriceLBL.setText(String.format("%,.2f", Double.parseDouble(originalPrice + "")));
        PV_TotalPrice.setText(String.format("%,.2f", Double.parseDouble(totalPrice + "")));
    }

    private void addItemToQoute(String itemName) {
        //check for duplication
        for (List item : QouteList) {
            if (item.get(0).equals(itemName)) {

                int qty = Integer.parseInt(item.get(2) + "");
                item.set(2, qty + 1);
                loadQouteTable();

                QouteInputTF.setText("");

                handleQouteChanges();
                return;
            }
        }

        //search for the item
        try {
            Item inputItem = ItemDAO.getItem(itemName);

            if (inputItem != null) {

                List item = new ArrayList<>();
                item.add(inputItem.getItemCode());
                item.add(inputItem.getItemName());

                item.add(1);

                item.add(inputItem.getItemRetail());
                item.add(inputItem.getItemPrice());

                //item.add(PriceTypeCB.getSelectedIndex() == 0 ? inputItem.getItemRetail() : inputItem.getItemPrice());
                item.add(0);
                item.add(0);

                QouteList.add(item);

                QouteInputTF.setText("");

                loadQouteTable();
                handleQouteChanges();
            } else {

                List<Item> searchList = ItemDAO.getList(QouteInputTF.getText(), QouteInputTF.getText());

                if (searchList.size() == 1) {
                    addItemToQoute(searchList.get(0).getItemCode());
                    return;
                }

                if (searchList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Cannot Find " + QouteInputTF.getText(), "Error", JOptionPane.ERROR_MESSAGE);
                    QouteInputTF.setText("");
                } else {
                    DefaultTableModel model = (DefaultTableModel) PopUpTable.getModel();
                    model.setRowCount(0);
                    for (int i = 0; i < searchList.size(); i++) {
                        List l = new ArrayList();

                        l.add(searchList.get(i).getItemCode());
                        l.add(searchList.get(i).getItemName());
                        l.add(searchList.get(i).getItemQty());
                        l.add(searchList.get(i).getItemPrice());

                        model.addRow(l.toArray());
                    }
                    PopUpDialog.setLocationRelativeTo(null);
                    PopUpDialog.setVisible(true);
                }
            }
            QouteInputTF.requestFocus();
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleQouteChanges() {
        DefaultTableModel model = (DefaultTableModel) QouteTable.getModel();

        int priceType = PriceTypeCB.getSelectedIndex();

        if(QouteList==null)return;
        for (int i = 0; i < QouteList.size(); i++) {

            int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
            QouteList.get(i).set(2, qty);

            double total;

            if (priceType == 0 || priceType == 7 || priceType == 11
                    || priceType == 17 || priceType == 18 || priceType == 19 || priceType == 20) {
                total = Double.parseDouble(QouteList.get(i).get(3).toString());
            } else {
                total = Double.parseDouble(QouteList.get(i).get(4).toString());
            }
            QouteList.get(i).set(6, total);

            double amount = qty * total;

            switch (priceType) {
                case 2:
                    amount += amount * Addon.HC_6;
                    break;
                case 3:
                    amount += amount * Addon.HC_9;
                    break;
                case 4:
                    amount += amount * Addon.HC_12;
                    break;
                case 5:
                    amount += amount * Addon.ASL_3mos;
                    break;
                case 6:
                    amount += amount * Addon.ASL_6mos;
                    break;
//                case 7:
//                    amount += amount * Addon.ASL_12mos;
//                    break;
                case 8:
                    amount += amount * Addon.RCBC_3mos;
                    break;
                case 9:
                    amount += amount * Addon.RCBC_6mos;
                    break;
                case 10:
                    amount += amount * Addon.RCBC_9mos;
                    break;
//                case11:
//                    amount += amount * Addon.RCBC_12mos;
//                    break;
                case 12:
                    amount += amount * Addon.RCBC_18mos;
                    break;
                case 13:
                    amount += amount * Addon.RCBC_24mos;
                    break;
                case 14:
                    amount += amount * Addon.RCBC_36mos;
                    break;
                case 15:
                    amount += amount * Addon.MB_INS;
                    break;
                case 16:
                    amount += amount * Addon.MB_zeroPL3;
                    break;
//                case 17:
//                    amount += amount * Addon.MB_zero6/9/12;
//                    break;
                case 18:
                    amount += amount * Addon.MB_zeroPL18;
                    break;
                case 19:
                    amount += amount * Addon.MB_zeroPL24;
                    break;
                case 20:
                    amount += amount * Addon.MB_zeroPL36;
                    break;
            }
            QouteList.get(i).set(5, (int) Math.ceil(amount));
            QouteList.get(i).set(6, (int) Math.ceil(amount) / qty);

//            if ((double) total != (double) QouteList.get(i).get(3) && (float) total != (float) QouteList.get(i).get(4)) {
//                QouteList.get(i).set(6, (int) Math.ceil(amount) / qty);
//            }
        }

        loadQouteTable();
    }

    private void filterTable() {
        clearTable();
        loadQueryTable(ItemCodeTF.getText(), ItemNameTF.getText());
    }

    private void clearTable() {
        DefaultTableModel dtm = (DefaultTableModel) DataTable.getModel();
        dtm.setRowCount(0);
    }

    private void previewItemInfo() {
        if (DataTable.getSelectedRow() != 0 || DataTable.getSelectedColumn() != 0) {
            int row = DataTable.getSelectedRow();

            PV_ItemName.setText(DataTable.getModel().getValueAt(row, 1).toString());
            PV_ItemPrice.setText(String.format("%,.2f", DataTable.getModel().getValueAt(row, 4)));

            ItemCodeLBL.setText(DataTable.getModel().getValueAt(row, 0).toString());
            ItemPriceLBL.setText(String.format("%,.2f", DataTable.getModel().getValueAt(row, 3)));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PopUpDialog = new javax.swing.JDialog();
        jScrollPane3 = new javax.swing.JScrollPane();
        PopUpTable = new javax.swing.JTable();
        RibbonPanel = new javax.swing.JPanel();
        qouteTabBtn = new javax.swing.JButton();
        queryTabBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        FileDateStampLBL = new javax.swing.JLabel();
        DateNowLBL = new javax.swing.JLabel();
        UpdateBtn = new javax.swing.JButton();
        LayeredPane = new javax.swing.JLayeredPane();
        qouteTab = new javax.swing.JPanel();
        HeaderPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        PresetsPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        PreviewPane1 = new javax.swing.JPanel();
        PV_TotalPrice = new javax.swing.JTextField();
        QouteInputTF = new javax.swing.JTextField();
        PV_ItemCode1 = new javax.swing.JLabel();
        OriginalPriceLBL = new javax.swing.JLabel();
        jlabel11 = new javax.swing.JLabel();
        jlabel12 = new javax.swing.JLabel();
        PriceTypeCB = new javax.swing.JComboBox<>();
        PV_ItemCode2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        QouteTable = new javax.swing.JTable();
        queryTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        DataTable = new javax.swing.JTable();
        PreviewPane = new javax.swing.JPanel();
        PV_ItemPrice = new javax.swing.JTextField();
        PV_ItemName = new javax.swing.JTextField();
        PV_ItemCode = new javax.swing.JLabel();
        jlabel5 = new javax.swing.JLabel();
        ItemPriceLBL = new javax.swing.JLabel();
        ItemCodeLBL = new javax.swing.JLabel();
        jlabel6 = new javax.swing.JLabel();
        jlabel7 = new javax.swing.JLabel();
        SearchPanel = new javax.swing.JPanel();
        ItemNameTF = new javax.swing.JTextField();
        jlabel8 = new javax.swing.JLabel();
        ItemCodeTF = new javax.swing.JTextField();
        jlabel9 = new javax.swing.JLabel();
        itemCountLabel = new javax.swing.JLabel();
        HeaderPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        PopUpDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        PopUpDialog.setTitle("Select Item");
        PopUpDialog.setAlwaysOnTop(true);
        PopUpDialog.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        PopUpDialog.setSize(new java.awt.Dimension(800, 500));

        PopUpTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        PopUpTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ITEM CODE", "ITEM NAME", "QTY", "PRICE"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        PopUpTable.setRowHeight(30);
        PopUpTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                PopUpTablePropertyChange(evt);
            }
        });
        PopUpTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PopUpTableKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(PopUpTable);
        PopUpTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (PopUpTable.getColumnModel().getColumnCount() > 0) {
            PopUpTable.getColumnModel().getColumn(0).setMinWidth(200);
            PopUpTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            PopUpTable.getColumnModel().getColumn(0).setMaxWidth(200);
            PopUpTable.getColumnModel().getColumn(1).setPreferredWidth(300);
            PopUpTable.getColumnModel().getColumn(2).setMinWidth(30);
            PopUpTable.getColumnModel().getColumn(2).setPreferredWidth(30);
            PopUpTable.getColumnModel().getColumn(2).setMaxWidth(30);
            PopUpTable.getColumnModel().getColumn(3).setMinWidth(70);
            PopUpTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            PopUpTable.getColumnModel().getColumn(3).setMaxWidth(70);
        }

        javax.swing.GroupLayout PopUpDialogLayout = new javax.swing.GroupLayout(PopUpDialog.getContentPane());
        PopUpDialog.getContentPane().setLayout(PopUpDialogLayout);
        PopUpDialogLayout.setHorizontalGroup(
            PopUpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
        );
        PopUpDialogLayout.setVerticalGroup(
            PopUpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JOYJOY");
        setBackground(new java.awt.Color(102, 204, 255));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        RibbonPanel.setBackground(new java.awt.Color(51, 51, 51));
        RibbonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        qouteTabBtn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        qouteTabBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/quote.png"))); // NOI18N
        qouteTabBtn.setText("QUOTATION");
        qouteTabBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        qouteTabBtn.setMargin(new java.awt.Insets(0, 5, 0, 5));
        qouteTabBtn.setOpaque(false);
        qouteTabBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qouteTabBtnActionPerformed(evt);
            }
        });

        queryTabBtn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        queryTabBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/query.png"))); // NOI18N
        queryTabBtn.setText("QUERY");
        queryTabBtn.setMargin(new java.awt.Insets(0, 5, 0, 5));
        queryTabBtn.setOpaque(false);
        queryTabBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryTabBtnActionPerformed(evt);
            }
        });

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("EXPORTED EXCEL FILE");

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("TODAY");

        FileDateStampLBL.setBackground(new java.awt.Color(255, 255, 255));
        FileDateStampLBL.setText("______________________________________");
        FileDateStampLBL.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        FileDateStampLBL.setOpaque(true);

        DateNowLBL.setBackground(new java.awt.Color(255, 255, 255));
        DateNowLBL.setText("______________________________________");
        DateNowLBL.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        DateNowLBL.setOpaque(true);

        UpdateBtn.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        UpdateBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        UpdateBtn.setText("<html><body>UPDATE<br>DATABASE</body></html>");
        UpdateBtn.setMargin(new java.awt.Insets(0, 5, 0, 5));
        UpdateBtn.setMaximumSize(new java.awt.Dimension(545, 521));
        UpdateBtn.setMinimumSize(new java.awt.Dimension(545, 521));
        UpdateBtn.setOpaque(false);
        UpdateBtn.setPreferredSize(new java.awt.Dimension(23, 23));
        UpdateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RibbonPanelLayout = new javax.swing.GroupLayout(RibbonPanel);
        RibbonPanel.setLayout(RibbonPanelLayout);
        RibbonPanelLayout.setHorizontalGroup(
            RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RibbonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(queryTabBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qouteTabBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(FileDateStampLBL, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                    .addComponent(DateNowLBL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UpdateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        RibbonPanelLayout.setVerticalGroup(
            RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RibbonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RibbonPanelLayout.createSequentialGroup()
                        .addGroup(RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(FileDateStampLBL)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(RibbonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DateNowLBL)
                            .addComponent(jLabel4)))
                    .addComponent(qouteTabBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(queryTabBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(RibbonPanelLayout.createSequentialGroup()
                        .addComponent(UpdateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        LayeredPane.setBackground(new java.awt.Color(204, 204, 204));

        qouteTab.setBackground(new java.awt.Color(255, 255, 255));

        HeaderPanel1.setBackground(new java.awt.Color(255, 0, 0));
        HeaderPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setBackground(new java.awt.Color(204, 204, 255));
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/quote.png"))); // NOI18N
        jLabel2.setText("PRICE QOUTATION");
        jLabel2.setPreferredSize(new java.awt.Dimension(123, 52));

        javax.swing.GroupLayout HeaderPanel1Layout = new javax.swing.GroupLayout(HeaderPanel1);
        HeaderPanel1.setLayout(HeaderPanel1Layout);
        HeaderPanel1Layout.setHorizontalGroup(
            HeaderPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE)
        );
        HeaderPanel1Layout.setVerticalGroup(
            HeaderPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        PresetsPanel.setBackground(new java.awt.Color(255, 255, 255));
        PresetsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("CONTACT US:  0920-671-1761 or (038) 412-2484");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("PRICES ARE SUBJECT TO CHANGE WITHOUT PRIOR NOTICE");

        javax.swing.GroupLayout PresetsPanelLayout = new javax.swing.GroupLayout(PresetsPanel);
        PresetsPanel.setLayout(PresetsPanelLayout);
        PresetsPanelLayout.setHorizontalGroup(
            PresetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PresetsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        PresetsPanelLayout.setVerticalGroup(
            PresetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PresetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        PreviewPane1.setBackground(new java.awt.Color(204, 204, 204));
        PreviewPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        PV_TotalPrice.setEditable(false);
        PV_TotalPrice.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        PV_TotalPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        PV_TotalPrice.setText("0.00");
        PV_TotalPrice.setMargin(new java.awt.Insets(2, 2, 2, 10));

        QouteInputTF.setBackground(new java.awt.Color(255, 255, 204));
        QouteInputTF.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        QouteInputTF.setMargin(new java.awt.Insets(2, 10, 2, 2));
        QouteInputTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                QouteInputTFKeyPressed(evt);
            }
        });

        PV_ItemCode1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        PV_ItemCode1.setForeground(new java.awt.Color(204, 204, 204));
        PV_ItemCode1.setText("ORIGINAL PRICE:");

        OriginalPriceLBL.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        OriginalPriceLBL.setForeground(new java.awt.Color(204, 204, 204));
        OriginalPriceLBL.setText("0.00");

        jlabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlabel11.setText(" INPUT ITEM");

        jlabel12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jlabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlabel12.setText(" TOTAL PRICE");

        PriceTypeCB.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        PriceTypeCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ORIGINAL", "CASH", "HC 6mos", "HC 9mos", "HC 12-18mos", "AUB,SB,LB 3mos", "AUB,SB,LB 6mos", "AUB,SB,LB 12mos", "RCBC 3mos", "RCBC 6mos", "RCBC 9mos", "RCBC 12mos", "RCBC 18mos", "RCBC 24mos", "RCBC 36mos", "MB Installment", "MB 0% 3mos", "MB 0% 6/9/12mos", "MB 0% 18mos", "MB 0% 24mos", "MB 0% 36mos" }));
        PriceTypeCB.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                PriceTypeCBPropertyChange(evt);
            }
        });

        PV_ItemCode2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        PV_ItemCode2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        PV_ItemCode2.setText("PRICE TYPE:");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout PreviewPane1Layout = new javax.swing.GroupLayout(PreviewPane1);
        PreviewPane1.setLayout(PreviewPane1Layout);
        PreviewPane1Layout.setHorizontalGroup(
            PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PreviewPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PreviewPane1Layout.createSequentialGroup()
                        .addComponent(QouteInputTF, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(PreviewPane1Layout.createSequentialGroup()
                                .addComponent(PV_ItemCode1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(OriginalPriceLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PreviewPane1Layout.createSequentialGroup()
                                .addComponent(PV_ItemCode2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PriceTypeCB, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(PreviewPane1Layout.createSequentialGroup()
                        .addComponent(jlabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(PV_TotalPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(jlabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PreviewPane1Layout.setVerticalGroup(
            PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PreviewPane1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jlabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6))
                    .addComponent(jlabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PreviewPane1Layout.createSequentialGroup()
                        .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(QouteInputTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(PriceTypeCB)
                                .addComponent(PV_ItemCode2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PreviewPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(OriginalPriceLBL, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                            .addComponent(PV_ItemCode1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PreviewPane1Layout.createSequentialGroup()
                        .addComponent(PV_TotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        QouteTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        QouteTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ITEM CODE", "ITEM NAME", "QTY", "UNIT PRICE", "TOTAL AMOUNT"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        QouteTable.setCellSelectionEnabled(true);
        QouteTable.setRowHeight(25);
        QouteTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                QouteTablePropertyChange(evt);
            }
        });
        QouteTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                QouteTableKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(QouteTable);
        QouteTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (QouteTable.getColumnModel().getColumnCount() > 0) {
            QouteTable.getColumnModel().getColumn(0).setMinWidth(200);
            QouteTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            QouteTable.getColumnModel().getColumn(0).setMaxWidth(200);
            QouteTable.getColumnModel().getColumn(1).setPreferredWidth(300);
            QouteTable.getColumnModel().getColumn(2).setMinWidth(30);
            QouteTable.getColumnModel().getColumn(2).setPreferredWidth(30);
            QouteTable.getColumnModel().getColumn(2).setMaxWidth(30);
            QouteTable.getColumnModel().getColumn(3).setMinWidth(70);
            QouteTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            QouteTable.getColumnModel().getColumn(3).setMaxWidth(70);
            QouteTable.getColumnModel().getColumn(4).setMinWidth(100);
            QouteTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            QouteTable.getColumnModel().getColumn(4).setMaxWidth(100);
        }

        javax.swing.GroupLayout qouteTabLayout = new javax.swing.GroupLayout(qouteTab);
        qouteTab.setLayout(qouteTabLayout);
        qouteTabLayout.setHorizontalGroup(
            qouteTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, qouteTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(qouteTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addComponent(PresetsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PreviewPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(qouteTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(HeaderPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        qouteTabLayout.setVerticalGroup(
            qouteTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(qouteTabLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(PresetsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PreviewPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(qouteTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(qouteTabLayout.createSequentialGroup()
                    .addComponent(HeaderPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 576, Short.MAX_VALUE)))
        );

        queryTab.setBackground(new java.awt.Color(255, 255, 255));

        DataTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        DataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ITEM CODE", "ITEM NAME", "QTY", "ORIGINAL", "PRESENT"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        DataTable.setCellSelectionEnabled(true);
        DataTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        DataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                DataTableMouseReleased(evt);
            }
        });
        DataTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                DataTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(DataTable);
        DataTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (DataTable.getColumnModel().getColumnCount() > 0) {
            DataTable.getColumnModel().getColumn(0).setMinWidth(200);
            DataTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            DataTable.getColumnModel().getColumn(0).setMaxWidth(200);
            DataTable.getColumnModel().getColumn(1).setPreferredWidth(300);
            DataTable.getColumnModel().getColumn(2).setMinWidth(30);
            DataTable.getColumnModel().getColumn(2).setPreferredWidth(30);
            DataTable.getColumnModel().getColumn(2).setMaxWidth(30);
            DataTable.getColumnModel().getColumn(3).setMinWidth(70);
            DataTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            DataTable.getColumnModel().getColumn(3).setMaxWidth(70);
            DataTable.getColumnModel().getColumn(4).setMinWidth(70);
            DataTable.getColumnModel().getColumn(4).setPreferredWidth(70);
            DataTable.getColumnModel().getColumn(4).setMaxWidth(70);
        }

        PreviewPane.setBackground(new java.awt.Color(204, 204, 204));
        PreviewPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        PV_ItemPrice.setEditable(false);
        PV_ItemPrice.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        PV_ItemPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        PV_ItemPrice.setText("0.00");
        PV_ItemPrice.setMargin(new java.awt.Insets(2, 2, 2, 10));

        PV_ItemName.setEditable(false);
        PV_ItemName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        PV_ItemName.setMargin(new java.awt.Insets(2, 10, 2, 2));

        PV_ItemCode.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        PV_ItemCode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        PV_ItemCode.setText("ORIGINAL PRICE:");

        jlabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlabel5.setText(" ITEM CODE:");

        ItemPriceLBL.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        ItemPriceLBL.setText("0.00");

        ItemCodeLBL.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        jlabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlabel6.setText(" ITEM NAME/DESCRIPTION");

        jlabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jlabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlabel7.setText("CASH PRICE");

        javax.swing.GroupLayout PreviewPaneLayout = new javax.swing.GroupLayout(PreviewPane);
        PreviewPane.setLayout(PreviewPaneLayout);
        PreviewPaneLayout.setHorizontalGroup(
            PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PreviewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PreviewPaneLayout.createSequentialGroup()
                        .addComponent(jlabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ItemCodeLBL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PV_ItemCode, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ItemPriceLBL, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(PV_ItemName)
                    .addComponent(jlabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(PV_ItemPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(jlabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        PreviewPaneLayout.setVerticalGroup(
            PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PreviewPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jlabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PreviewPaneLayout.createSequentialGroup()
                        .addComponent(PV_ItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PreviewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jlabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ItemCodeLBL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PV_ItemCode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ItemPriceLBL, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)))
                    .addComponent(PV_ItemPrice))
                .addContainerGap())
        );

        SearchPanel.setBackground(new java.awt.Color(255, 255, 255));
        SearchPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ItemNameTF.setBackground(new java.awt.Color(255, 255, 204));
        ItemNameTF.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        ItemNameTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ItemNameTFKeyTyped(evt);
            }
        });

        jlabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlabel8.setText(" SEARCH ITEM NAME");

        ItemCodeTF.setBackground(new java.awt.Color(255, 255, 204));
        ItemCodeTF.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        ItemCodeTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ItemCodeTFKeyTyped(evt);
            }
        });

        jlabel9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlabel9.setText(" SEARCH ITEM CODE");

        itemCountLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        itemCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        itemCountLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/search.png"))); // NOI18N
        itemCountLabel.setText("ITEMS FOUND");
        itemCountLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout SearchPanelLayout = new javax.swing.GroupLayout(SearchPanel);
        SearchPanel.setLayout(SearchPanelLayout);
        SearchPanelLayout.setHorizontalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ItemCodeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ItemNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(itemCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );
        SearchPanelLayout.setVerticalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SearchPanelLayout.createSequentialGroup()
                        .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jlabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jlabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ItemCodeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ItemNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(SearchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(itemCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(5, 5, 5))
        );

        HeaderPanel.setBackground(new java.awt.Color(255, 0, 0));
        HeaderPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setBackground(new java.awt.Color(204, 204, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/query.png"))); // NOI18N
        jLabel1.setText("QUERY ITEM STOCK");
        jLabel1.setPreferredSize(new java.awt.Dimension(123, 52));

        javax.swing.GroupLayout HeaderPanelLayout = new javax.swing.GroupLayout(HeaderPanel);
        HeaderPanel.setLayout(HeaderPanelLayout);
        HeaderPanelLayout.setHorizontalGroup(
            HeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        HeaderPanelLayout.setVerticalGroup(
            HeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout queryTabLayout = new javax.swing.GroupLayout(queryTab);
        queryTab.setLayout(queryTabLayout);
        queryTabLayout.setHorizontalGroup(
            queryTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PreviewPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SearchPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap())
            .addComponent(HeaderPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        queryTabLayout.setVerticalGroup(
            queryTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, queryTabLayout.createSequentialGroup()
                .addComponent(HeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PreviewPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        LayeredPane.setLayer(qouteTab, javax.swing.JLayeredPane.DEFAULT_LAYER);
        LayeredPane.setLayer(queryTab, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout LayeredPaneLayout = new javax.swing.GroupLayout(LayeredPane);
        LayeredPane.setLayout(LayeredPaneLayout);
        LayeredPaneLayout.setHorizontalGroup(
            LayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(queryTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(LayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(qouteTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LayeredPaneLayout.setVerticalGroup(
            LayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(queryTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(LayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(qouteTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LayeredPane)
            .addComponent(RibbonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(RibbonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(LayeredPane))
        );

        setSize(new java.awt.Dimension(950, 729));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void ItemNameTFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ItemNameTFKeyTyped
        if (evt.getKeyCode() == KeyEvent.VK_KP_DOWN) {
            DataTable.requestFocus();
            System.out.println("Pressed");
            return;
        }
        filterTable();
    }//GEN-LAST:event_ItemNameTFKeyTyped

    private void ItemCodeTFKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ItemCodeTFKeyTyped
        filterTable();
    }//GEN-LAST:event_ItemCodeTFKeyTyped

    private void UpdateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateBtnActionPerformed
        try {
            clearTable();
//            Thread.sleep(2000);
            ItemCodeTF.setEnabled(false);
            ItemNameTF.setEnabled(false);
            UpdateBtn.setEnabled(false);

            loadDB();
            Thread.sleep(2000);
            loadQueryTable("", "");

            JOptionPane.showMessageDialog(this, "Database Updated Succesfully", "Complete", JOptionPane.INFORMATION_MESSAGE);

            ItemCodeTF.setEnabled(true);
            ItemNameTF.setEnabled(true);
            UpdateBtn.setEnabled(true);

            displayStatus();

        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_UpdateBtnActionPerformed

    private void DataTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DataTableMouseReleased
        previewItemInfo();
    }//GEN-LAST:event_DataTableMouseReleased

    private void DataTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DataTableKeyReleased
        previewItemInfo();
    }//GEN-LAST:event_DataTableKeyReleased

    private void queryTabBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryTabBtnActionPerformed
        queryTabBtn.setBackground(Color.BLUE);
        qouteTabBtn.setBackground(Color.GRAY);

        qouteTab.setVisible(false);
        queryTab.setVisible(true);
        ItemNameTF.requestFocus();
    }//GEN-LAST:event_queryTabBtnActionPerformed

    private void qouteTabBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qouteTabBtnActionPerformed
        queryTabBtn.setBackground(Color.GRAY);
        qouteTabBtn.setBackground(Color.BLUE);

        qouteTab.setVisible(true);
        queryTab.setVisible(false);
        QouteInputTF.requestFocus();
    }//GEN-LAST:event_qouteTabBtnActionPerformed

    private void QouteInputTFKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_QouteInputTFKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && !QouteInputTF.getText().isEmpty()) {
            addItemToQoute(QouteInputTF.getText());
        }
    }//GEN-LAST:event_QouteInputTFKeyPressed

    private void QouteTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_QouteTableKeyPressed
        try {
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) {

                int row = QouteTable.getSelectedRow();

                for (int i = 0; i < QouteList.size(); i++) {
                    if (QouteList.get(i).get(0).equals(QouteTable.getModel().getValueAt(row, 0).toString())) {
                        QouteList.remove(i);
                    }
                }
                loadQouteTable();
            }
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_QouteTableKeyPressed

    private void PriceTypeCBPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_PriceTypeCBPropertyChange
        handleQouteChanges();
    }//GEN-LAST:event_PriceTypeCBPropertyChange

    private void QouteTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_QouteTablePropertyChange
        handleQouteChanges();
    }//GEN-LAST:event_QouteTablePropertyChange

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        this.requestFocusInWindow(true);
        //this.requestFocus(true);

        if (evt.getKeyCode() == KeyEvent.VK_F1) {
            queryTabBtn.doClick();
        }
        if (evt.getKeyCode() == KeyEvent.VK_F2) {
            qouteTabBtn.doClick();
        }
    }//GEN-LAST:event_formKeyPressed

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        this.requestFocusInWindow(true);
        this.setFocusable(true);
        this.requestFocus();
    }//GEN-LAST:event_formWindowLostFocus

    private void PopUpTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_PopUpTablePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_PopUpTablePropertyChange

    private void PopUpTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PopUpTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            QouteInputTF.setText("");
            PopUpDialog.setVisible(false);
            return;
        }

        int row = PopUpTable.getSelectedRowCount();
        DefaultTableModel model = (DefaultTableModel) PopUpTable.getModel();

        if (row != 0 && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addItemToQoute(model.getValueAt(PopUpTable.getSelectedRow(), 0).toString());
            PopUpDialog.setVisible(false);
        }
    }//GEN-LAST:event_PopUpTableKeyPressed
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {

                    //UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                    //UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    //UIManager.setLookAndFeel(new FlatLightLaf());
                    //UIManager.setLookAndFeel(new FlatDarkLaf());
                    new MainForm().setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable DataTable;
    private javax.swing.JLabel DateNowLBL;
    private javax.swing.JLabel FileDateStampLBL;
    private javax.swing.JPanel HeaderPanel;
    private javax.swing.JPanel HeaderPanel1;
    private javax.swing.JLabel ItemCodeLBL;
    private javax.swing.JTextField ItemCodeTF;
    private javax.swing.JTextField ItemNameTF;
    private javax.swing.JLabel ItemPriceLBL;
    private javax.swing.JLayeredPane LayeredPane;
    private javax.swing.JLabel OriginalPriceLBL;
    private javax.swing.JLabel PV_ItemCode;
    private javax.swing.JLabel PV_ItemCode1;
    private javax.swing.JLabel PV_ItemCode2;
    private javax.swing.JTextField PV_ItemName;
    private javax.swing.JTextField PV_ItemPrice;
    private javax.swing.JTextField PV_TotalPrice;
    private javax.swing.JDialog PopUpDialog;
    private javax.swing.JTable PopUpTable;
    private javax.swing.JPanel PresetsPanel;
    private javax.swing.JPanel PreviewPane;
    private javax.swing.JPanel PreviewPane1;
    private javax.swing.JComboBox<String> PriceTypeCB;
    private javax.swing.JTextField QouteInputTF;
    private javax.swing.JTable QouteTable;
    private javax.swing.JPanel RibbonPanel;
    private javax.swing.JPanel SearchPanel;
    private javax.swing.JButton UpdateBtn;
    private javax.swing.JLabel itemCountLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel jlabel11;
    private javax.swing.JLabel jlabel12;
    private javax.swing.JLabel jlabel5;
    private javax.swing.JLabel jlabel6;
    private javax.swing.JLabel jlabel7;
    private javax.swing.JLabel jlabel8;
    private javax.swing.JLabel jlabel9;
    private javax.swing.JPanel qouteTab;
    private javax.swing.JButton qouteTabBtn;
    private javax.swing.JPanel queryTab;
    private javax.swing.JButton queryTabBtn;
    // End of variables declaration//GEN-END:variables

}
