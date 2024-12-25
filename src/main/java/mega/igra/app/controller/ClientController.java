package mega.igra.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mega.igra.app.model.Client;
import mega.igra.app.model.Game;
import mega.igra.app.repository.ClientRepository;
import mega.igra.app.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Client", description = "Operations related to clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final GameRepository gameRepository;

    public ClientController(ClientRepository clientRepository, GameRepository gameRepository) {
        this.clientRepository = clientRepository;
        this.gameRepository = gameRepository;
    }


    @Operation(summary = "Get client by ID", description = "Retrieve details of a client by their unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Client.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"login\":\"user1\",\"nickname\":\"nickname1\",\"balance\":100.0}"))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "null")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return clientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @Operation(summary = "Create a new client", description = "Add a new client to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Client.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"login\":\"user1\",\"nickname\":\"nickname1\",\"balance\":0.0}"))),
            @ApiResponse(responseCode = "400", description = "Client already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "null")))
    })
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        if (clientRepository.existsByLogin(client.getLogin()) || clientRepository.existsByNickname(client.getNickname())) {
            return ResponseEntity.badRequest().body(null);
        }
        client.setBalance(0.0);
        return ResponseEntity.ok(clientRepository.save(client));
    }



    @Operation(summary = "Add a game to client cart", description = "Add a specific game to the client's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game successfully added to cart", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client or Game not found", content = @Content)
    })
    @PostMapping("/{clientId}/cart/{gameId}")
    public ResponseEntity<String> addGameToCart(@PathVariable Long clientId, @PathVariable Long gameId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (clientOptional.isEmpty() || gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Client client = clientOptional.get();
        Game game = gameOptional.get();

        if (!client.getCartGames().contains(game)) {
            client.getCartGames().add(game);
            clientRepository.save(client);
        }

        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Remove a game from client cart", description = "Remove a specific game from the client's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game successfully removed from cart", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client or Game not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Game not found in cart", content = @Content)
    })
    @DeleteMapping("/{clientId}/cart/{gameId}")
    public ResponseEntity<String> removeGameFromCart(@PathVariable Long clientId, @PathVariable Long gameId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Client client = clientOptional.get();

        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (gameOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Game game = gameOptional.get();

        if (client.getCartGames().contains(game)) {
            client.getCartGames().remove(game);
            clientRepository.save(client);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }



    @Operation(summary = "Purchase a game", description = "Allows a client to purchase a specific game.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game successfully purchased", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client or Game not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or no keys available", content = @Content)
    })
    @PostMapping("/{clientId}/purchase/{gameId}")
    public ResponseEntity<String> purchaseGame(@PathVariable Long clientId, @PathVariable Long gameId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (clientOptional.isEmpty() || gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Client client = clientOptional.get();
        Game game = gameOptional.get();

        if (!client.getCartGames().contains(game)) {
            return ResponseEntity.status(404).build();
        }

        if (client.getBalance() < game.getCost()) {
            return ResponseEntity.status(400).build();
        }

        if (game.getKeys().isEmpty()) {
            return ResponseEntity.status(404).build();
        }


        String key = game.getKeys().remove(0);
        gameRepository.save(game);


        Game purchasedGame = new Game();
        purchasedGame.setName(game.getName());
        purchasedGame.setCost(game.getCost());
        purchasedGame.setKeys(new ArrayList<>());
        purchasedGame.getKeys().add(key);
        purchasedGame.setSold(true);
        gameRepository.save(purchasedGame);


        client.setBalance(client.getBalance() - game.getCost());
        client.getCartGames().remove(game);
        client.getPurchasedGames().add(purchasedGame);
        clientRepository.save(client);

        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Purchase all games in the cart", description = "Allows a client to purchase all games currently in their cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All games successfully purchased", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or no keys available", content = @Content)
    })
    @PostMapping("/{clientId}/purchase-all")
    public ResponseEntity<String> purchaseAllGames(@PathVariable Long clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if (clientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Client client = clientOptional.get();

        double totalCost = client.getCartGames().stream().mapToDouble(Game::getCost).sum();

        if (client.getBalance() < totalCost) {
            return ResponseEntity.status(400).build();
        }

        for (Game game : new ArrayList<>(client.getCartGames())) {
            if (game.getKeys().isEmpty()) {
                continue;
            }

            String key = game.getKeys().remove(0);
            gameRepository.save(game);

            Game purchasedGame = new Game();
            purchasedGame.setName(game.getName());
            purchasedGame.setCost(game.getCost());
            purchasedGame.setKeys(new ArrayList<>());
            purchasedGame.getKeys().add(key);
            purchasedGame.setSold(true);
            gameRepository.save(purchasedGame);

            client.getPurchasedGames().add(purchasedGame);
        }

        client.setBalance(client.getBalance() - totalCost);
        client.getCartGames().clear();
        clientRepository.save(client);

        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Add balance to client account", description = "Add a specified amount to the client's balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance successfully added", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid amount", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content)
    })
    @PostMapping("/{clientId}/add-balance")
    public ResponseEntity<String> addBalance(@PathVariable Long clientId, @RequestParam Double amount) {
        if (amount <= 0) {
            return ResponseEntity.status(400).build();
        }

        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if (clientOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Client client = clientOptional.get();
        client.setBalance(client.getBalance() + amount);
        clientRepository.save(client);

        return ResponseEntity.ok().build();
    }
}

