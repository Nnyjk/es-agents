package com.easystation.system.domain;

import com.easystation.system.domain.enums.ModuleType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sys_module")
@Getter
@Setter
public class Module extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String code;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    public ModuleType type;

    public String path;

    public UUID parentId;

    public Integer sortOrder;
}
