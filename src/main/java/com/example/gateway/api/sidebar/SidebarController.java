package com.example.gateway.api.sidebar;

import com.example.gateway.api.dto.SidebarDtos;
import com.example.gateway.application.sidebar.SidebarService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@RestController
public class SidebarController {

    private final SidebarService sidebarService;

    public SidebarController(SidebarService sidebarService) {
        this.sidebarService = sidebarService;
    }

    @GetMapping("/sidebar")
    public Mono<SidebarDtos.SidebarResponse> getSidebar() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> {
                if (ctx.getAuthentication() == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
                }
                return ctx.getAuthentication().getName(); 
            })
            .flatMap(userId -> sidebarService.getSidebar(userId));
    }
}
