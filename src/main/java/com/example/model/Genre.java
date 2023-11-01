package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genres")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Genre {

    @Id
    @Column(name = "name")
    private String name;
}
