package com.Api.SteamApp.service;

import com.Api.SteamApp.model.Game;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class SteamService {

    private static final String BASE_URL = "https://store.steampowered.com/api/appdetails?appids=";
    private static final String SEARCH_URL = "https://store.steampowered.com/api/storesearch/?term=";

    private final HttpClient client = HttpClient.newHttpClient();

    /** Busca informações completas do jogo pelo appId */
    public Game getGameInfo(int appId) throws Exception {
        String url = BASE_URL + appId + "&cc=br&l=pt";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject gameData = json.getAsJsonObject(String.valueOf(appId)).getAsJsonObject("data");

        String name = gameData.get("name").getAsString();
        String type = gameData.get("type").getAsString();
        String releaseDate = gameData.getAsJsonObject("release_date").get("date").getAsString();

        String price = "Gratuito";
        if (gameData.has("price_overview")) {
            JsonObject priceOverview = gameData.getAsJsonObject("price_overview");
            price = priceOverview.get("final_formatted").getAsString();
        }

        return new Game(appId, name, type, releaseDate, price);
    }

    /** Busca pelo nome: retorna 1 jogo se for específico, lista se for genérico */
    public List<Game> searchGameByName(String name) throws Exception {
        String url = SEARCH_URL + name.replace(" ", "%20") + "&cc=br";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray items = json.getAsJsonArray("items");

        if (items.size() == 0) {
            throw new RuntimeException("Jogo não encontrado: " + name);
        }

        List<Game> results = new ArrayList<>();

        // Limite de requisições extras: para não sobrecarregar a Steam, pegamos todos, mas você pode limitar se quiser
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            int appId = item.get("id").getAsInt();

            try {
                // Chama getGameInfo para pegar tipo, releaseDate e preço real
                Game game = getGameInfo(appId);
                results.add(game);
            } catch (Exception e) {
                // Se falhar, adiciona pelo menos id, name e preço básico
                String gameName = item.get("name").getAsString();
                String price = "Indisponível";
                if (item.has("price") && item.getAsJsonObject("price").has("final")) {
                    double finalPrice = item.getAsJsonObject("price").get("final").getAsDouble() / 100.0;
                    price = "R$ " + String.format("%.2f", finalPrice);
                }
                results.add(new Game(appId, gameName, "desconhecido", "-", price));
            }
        }

        return results;
    }
}
