package com.danthy.pizzafun.app.controllers.pizzaria.widgets.itemstockcell;

import com.danthy.pizzafun.app.contracts.IController;
import com.danthy.pizzafun.domain.models.ItemModel;
import com.danthy.pizzafun.domain.models.ItemStockModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ItemStockCellListController implements IController {
    @FXML
    private Label itemStockNameLabel;

    @FXML
    private Label itemStockQuantityLabel;

    @FXML
    private HBox cellRoot;

    @FXML
    private ImageView itemStockBg;

    @Override
    public void initComponents() {
        cellRoot.widthProperty().addListener((observable, oldValue, newValue) -> {
            itemStockBg.setFitWidth(newValue.doubleValue());
        });
    }

    public void initCell(ItemStockModel itemStockModel) {
        ItemModel itemModel = itemStockModel.getItemModel();

        itemStockNameLabel.setText(itemModel.getName());
        itemStockQuantityLabel.setText(itemStockModel.getQuantity() + "x");
    }
}
