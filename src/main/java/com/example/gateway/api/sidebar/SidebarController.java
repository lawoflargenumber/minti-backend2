package com.example.gateway.api.sidebar;

import com.example.gateway.api.dto.SidebarDtos;
import com.example.gateway.application.sidebar.SidebarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sidebar")
public class SidebarController {

    private final SidebarService sidebarService;

    public SidebarController(SidebarService sidebarService) {
        this.sidebarService = sidebarService;
    }

    @GetMapping
    public Mono<SidebarDtos.SidebarResponse> getSidebar() {
        return sidebarService.getSidebar("test-user-id");
    }
}