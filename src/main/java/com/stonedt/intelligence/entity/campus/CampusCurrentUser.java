package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusCurrentUser {

    private Long userId;
    private String username;
    private String telephone;
    private String organizationId;
    private List<CampusPermissionRole> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<CampusPermissionMenu> menus = new ArrayList<>();
}
