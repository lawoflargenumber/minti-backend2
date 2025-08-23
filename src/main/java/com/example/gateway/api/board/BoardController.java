package com.example.gateway.api.board;

import com.example.gateway.api.dto.BoardDtos;
import com.example.gateway.application.board.BoardService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping(value="/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BoardDtos.BoardItem> all(@RequestHeader("Authorization") String token) {
        return boardService.all(token.replace("Bearer ", ""));
    }

    @GetMapping("/brand")
    public Flux<BoardDtos.BoardItem> brand(@RequestHeader("Authorization") String token) {
        return boardService.brand(token.replace("Bearer ", ""));
    }

    @GetMapping("/category")
    public Flux<BoardDtos.BoardItem> category(@RequestHeader("Authorization") String token) {
        return boardService.category(token.replace("Bearer ", ""));
    }
}