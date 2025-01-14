package com.danthy.pizzafun.app.events.mediator;

import com.danthy.pizzafun.app.contracts.IEvent;
import com.danthy.pizzafun.domain.models.OrderModel;

public record OrderGenerateEvent(OrderModel orderModel) implements IEvent {
}
