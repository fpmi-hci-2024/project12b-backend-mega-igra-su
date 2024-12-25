package mega.igra.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mega.igra.app.model.Game;
import mega.igra.app.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Game", description = "Operations related to games")
@RequiredArgsConstructor
public class GameController {
    private final GameRepository gameRepository;

    @Operation(summary = "Get all unsold games", description = "Retrieve a list of all games that are not marked as sold.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of unsold games",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Game.class),
                            examples = @ExampleObject(value = "[{\"id\":1,\"name\":\"Game1\",\"cost\":50.0,\"keys\":[\"key1\",\"key2\"],\"sold\":false}]")))
    })
    @GetMapping
    public List<Game> getAllGames() {
        return gameRepository.findBySoldFalse();
    }


    @Operation(summary = "Create a new game", description = "Add a new game to the database. The game must have a name, cost, and at least one key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Game successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Game.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Game1\",\"cost\":50.0,\"keys\":[\"key1\",\"key2\"],\"sold\":false}"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Bad Request\",\"message\":\"Name, Cost, and at least one Key are required fields\"}"))),
            @ApiResponse(responseCode = "409", description = "Duplicate keys found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\":\"Conflict\",\"message\":\"One or more keys already exist in another game\"}")))
    })
    @PostMapping
    public ResponseEntity<?> createGame(@RequestBody Game game) {
        if (game.getName() == null || game.getKeys() == null || game.getKeys().isEmpty() || game.getCost() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bad Request",
                    "message", "Name, Cost, and at least one Key are required fields"
            ));
        }

        boolean duplicateKeyExists = game.getKeys().stream()
                .anyMatch(key -> gameRepository.existsByKeysContaining(key));

        if (duplicateKeyExists) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Conflict",
                    "message", "One or more keys already exist in another game"
            ));
        }

        game.setSold(false);
        Game savedGame = gameRepository.save(game);
        return ResponseEntity.status(201).body(savedGame);
    }



    @Operation(summary = "Get a game by ID", description = "Retrieve details of a game by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Game.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Game1\",\"cost\":50.0,\"keys\":[\"key1\",\"key2\"],\"sold\":false}"))),
            @ApiResponse(responseCode = "404", description = "Game not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "null")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Optional<Game> game = gameRepository.findById(id);
        return game.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(404).body(null));
    }

}
