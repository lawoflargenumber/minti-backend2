package com.example.gateway.api.board;

import com.example.gateway.api.dto.BoardDtos;
import com.example.gateway.application.board.BoardService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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
    public Flux<BoardDtos.BoardItem> all() {
        return boardService.all();
    }

    @GetMapping("/brand")
    public Flux<BoardDtos.BoardItem> brand() {
        return boardService.brand();
    }

    @GetMapping("/category")
    public Flux<BoardDtos.BoardItem> category() {
        return boardService.category();
    }
}