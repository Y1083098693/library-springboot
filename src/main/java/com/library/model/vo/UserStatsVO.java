package com.library.model.vo;

import lombok.Data;

@Data
public class UserStatsVO {
    private int orderTotal;
    private int favoriteTotal;
    private double spendTotal;
}