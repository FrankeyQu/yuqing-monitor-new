package com.stonedt.intelligence.service.impl.campus;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.dao.campus.CampusPublicWebWhitelistDao;
import com.stonedt.intelligence.entity.campus.CampusPublicWebWhitelist;
import com.stonedt.intelligence.service.campus.CampusPublicWebWhitelistService;
import com.stonedt.intelligence.service.campus.ingest.publicweb.PublicWebWhitelistValidator;
import com.stonedt.intelligence.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CampusPublicWebWhitelistServiceImpl implements CampusPublicWebWhitelistService {

    private static final int MIN_RATE_LIMIT_SECONDS = 60;

    private final CampusPublicWebWhitelistDao campusPublicWebWhitelistDao;

    public CampusPublicWebWhitelistServiceImpl(CampusPublicWebWhitelistDao campusPublicWebWhitelistDao) {
        this.campusPublicWebWhitelistDao = campusPublicWebWhitelistDao;
    }

    @Override
    public CampusPublicWebWhitelist save(CampusPublicWebWhitelist whitelist, Long operatorUserId) {
        validate(whitelist);
        applyDefaults(whitelist, operatorUserId);
        validateBaseUrlInWhitelist(whitelist);
        if (whitelist.getWhitelistId() == null) {
            whitelist.setWhitelistId(SnowflakeUtil.getId());
            campusPublicWebWhitelistDao.insert(whitelist);
            return campusPublicWebWhitelistDao.selectByWhitelistId(whitelist.getWhitelistId());
        }
        requireExisting(whitelist.getWhitelistId());
        campusPublicWebWhitelistDao.update(whitelist);
        return campusPublicWebWhitelistDao.selectByWhitelistId(whitelist.getWhitelistId());
    }

    @Override
    public CampusPublicWebWhitelist updateStatus(Long whitelistId, Integer enabled, Long operatorUserId) {
        requireExisting(whitelistId);
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new IllegalArgumentException("白名单状态只能为启用或停用");
        }
        campusPublicWebWhitelistDao.updateStatus(whitelistId, enabled, operatorUserId);
        return campusPublicWebWhitelistDao.selectByWhitelistId(whitelistId);
    }

    @Override
    public void delete(Long whitelistId, Long operatorUserId) {
        requireExisting(whitelistId);
        campusPublicWebWhitelistDao.logicalDelete(whitelistId, operatorUserId);
    }

    @Override
    public CampusPublicWebWhitelist requireEnabled(Long whitelistId) {
        CampusPublicWebWhitelist whitelist = requireExisting(whitelistId);
        if (whitelist.getEnabled() == null || whitelist.getEnabled() != 1) {
            throw new IllegalArgumentException("公开网页白名单未启用");
        }
        return whitelist;
    }

    @Override
    public PageInfo<CampusPublicWebWhitelist> list(Integer pageNum,
                                                   Integer pageSize,
                                                   String keyword,
                                                   String siteDomain,
                                                   Integer enabled) {
        PageHelper.startPage(defaultPageNum(pageNum), defaultPageSize(pageSize));
        return new PageInfo<>(campusPublicWebWhitelistDao.list(keyword, siteDomain, enabled));
    }

    private void validate(CampusPublicWebWhitelist whitelist) {
        if (whitelist == null) {
            throw new IllegalArgumentException("公开网页白名单不能为空");
        }
        if (StringUtils.isBlank(whitelist.getSiteName())) {
            throw new IllegalArgumentException("站点名称不能为空");
        }
        PublicWebWhitelistValidator.validateDomainFormat(whitelist.getSiteDomain());
        PublicWebWhitelistValidator.validateHttpUrl(whitelist.getBaseUrl());
        if (StringUtils.isBlank(whitelist.getAuthorizationBasis())) {
            throw new IllegalArgumentException("授权或来源依据不能为空");
        }
        if (StringUtils.isBlank(whitelist.getAuthorizationScope())) {
            throw new IllegalArgumentException("授权范围不能为空");
        }
        if (whitelist.getEnabled() != null && whitelist.getEnabled() != 0 && whitelist.getEnabled() != 1) {
            throw new IllegalArgumentException("白名单状态只能为启用或停用");
        }
    }

    private void applyDefaults(CampusPublicWebWhitelist whitelist, Long operatorUserId) {
        whitelist.setSiteDomain(whitelist.getSiteDomain().trim().toLowerCase());
        whitelist.setAllowedPathPrefix(normalizePathPrefix(whitelist.getAllowedPathPrefix()));
        whitelist.setEnabled(whitelist.getEnabled() == null ? 1 : whitelist.getEnabled());
        whitelist.setRateLimitSeconds(whitelist.getRateLimitSeconds() == null || whitelist.getRateLimitSeconds() < MIN_RATE_LIMIT_SECONDS
                ? MIN_RATE_LIMIT_SECONDS
                : whitelist.getRateLimitSeconds());
        whitelist.setMaxDepth(whitelist.getMaxDepth() == null ? 0 : Math.max(whitelist.getMaxDepth(), 0));
        whitelist.setDeleted(0);
        whitelist.setCreateUserId(operatorUserId);
        whitelist.setUpdateUserId(operatorUserId);
    }

    private String normalizePathPrefix(String pathPrefix) {
        if (StringUtils.isBlank(pathPrefix)) {
            return "/";
        }
        String normalized = pathPrefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("允许路径前缀不能包含上级目录");
        }
        return normalized;
    }

    private void validateBaseUrlInWhitelist(CampusPublicWebWhitelist whitelist) {
        PublicWebWhitelistValidator.validateUrlInWhitelist(whitelist.getBaseUrl(), whitelist);
    }

    private CampusPublicWebWhitelist requireExisting(Long whitelistId) {
        if (whitelistId == null) {
            throw new IllegalArgumentException("白名单ID不能为空");
        }
        CampusPublicWebWhitelist whitelist = campusPublicWebWhitelistDao.selectByWhitelistId(whitelistId);
        if (whitelist == null) {
            throw new IllegalArgumentException("公开网页白名单不存在");
        }
        return whitelist;
    }

    private int defaultPageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int defaultPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
