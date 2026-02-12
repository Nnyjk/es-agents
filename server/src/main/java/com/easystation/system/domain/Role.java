package com.easystation.system.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sys_role")
@Getter
@Setter
public class Role extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String code;

    @Column(nullable = false)
    public String name;

    public String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_role_module",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    public Set<Module> modules = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_role_module_action",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "module_action_id")
    )
    public Set<ModuleAction> actions = new HashSet<>();
}
