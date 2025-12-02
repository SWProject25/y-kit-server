package com.twojz.y_kit.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Region {
    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegionLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code")
    private Region parent;

    @OneToMany(mappedBy = "parent")
    private final List<Region> children = new ArrayList<>();

    @Builder
    public Region(String name, String fullName, String code, RegionLevel level, Region parent) {
        this.name = name;
        this.fullName = fullName;
        this.code = code;
        this.level = level;
        this.parent = parent;
    }
}
