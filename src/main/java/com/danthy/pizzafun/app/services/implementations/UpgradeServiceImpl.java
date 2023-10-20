package com.danthy.pizzafun.app.services.implementations;

import com.danthy.pizzafun.app.contracts.EventMap;
import com.danthy.pizzafun.app.contracts.IObserverEmitter;
import com.danthy.pizzafun.app.contracts.IEvent;
import com.danthy.pizzafun.app.contracts.ReactOn;
import com.danthy.pizzafun.app.events.mediator.StartGameEvent;
import com.danthy.pizzafun.app.events.services.SuccessLevelUpEvent;
import com.danthy.pizzafun.app.logic.GetIt;
import com.danthy.pizzafun.app.services.IUpgradeService;
import com.danthy.pizzafun.app.states.UpgradeState;
import com.danthy.pizzafun.domain.enums.UpgradeType;
import com.danthy.pizzafun.domain.models.UpgradeModel;
import javafx.collections.ObservableList;

public class UpgradeServiceImpl implements IUpgradeService, IObserverEmitter  {
    private UpgradeState upgradeState;

    @Override
    public void upgrade(UpgradeType upgradeType) {
        for (int i = 0;i < upgradeState.getUpgradeModelObservableList().size();i++) {
            UpgradeModel upgradeModel = upgradeState.getUpgradeModelObservableList().get(i);

            if (upgradeModel.getUpgradeType() == upgradeType) {
                upgradeModel.upgrade();

                upgradeState.getUpgradeModelObservableList().set(i, upgradeModel.getClone());
            }
        }
    }

    @Override
    public int getLevel(UpgradeType upgradeType) {
        for (UpgradeModel upgradeModel : upgradeState.getUpgradeModelObservableList())
            if (upgradeModel.getUpgradeType() == upgradeType) return upgradeModel.getLevel();

        throw new RuntimeException("Upgrade not mapped yet.");
    }

    @Override
    public ObservableList<UpgradeModel> getUpgradeModelObservableList() {
        return upgradeState.getUpgradeModelObservableList();
    }

    @ReactOn(StartGameEvent.class)
    public void reactOnStartGameEvent(IEvent event) {
        upgradeState = GetIt.getInstance().find(UpgradeState.class);
    }

    @EventMap(SuccessLevelUpEvent.class)
    public void onSuccessLevelUpEvent(IEvent event) {
        SuccessLevelUpEvent successLevelUpEvent = (SuccessLevelUpEvent) event;

        upgrade(successLevelUpEvent.upgradeModel().getUpgradeType());
    }
}
