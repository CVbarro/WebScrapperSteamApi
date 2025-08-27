package com.Api.SteamApp.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    private int appId;
    private String name;
    private String type;
    private String releaseDate;
    private String price;
}
