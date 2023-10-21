package com.danthy.pizzafun.app.services.implementations;

import com.danthy.pizzafun.app.config.ApplicationProperties;
import com.danthy.pizzafun.app.contracts.*;
import com.danthy.pizzafun.app.enums.NotifyType;
import com.danthy.pizzafun.app.events.mediator.NotifyEvent;
import com.danthy.pizzafun.app.events.mediator.ReStockEvent;
import com.danthy.pizzafun.app.events.mediator.RequestProduceOrderEvent;
import com.danthy.pizzafun.app.events.mediator.StartGameEvent;
import com.danthy.pizzafun.app.events.services.SuccessBuySupplierEvent;
import com.danthy.pizzafun.app.events.services.SuccessLevelUpEvent;
import com.danthy.pizzafun.app.logic.GetIt;
import com.danthy.pizzafun.app.services.IStockService;
import com.danthy.pizzafun.app.states.StockState;
import com.danthy.pizzafun.app.utils.TimelineUtil;
import com.danthy.pizzafun.domain.enums.UpgradeType;
import com.danthy.pizzafun.domain.models.*;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;

import java.util.List;

public class StockServiceImpl implements IStockService, IMediatorEmitter, IObserverEmitter {
    private StockState stockState;

    @Override
    public void onBoostRateSpeedEvent(MouseEvent event) {
        double currentRateSpeed = stockState.getRateSpeedObservable().getValue();

        getRateSpeedProperty().setValue(currentRateSpeed + 0.1);

        TimelineUtil.runFunctionAfterTimeInSeconds(2, (timelineEvent) -> {
            getRateSpeedProperty().setValue(getRateSpeedProperty().getValue() - 0.1);
        });
    }

    @Override
    public ObservableList<ItemStockModel> getItemStockModelObservableList() {
        return stockState.getItemStockModelObservableList();
    }

    @Override
    public Property<Double> getTimerToNextRestockProperty() {
        return stockState.getTimerToNextRestockObservable().getProperty();
    }

    @Override
    public Property<Integer> getCurrentStockWeightProperty() {
        return stockState.getCurrentStockWeight().getProperty();
    }

    @Override
    public Property<Integer> getTotalStockWeightProperty() {
        return stockState.getTotalStockWeight().getProperty();
    }

    @Override
    public Property<Double> getRateSpeedProperty() {
        return stockState.getRateSpeedObservable().getProperty();
    }

    @EventMap(SuccessBuySupplierEvent.class)
    public void onSuccessBuySupplierEvent(IEvent event) {
        SuccessBuySupplierEvent successBuySupplierEvent = (SuccessBuySupplierEvent) event;
        SupplierModel supplierModel = successBuySupplierEvent.supplierModel();

        getTimerToNextRestockProperty().setValue(supplierModel.getDeliveryTimeInSeconds());
    }

    @EventMap(SuccessLevelUpEvent.class)
    private void onSuccessLevelUpEvent(IEvent event) {
        SuccessLevelUpEvent successLevelUpEvent = (SuccessLevelUpEvent) event;
        UpgradeModel upgradeModel = successLevelUpEvent.upgradeModel();

        if(upgradeModel.getUpgradeType() == UpgradeType.REPLACER){
            int level = upgradeModel.getLevel();
            double calcRatetime = 0.09 * level;
            stockState.incrementRateSpeed(calcRatetime);
        } else if (upgradeModel.getUpgradeType() == UpgradeType.PIZZARIA) {
            int level = upgradeModel.getLevel();
            int calcStockWeight = 75 * level;
            stockState.incrementTotalStockWeight(calcStockWeight);
        }
    }

    @ReactOn(StartGameEvent.class)
    public void reactOnStartGameEvent(IEvent event) {
        stockState = GetIt.getInstance().find(StockState.class);
    }

    @ReactOn(RequestProduceOrderEvent.class)
    public void reactOnRequestProduceOrderEvent(IEvent event) {
        RequestProduceOrderEvent requestProduceOrderEvent = (RequestProduceOrderEvent) event;

        if (this.isRemoveOrderValid(requestProduceOrderEvent.orderWrapper().getOrderModel())) {
            requestProduceOrderEvent.cellController().startToProduceOrder();

            OrderModel orderModel = requestProduceOrderEvent.orderWrapper().getOrderModel();

            this.removeItemStockFromOrder(orderModel);
            this.refreshStockWeight(orderModel);
        } else {
            this.sendEvent(new NotifyEvent(NotifyType.INSUFFICIENTSTOCK));
        }
    }

    @ReactOn(ReStockEvent.class)
    public void reactOnRestockEvent(IEvent event) {
        ReStockEvent reStockEvent = (ReStockEvent) event;
        SupplierModel supplierModel = reStockEvent.supplierModel();

        getTimerToNextRestockProperty().setValue(supplierModel.getDeliveryTimeInSeconds());

        ObservableList<ItemStockModel> itemStockWrapperObservableList = stockState.getItemStockModelObservableList();

        int itemMaxWeight = ApplicationProperties.itemMaxWeight;
        int stockWeightGained = getTotalWeightGainedFromRestock(supplierModel);
        int currentStockWeight = getCurrentStockWeightProperty().getValue();

        if (stockWeightGained + currentStockWeight > stockState.getTotalStockWeight().getValue()) {
            sendEvent(new NotifyEvent(NotifyType.MAXSTOCKWEIGHT));
            return;
        }

        for (int i = 0; i < itemStockWrapperObservableList.size(); i++) {
            ItemStockModel itemStockModel = itemStockWrapperObservableList.get(i);

            int weight = itemStockModel.getItemModel().getWeight();
            int quantity = itemMaxWeight - weight + 1;
            itemStockModel.incrementQuantity(quantity);

            itemStockWrapperObservableList.set(i, itemStockModel);
        }

        getCurrentStockWeightProperty().setValue(currentStockWeight + stockWeightGained);
    }

    private int getTotalWeightGainedFromRestock(SupplierModel supplierModel) {
        ObservableList<ItemStockModel> itemStockWrapperObservableList = stockState.getItemStockModelObservableList();

        int itemMaxWeight = ApplicationProperties.itemMaxWeight;
        int stockWeightGained = 0;
        for (ItemStockModel itemStockModel : itemStockWrapperObservableList) {
            int weight = itemStockModel.getItemModel().getWeight();
            int quantity = itemMaxWeight - weight + 1;
            stockWeightGained += quantity * weight;
        }

        return stockWeightGained;
    }

    private boolean isRemoveOrderValid(OrderModel orderModel) {
        List<ItemPizzaModel> itemPizzaModelList = orderModel.getPizzaModel().getItemPizzaModels();

        for (ItemStockModel itemStockModel : stockState.getItemStockModelObservableList()) {
            ItemModel itemModel = itemStockModel.getItemModel();

            for (ItemPizzaModel itemPizzaModel : itemPizzaModelList) {
                ItemModel itemModelAux = itemPizzaModel.getItemModel();

                if (itemModel.equals(itemModelAux)) {
                    if (itemPizzaModel.getQuantity() > itemStockModel.getQuantity()) return false;
                }
            }
        }

        return true;
    }

    private void removeItemStockFromOrder(OrderModel orderModel) {
        ObservableList<ItemStockModel> itemStockWrapperObservableList = stockState.getItemStockModelObservableList();
        List<ItemPizzaModel> itemPizzaModelList = orderModel.getPizzaModel().getItemPizzaModels();

        for (int i = 0; i < itemStockWrapperObservableList.size(); i++) {
            ItemStockModel itemStockModel = itemStockWrapperObservableList.get(i);
            ItemModel itemModel = itemStockModel.getItemModel();

            for (ItemPizzaModel itemPizzaModel : itemPizzaModelList) {
                ItemModel itemModelAux = itemPizzaModel.getItemModel();

                if (itemModel.equals(itemModelAux)) {
                    itemStockModel.decrementQuantity(itemPizzaModel.getQuantity());
                    itemStockWrapperObservableList.set(i, itemStockModel);
                    break;
                }
            }
        }
    }

    private void refreshStockWeight(OrderModel orderModel) {
        int stockWeightLosted = orderModel.getPizzaModel()
                .getItemPizzaModels()
                .stream()
                .map(itemPizzaModel -> itemPizzaModel.getQuantity() * itemPizzaModel.getItemModel().getWeight())
                .reduce(0, Integer::sum);

        int currentStockWeight = getCurrentStockWeightProperty().getValue();
        getCurrentStockWeightProperty().setValue(currentStockWeight - stockWeightLosted);
    }
}
