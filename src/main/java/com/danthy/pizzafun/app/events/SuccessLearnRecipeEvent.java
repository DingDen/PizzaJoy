package com.danthy.pizzafun.app.events;

import com.danthy.pizzafun.app.contracts.IEvent;
import com.danthy.pizzafun.app.controllers.widgets.recipecell.RecipeWrapper;

public record SuccessLearnRecipeEvent(RecipeWrapper recipeWrapper) implements IEvent {
}