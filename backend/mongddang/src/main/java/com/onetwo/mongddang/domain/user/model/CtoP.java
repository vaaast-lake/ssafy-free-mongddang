package com.onetwo.mongddang.domain.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class CtoP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private User child;

    @NotNull
    @ManyToOne
    private User protector;
}
