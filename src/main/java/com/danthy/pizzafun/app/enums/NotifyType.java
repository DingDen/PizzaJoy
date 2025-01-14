package com.danthy.pizzafun.app.enums;

import lombok.Getter;

@Getter
public enum NotifyType {
    INSUFFICIENTMONEY("DINHEIRO INSUFICIENTE"), INSUFFICIENTTOKEN("TOKEN INSUFICIENTE"), INSUFFICIENTSTOCK("SEM ESTOQUE"), MAXSTOCKWEIGHT("ESTOQUE LOTADO");

    private final String message;

    NotifyType(String message) {
        this.message = message;
    }
}
