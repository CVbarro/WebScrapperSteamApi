package com.Api.SteamApp.controller;

import com.Api.SteamApp.model.Game;
import com.Api.SteamApp.service.SteamService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/steam")
public class SteamController {

    private final SteamService steamService;

    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    // Buscar pelo appId
    @GetMapping("/game/{appId}")
    public Game getGame(@PathVariable int appId) throws Exception {
        return steamService.getGameInfo(appId);
    }

    // Buscar pelo nome
    @GetMapping("/game/search")
    public Object searchGame(@RequestParam String name) throws Exception {
        return steamService.searchGameByName(name);
    }
}
