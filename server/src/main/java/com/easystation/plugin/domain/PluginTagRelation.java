package com.easystation.plugin.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "plugin_tag_relation")
@Getter
@Setter
public class PluginTagRelation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(name = "tag_id", nullable = false)
    public UUID tagId;
}