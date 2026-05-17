package com.stonedt.intelligence.entity.campus;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CampusPermissionMenu {

    private Long id;
    private Long menuId;
    private Long parentId;
    private String menuCode;
    private String menuName;
    private String menuType;
    private String routePath;
    private String componentPath;
    private String permissionCode;
    private String icon;
    private Integer sortNo;
    private Integer visible;
    private Integer status;
    private Integer deleted;
    private Long createUserId;
    private Long updateUserId;
    private Date createTime;
    private Date updateTime;
    private List<CampusPermissionMenu> children = new ArrayList<>();
}
