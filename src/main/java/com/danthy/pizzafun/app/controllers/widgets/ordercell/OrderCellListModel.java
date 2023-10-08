package com.danthy.pizzafun.app.controllers.widgets.ordercell;

import com.danthy.pizzafun.app.config.ApplicationProperties;
import com.danthy.pizzafun.app.events.ProducedOrderEvent;
import com.danthy.pizzafun.app.logic.EventPublisher;
import com.danthy.pizzafun.app.logic.GetIt;
import com.danthy.pizzafun.app.services.IPizzariaService;
import com.danthy.pizzafun.app.wrappers.OrderWrapper;
import com.danthy.pizzafun.domain.models.OrderModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class OrderCellListModel {
    private IPizzariaService roomService;
    private OrderWrapper orderWrapper;

    public OrderCellListModel(OrderWrapper orderWrapper, IPizzariaService roomService) {
        this.orderWrapper = orderWrapper;
    }

    public boolean produceOrder() {
        OrderModel orderModel = orderWrapper.getOrderModel();
        boolean isLoading = orderWrapper.isLoading();
        boolean isItemStockAlreadyRemoved = orderWrapper.isItemStockAlreadyRemoved();

        if (roomService.isRemoveOrderValid(orderModel) || isLoading) {
            if (!isItemStockAlreadyRemoved) {
                roomService.removeItemStockFromOrder(orderModel);
                isItemStockAlreadyRemoved = true;
                orderWrapper.setItemStockAlreadyRemoved(true);
            }

            isLoading = true;
            orderWrapper.setLoading(true);

            Platform.runLater(() -> {
                int totDurationSeconds = ApplicationProperties.pizzaProduceBaseTime;
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), timeEvent -> {
                    double progress = orderWrapper.getProgress();

                    if (progress < 1.0) {
                        orderWrapper.setProgress(progress + 1.0 / totDurationSeconds);

                        orderWrapper.getProgressBar().setProgress(orderWrapper.getProgress());
                    }
                }));


                timeline.setCycleCount(totDurationSeconds);
                timeline.setOnFinished(onFinish -> {
                    GetIt.getInstance().find(EventPublisher.class).notifyAll(new ProducedOrderEvent(orderWrapper));
                });
                timeline.play();
            });

            return true;
        }

        return false;
    }
}