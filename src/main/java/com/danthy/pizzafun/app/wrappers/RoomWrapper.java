package com.danthy.pizzafun.app.wrappers;

import com.danthy.pizzafun.app.utils.ApplicationProperties;
import com.danthy.pizzafun.domain.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class RoomWrapper {
    private final RoomModel roomModel;

    private final ObservableList<OrderWrapper> orderModelObservableList;

    private final ObservableList<ItemStockWrapper> itemStockModelObservableList;

    private SupplierModel supplierModel;

    public RoomWrapper(RoomModel roomModel) {
        this.roomModel = roomModel;

        this.orderModelObservableList = FXCollections.observableArrayList();

        List<ItemStockWrapper> itemStockWrapperList = roomModel
                .getStockModel()
                .getItemStockModels()
                .stream()
                .map(ItemStockWrapper::new)
                .toList();

        this.itemStockModelObservableList = FXCollections.observableArrayList(itemStockWrapperList);
    }

    public void addOrder(OrderWrapper orderWrapper) {
        this.orderModelObservableList.add(orderWrapper);
    }

    public void restockBySupplier(SupplierModel supplierModel) {
        int itemMaxWeight = ApplicationProperties.itemMaxWeight;

        for (int i = 0;i < itemStockModelObservableList.size();i++) {
            ItemStockWrapper itemStockWrapper = itemStockModelObservableList.get(i);

            itemStockWrapper.incrementQuantity(itemMaxWeight - itemStockWrapper.getItem().getWeight() + 1);

            itemStockModelObservableList.set(i, itemStockWrapper);
        }

        roomModel.decBalance(supplierModel.getCost());
    }

    public boolean isRemoveOrderValid(OrderModel orderModel) {
        List<ItemPizzaModel> itemPizzaModelList = orderModel.getPizzaModel().getItemPizzaModels();

        for (ItemStockWrapper itemStockWrapper : itemStockModelObservableList) {
            ItemModel itemModel = itemStockWrapper.getItem();

            for (ItemPizzaModel itemPizzaModel : itemPizzaModelList) {
                ItemModel itemModelAux = itemPizzaModel.getItem();

                if (itemModel.equals(itemModelAux)) {
                    if (itemPizzaModel.getQuantity() > itemStockWrapper.getQuantity()) return false;
                }
            }
        }

        return true;
    }

    public void removeItemStockFromOrder(OrderModel orderModel) {
        List<ItemPizzaModel> itemPizzaModelList = orderModel.getPizzaModel().getItemPizzaModels();

        for (int i = 0;i < itemStockModelObservableList.size();i++) {
            ItemStockWrapper itemStockWrapper = itemStockModelObservableList.get(i);
            ItemModel itemModel = itemStockWrapper.getItem();

            for (ItemPizzaModel itemPizzaModel : itemPizzaModelList) {
                ItemModel itemModelAux = itemPizzaModel.getItem();

                if (itemModel.equals(itemModelAux)) {
                    itemStockWrapper.decrementQuantity(itemPizzaModel.getQuantity());
                    itemStockModelObservableList.set(i, itemStockWrapper);
                    break;
                }
            }
        }
    }

    public void removeOrder(OrderWrapper orderWrapper) {
        this.orderModelObservableList.remove(orderWrapper);

        roomModel.incBalance(orderWrapper.getOrderModel().getPizzaModel().getPrice());
    }

    public String getBalancePrint() {
        return String.format("Dinheiro: $%.2f", roomModel.getBalance());
    }

    public int getOrdersAmount() {
        return this.orderModelObservableList.size();
    }

    public ObservableList<OrderWrapper> getOrderModelObservableList() {
        return orderModelObservableList;
    }

    public ObservableList<ItemStockWrapper> getItemStockModelObservableList() {
        return itemStockModelObservableList;
    }
}
